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

package com.tle.web.cloud.settings.section;

import javax.inject.Inject;

import com.tle.common.settings.standard.SearchSettings;
import com.tle.core.cloud.settings.CloudSettings;
import com.tle.core.guice.Bind;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.settings.SearchSettingsExtension;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.annotations.Component;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
public class CloudSettingsSection extends AbstractPrototypeSection<Object>
	implements
		SearchSettingsExtension,
		HtmlRenderer
{
	@Component(name = "dc")
	private Checkbox disableCloudCheckbox;

	@ViewFactory
	private FreemarkerFactory view;

	@Inject
	private ConfigurationService configService;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		final CloudSettings cloudSettings = getCloudSettings();
		disableCloudCheckbox.setChecked(context, cloudSettings.isDisabled());
		return view.createResult("cloudsetting.ftl", context);
	}

	@Override
	public void save(SectionInfo info, SearchSettings settings)
	{
		final CloudSettings cloudSettings = getCloudSettings();
		cloudSettings.setDisabled(disableCloudCheckbox.isChecked(info));
		configService.setProperties(cloudSettings);
	}

	private CloudSettings getCloudSettings()
	{
		return configService.getProperties(new CloudSettings());
	}

	public Checkbox getDisableCloudCheckbox()
	{
		return disableCloudCheckbox;
	}
}
