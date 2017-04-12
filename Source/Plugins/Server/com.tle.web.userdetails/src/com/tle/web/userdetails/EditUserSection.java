package com.tle.web.userdetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import javax.inject.Inject;

import com.dytech.edge.common.Constants;
import com.dytech.edge.common.valuebean.ValidationError;
import com.dytech.edge.exceptions.InvalidDataException;
import com.dytech.edge.web.WebConstants;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.system.AutoLogin;
import com.tle.beans.user.TLEUser;
import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.util.DateHelper;
import com.tle.core.accessibility.AccessibilityModeService;
import com.tle.core.notification.NotificationService;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.entity.ItemDefinitionService;
import com.tle.core.services.language.LanguageService;
import com.tle.core.services.user.TLEUserService;
import com.tle.core.services.user.UserPreferenceService;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.user.CurrentUser;
import com.tle.core.user.ModifiableUserState;
import com.tle.core.user.UserState;
import com.tle.core.workflow.notification.WorkflowPreferencesService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.i18n.BundleCache;
import com.tle.web.i18n.BundleNameValue;
import com.tle.web.navigation.MenuService;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.layout.TwoColumnLayout;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.equella.render.EquellaButtonExtension;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.generic.function.ReloadFunction;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.HtmlListModel;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.sections.standard.renderers.list.CheckListRenderer;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@NonNullByDefault
@SuppressWarnings("nls")
public class EditUserSection extends TwoColumnLayout<EditUserSection.EditUserModel>
{
	public static final String GMT_TIMEZONE_ID = "Etc/GMT";
	public static final String DATE_FORMAT_APPROX = "format.approx";
	public static final String DATE_FORMAT_EXACT = "format.exact";

	@PlugKey("common.savesuccess")
	private static Label SAVE_SUCCESS;

	@PlugKey("internal.change")
	private static Label PASS_LABEL;
	@PlugKey("my.title")
	private static Label MY_TITLE;
	@PlugKey("internal.title")
	private static Label INTERNAL_TITLE;
	@PlugKey("common.save")
	private static Label SAVE_LABEL;
	@PlugKey("regional.usedefault")
	private static String USE_BROWSER_LANG_KEY;

	@PlugKey("dateformat.usedefault")
	private static String USE_SYS_DEFAULT;
	@PlugKey("dateformat.exact")
	private static String USE_EXACT;
	@PlugKey("dateformat.relative")
	private static String USE_APPROX;

	@PlugURL("css/search.css")
	private static String URL_CSS;

	private final CssInclude cssInclude = new CssInclude(URL_CSS);

	@Inject
	private ItemDefinitionService itemDefService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private UserPreferenceService userPrefs;
	@Inject
	private WorkflowPreferencesService workflowPrefs;
	@Inject
	private LanguageService languageService;
	@Inject
	private TLEUserService tleUserService;
	@Inject
	private UserSessionService sessionService;
	@Inject
	private MenuService menuService;
	@Inject
	private ConfigurationService configService;
	@Inject
	private ReceiptService receiptService;
	@Inject
	private NotificationService notificationService;
	@Inject
	private AccessibilityModeService acModeService;

	@Component
	@Inject
	private ChangePasswordDialog passDialog;

	@Component(name = "fan")
	private TextField familyName;
	@Component(name = "fin")
	private TextField firstName;
	@Component(name = "em")
	private TextField email;

	@Component(name = "hln")
	private Checkbox hideLoginNotice;

	@Component(name = "acm")
	private Checkbox accessibilityMode;

	@Component(stateful = false)
	private Checkbox enableEmails;
	@Component
	private Button saveButton;
	@Component
	private Button closeButton;
	@Component
	private Button changePassButton;

	@Component(name = "itd")
	protected CollectionsList itemDefs;

	// i18n screen
	@Component(name = "ll")
	protected SingleSelectionList<NameValue> languageList;
	@Component(name = "tz")
	private SingleSelectionList<NameValue> timeZones;
	@Component(name = "df")
	private SingleSelectionList<NameValue> dateFormats;

	@EventFactory
	private EventGenerator events;

