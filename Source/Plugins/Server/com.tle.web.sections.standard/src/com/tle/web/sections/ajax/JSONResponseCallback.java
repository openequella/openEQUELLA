package com.tle.web.sections.ajax;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;

@NonNullByDefault
public interface JSONResponseCallback
{
	@Nullable
	Object getResponseObject(AjaxRenderContext context);
}
