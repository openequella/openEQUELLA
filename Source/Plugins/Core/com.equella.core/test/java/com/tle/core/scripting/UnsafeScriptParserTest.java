package com.tle.core.scripting;

import com.dytech.edge.admin.script.InvalidScriptException;
import com.dytech.edge.admin.script.basicmodel.BasicParser;
import com.dytech.edge.common.ScriptContext.SafeScriptContext;
import com.tle.core.scripting.service.SafeScriptOptions;
import java.io.StringReader;
import org.junit.Test;

public class UnsafeScriptParserTest {

  private void parseScript(String script) throws InvalidScriptException {
    new BasicParser(new SafeScriptOptions(SafeScriptContext.Wizard), new StringReader(script))
        .importScript();
  }

  @Test
  public void testUnsafeScript() {
    try {
      parseScript(
          "var bRet = false; \n"
              + "if( xml.contains('/item/name', console.log(\"Dangerous\") ) \n"
              + "{ \n"
              + "    bRet = true; \n"
              + "} \n"
              + "return bRet; \n");
      throw new Error("This script is dangerous and should not be allowed to run");
    } catch (InvalidScriptException ise) {
      // what we expected
    }
  }

  @Test
  public void testSimpleDOS() {
    try {
      parseScript("while(true) { }");
      throw new Error("This script is dangerous and should not be allowed to run");
    } catch (InvalidScriptException ise) {
      // what we expected
    }
  }
}
