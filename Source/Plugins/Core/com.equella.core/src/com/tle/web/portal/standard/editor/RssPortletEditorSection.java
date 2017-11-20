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

package com.tle.web.portal.standard.editor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.edge.common.Constants;
import com.tle.common.Check;
import com.tle.common.portal.entity.Portlet;
import com.tle.core.filesystem.CachedFile;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.portal.service.PortletEditingBean;
import com.tle.core.services.FileSystemService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.editor.AbstractPortletEditorSection;
import com.tle.web.portal.standard.PortalStandardConstants;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
public class RssPortletEditorSection
	extends
		AbstractPortletEditorSection<RssPortletEditorSection.RssPortletEditorModel>
{
	private static final String TYPE = "rss";
	public static final String KEY_RESULTCOUNT = "defaultResultCount";
	public static final String KEY_TITLEONLY = "titleOnly";

	private static final PluginResourceHelper RESOURCES = ResourcesService
		.getResourceHelper(RssPortletEditorSection.class);

	@PlugKey("editor.rss.label.titleonly")
	private static String titleOnly;
	@PlugKey("editor.rss.label.titledesc")
	private static String titleAndDescription;

	@Inject
	private FileSystemService fileService;
	@Inject
	private PortalStandardConstants portalSettings;

	@ViewFactory
	private FreemarkerFactory thisView;

	@Component(name = "u", stateful = false)
	private TextField url;
	@Component(name = "r", stateful = false)
	private TextField defaultResultsCount;
	@Component(name = "d", stateful = false)
	private SingleSelectionList<BundleNameValue> displayTypeList;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		displayTypeList.setAlwaysSelect(true);
		displayTypeList.setListModel(new SimpleHtmlListModel<BundleNameValue>(new BundleNameValue(titleAndDescription,
			null), new BundleNameValue(titleOnly, KEY_TITLEONLY)));
	}

	@Override
	protected Portlet createNewPortlet()
	{
		return new Portlet(TYPE);
	}

	@Override
	protected SectionRenderable customRender(RenderEventContext context, RssPortletEditorModel model,
		PortletEditingBean portlet) throws Exception
	{
		return thisView.createResult("edit/editrssportlet.ftl", context);
	}

	@Override
	protected void customLoad(SectionInfo info, PortletEditingBean portlet)
	{
		final String u = portlet.getConfig();
		if( !Check.isEmpty(u) )
		{
			url.setValue(info, u);
		}
		else
		{
			url.setValue(info, "http://");
		}

		defaultResultsCount.setValue(info, portlet.getAttribute(KEY_RESULTCOUNT));
		displayTypeList.setSelectedStringValue(info, portlet.getAttribute(KEY_TITLEONLY));
	}

	@Override
	protected void customSave(SectionInfo info, PortletEditingBean portlet)
	{
		portlet.setConfig(url.getValue(info));
		portlet.setAttribute(KEY_RESULTCOUNT, defaultResultsCount.getValue(info));
		portlet.setAttribute(KEY_TITLEONLY, displayTypeList.getSelectedValueAsString(info));
		// invalidate the cache (if any)
		if( portlet.getId() != 0 )
		{
			CachedFile rssCache = new CachedFile(portlet.getUuid());
			fileService.removeFile(rssCache, PortalStandardConstants.FEED_CACHE_FILE);
		}
	}

	@Override
	protected void customClear(SectionInfo info)
	{
		url.setValue(info, Constants.BLANK);
		defaultResultsCount.setValue(info, Constants.BLANK);
		displayTypeList.setSelectedStringValue(info, null);
	}

	@Override
	protected void customValidate(SectionInfo info, PortletEditingBean portlet, Map<String, Object> errors)
	{
		// FIXME: would need be duplicated in the service... extension point??
		try
		{
			final URL u = new URL(url.getValue(info));
			try
			{
				u.openConnection().connect();
			}
			catch( Exception e )
			{
				errors.put(
					"url",
					RESOURCES.getString("editor.rss.error.url.notreachable",
						e.getClass().getName() + ' ' + e.getMessage()));
			}
		}
		catch( MalformedURLException mal )
		{
			errors.put("url", RESOURCES.getString("editor.rss.error.url.notvalid"));
		}
		try
		{
			final int results = Integer.parseInt(defaultResultsCount.getValue(info));
			if( results <= 0 || results > portalSettings.getMaxRssResults() )
			{
				errors.put("results", RESOURCES.getString("editor.rss.error.results.outofrange"));
			}
		}
		catch( NumberFormatException nfe )
		{
			errors.put("results", RESOURCES.getString("editor.rss.error.results.notanumber"));
		}
	}

	@Override
	public Class<RssPortletEditorModel> getModelClass()
	{
		return RssPortletEditorModel.class;
	}

	public TextField getUrl()
	{
		return url;
	}

	public TextField getDefaultResultsCount()
	{
		return defaultResultsCount;
	}

	public SingleSelectionList<BundleNameValue> getDisplayTypeList()
	{
		return displayTypeList;
	}

	public static class RssPortletEditorModel extends AbstractPortletEditorSection.AbstractPortletEditorModel
	{
		// Nothing by default
	}

	@Override
	public SectionRenderable renderHelp(RenderContext context)
	{
		return thisView.createResult("help/rssportleteditorhelp.ftl", this);
	}
}
