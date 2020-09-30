package wb.java8;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

class JavaScriptEngine {

    public static void main(String[] args) throws ScriptException {

        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine nashorn = sem.getEngineByName("nashorn");

        Object eval = nashorn.eval("1+2");
        System.out.println(eval);
    }
}
