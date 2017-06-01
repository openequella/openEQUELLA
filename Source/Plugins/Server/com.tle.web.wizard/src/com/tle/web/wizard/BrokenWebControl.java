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

package com.tle.web.wizard;

import javax.servlet.http.HttpServletRequest;

import com.dytech.devlib.PropBagEx;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.core.wizard.controls.HTMLCtrlWrapper;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.SimpleElementId;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.sections.standard.js.impl.CombinedDisableable;
import com.tle.web.wizard.controls.WebControl;

public class BrokenWebControl extends HTMLCtrlWrapper implements WebControl
{
	public BrokenWebControl(HTMLControl control)
	{
		super(control);
	}

	private WebWizardPage webWizardPage;
	private boolean inColumn;
	private String sectionId;
	private SectionTree tree;

	@Override
	public boolean isInColumn()
	{
		return inColumn;
	}

	@Override
	public void setInColumn(boolean inColumn)
	{
		this.inColumn = inColumn;
	}

	@Override
	public Section getSectionObject()
	{
		return null;
	}

	@Override
	public SectionTree getTree()
	{
		return tree;
	}

	@Override
	public CombinedDisableable getDisabler(SectionInfo info)
	{
		return new CombinedDisableable(new SimpleElementId(getSectionId()));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		return new SimpleSectionResult("No Web Control for: '" //$NON-NLS-1$
			+ getControlBean().getClassType() + "'<br>"); //$NON-NLS-1$
	}

	public void setItemsHTTP(HttpServletRequest request)
	{
		// nothing
	}

	@Override
	public WebWizardPage getWebWizardPage()
	{
		return webWizardPage;
	}

	@Override
	public void setWebWizardPage(WebWizardPage webWizardPage)
	{
		this.webWizardPage = webWizardPage;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "broken"; //$NON-NLS-1$
	}

	public Class<?> getModelClass()
	{
		return Object.class;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return null;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		this.sectionId = id;
		this.tree = tree;
	}

	@Override
	public String getSectionId()
	{
		return sectionId;
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		// nothing
	}

	@Override
	public void doEditsIfRequired(SectionInfo info)
	{
		// nothing
	}

	@Override
	public void doReads(SectionInfo info)
	{
		// nothing
	}

	@Override
	public void clearTargets(SectionInfo info, PropBagEx doc)
	{
		clearTargets(doc);
	}

	public JSCallable getDisableFunction(SectionInfo info)
	{
		return null;
	}

	@Override
	public boolean isNested()
	{
		return false;
	}

	@Override
	public void setNested(boolean nested)
	{
		// nothing
	}

	@Override
	public boolean canHaveChildren()
	{
		return false;
	}

	@Override
	public void deletedFromParent(SectionInfo info)
	{
		// nothing
	}

	@Override
	public boolean isTreeIndexed()
	{
		return false;
	}
}
