/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
