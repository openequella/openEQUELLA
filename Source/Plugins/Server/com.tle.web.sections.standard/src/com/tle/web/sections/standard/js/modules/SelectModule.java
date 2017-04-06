package com.tle.web.sections.standard.js.modules;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
public class SelectModule implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	public static final IncludeFile INCLUDE = new IncludeFile(ResourcesService.getResourceHelper(SelectModule.class)
		.url("js/select.js"));
	public static final JSCallable SELECTED_TEXT = new ExternallyDefinedFunction("getSelectedText", 1, INCLUDE);
	public static final JSCallable SELECTED_TEXTS = new ExternallyDefinedFunction("getSelectedTexts", 1, INCLUDE);
	public static final JSCallable SELECTED_VALUES = new ExternallyDefinedFunction("getSelectedValues", 1, INCLUDE);
	public static final JSCallable SELECTED_VALUE = new ExternallyDefinedFunction("getSelectedValue", 1, INCLUDE);
	public static final JSCallable ALL_VALUES = new ExternallyDefinedFunction("getAllSelectValues", 1, INCLUDE);
	public static final JSCallable SET_VALUE = new ExternallyDefinedFunction("setSelectedValue", 2, INCLUDE);
	public static final JSCallable SELECT_ALL = new ExternallyDefinedFunction("selectAll", 1, INCLUDE);
	public static final JSCallable ADD_OPTION = new ExternallyDefinedFunction("addOption", 3, INCLUDE);
	public static final JSCallable REMOVE_SELECTED = new ExternallyDefinedFunction("removeSelected", 1, INCLUDE);
	public static final JSCallable RESET_SELECTED_VALUES = new ExternallyDefinedFunction("resetSelectedValues", 1,
		INCLUDE);

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.standard.js.modules.select.name");
	}

	@Override
	public String getId()
	{
		return "select";
	}

	@Override
	public PreRenderable getPreRenderer()
	{
		return INCLUDE;
	}
}
