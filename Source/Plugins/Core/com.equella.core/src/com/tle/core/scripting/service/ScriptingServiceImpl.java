/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.scripting.service;

import com.dytech.edge.common.PropBagWrapper;
import com.dytech.edge.common.ScriptContext;
import com.google.common.base.Predicate;
import com.tle.common.Check;
import com.tle.common.Utils;
import com.tle.common.scripting.ScriptException;
import com.tle.common.scripting.objects.LoggingScriptObject;
import com.tle.common.scripting.service.ScriptContextCreationParams;
import com.tle.common.scripting.service.ScriptingService;
import com.tle.common.util.Logger;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.scripting.DefaultScriptContext;
import com.tle.core.services.LoggingService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.commonjs.module.ModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.Require;
import org.mozilla.javascript.commonjs.module.RequireBuilder;
import org.mozilla.javascript.commonjs.module.provider.SoftCachingModuleScriptProvider;

@SuppressWarnings("nls")
@Bind(ScriptingService.class)
@Singleton
public class ScriptingServiceImpl implements ScriptingService {
  private static final String SCRIPT_TYPE_KEY = "type";
  private static final String SCRIPT_TYPE = "standard";
  private static final String MODERATION_ALLOWED = "moderationallowed";
  private static final String IS_AN_OWNER = "isanowner";
  private static final String ITEM_XML = "xml";

  private Logger logger;
  private PluginTracker<ScriptObjectContributor> scriptObjectTracker;
  private PluginTracker<UserScriptObjectContributor> userScriptObjectTracker;
  private static final PluginResourceHelper r =
      ResourcesService.getResourceHelper(ScriptingServiceImpl.class);

  @Override
  public ScriptContext createScriptContext(ScriptContextCreationParams params) {
    Map<String, Object> scriptObjects = new HashMap<String, Object>();
    Map<String, Object> userSriptObjects = new HashMap<String, Object>();

    scriptObjects.put(SCRIPT_TYPE_KEY, SCRIPT_TYPE);
    scriptObjects.put(MODERATION_ALLOWED, params.isModerationAllowed());
    scriptObjects.put(IS_AN_OWNER, params.isAnOwner());

    params.getAttributes().put(LoggingScriptObject.DEFAULT_VARIABLE, logger);

    // add plugin objects
    for (ScriptObjectContributor ext : scriptObjectTracker.getBeanList()) {
      ext.addScriptObjects(scriptObjects, params);
    }

    // Anyone contribute an xml object? We need reference to this
    PropBagWrapper xml = (PropBagWrapper) scriptObjects.get(ITEM_XML);
    if (xml == null) {
      xml = new PropBagWrapper();
    }

    List<UserScriptObjectContributor> beanList = userScriptObjectTracker.getBeanList();
    for (UserScriptObjectContributor us : beanList) {
      us.addUserScriptObject(userSriptObjects);
    }

    return new DefaultScriptContext(scriptObjects, userSriptObjects, xml);
  }

  @Override
  public boolean evaluateScript(
      final String script, final String scriptName, final ScriptContext context)
      throws ScriptException {
    return evaluateInternal(script, scriptName, context);
  }

  private boolean evaluateInternal(
      final String script, final String scriptName, final ScriptContext context)
      throws ScriptException {
    boolean returnValue = true;
    if (script != null && script.length() > 0) {
      Object result = executeInternal(script, scriptName, context, true, Boolean.class);
      if (result instanceof Boolean) {
        returnValue = ((Boolean) result).booleanValue();
      }
    }
    return returnValue;
  }

  @Override
  public Object executeScript(
      final String script,
      final String scriptName,
      final ScriptContext context,
      final boolean function)
      throws ScriptException {
    return executeInternal(script, scriptName, context, function, null);
  }

  @Override
  public Object executeScript(
      String script,
      String scriptName,
      ScriptContext context,
      boolean function,
      Class<?> expectedReturnClass)
      throws ScriptException {
    return executeInternal(script, scriptName, context, function, expectedReturnClass);
  }

