package com.tle.web.diagnostics.security;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

/**
 * @author Dongsheng Cai
 */
@Bind
public class DiagnosticsSettingsPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	/**
	 * The string id expression to SettingsTarget constructor is exactly as
	 * spelled in the old invocation to construct a SystemSettings in
	 * SettingsPrivilegeTreeProvider::gatherChildTargets. Accordingly
	 * inconsistency with regard to camel-case/lower case etc across different
	 * sub-classes of AbstractSettingsPrivilegeTreeProvider is a legacy
	 */
	@SuppressWarnings("nls")
	public DiagnosticsSettingsPrivilegeTreeProvider()
	{
		super(Type.SYSTEM_SETTING, ResourcesService.getResourceHelper(DiagnosticsSettingsPrivilegeTreeProvider.class)
			.key("securitytree.diagnostics"), new SettingsTarget("diagnostics"));
	}
}