/**
 * 
 */
package com.tle.web.harvesterskipdrmsettings;

import javax.inject.Inject;

import com.tle.beans.system.HarvesterSkipDrmSettings;
import com.tle.core.services.config.ConfigurationService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author larry
 */
public class RootHarvesterSkipDrmSettingsSection
	extends
		OneColumnLayout<RootHarvesterSkipDrmSettingsSection.HarvesterSkipDrmSettingsModel>
{
	@PlugKey("harvesterskipdrmsettings.title")
	private static Label TITLE_LABEL;

	@Component(name = "sd", parameter = "sdk", supported = true)
	protected Checkbox allowSkip;

	@Component
	@PlugKey("settings.save.button")
	private Button saveButton;

	@PlugKey("settings.save.receipt")
	private static Label SAVE_RECEIPT_LABEL;

	@Inject
	private ConfigurationService configService;
	@Inject
	private HarvesterSkipDrmSettingsPrivilegeTreeProvider securityProvider;
	@Inject
	private ReceiptService receiptService;
	@EventFactory
	private EventGenerator events;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		saveButton.setClickHandler(events.getNamedHandler("save"));
	}

	@Override
	protected TemplateResult setupTemplate(RenderEventContext info)
	{
		securityProvider.checkAuthorised();
		allowSkip.setChecked(info, configService.getProperties(new HarvesterSkipDrmSettings()).isHarvestingSkipDrm());
		return new GenericTemplateResult(viewFactory.createNamedResult(BODY, "harvesterskipdrmsettings.ftl", this));
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		HarvesterSkipDrmSettings settings = configService.getProperties(new HarvesterSkipDrmSettings());
		boolean oldAllowSkip = settings.isHarvestingSkipDrm();
		boolean newAllowSkip = allowSkip.isChecked(info);
		if( oldAllowSkip != newAllowSkip )
		{
			settings.setHarvestingSkipDrm(newAllowSkip);
			configService.setProperties(settings);
			receiptService.setReceipt(SAVE_RECEIPT_LABEL);
		}
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setTitle(TITLE_LABEL);
		crumbs.addToStart(SettingsUtils.getBreadcrumb());
	}

	public Checkbox getAllowSkip()
	{
		return allowSkip;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	@Override
	public Class<HarvesterSkipDrmSettingsModel> getModelClass()
	{
		return HarvesterSkipDrmSettingsModel.class;
	}

	public static class HarvesterSkipDrmSettingsModel extends OneColumnLayout.OneColumnLayoutModel
	{
		// Empty class, if we haven't thought of anything we need to carry from
		// section to template.
	}
}
