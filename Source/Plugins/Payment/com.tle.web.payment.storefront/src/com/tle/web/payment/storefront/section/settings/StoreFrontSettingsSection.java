package com.tle.web.payment.storefront.section.settings;

import javax.inject.Inject;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.core.guice.Bind;
import com.tle.core.payment.storefront.privileges.StoreFrontSettingsPrivilegeTreeProvider;
import com.tle.core.payment.storefront.settings.StoreFrontSettings;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.entity.ItemDefinitionService;
import com.tle.web.i18n.BundleCache;
import com.tle.web.i18n.BundleNameValue;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.generic.expression.SelectNotEmpty;
import com.tle.web.sections.js.validators.ConditionalConfirm;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.LabelOption;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author aaron
 */
@Bind
@SuppressWarnings("nls")
public class StoreFrontSettingsSection extends OneColumnLayout<StoreFrontSettingsSection.StorefrontSettingsModel>
{
	@PlugKey("settings.page.title")
	private static Label TITLE_LABEL;
	@PlugKey("settings.save.receipt")
	private static Label SAVE_RECEIPT_LABEL;
	@PlugKey("settings.collection.none.selected.warning")
	private static Label LABEL_NON_SELECTION_WARNING;
	@PlugKey("settings.include.tax.true")
	private static Label LABEL_INCLUDE_TAX_TRUE;
	@PlugKey("settings.include.tax.false")
	private static Label LABEL_INCLUDE_TAX_FALSE;
	// Note: this isn't a Label because it contains '<' and is treated as HTML
	// by default, which breaks the dropdown!
	@PlugKey("settings.collection.none")
	private static String KEY_COLLECTION_NONE;

	@Inject
	private StoreFrontSettingsPrivilegeTreeProvider securityProvider;
	@Inject
	private ConfigurationService configService;
	@Inject
	private ReceiptService receiptService;
	@Inject
	private ItemDefinitionService itemDefService;
	@Inject
	private BundleCache bundleCache;

	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	protected AjaxGenerator ajax;

	/**
	 * Validation logic expects that the top top option will be a "None"
	 */
	@Component(name = "cl", stateful = false)
	private SingleSelectionList<BaseEntityLabel> collectionList;
	@Component(name = "it", stateful = false)
	private SingleSelectionList<Boolean> includeTax;
	@Component
	@PlugKey("settings.save.button")
	private Button saveButton;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		collectionList.setListModel(new CollectionsListModel());
		collectionList.setAlwaysSelect(true);

		saveButton.setClickHandler(events.getNamedHandler("save").addValidator(
			new ConditionalConfirm(new SelectNotEmpty(collectionList, true), LABEL_NON_SELECTION_WARNING)));

		includeTax.setListModel(new SimpleHtmlListModel<Boolean>(true, false)
		{
			@Override
			protected Option<Boolean> convertToOption(Boolean obj)
			{
				return new LabelOption<Boolean>(obj ? LABEL_INCLUDE_TAX_TRUE : LABEL_INCLUDE_TAX_FALSE, Boolean
					.toString(obj), obj);
			}
		});
	}

	@Override
	protected TemplateResult setupTemplate(RenderEventContext context)
	{
		securityProvider.checkAuthorised();

		StoreFrontSettings settings = configService.getProperties(new StoreFrontSettings());
		String collection = settings.getCollection();
		if( collection == null )
		{
			collection = "";
		}
		collectionList.setSelectedStringValue(context, collection);
		includeTax.setSelectedStringValue(context, Boolean.toString(settings.isIncludeTax()));

		return new GenericTemplateResult(viewFactory.createNamedResult(BODY, "storefrontsettings.ftl", this));
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setTitle(TITLE_LABEL);
		crumbs.addToStart(SettingsUtils.getBreadcrumb());
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		final StoreFrontSettings settings = configService.getProperties(new StoreFrontSettings());
		settings.setIncludeTax(includeTax.getSelectedValue(info));
		final BaseEntityLabel selection = collectionList.getSelectedValue(info);
		settings.setCollection(selection != null ? selection.getUuid() : null);
		configService.setProperties(settings);

		receiptService.setReceipt(SAVE_RECEIPT_LABEL);
	}

	@Override
	public StorefrontSettingsModel instantiateModel(SectionInfo info)
	{
		return new StorefrontSettingsModel();
	}

	protected class CollectionsListModel extends DynamicHtmlListModel<BaseEntityLabel>
	{
		public CollectionsListModel()
		{
			setSort(true);
		}

		@Override
		protected Option<BaseEntityLabel> getTopOption()
		{
			return new LabelOption<BaseEntityLabel>(new KeyLabel(false, KEY_COLLECTION_NONE), "", null);
		}

		@Override
		protected Iterable<BaseEntityLabel> populateModel(SectionInfo info)
		{
			return itemDefService.listAll();
		}

		@Override
		protected Option<BaseEntityLabel> convertToOption(SectionInfo info, BaseEntityLabel bent)
		{
			return new NameValueOption<BaseEntityLabel>(new BundleNameValue(bent.getBundleId(), bent.getUuid(),
				bundleCache), bent);
		}
	}

	public static class StorefrontSettingsModel extends OneColumnLayout.OneColumnLayoutModel
	{
		// Nothing to add
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public SingleSelectionList<BaseEntityLabel> getCollectionList()
	{
		return collectionList;
	}

	public SingleSelectionList<Boolean> getIncludeTax()
	{
		return includeTax;
	}
}
