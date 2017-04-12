/*
 * Created on Jun 22, 2005
 */

package com.tle.beans.entity.itemdef;

import java.io.Serializable;
import java.util.List;

import com.dytech.edge.wizard.beans.FixedMetadata;
import com.dytech.edge.wizard.beans.WizardPage;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class Wizard implements Serializable
{
	private static final long serialVersionUID = 1;

	private String name;
	private List<WizardPage> pages;
	private FixedMetadata metadata;

	private String redraftScript;
	private String saveScript;

	private boolean allowNonSequentialNavigation;
	private boolean showPageTitlesNextPrev;

	private String additionalCssClass;

	@Deprecated
	@XStreamOmitField
	@SuppressWarnings("unused")
	private String layout;

	public Wizard()
	{
		super();
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public List<WizardPage> getPages()
	{
		return pages;
	}

	public FixedMetadata getMetadata()
	{
		return metadata;
	}

	public void setMetadata(FixedMetadata metadata)
	{
		this.metadata = metadata;
	}

	public String getRedraftScript()
	{
		return redraftScript;
	}

	public void setRedraftScript(String redraftScript)
	{
		this.redraftScript = redraftScript;
	}

	public String getSaveScript()
	{
		return saveScript;
	}

	public void setSaveScript(String saveScript)
	{
		this.saveScript = saveScript;
	}

	public void setPages(List<WizardPage> pages)
	{
		this.pages = pages;
	}

	public boolean isAllowNonSequentialNavigation()
	{
		return allowNonSequentialNavigation;
	}

	public void setAllowNonSequentialNavigation(boolean allowNonSequentialNavigation)
	{
		this.allowNonSequentialNavigation = allowNonSequentialNavigation;
	}

	public boolean isShowPageTitlesNextPrev()
	{
		return showPageTitlesNextPrev;
	}

	public void setShowPageTitlesNextPrev(boolean showPageTitlesNextPrev)
	{
		this.showPageTitlesNextPrev = showPageTitlesNextPrev;
	}

	public String getAdditionalCssClass()
	{
		return additionalCssClass;
	}

	public void setAdditionalCssClass(String additionalCssClass)
	{
		this.additionalCssClass = additionalCssClass;
	}
}
