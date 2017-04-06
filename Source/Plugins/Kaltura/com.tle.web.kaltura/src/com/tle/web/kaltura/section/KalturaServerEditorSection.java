package com.tle.web.kaltura.section;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import com.dytech.edge.common.valuebean.ValidationError;
import com.dytech.edge.exceptions.InvalidDataException;
import com.google.common.collect.Lists;
import com.kaltura.client.enums.KalturaSessionType;
import com.kaltura.client.types.KalturaUiConf;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.i18n.LangUtils;
import com.tle.common.kaltura.entity.KalturaServer;
import com.tle.core.guice.Bind;
import com.tle.core.kaltura.service.KalturaService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.i18n.BundleNameValue;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.MultiEditBox;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
@Bind
public class KalturaServerEditorSection
	extends
		AbstractPrototypeSection<KalturaServerEditorSection.KalturaServerEditorModel>
	implements
		HtmlRenderer,
		ModalKalturaServerSection
{
	private static final CssInclude CSS = CssInclude.include(
		ResourcesService.getResourceHelper(KalturaServerEditorSection.class).url("css/kalturaservers.css")).make();

	@PlugKey("editor.label.page.title.new")
	private static Label LABEL_CREATE_TITLE;
	@PlugKey("editor.label.page.title.edit")
	private static Label LABEL_EDIT_TITLE;
	@PlugKey("editor.save.receipt")
	private static Label SAVE_RECEIPT_LABEL;
	@PlugKey("editor.label.partnerid.unlockwarning")
	private static Label LABEL_PARTNERID_WARNING;
	@PlugKey("editor.label.subpartnerid.unlockwarning")
	private static Label LABEL_SUBPARTNERID_WARNING;

	@PlugKey("editor.test.failure")
	private static Label TEST_CONNECTION_FAILURE;
	@PlugKey("editor.test.nottested")
	private static Label TEST_CONNECTION_NOTTESTED;

	@PlugKey("editor.label.uiconf.default")
	private static String EQUELLA_DEFAULT;
	@PlugKey("editor.validation.")
	private static String KEYPFX_VALIDATE;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Inject
	private ReceiptService receiptService;
	@Inject
	private KalturaService kalturaService;

	@Inject
	@Component(name = "t")
	private MultiEditBox title;
	@Inject
	@Component(name = "d")
	private MultiEditBox description;
	@Component(name = "ep")
	private TextField endPoint;
	@Component(name = "pid")
	private TextField partnerId;
	@Component(name = "spid")
	private TextField subPartnerId;
	@Component(name = "as")
	private TextField adminSecret;
	@Component(name = "us")
	private TextField userSecret;
	@Component(name = "confid")
	private SingleSelectionList<NameValue> selectConfId;

	@Component
	@PlugKey("editor.button.save")
	private Button saveButton;
	@Component
	@PlugKey("editor.button.cancel")
	private Button cancelButton;

	@Component
	@PlugKey("editor.test.button")
	private Button testButton;

	@Component
	@PlugKey("editor.button.unlock")
	private Button unlockPartnerIdButton;

	@Component(name = "upids", stateful = false)
	private Checkbox unlockPidState;

	@Component
	@PlugKey("editor.button.unlock")
	private Button unlockSubPartnerIdButton;

	@Component(name = "uspids", stateful = false)
	private Checkbox unlockSpidState;

	@TreeLookup
	private OneColumnLayout<?> rootSection;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		description.setSize(3);

		unlockPartnerIdButton.setClickHandler(new StatementHandler(Js.call_s(
			unlockPartnerIdButton.createDisableFunction(), partnerId.createGetExpression()), Js.call_s(
			partnerId.createDisableFunction(), false), Js.call_s(unlockPidState.createSetFunction(), true))
			.addValidator(new Confirm(LABEL_PARTNERID_WARNING)));

		unlockSubPartnerIdButton.setClickHandler(new StatementHandler(Js.call_s(
			unlockSubPartnerIdButton.createDisableFunction(), subPartnerId.createGetExpression()), Js.call_s(
			subPartnerId.createDisableFunction(), false), Js.call_s(unlockSpidState.createSetFunction(), true))
			.addValidator(new Confirm(LABEL_SUBPARTNERID_WARNING)));

		saveButton.setClickHandler(events.getNamedHandler("save"));
		cancelButton.setClickHandler(events.getNamedHandler("cancel"));
		testButton.setClickHandler(new StatementHandler(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("testConnection"), ajax.getEffectFunction(EffectType.REPLACE_WITH_LOADING),
			"connectionstatus", "required", "testbutton")));

		selectConfId.setListModel(new DynamicHtmlListModel<NameValue>()
		{
			@Override
			protected Iterable<NameValue> populateModel(SectionInfo info)
			{
				List<NameValue> opts = Lists.newArrayList();

				if( getModel(info).isSuccessful() )
				{
					KalturaServer ks = getDetailsFromForm(info);
					List<KalturaUiConf> players = kalturaService.getPlayers(ks);
					for( KalturaUiConf uiConf : players )
					{
						opts.add(new NameValue(uiConf.name, Integer.toString(uiConf.id)));
					}

					Collections.sort(opts, new Comparator<NameValue>()
					{
						@Override
						public int compare(NameValue nv1, NameValue nv2)
						{
							return nv1.getName().compareTo(nv2.getName());
						}
					});

					opts.add(
						0,
						new BundleNameValue(EQUELLA_DEFAULT,
							Integer.toString(kalturaService.getDefaultKdpUiConf(ks).id)));
				}
				return opts;
			}
		});
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		KalturaServerEditorModel model = getModel(context);
		model.setPageTitle(getPageTitle(context));

		if( !Check.isEmpty(model.getEditUuid()) )
		{
			partnerId.setDisabled(context, !unlockPidState.isChecked(context));
			unlockPartnerIdButton.setDisabled(context, unlockPidState.isChecked(context));
			partnerId.getState(context).addClasses("edit");

			subPartnerId.setDisabled(context, !unlockSpidState.isChecked(context));
			unlockSubPartnerIdButton.setDisabled(context, unlockSpidState.isChecked(context));
			subPartnerId.getState(context).addClasses("edit");
		}

		final GenericTemplateResult templateResult = new GenericTemplateResult();
		templateResult.addNamedResult(OneColumnLayout.BODY,
			new CombinedRenderer(viewFactory.createResult("kalturaservereditor.ftl", this), CSS));
		return templateResult;
	}

	public void createNew(SectionInfo info)
	{
		getModel(info).setEditing(true);
	}

	public void startEdit(SectionInfo info, String kalturaServerUuid)
	{
		final KalturaServerEditorModel model = getModel(info);
		model.setEditUuid(kalturaServerUuid);
		model.setEditing(true);

		KalturaServer ks = kalturaService.getForEdit(kalturaServerUuid);

		final LanguageBundle name = ks.getName();
		if( name != null )
		{
			title.setLanguageBundle(info, LangUtils.convertBundleToBean(name));
		}

		final LanguageBundle desc = ks.getDescription();
		if( desc != null )
		{
			description.setLanguageBundle(info, LangUtils.convertBundleToBean(desc));
		}

		endPoint.setValue(info, ks.getEndPoint());
		partnerId.setValue(info, Integer.toString(ks.getPartnerId()));
		int spid = ks.getSubPartnerId();
		subPartnerId.setValue(info, spid > 0 ? Integer.toString(spid) : "");
		adminSecret.setValue(info, ks.getAdminSecret());
		userSecret.setValue(info, ks.getUserSecret());
		selectConfId.setSelectedStringValue(info, Integer.toString(ks.getKdpUiConfId()));

		model.setSuccessful(false);
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_MODAL_LOGIC)
	public void checkModal(SectionInfo info)
	{
		final KalturaServerEditorModel model = getModel(info);
		if( model.isEditing() )
		{
			rootSection.setModalSection(info, this);
		}
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		KalturaServerEditorModel model = getModel(info);
		if( model.isSuccessful() && saveKalturaSettings(info, false) )
		{
			receiptService.setReceipt(SAVE_RECEIPT_LABEL);
			model.setEditing(false);
			model.setEditUuid(null);
			model.setSuccessful(false);
			SectionUtils.clearModel(info, this);
		}
		else
		{
			loadDisabledEdit(info);
			model.addError("nottested", TEST_CONNECTION_NOTTESTED);
			info.preventGET();
		}
	}

	@EventHandlerMethod
	public void cancel(SectionInfo info)
	{
		KalturaServerEditorModel model = getModel(info);
		model.setEditing(false);
		model.setEditUuid(null);
		model.setSuccessful(false);
		SectionUtils.clearModel(info, this);
	}

	@EventHandlerMethod
	public void testConnection(SectionInfo info)
	{
		KalturaServerEditorModel model = getModel(info);
		boolean test = saveKalturaSettings(info, true);
		model.setSuccessful(test);
	}

	public int parseInput(String field, String input)
	{
		try
		{
			return Check.isEmpty(input) ? 0 : Integer.parseInt(input);
		}
		catch( NumberFormatException e )
		{
			return -1;
		}
	}

	private KalturaServer getDetailsFromForm(SectionInfo info)
	{
		loadDisabledEdit(info);
		KalturaServer ks = new KalturaServer();
		ks.setName(LangUtils.convertBeanToBundle(title.getLanguageBundle(info)));
		ks.setDescription(LangUtils.convertBeanToBundle(description.getLanguageBundle(info)));
		ks.setEndPoint(endPoint.getValue(info));
		ks.setPartnerId(parseInput("partnerid", partnerId.getValue(info)));
		ks.setSubPartnerId(parseInput("subpartnerid", subPartnerId.getValue(info)));
		ks.setAdminSecret(adminSecret.getValue(info));
		ks.setUserSecret(userSecret.getValue(info));

		return ks;
	}

	private void loadDisabledEdit(SectionInfo info)
	{
		String editUuid = getModel(info).getEditUuid();
		KalturaServer edited = null;
		if( !Check.isEmpty(editUuid) )
		{
			edited = kalturaService.getByUuid(editUuid);
			if( edited != null )
			{
				if( Check.isEmpty(partnerId.getValue(info)) )
				{
					partnerId.setValue(info, Integer.toString(edited.getPartnerId()));
				}

				if( Check.isEmpty(subPartnerId.getValue(info)) )
				{
					int spid = edited.getSubPartnerId();
					subPartnerId.setValue(info, spid > 0 ? Integer.toString(spid) : "");
				}
			}
		}
	}

	private boolean saveKalturaSettings(SectionInfo info, boolean testing)
	{
		KalturaServerEditorModel model = getModel(info);

		try
		{
			KalturaServer ks = getDetailsFromForm(info);

			// Not testing
			if( !testing )
			{
				testKalturaConnection(model, ks);
				ks.setAttribute("test", model.isSuccessful());
				ks.setKdpUiConfId(parseInput("uiconfid", selectConfId.getSelectedValue(info).getValue()));
				String editUuid = model.getEditUuid();
				if( Check.isEmpty(editUuid) )
				{
					ks.setUuid(UUID.randomUUID().toString());
					ks.setEnabled(true);
					return !Check.isEmpty(kalturaService.addKalturaServer(ks));
				}

				kalturaService.editKalturaServer(editUuid, ks);
				return true;
			}

			kalturaService.validate(null, ks);
			return testKalturaConnection(model, ks);

		}
		catch( InvalidDataException ide )
		{
			List<ValidationError> errors = ide.getErrors();
			for( ValidationError error : errors )
			{
				model.addError(error.getField(),
					new KeyLabel(KEYPFX_VALIDATE + error.getField() + '.' + error.getMessage()));
			}
		}

		return false;
	}

	private boolean testKalturaConnection(KalturaServerEditorModel model, KalturaServer ks)
	{
		boolean success = false;
		try
		{
			success = kalturaService.testKalturaSetup(ks, KalturaSessionType.ADMIN)
				&& kalturaService.testKalturaSetup(ks, KalturaSessionType.USER);

		}
		catch( Exception e )
		{
			if( model.isSuccessful() )
			{
				model.setSuccessful(false);
				throw new InvalidDataException(Lists.newArrayList(new ValidationError("connectiontest", "invalid")));
			}

			model.addError("connectiontest", TEST_CONNECTION_FAILURE);
		}
		return success;
	}

	@Override
	public void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setTitle(getPageTitle(info));
	}

	private Label getPageTitle(SectionInfo info)
	{
		final KalturaServerEditorModel model = getModel(info);
		return (!Check.isEmpty(model.getEditUuid()) ? LABEL_EDIT_TITLE : LABEL_CREATE_TITLE);
	}

	@Override
	public Class<KalturaServerEditorModel> getModelClass()
	{
		return KalturaServerEditorModel.class;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new KalturaServerEditorModel();
	}

	public class KalturaServerEditorModel
	{
		@Bookmarked(name = "ed")
		private boolean editing; // Show the editor
		@Bookmarked
		private boolean successful; // Testing successful
		@Bookmarked
		private String editUuid; // Which server to edit

		private Label pageTitle; // Add or Edit

		private final Map<String, Label> errors = new HashMap<String, Label>();

		public Map<String, Label> getErrors()
		{
			return errors;
		}

		public void addError(String key, Label label)
		{
			this.errors.put(key, label);
		}

		public boolean isSuccessful()
		{
			return successful;
		}

		public void setSuccessful(boolean successful)
		{
			this.successful = successful;
		}

		public Label getPageTitle()
		{
			return pageTitle;
		}

		public void setPageTitle(Label pageTitle)
		{
			this.pageTitle = pageTitle;
		}

		public boolean isEditing()
		{
			return editing;
		}

		public void setEditing(boolean editing)
		{
			this.editing = editing;
		}

		public String getEditUuid()
		{
			return editUuid;
		}

		public void setEditUuid(String editUuid)
		{
			this.editUuid = editUuid;
		}
	}

	public MultiEditBox getTitle()
	{
		return title;
	}

	public MultiEditBox getDescription()
	{
		return description;
	}

	public TextField getEndPoint()
	{
		return endPoint;
	}

	public TextField getPartnerId()
	{
		return partnerId;
	}

	public TextField getUserSecret()
	{
		return userSecret;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public Button getTestButton()
	{
		return testButton;
	}

	public TextField getAdminSecret()
	{
		return adminSecret;
	}

	public TextField getSubPartnerId()
	{
		return subPartnerId;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	public SingleSelectionList<NameValue> getSelectConfId()
	{
		return selectConfId;
	}

	public Button getUnlockPartnerIdButton()
	{
		return unlockPartnerIdButton;
	}

	public Button getUnlockSubPartnerIdButton()
	{
		return unlockSubPartnerIdButton;
	}

	public Checkbox getUnlockPidState()
	{
		return unlockPidState;
	}

	public Checkbox getUnlockSpidState()
	{
		return unlockSpidState;
	}
}
