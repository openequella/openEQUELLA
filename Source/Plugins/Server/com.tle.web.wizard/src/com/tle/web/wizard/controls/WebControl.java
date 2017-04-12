package com.tle.web.wizard.controls;

import com.dytech.devlib.PropBagEx;
import com.tle.annotation.NonNullByDefault;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.js.impl.CombinedDisableable;
import com.tle.web.wizard.WebWizardPage;

@NonNullByDefault
public interface WebControl extends HTMLControl, Section, HtmlRenderer
{
	WebWizardPage getWebWizardPage();

	void setWebWizardPage(WebWizardPage page);

	void setInColumn(boolean b);

	boolean isInColumn();

	HTMLControl getWrappedControl();

	void setWrappedControl(HTMLControl control);

	CombinedDisableable getDisabler(SectionInfo info);

	// Yes this is rubbish
	void doEditsIfRequired(SectionInfo info);

	void doReads(SectionInfo info);

	void setNested(boolean nested);

	boolean isNested();

	void clearTargets(SectionInfo info, PropBagEx itemxml);

	boolean canHaveChildren();

	void deletedFromParent(SectionInfo info);
}
