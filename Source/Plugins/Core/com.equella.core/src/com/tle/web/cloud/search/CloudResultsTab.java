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

package com.tle.web.cloud.search;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.tle.annotation.NonNullByDefault;
import com.tle.core.cloud.service.CloudService;
import com.tle.core.guice.Bind;
import com.tle.web.cloud.CloudWebConstants;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.searching.OnSearchExtension;
import com.tle.web.searching.SearchTab;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.AbstractQuerySection;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.RuntimeExpression;
import com.tle.web.sections.js.generic.expression.StringExpression;
import com.tle.web.sections.js.generic.function.CallAndReferenceFunction;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.Div;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.selection.section.CourseListSection;

@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class CloudResultsTab extends AbstractPrototypeSection<CloudResultsTab.CloudResultsTabModel>
	implements
		SearchTab,
		OnSearchExtension
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CloudResultsTab.class);

	private static PluginResourceHelper resources = ResourcesService.getResourceHelper(CloudResultsTab.class);

	private static IncludeFile JS_INCLUDE = new IncludeFile(resources.url("scripts/cloud.js"));
	private static final JSCallAndReference CLOUD_CLASS = new ExternallyDefinedFunction("Cloud", JS_INCLUDE);
	private static final ExternallyDefinedFunction FUNC_ON_SEARCH = new ExternallyDefinedFunction(CLOUD_CLASS,
		"onSearch", 1, JS_INCLUDE);

	@PlugKey("resultstab.inactive.resultcount.title")
	private static String KEY_CLOUD_COUNT;
	@PlugKey("resultstab.inactive.searching.title")
	private static Label LABEL_SEARCHING_CLOUD;
	@PlugKey("resultstab.inactive.blanksearch.title")
	private static Label LABEL_SEARCH_THE_CLOUD;

	@Component(name = "cs")
	private Div countSpan;
	private JSCallAndReference updateCountFunction;

	@Inject
	private CloudService cloudService;

	@AjaxFactory
	private AjaxGenerator ajax;
	@ViewFactory
	private FreemarkerFactory view;
	@EventFactory
	private EventGenerator events;

	@TreeLookup
	private AbstractQuerySection<?, ?> sqs;

	private boolean active;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		if( !cloudService.isCloudy() )
		{
			return null;
		}

		countSpan.setRendererType(context, "span");
		return view.createResult("cloudtab.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		if( !isActive() )
		{
			updateCountFunction = CallAndReferenceFunction.get(Js.function(Js.call_s(FUNC_ON_SEARCH,
				ajax.getAjaxFunction("onSearch"), Jq.$(countSpan), new RuntimeExpression()
				{
					@Override
					protected JSExpression createExpression(RenderContext info)
					{
						return new StringExpression(LABEL_SEARCHING_CLOUD.getText());
					}
				})), countSpan);

			countSpan.addReadyStatements(updateCountFunction);
		}
	}

	@Override
	public String getId()
	{
		return "cloud";
	}

	@Override
	public SectionInfo getForward(SectionInfo info)
	{
		// TODO: this is less hacky than before, but still sub-ottstimal.
		final CourseListSection cls = info.lookupSection(CourseListSection.class);
		if( cls != null && cls.isApplicable(info) )
		{
			return info.createForward("/access/course/cloudsearch.do");
		}
		return info.createForward(CloudWebConstants.URL_CLOUD_SEARCH);
	}

	@AjaxMethod
	public CloudAjaxResponse onSearch(SectionInfo info)
	{
		final String q = sqs.getParsedQuery(info);
		final CloudAjaxResponse response = new CloudAjaxResponse();
		try
		{
			response.setCount(cloudService.resultCount(q));
		}
		catch( Exception e )
		{
			LOGGER.warn("Error getting result count from cloud", e);
			response.setCount(0);
		}
		if( Strings.isNullOrEmpty(q) )
		{
			response.setText(LABEL_SEARCH_THE_CLOUD.getText());
		}
		else
		{
			response.setText(new PluralKeyLabel(KEY_CLOUD_COUNT, response.getCount()).getText());
		}
		return response;
	}

	@Override
	public JSCallAndReference getOnSearchCallable()
	{
		return updateCountFunction;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new CloudResultsTabModel();
	}

	public Div getCountSpan()
	{
		return countSpan;
	}

	@Override
	public void setActive()
	{
		active = true;
	}

	@Override
	public boolean isActive()
	{
		return active;
	}

	@NonNullByDefault(false)
	public static class CloudAjaxResponse
	{
		private int count;
		private String text;

		public int getCount()
		{
			return count;
		}

		public void setCount(int count)
		{
			this.count = count;
		}

		public String getText()
		{
			return text;
		}

		public void setText(String text)
		{
			this.text = text;
		}
	}

	@NonNullByDefault(false)
	public static class CloudResultsTabModel
	{
		// Nothing, yet
	}
}