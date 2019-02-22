/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dytech.gui.workers;

import java.lang.reflect.Method;

/**
 * This is a very cool customisation of <code>AdvancedSwingWorker</code> which uses the return value
 * of <code>construct()</code> to dispatch to a method declared in subclass. The dispatch methods
 * will execute in the event thread. If you return the String "myFirstAction" from the <code>
 * construct</code> method, then a call will be dispatched to the method <code>doMyFirstAction
 * </code>. Note the "do" at the front of the method name, as well as the capitalisation of the
 * first character of the returned action name. Returning <code>null</code> or an action name which
 * does not have a corresponding method will invoke <code>unspecified</code>. This may be overriden
 * for your own purposes. <b>!!! DO NOT OVERRIDE THE <code>finished</code> METHOD !!!</b> Example
 * Usage:
 *
 * <pre>
 *   final DispatchSwingWorker worker = new DispatchSwingWorker()
 *   {
 *     public Object construct()
 *     {
 *        int code = doSomethingBig();
 *        switch( code )
 *        {
 * 		    case 0:  return "allGood";
 *          default: return "badStuff";
 *        }
 *     }
 *
 *     public void doAllGood()
 *     {
 *       // do nice GUI stuff...
 *     }
 *
 *     public void doBadStuff()
 *     {
 *       // do error handling GUI stuff...
 *     }
 *   }
 * </pre>
 *
 * @author Nicholas Read
 */
public abstract class DispatchSwingWorker<RESULT> extends AdvancedSwingWorker<RESULT> {
  private static final String DISPATCH_PREFIX = "do";

  public DispatchSwingWorker() {
    super();
  }

  @Override
  public void finished() {
    Object action = get();
    if (action == null) {
      return;
    }

    String name = getMethodName(action.toString());
    try {
      Method m = getClass().getMethod(name, new Class[0]);
      m.invoke(this, new Object[0]);
    } catch (Exception ex) {
      unspecified();
    }
  }

  private String getMethodName(String action) {
    StringBuilder name = new StringBuilder();
    name.append(DISPATCH_PREFIX);
    name.append(Character.toUpperCase(action.charAt(0)));
    name.append(action.substring(1));

    return name.toString();
  }

  public void unspecified() {
    // This is for the overriders.
  }
}
