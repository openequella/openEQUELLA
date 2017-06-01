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

package com.tle.web.viewitem.summary.section;

import java.io.StringReader;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.PropBagWrapper;
import com.dytech.edge.common.ScriptContext;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.beans.item.ItemPack;
import com.tle.common.Check;
import com.tle.common.scripting.service.ScriptingService;
import com.tle.core.guice.Bind;
import com.tle.core.scripting.service.StandardScriptContextParams;
import com.tle.web.freemarker.BasicFreemarkerFactory;
import com.tle.web.freemarker.FreemarkerSectionResult;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.viewitem.section.AbstractParentViewItemSection;
import com.tle.web.viewitem.service.ItemXsltService;
import com.tle.web.viewurl.ItemSectionInfo;

@SuppressWarnings("nls")
@Bind
public class FreemarkerSection extends AbstractParentViewItemSection<Object> implements DisplaySectionConfiguration
{
	@Inject
	private ScriptingService scriptService;
	@Inject
	private ItemXsltService xmlService;
	@Inject
	private BasicFreemarkerFactory custFactory;

	private String markup;
	private String script;

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return true;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( Check.isEmpty(markup) && Check.isEmpty(script) )
		{
			return null;
		}

		ItemSectionInfo itemInfo = getItemInfo(context);

		// The xmlService.getXmlForXslt call happens twice if you have both XSLT
		// and Freemarker. No harm but a bit ghetto
		ItemPack itemPack = new ItemPack(itemInfo.getItem(), xmlService.getXmlForXslt(context, itemInfo), null);

		StandardScriptContextParams params = new StandardScriptContextParams(itemPack, null,
			true, null);

		params.getAttributes().put("context", context.getPreRenderContext());
		ScriptContext scriptContext = scriptService.createScriptContext(params);
		scriptContext.addScriptObject("attributes", new PropBagWrapper(new PropBagEx()));

		// Run script
		scriptService.executeScript(script, "itemSummary", scriptContext, false);

		// Uses custom Freemarker Factory (Removes access to internal sections
		// functions) AdvancedWebScriptControl uses similar
		FreemarkerSectionResult result = custFactory.createResult("viewItemFreemarker", //$NON-NLS-1$
			new StringReader(markup), context);
		// script context objects are available in the Freemarker templates
		for( Map.Entry<String, Object> entry : scriptContext.getScriptObjects().entrySet() )
		{
			result.addExtraObject(entry.getKey(), entry.getValue());
		}

		return result;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "freemarker";
	}

	@Override
	public void associateConfiguration(SummarySectionsConfig config)
	{
		String configuration = config.getConfiguration();
		try
		{
			PropBagEx xml = new PropBagEx(configuration);
			markup = xml.getNode("markup");
			script = xml.getNode("script");
		}
		catch( Exception e )
		{
			markup = configuration;
		}
	}
}