	@Override
	protected TemplateResult setupTemplate(RenderEventContext context)
	{
		if( !isEditUserDetailsAvailable() )
		{
			throw new AccessDeniedException(CurrentLocale.get("com.tle.web.userdetails.common.denied"));
		}

		EditUserModel model = getModel(context);
		model.setDefaultLanguage(Locale.getDefault().getDisplayName());
		if( model.isSetUpForm() )
		{
			// User details...
			familyName.setValue(context, CurrentUser.getDetails().getLastName());
			firstName.setValue(context, CurrentUser.getDetails().getFirstName());
			email.setValue(context, CurrentUser.getDetails().getEmailAddress());

			if( !model.isInternal() )
			{
				familyName.setDisabled(context, true);
				firstName.setDisabled(context, true);
				email.setDisabled(context, true);
			}

			// General
			hideLoginNotice.setChecked(context, userPrefs.isHideLoginNotice());
			enableEmails.setChecked(context, notificationService.isEmailEnabled());
			accessibilityMode.setChecked(context, acModeService.isAccessibilityMode());

			// Notifications
			prepareNotifications(context, workflowPrefs.getWatchedCollections());

			// Regional
			HtmlListModel<NameValue> languagesListModel = new LanguagesModel();
			languageList.setListModel(languagesListModel);
			Locale locale = userPrefs.getLocale();
			if( locale != null )
			{
				languageList.setSelectedStringValue(context, locale.toString());
			}
			TimeZone userZone = userPrefs.getTimeZone();
			if( userZone != null )
			{
				String zoneId = userZone.getID();
				if( "GMT".equals(zoneId) )
				{
					timeZones.setSelectedStringValue(context, GMT_TIMEZONE_ID);
				}
				else
				{
					timeZones.setSelectedStringValue(context, zoneId);
				}
			}

			model.setSetUpForm(false);
		}
		// Date format
		HtmlListModel<NameValue> dateFormatListModel = new DateFormatModel();
		dateFormats.setListModel(dateFormatListModel);
		String dateFormatValue = userPrefs.getDateFormat();
		if( dateFormatValue != null )
		{
			dateFormats.setSelectedStringValue(context, dateFormatValue);
		}

		GenericTemplateResult template = new GenericTemplateResult();
		template.addNamedResult(LEFT, viewFactory.createResult("edit/edit.ftl", this));
		template.addNamedResult(RIGHT, viewFactory.createResult("edit/save.ftl", this));
		template.addNamedResult("body", new CombinedRenderer(cssInclude));
		return template;
	}

	private boolean isEditUserDetailsAvailable()
	{
		UserState us = CurrentUser.getUserState();
		if( us.isGuest() || us.isSystem() )
		{
			return false;
		}

		if( us.wasAutoLoggedIn() )
		{
			AutoLogin autoLogin = configService.getProperties(new AutoLogin());
			return !autoLogin.isEditDetailsDisallowed();
		}

		return true;
	}

	public class LanguagesModel extends DynamicHtmlListModel<NameValue>
	{
		@Override
		protected Iterable<NameValue> populateModel(SectionInfo info)
		{
			List<NameValue> nvs = new ArrayList<NameValue>();
			nvs.add(new NameValue(CurrentLocale.get(USE_BROWSER_LANG_KEY), Constants.BLANK));
			for( Entry<String, Locale> entry : createLangMap().entrySet() )
			{
				nvs.add(new NameValue(entry.getValue().getDisplayName(), entry.getKey()));
			}
			return nvs;
		}
	}

	private class DateFormatModel extends DynamicHtmlListModel<NameValue>
	{
		@Override
		protected Iterable<NameValue> populateModel(SectionInfo info)
		{
			List<NameValue> values = new ArrayList<NameValue>();
			values.add(new NameValue(CurrentLocale.get(USE_SYS_DEFAULT), Constants.BLANK));
			values.add(new NameValue(CurrentLocale.get(USE_APPROX), DATE_FORMAT_APPROX));
			values.add(new NameValue(CurrentLocale.get(USE_EXACT), DATE_FORMAT_EXACT));
			return values;
		}
	}

