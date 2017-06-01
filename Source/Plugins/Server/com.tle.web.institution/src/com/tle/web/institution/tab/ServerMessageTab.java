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

package com.tle.web.institution.tab;

import javax.inject.Inject;

import com.tle.core.system.SystemConfigService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.institution.InstitutionSection;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
public class ServerMessageTab extends AbstractPrototypeSection<Object> implements HtmlRenderer
{
	@Inject
	private SystemConfigService systemConfigService;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@TreeLookup
	private InstitutionSection institutionSection;

	@Component
	private Checkbox enabled;
	@Component
	private TextField message;
	@Component
	@PlugKey(value = "institutions.server.message.save", global = true)
	private Button save;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		save.setClickHandler(events.getNamedHandler("saveClicked"));
	}

	@EventHandlerMethod
	public void saveClicked(SectionContext context)
	{
		systemConfigService.setServerMessage(message.getValue(context), enabled.isChecked(context));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( systemConfigService.adminPasswordNotSet() )
		{
			return null;
		}

		enabled.setChecked(context, systemConfigService.isServerMessageEnabled());
		message.setValue(context, systemConfigService.getServerMessage());
		return viewFactory.createResult("tab/servermessage.ftl", context);
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "server_message";
	}

	public Checkbox getEnabled()
	{
		return enabled;
	}

	public TextField getMessage()
	{
		return message;
	}

	public Button getSave()
	{
		return save;
	}
}
