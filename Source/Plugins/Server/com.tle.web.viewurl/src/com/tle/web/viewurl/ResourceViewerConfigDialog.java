package com.tle.web.viewurl;

import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.standard.Button;

public interface ResourceViewerConfigDialog
{
	JSCallable getCollectFunction();

	JSCallable getPopulateFunction();

	JSCallable getOpenFunction();

	JSCallable getCloseFunction();

	Button getOkButton();
}
