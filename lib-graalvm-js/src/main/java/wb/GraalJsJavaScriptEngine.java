package wb;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

class GraalJsJavaScriptEngine {

    public static void main(String[] args) throws ScriptException {

        // enable support for Nashorn features see GraalJSScriptEngine
        System.setProperty("polyglot.js.nashorn-compat","true");

        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine scriptEngine = sem.getEngineByName("js");

        Object eval = scriptEngine.eval("java.lang.System.currentTimeMillis()");
        System.out.println(eval);
    }
}
