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

package com.tle.web.htmleditor.tinymce.addon.tle;

import java.util.Map;

import javax.inject.Inject;

import com.tle.beans.mime.MimeEntry;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleNameValue;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.htmleditor.tinymce.addon.tle.service.MimeTemplateService;
import com.tle.web.mimetypes.MimeEditExtension;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
public class MimeTemplateSection extends AbstractPrototypeSection<Object> implements MimeEditExtension, HtmlRenderer
{
	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(MimeTemplateSection.class);
	private static final NameValue TAB_TEMPLATES = new BundleNameValue(resources.key("mimetemplate.title"),
		"EmbedTemplate");

	@Component
	private TextField template;
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private MimeTemplateService mimeTemplateService;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return viewFactory.createResult("mimeext.ftl", context);
	}

	@Override
	public NameValue getTabToAppearOn()
	{
		return TAB_TEMPLATES;
	}

	@Override
	public boolean isVisible(SectionInfo info)
	{
		return true;
	}

	@Override
	public void loadEntry(SectionInfo info, MimeEntry entry)
	{
		if( entry != null )
		{
			template.setValue(info, mimeTemplateService.getTemplateForMimeEntry(entry));
		}
	}

	@Override
	public void saveEntry(SectionInfo info, MimeEntry entry)
	{
		String value = template.getValue(info);
		Map<String, String> attrs = entry.getAttributes();
		if( !Check.isEmpty(value) )
		{
			attrs.put(TinyMceAddonConstants.MIME_TEMPLATE_KEY, value);
		}
		else
		{
			attrs.remove(TinyMceAddonConstants.MIME_TEMPLATE_KEY);
		}
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "mts";
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}

	public TextField getTemplate()
	{
		return template;
	}
}
