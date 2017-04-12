package com.tle.web.sections.standard.dialog.renderer;

import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@SuppressWarnings("nls")
public final class ButtonKeys
{
	private static final PluginResourceHelper KEY_HELPER = ResourcesService.getResourceHelper(ButtonKeys.class);

	public static final String OK = KEY_HELPER.key("stdbut.ok");
	public static final String SAVE = KEY_HELPER.key("stdbut.save");
	public static final String CANCEL = KEY_HELPER.key("stdbut.cancel");

	private ButtonKeys()
	{
		throw new Error();
	}
}
