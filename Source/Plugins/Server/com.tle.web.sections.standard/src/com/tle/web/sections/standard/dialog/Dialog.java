package com.tle.web.sections.standard.dialog;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.standard.dialog.renderer.DialogRenderer;
import com.tle.web.sections.standard.js.DelayedRenderer;

public interface Dialog extends SectionId, DelayedRenderer<DialogRenderer>
{
	JSCallable getOpenFunction();

	JSCallable getCloseFunction();
}
