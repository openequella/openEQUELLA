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

package com.tle.web.licence;

import javax.inject.Inject;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableRow;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.template.Decorations;

/**
 * Originally based on the Equella 5.0 CreditsSection in the plugin
 * com.tle.web.sections.equella. Moved to its own plugin. Function changed to
 * present 3rd party licence information.
 */
@Bind
@SuppressWarnings("nls")
public class LicenceSection extends AbstractPrototypeSection<Object> implements HtmlRenderer
{
	private static final PluginResourceHelper RESOURCES = ResourcesService.getResourceHelper(LicenceSection.class);

	private static final String THIRD_PARTY_NAME = "thirdPartyName";
	private static final String LICENCE_NAME = "licenceName";
	private static final String LICENCE_URL_INTERNAL = "licenceURLInternal";
	private static final String LICENCE_URL_EXTERNAL = "licenceURLExternal";
	private static final String THIRD_PARTY_HOME = "thirdPartyHome";

	@PlugKey("licences.title")
	private static Label LICENCES_TITLE;
	@PlugKey("licences.thirdPartyEntityColHeading")
	private static Label LABEL_ENTITY;
	@PlugKey("licences.licensedUnderColHeading")
	private static Label LABEL_UNDER;
	@PlugKey("licences.thirdPartyHomeHeading")
	private static Label LABEL_HOME;
	@PlugKey("licences.defaultlicencename")
	private static Label LABEL_DEFAULT_LICENCE_NAME;

	private PluginTracker<Object> extensionTracker;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component(name = "l")
	private Table licenceTable;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		Decorations.getDecorations(context).setTitle(LICENCES_TITLE);

		gatherThirdPartyLicenceFacts(context);

		return viewFactory.createResult("licences.ftl", this);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		licenceTable.setColumnHeadings(LABEL_ENTITY, LABEL_UNDER, LABEL_HOME);
		licenceTable.setColumnSorts(Sort.PRIMARY_ASC, Sort.SORTABLE_ASC, Sort.NONE);
	}

	private void gatherThirdPartyLicenceFacts(SectionInfo info)
	{
		final TableState tableState = licenceTable.getState(info);

		for( Extension extension : extensionTracker.getExtensions() )
		{
			final String thirdPartyName = getParam(extension, THIRD_PARTY_NAME);
			String licenceName = getParam(extension, LICENCE_NAME);
			final String licenceUrlInternal = getParam(extension, LICENCE_URL_INTERNAL);
			final String licenceUrlExternal = getParam(extension, LICENCE_URL_EXTERNAL);
			final String thirdPartyUrl = getParam(extension, THIRD_PARTY_HOME);

			final String licenceUrl = (Check.isEmpty(licenceUrlExternal) ? RESOURCES.url("documentation/"
				+ licenceUrlInternal) : licenceUrlExternal);
			final HtmlLinkState url = new HtmlLinkState(new SimpleBookmark(licenceUrl));
			url.setLabel(Check.isEmpty(licenceName) ? LABEL_DEFAULT_LICENCE_NAME : new TextLabel(licenceName));
			url.setTarget(HtmlLinkState.TARGET_BLANK);

			final DivRenderer homeUrlDiv = new DivRenderer(new HtmlComponentState());
			homeUrlDiv.addClass("thirdPartyHome");
			if( !Check.isEmpty(thirdPartyUrl) )
			{
				final HtmlLinkState thirdPartyLink = new HtmlLinkState(new SimpleBookmark(thirdPartyUrl));
				thirdPartyLink.setLabel(new TextLabel(thirdPartyUrl));
				thirdPartyLink.setTarget(HtmlLinkState.TARGET_BLANK);
				homeUrlDiv.setNestedRenderable(new LinkRenderer(thirdPartyLink));
			}

			final TableRow row = tableState.addRow(thirdPartyName, url, homeUrlDiv);
			row.setSortData(thirdPartyName, licenceName, null);
		}
	}

	private String getParam(Extension extension, String parameterName)
	{
		final Parameter param = extension.getParameter(parameterName);
		if( param != null )
		{
			return param.valueAsString();
		}
		return null;
	}

	public Table getLicenceTable()
	{
		return licenceTable;
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		extensionTracker = new PluginTracker<Object>(pluginService, "com.tle.common.licences", "licenceExtension", null);
	}
}