	private Map<String, Locale> createLangMap()
	{
		Map<String, Locale> langMap = new LinkedHashMap<String, Locale>();
		for( Locale locale : languageService.listAvailableResourceBundles() )
		{
			// Don't let people select the root locale - it'll just happen :)
			if( locale != Locale.ROOT )
			{
				langMap.put(locale.toString(), locale);
			}
		}
		return langMap;
	}

	private void prepareNotifications(RenderContext context, Set<String> watchedItemDefs)
	{
		itemDefs.setListModel(new CollectionsListModel());
		itemDefs.setSelectedStringValues(context, watchedItemDefs);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		saveButton.setClickHandler(events.getNamedHandler("save"));
		saveButton.setDefaultRenderer(EquellaButtonExtension.ACTION_BUTTON);
		saveButton.setLabel(SAVE_LABEL);
		saveButton.setStyleClass("profile-save-button");
		timeZones.setListModel(new SimpleHtmlListModel<NameValue>(DateHelper.getTimeZoneNameValues(new BundleNameValue(
			"com.tle.web.userdetails.defaulttimezone", ""), false)));

		changePassButton.setLabel(PASS_LABEL);
		changePassButton.setDefaultRenderer(EquellaButtonExtension.ACTION_BUTTON);
		changePassButton.setClickHandler(passDialog.getOpenFunction());
		changePassButton.setStyleClass("change-pass-button");

		// So much better!
		// changePassLink.setDefaultRenderer(EquellaButtonExtension.ACTION_BUTTON);
		passDialog.setOkCallback(new ReloadFunction()); // Not used
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setTitle(CurrentUser.getUserState().isInternal() ? INTERNAL_TITLE : MY_TITLE);
		decorations.setContentBodyClass("user-details");
		HtmlLinkState linkState = new HtmlLinkState(new SimpleBookmark(WebConstants.DEFAULT_HOME_PAGE));
		linkState.setLabel(new KeyLabel("com.tle.web.portal.page.portal.title"));
		linkState.setTitle(new KeyLabel("com.tle.web.portal.page.breadcrumb.title"));
		crumbs.add(linkState);
	}

	@EventHandlerMethod
	public void save(SectionContext context)
	{
		EditUserModel model = getModel(context);
		model.getErrors().clear();

		if( !model.isInternal() )
		{
			saveUser(context);
		}
		else
		{
			TLEUser old = tleUserService.get(CurrentUser.getUserID());

			old.setEmailAddress(email.getValue(context));
			old.setFirstName(firstName.getValue(context));
			old.setLastName(familyName.getValue(context));

			try
			{
				tleUserService.validate(old, false);
			}
			catch( InvalidDataException ex )
			{
				for( ValidationError error : ex.getErrors() )
				{
					model.addError(error.getField(), error.getMessage());
				}
			}
			if( model.getErrors().size() == 0 )
			{
				tleUserService.editSelf(old, false);
				saveUser(context);

				// We probably need a nicer way of doing this
				UserState userState = CurrentUser.getUserState();
				if( userState instanceof ModifiableUserState )
				{
					ModifiableUserState s = (ModifiableUserState) userState;
					s.setLoggedInUser(old);
					CurrentUser.setUserState(s);
				}

				model.setSetUpForm(true);
				receiptService.setReceipt(SAVE_SUCCESS);
			}
			else
			{
				old.setEmailAddress(CurrentUser.getDetails().getEmailAddress());
				old.setFirstName(CurrentUser.getDetails().getFirstName());
				old.setLastName(CurrentUser.getDetails().getLastName());

				// TODO: Ummm.... no.
				context.preventGET();
			}
		}
		model.setStatus(true);

	}

