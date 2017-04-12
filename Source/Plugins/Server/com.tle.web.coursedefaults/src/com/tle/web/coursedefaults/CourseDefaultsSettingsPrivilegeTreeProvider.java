/**
 * 
 */
package com.tle.web.coursedefaults;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

/**
 * @author larry
 */
@Bind
public class CourseDefaultsSettingsPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	/**
	 * The string id expression to SettingsTarget constructor is exactly as
	 * spelled in the old invocation to construct a SystemSettings in
	 * SettingsPrivilegeTreeProvider::gatherChildTargets. Accordingly
	 * inconsistency with regard to camel-case/lower case etc across different
	 * sub-classes of AbstractSettingsPrivilegeTreeProvider is a legacy
	 */
	public CourseDefaultsSettingsPrivilegeTreeProvider()
	{
		super(Type.SYSTEM_SETTING, ResourcesService
			.getResourceHelper(CourseDefaultsSettingsPrivilegeTreeProvider.class).key(
				"securitytree.coursedefaultssettings"), new SettingsTarget("courseDefaults"));
	}
}
