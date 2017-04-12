/*
 * Created on Aug 15, 2005
 */
package com.tle.web.wizard.standard.controls;

import java.util.ArrayList;
import java.util.List;

import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.wizard.WebWizardPage;
import com.tle.web.wizard.controls.AbstractWebControl;
import com.tle.web.wizard.controls.GroupsCtrl;
import com.tle.web.wizard.controls.GroupsCtrl.ControlGroup;
import com.tle.web.wizard.controls.WebControl;
import com.tle.web.wizard.controls.WebControlModel;
import com.tle.web.wizard.controls.WizardGroupListener;

public abstract class GroupWebControl<M extends WebControlModel> extends AbstractWebControl<M>
	implements
		WizardGroupListener
{
	@ViewFactory(fixed = false)
	protected FreemarkerFactory viewFactory;

	private final List<List<WebControl>> webGroups = new ArrayList<List<WebControl>>();
	private GroupsCtrl gctrl;

	@Override
	public void setWrappedControl(HTMLControl control)
	{
		super.setWrappedControl(control);
		gctrl = (GroupsCtrl) control;
	}

	public List<List<WebControl>> getWebGroups()
	{
		return webGroups;
	}

	@Override
	public void setWebWizardPage(WebWizardPage webWizardPage)
	{
		super.setWebWizardPage(webWizardPage);
		gctrl.setListener(this);
		for( ControlGroup group : gctrl.getGroups() )
		{
			addNewGroup(group);
		}
	}

	@Override
	public void addNewGroup(ControlGroup group)
	{
		WebWizardPage webWizardPage = getWebWizardPage();
		List<WebControl> wrappedGroup = webWizardPage.wrapControls(group.getControls());
		webGroups.add(wrappedGroup);
	}

	@Override
	public void removeFromGroup(SectionInfo info, int i)
	{
		List<WebControl> removed = webGroups.remove(i);
		getWebWizardPage().removeControls(removed);

		for( WebControl webControl : removed )
		{
			webControl.deletedFromParent(info);
		}
	}

	@Override
	public void deletedFromParent(SectionInfo info)
	{
		for( List<WebControl> webList : webGroups )
		{
			for( WebControl webControl : webList )
			{
				webControl.deletedFromParent(info);
			}
		}
	}

	public static void processGroup(List<WebControl> group, SectionInfo info)
	{
		for( WebControl control : group )
		{
			control.doEditsIfRequired(info);
		}
	}

	protected void addDisableablesForControls(SectionInfo info)
	{
		if( webGroups != null )
		{
			for( List<WebControl> webGroup : webGroups )
			{
				for( WebControl wc : webGroup )
				{
					addDisabler(info, wc.getDisabler(info));
				}
			}
		}
	}

	@Override
	public boolean canHaveChildren()
	{
		return true;
	}
}
