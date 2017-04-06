package com.tle.core.taxonomy.wizard;

import com.tle.common.taxonomy.wizard.TermSelectorControl;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.wizard.WebWizardPage;
import com.tle.web.wizard.controls.CCustomControl;

public abstract class TermSelectorDisplayDelegate<M> implements Section, HtmlRenderer
{
	protected TermSelectorControl definitionControl;
	protected TermSelectorWebControl termWebControl;
	protected CCustomControl storageControl;
	protected String propertyName;
	private String sectionId;
	private SectionTree treeRegisteredIn;
	private WebWizardPage webWizardPage;

	public abstract boolean isEmpty();

	public abstract void doEdits(SectionInfo info);

	public void init(String propertyName, TermSelectorControl definitionControl, CCustomControl storageControl,
		TermSelectorWebControl termWebControl)
	{
		this.propertyName = propertyName;
		this.definitionControl = definitionControl;
		this.storageControl = storageControl;
		this.termWebControl = termWebControl;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		sectionId = id;
		treeRegisteredIn = tree;
	}

	@Override
	public boolean isTreeIndexed()
	{
		return true;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return propertyName;
	}

	@Override
	public String getSectionId()
	{
		return sectionId;
	}

	@Override
	public SectionTree getTree()
	{
		return treeRegisteredIn;
	}

	@Override
	public Section getSectionObject()
	{
		return this;
	}

	@SuppressWarnings("unchecked")
	public M getModel(SectionInfo info)
	{
		return (M) info.getModelForId(getSectionId());
	}

	@Override
	public abstract M instantiateModel(SectionInfo info);

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		// Nothing to do here
	}

	public void addDisabler(SectionInfo info, JSDisableable disabler)
	{
		termWebControl.getDisabler(info).addDisabler(disabler);
	}

	public void addDisablers(SectionInfo info, JSDisableable... disablers)
	{
		termWebControl.getDisabler(info).addDisablers(disablers);
	}

	public WebWizardPage getWebWizardPage()
	{
		return webWizardPage;
	}

	public void setWebWizardPage(WebWizardPage webWizardPage)
	{
		this.webWizardPage = webWizardPage;
	}
}
