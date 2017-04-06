package com.tle.web.sections.standard.dialog;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSFunction;
import com.tle.web.sections.render.Label;

@NonNullByDefault
public interface OkayableDialog extends Dialog
{
	void setOkCallback(JSCallable okCallback);

	void setCancelCallback(JSCallable cancelCallback);

	void setOkLabel(Label okLabel);

	void setDialogOpenedCallback(JSFunction function);
}
