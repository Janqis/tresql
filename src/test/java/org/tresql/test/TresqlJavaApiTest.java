package org.tresql.test;

import java.sql.Connection;
import java.util.*;

import org.tresql.SimpleCache;
import org.tresql.java_api.*;

public class TresqlJavaApiTest implements Runnable {

    public static class TresqlJavaApiTestFunctions {
        public String echo(String s) {
            return s;
        }

        public Long plus(Long a, Long b) {
            return a + b;
        }
    }

    public void run() {
        println("");
        println("---- Testing Java API ----");
        Logger logger = Env.getLogger();
        Env.setLogger(new Logger() {
            // TODO test msg laziness
            @Override
            public void log(LogMessage msg, int level) {
                println("Java API logger [" + level + "]: " + msg.get());
            }
        });
        org.tresql.Cache cache = Env.getCache();
        Env.setCache(new SimpleCache(-1));
        Connection c = Env.getConnection();
        Env.setConnection(c);
        scala.PartialFunction dialect = Env.getDialect();
        Env.setDialect(Dialects.HSQL());
        Object functions = Env.getFunctions();
        Env.setFunctions(new TresqlJavaApiTestFunctions());
        IdExprFunc idExpr = Env.getIdExprFunc();
        println("id expr: " + Env.getIdExprFunc().getIdExpr("my_table"));
        Env.setIdExprFunc(new IdExprFunc() {
            @Override
            public String getIdExpr(String table) {
                return "nextval(" + table + "_seq)";
            }
        });
        println("id expr: " + Env.getIdExprFunc().getIdExpr("my_table[2]"));
        org.tresql.MetaData metadata = Env.getMetadata();
        Env.setMetadata(Metadata.JDBC(null));

        println("");
        for (Row r : Query.select("dept[deptno < 100]{deptno, dname}")) {
            println("" + r.i(0) + ": " + r.s(1));
        }

        println("");
        for (Row r : Query.select("dept[deptno < ?]{deptno, dname}", 40)) {
            println("" + r.int_(0) + ": " + r.string(1));
        }

        println("");
        java.util.Map<String, Object> pars = new java.util.HashMap<String, Object>();
        pars.put("id", 10);
        for (Row r : Query.select(
                "dept[deptno = :id]{deptno, dname, |emp {ename} emps}", pars)) {
            println("" + r.int_(0) + ": " + r.string(1) + ": " + "emps:");
            for (Row er : r.result("emps")) {
                println("  " + er.s("ename"));
            }
        }

        println("");
        for (Map<String, Object> r : Query.select(
                "dept[deptno < 30]{deptno, dname, |emp {ename} emps}")
                .toListOfMaps()) {
            println("toListOfMaps() - " + r.get("deptno") + ": "
                    + r.get("dname") + ", " + "emps:");
            for (Map<String, Object> er : (List<Map<String, Object>>) r
                    .get("emps")) {
                println("toListOfMaps() -   " + er.get("ename"));
            }
        }

        println("");
        for (Row r : Query.select("dept[60]{deptno, dname}")) {
            println("" + r.i(0) + ": " + r.s(1));
        }
        Query.execute("dept[60]{dname} = ['POLAR FOX']");
        for (Row r : Query.select("dept[60]{deptno, dname}")) {
            println("" + r.i("deptno") + ": " + r.s("dname"));
        }

        println("");
        for (Row r : Query.select("dept[60]{deptno, dname}")) {
            java.util.Map<String, Object> map = r.rowToMap();
            println("rowToMap() - " + map.get("deptno") + ": "
                    + map.get("dname"));
        }

        println("");
        Result res = Query.select("dept[60]{deptno, dname}");
        println("columns(0).name, index: " + res.column(0).name + ", "
                + res.column(0).index);
        println("columns(1).name, index: " + res.columns().get(1).name + ", "
                + res.columns().get(1).index);
        println("column count: " + res.columnCount() + " ("
                + res.columns().size() + ", actually)");

        println("");
        for (Row r : Query.select("dummy {plus(1, 2)}")) {
            println("" + r.l(0));
        }
        println("--------------------------");
        println("");

        //set back previous env values
        Env.setLogger(logger);
        Env.setCache(cache);
        Env.setDialect(dialect);
        Env.setFunctions(functions);
        Env.setIdExprFunc(idExpr);
        Env.setMetadata(metadata);
    }

    private void println(String s) {
        System.out.println(s);
    }
}
