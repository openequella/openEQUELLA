package com.tle.web.htmleditor.settings;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

/**
 * @author Aaron
 */
@Bind
public class HtmlEditorSettingsPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	public HtmlEditorSettingsPrivilegeTreeProvider()
	{
		super(Type.SYSTEM_SETTING, ResourcesService.getResourceHelper(HtmlEditorSettingsPrivilegeTreeProvider.class)
			.key("securitytree.htmleditorsettings"), new SettingsTarget("htmleditor"));
	}
}
