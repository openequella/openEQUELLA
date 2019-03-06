package com.tle.webtests.pageobject.wizard;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.wizard.controls.ShuffleGroupControl;

public class DRMUsageWizardPage extends AbstractPage<DRMUsageWizardPage>
{
	private final WizardPageTab page;
	private final int whoSelect;
	private final int whoOthers;
	private final int whatSelect;
	private final int useCheckboxes;
	private final int reuseCheckboxes;

	public DRMUsageWizardPage(PageContext context, WizardPageTab page)
	{
		super(context);
		this.page = page;
		int offset = 1;
		whoSelect = offset++;
		whoOthers = offset++;
		offset++;
		offset++;
		whatSelect = offset++;
		useCheckboxes = offset++;
		reuseCheckboxes = offset++;
	}

	public void setWho(String value)
	{
		page.setCheckReload(whoSelect, value, true);
	}

	public void addOther(String name, String email)
	{
		ShuffleGroupControl group = page.shuffleGroup(whoOthers, 2);
		SubWizardPage page = group.add();
		page.editbox(1, name);
		page.editbox(2, email);
		group.ok();
	}

	public void setWhat(String value)
	{
		page.setCheckReload(whatSelect, value, true);
	}

	public void setCustomUse(boolean check, String... usages)
	{
		for( String usage : usages )
		{
			page.setCheck(useCheckboxes, usage, check);
		}
	}

	public void setCustomReuse(boolean check, String... usages)
	{
		for( String usage : usages )
		{
			page.setCheck(reuseCheckboxes, usage, check);
		}
	}
}
