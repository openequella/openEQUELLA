package com.tle.web.cloud.settings.section;

import javax.inject.Inject;

import com.tle.beans.system.SearchSettings;
import com.tle.core.cloud.settings.CloudSettings;
import com.tle.core.guice.Bind;
import com.tle.core.services.config.ConfigurationService;
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