	private void saveUser(SectionContext context)
	{
		userPrefs.setHideLoginNotice(hideLoginNotice.isChecked(context));
		notificationService.setEmailEnabled(enableEmails.isChecked(context));

		acModeService.setAccessibilityMode(accessibilityMode.isChecked(context));

		String languageCode = languageList.getSelectedValueAsString(context);
		if( languageCode != null )
		{
			Locale locale = createLangMap().get(languageCode);
			userPrefs.setLocale(locale);
			menuService.clearCachedData();
		}

		String timeZoneId = timeZones.getSelectedValueAsString(context);
		userPrefs.setTimeZone(timeZoneId);

		String dateformat = dateFormats.getSelectedValueAsString(context);
		userPrefs.setDateFormat(dateformat);

		sessionService.removeAttribute(WebConstants.KEY_TIMEZONE);
		sessionService.removeAttribute(WebConstants.KEY_LOCALE);

		saveNotifications(context);
	}

	private Set<String> saveNotifications(SectionInfo info)
	{
		Set<String> watches = itemDefs.getSelectedValuesAsStrings(info);
		workflowPrefs.setWatchedCollections(watches);
		return watches;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "ed";
	}

	@Override
	public Class<EditUserModel> getModelClass()
	{
		return EditUserModel.class;
	}

	public static class CollectionsList extends MultiSelectionList<ItemDefinition>
	{
		public CollectionsList()
		{
			super();
			setDefaultRenderer("checklist"); //$NON-NLS-1$
		}

		@Override
		public void rendererSelected(RenderContext info, SectionRenderable renderer)
		{
			CheckListRenderer clrenderer = (CheckListRenderer) renderer;
			clrenderer.setAsList(true);
		}
	}

	protected class CollectionsListModel extends DynamicHtmlListModel<ItemDefinition>
	{
		public CollectionsListModel()
		{
			setSort(true);
		}

		@Override
		protected Iterable<ItemDefinition> populateModel(SectionInfo info)
		{
			return itemDefService.enumerateSearchable();
		}

		@Override
		protected Option<ItemDefinition> convertToOption(SectionInfo info, ItemDefinition collection)
		{
			return new NameValueOption<ItemDefinition>(new BundleNameValue(collection.getName(), collection.getUuid(),
				bundleCache), collection);
		}
	}

	public static class EditUserModel extends TwoColumnLayout.TwoColumnModel
	{
		@Bookmarked(nodefault = true)
		private boolean setUpForm = true;
		private String defaultLanguage;
		private Boolean status;
		private Map<String, String> errors = new HashMap<String, String>();

		public boolean isSetUpForm()
		{
			return setUpForm;
		}

		public void setSetUpForm(boolean setUpForm)
		{
			this.setUpForm = setUpForm;
		}

		public String getDefaultLanguage()
		{
			return defaultLanguage;
		}

		public void setDefaultLanguage(String defaultLanguage)
		{
			this.defaultLanguage = defaultLanguage;
		}

		public void addError(String key, String error)
		{
			errors.put(key, error);
		}

		public Boolean getStatus()
		{
			return status;
		}

		public void setStatus(Boolean status)
		{
			this.status = status;
		}

		public String getUsername()
		{
			return CurrentUser.getUsername();
		}

		public boolean isInternal()
		{
			return CurrentUser.getUserState().isInternal();
		}

		public void setErrors(Map<String, String> errors)
		{
			this.errors = errors;
		}

		public Map<String, String> getErrors()
		{
			return errors;
		}
	}

	public TextField getFamilyName()
	{
		return familyName;
	}

	public String getUsername()
	{
		return CurrentUser.getUsername();
	}

	public TextField getFirstName()
	{
		return firstName;
	}

	public TextField getEmail()
	{
		return email;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public Button getChangePassButton()
	{
		return changePassButton;
	}

	public SingleSelectionList<NameValue> getLanguageList()
	{
		return languageList;
	}

	public SingleSelectionList<NameValue> getTimeZones()
	{
		return timeZones;
	}

	public Checkbox getHideLoginNotice()
	{
		return hideLoginNotice;
	}

	public Checkbox getAccessibilityMode()
	{
		return accessibilityMode;
	}

	public CollectionsList getItemDefs()
	{
		return itemDefs;
	}

	public Button getCloseButton()
	{
		return closeButton;
	}

	public Checkbox getEnableEmails()
	{
		return enableEmails;
	}

	public SingleSelectionList<NameValue> getDateFormats()
	{
		return dateFormats;
	}
}
