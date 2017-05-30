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