  private Object executeInternal(
      final String script,
      final String scriptName,
      final ScriptContext context,
      final boolean function,
      final Class<?> expectedResultClass)
      throws ScriptException {
    // never ever execute an empty script, it's just a waste of time
    if (!Check.isEmpty(script)) {
      // In most cases apart from calls from birt, we can expect that
      // there'll be no current context, thus one will be generated by
      // this call into the ContextFactory
      return ContextFactory.getGlobal()
          .call(
              new ContextAction() {
                @Override
                public Object run(Context cx) {
                  Logger errorLogger = context.getLogger();
                  if (errorLogger == null) {
                    errorLogger = logger;
                  }

                  Scriptable userScriptScope =
                      ((DefaultScriptContext) context).getUserScriptScope(cx);
                  ScriptableModuleSourceProvider sourceProvider =
                      new ScriptableModuleSourceProvider(userScriptScope);
                  ModuleScriptProvider scriptProvider =
                      new SoftCachingModuleScriptProvider(sourceProvider);

                  cx.initStandardObjects();
                  Scriptable scope = ((DefaultScriptContext) context).getScope(cx);

                  RequireBuilder builder = new RequireBuilder();
                  // The "uri" property must not exist in a sandbox
                  builder.setSandboxed(false);
                  builder.setModuleScriptProvider(scriptProvider);

                  Require require = builder.createRequire(cx, scope);

                  cx.setOptimizationLevel(-1);
                  cx.setErrorReporter(new ScriptErrorReporter(errorLogger));

                  final String execScript =
                      (function
                          ? "function runScript() {\n" + script + "\n}\n runScript();"
                          : script);
                  final String execScriptName = (scriptName != null ? scriptName : "script");

                  try {
                    context.scriptEnter();
                    require.install(scope);

                    Object result = cx.evaluateString(scope, execScript, execScriptName, 1, null);

                    context.scriptExit();
                    if (expectedResultClass == String.class) {
                      return result.toString();
                    }
                    return result;
                  } catch (JavaScriptException js) {
                    throw new ScriptException(js);
                  } catch (EcmaError ec) {
                    throw new ScriptException(ec);
                  } catch (EvaluatorException ev) {
                    throw new ScriptException(ev);
                  }
                }
              });
    }
    return Boolean.TRUE;
  }

  public static final class ScriptErrorReporter implements ErrorReporter {
    private final Logger errorLogger;

    private final List<String> errors = new ArrayList<String>();
    private final List<String> warnings = new ArrayList<String>();

    protected ScriptErrorReporter(Logger errorLogger) {
      this.errorLogger = errorLogger;
    }

    private String constructMsg(String msg, int lineNum, String line, int linePos) {
      String alteredLine =
          Utils.safeSubstring(line, 0, linePos - 1)
              + "<<<"
              + Utils.safeSubstring(line, linePos - 1);
      return msg + "\n" + r.getString("error.line", lineNum, alteredLine, linePos);
    }

    @Override
    public void error(String msg, String scriptName, int lineNum, String line, int linePos) {
      final String error = constructMsg(msg, lineNum, line, linePos);
      errors.add(error);
      errorLogger.error(error);
    }

    @Override
    public void warning(String msg, String scriptName, int lineNum, String line, int linePos) {
      final String warning = constructMsg(msg, lineNum, line, linePos);
      warnings.add(warning);
      errorLogger.warn(warning);
    }

    @Override
    public EvaluatorException runtimeError(
        String msg, String scriptName, int lineNum, String line, int linePos) {
      StringBuilder summary = new StringBuilder();
      summary.append(r.getString("error.preamble", scriptName) + "\n");
      summary.append(msg + "\n");

      for (String error : errors) {
        summary.append(r.getString("label.error") + ": " + error + "\n");
      }
      for (String warning : warnings) {
        summary.append(r.getString("label.warning") + ": " + warning + "\n");
      }
      return new EvaluatorException(summary.toString());
    }
  }

  @Inject
  public void setLoggingService(LoggingService loggingService) {
    logger = loggingService.getLogger(ScriptingService.class);
  }

  @Inject
  public void setPluginService(PluginService pluginService) {
    scriptObjectTracker =
        new PluginTracker<ScriptObjectContributor>(
            pluginService, "com.tle.core.scripting", "scriptObjects", "id");
    scriptObjectTracker.setBeanKey("class");

    userScriptObjectTracker =
        new PluginTracker<UserScriptObjectContributor>(
            pluginService, "com.tle.core.scripting", "userScriptObjects", "id");
    userScriptObjectTracker.setBeanKey("class");
  }

  public static final class StartsWithPredicate implements Predicate<String>, Serializable {
    private final String target;

    private StartsWithPredicate(String target) {
      this.target = target;
    }

    @Override
    public boolean apply(String t) {
      return t.startsWith(target);
    }

    @Override
    public int hashCode() {
      return target.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof StartsWithPredicate) {
        StartsWithPredicate that = (StartsWithPredicate) obj;
        return target.equals(that.target);
      }
      return false;
    }

    @Override
    public String toString() {
      return "StartsWithPredicate(" + target + ")";
    }

    private static final long serialVersionUID = 0;
  }
}
