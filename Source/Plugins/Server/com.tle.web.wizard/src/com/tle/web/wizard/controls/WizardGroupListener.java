/*
 * Created on Aug 15, 2005
 */
package com.tle.web.wizard.controls;

import com.tle.web.sections.SectionInfo;
import com.tle.web.wizard.controls.GroupsCtrl.ControlGroup;

public interface WizardGroupListener
{
	void addNewGroup(ControlGroup controls);

	void removeFromGroup(SectionInfo info, int i);

}
