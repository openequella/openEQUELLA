/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.js.generic.function;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.ScriptVariable;

public class PartiallyApply
{

    public static JSAssignable partial(JSCallable func, int extra, Object... params)
    {
        ScriptVariable[] extraParams = JSUtils.createParameters(extra);
        Object[] allParams = new Object[params.length + extra];
        System.arraycopy(params, 0, allParams, 0, params.length);
        System.arraycopy(extraParams,0, allParams, params.length, extra);
        return new AnonymousFunction(Js.call_s(func, allParams), extraParams);
    }
}
