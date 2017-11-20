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

package com.tle.core.taxonomy.wizard;

import javax.inject.Inject;

import org.java.plugin.registry.Extension;

import com.dytech.edge.wizard.beans.control.CustomControl;
import com.tle.common.Check;
import com.tle.common.taxonomy.wizard.TermSelectorControl;
import com.tle.core.freetext.queries.BaseQuery;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.taxonomy.TaxonomyService;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.wizard.controls.AbstractSimpleWebControl;
import com.tle.web.wizard.controls.CCustomControl;
import com.tle.web.wizard.render.WizardFreemarkerFactory;

@SuppressWarnings("nls")
@Bind
public class TermSelectorWebControl extends AbstractSimpleWebControl
{
	@ViewFactory(name = "wizardFreemarkerFactory")
	private WizardFreemarkerFactory viewFactory;

	@Inject
	private PluginService pluginService;
	@Inject
	private TaxonomyService taxonomyService;

	private TermSelectorDisplayDelegate<?> delegate;
	private String delegateSectionId;
	private TermSelectorControl definitionControl;
	private CCustomControl storageControl;

	public TermSelectorWebControl()
	{
		super();
	}

	@Override
	public void setWrappedControl(final HTMLControl control)
	{
		definitionControl = new TermSelectorControl((CustomControl) control.getControlBean());
		storageControl = (CCustomControl) control;
		super.setWrappedControl(control);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		delegate = getDelegate();
		delegate.init(getDefaultPropertyName() + "d", definitionControl, storageControl, this);
		delegate.setWebWizardPage(getWebWizardPage());

		delegateSectionId = tree.registerSections(delegate, id);
	}

	private TermSelectorDisplayDelegate<?> getDelegate()
	{
		String selectedTaxonomy = definitionControl.getSelectedTaxonomy();
		if( Check.isEmpty(selectedTaxonomy) || taxonomyService.getByUuid(selectedTaxonomy) == null )
		{
			throw new RuntimeException(
				"You must select a taxonomy for this control in the Collection Definition Editor");
		}
		final String extId = definitionControl.getDisplayType();
		for( Extension ext : pluginService.getConnectedExtensions("com.tle.core.taxonomy", "termSelectorDisplay") )
		{
			if( ext.getId().equals(extId) )
			{
				return (TermSelectorDisplayDelegate<?>) pluginService.getBean(ext.getDeclaringPluginDescriptor(), ext
					.getParameter("bean").valueAsString());
			}
		}
		throw new RuntimeException("No term selector display registered for '" + extId + '\'');
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		return viewFactory.createWizardResult(SectionUtils.renderSection(context, delegateSectionId),
			viewFactory.getDefaultTitleTemplate(), viewFactory.getDefaultTailTemplate(), context);
	}

	@Override
	public boolean isEmpty()
	{
		return delegate.isEmpty();
	}

	@Override
	public void doEdits(SectionInfo info)
	{
		delegate.doEdits(info);
	}

	@Override
	public BaseQuery getPowerSearchQuery()
	{
		if( storageControl.hasTargets() )
		{
			return storageControl.getDefaultPowerSearchQuery(storageControl.getValues(), false);
		}
		return null;
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return null;
	}
}
