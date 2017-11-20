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

package com.tle.web.login;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.settings.standard.AutoLogin;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.services.user.UserService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.SelectionsTable;
import com.tle.web.sections.equella.component.model.DynamicSelectionsTableModel;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.equella.utils.SelectUserDialog;
import com.tle.web.sections.equella.utils.SelectedUser;
import com.tle.web.sections.equella.utils.UserLinkSection;
import com.tle.web.sections.equella.utils.UserLinkService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author larry
 */
@SuppressWarnings("nls")
public class RootLoginSettingsSection extends OneColumnLayout<RootLoginSettingsSection.LoginSettingsModel>
{
	private static final String WILD_IP_REGEX = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?|\\*)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?|\\*)$";

	@PlugKey("login.title")
	private static Label TITLE_LABEL;
	@PlugKey("login.selectthisuser")
	private static Label OK_LABEL;
	@PlugKey("login.ipaddresslist.remove")
	private static Label DELETE_LABEL;
	@PlugKey("login.settings.save.receipt")
	private static Label SAVE_RECEIPT_LABEL;
	@PlugKey("login.ipaddresslist.empty")
	private static Label EMPTY_LABEL;
	@PlugKey("login.user.invalid")
	private static String INVALID_USER;
	@PlugKey("login.ipaddresslist.invalid")
	private static String INVALID_IP_LIST;

	@Inject
	private LoginSettingsPrivilegeTreeProvider securityProvider;
	@Inject
	private ConfigurationService configService;
	@Inject
	private ReceiptService receiptService;
	@Inject
	private UserService userService;
	@Inject
	private UserLinkService userLinkService;
	private UserLinkSection userLinkSection;

	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory viewFactory;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Component
	private Checkbox enableSSLCheck;
	@Component
	private Checkbox enableViaIpCheck;
	@Component
	private Checkbox disableAutoLoginCheck;
	@Component
	private Checkbox disallowUserEditCheck;
	@Component
	private Checkbox transientDRMCheck;
	@Component
	private Checkbox enableAnonACL;

	@Inject
	@Component
	private SelectUserDialog selectUserDialog;

	@Component
	@Inject
	private AddIpAddressDialog addIpAddressDialog;
	@Component
	private SelectionsTable ipAddressTable;
	@Component
	@PlugKey("ipaddress.dialog.title")
	private Link addIpAddressLink;

	@Component(name = "lnf")
	private TextField loginNoticeField;

	@Component
	@PlugKey("settings.save.button")
	private Button saveButton;

	private JSCallable deleteFunc;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		userLinkSection = userLinkService.register(tree, id);

		selectUserDialog.setAjax(true);
		selectUserDialog.setOkLabel(OK_LABEL);
		selectUserDialog.setMultipleUsers(false);

		JSCallable inplace = ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE);
		selectUserDialog.setOkCallback(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("selectLoginAsUser"), inplace, "selecteduser"));

		addIpAddressLink.setClickHandler(addIpAddressDialog.getOpenFunction());

		addIpAddressDialog.setOkCallback(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("addIpAddressToSectionList"), inplace, "enteredIpAddress"));
		deleteFunc = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("removeIpAddress"),
			"enteredIpAddress");

		// Add the toggleEnabler to the all-controlling enable-via-IP checkbox
		enableViaIpCheck.setClickHandler(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("toggleEnabled"), inplace, "innercontrols"));

		saveButton.setClickHandler(events.getNamedHandler("save"));

		ipAddressTable.setSelectionsModel(new DynamicSelectionsTableModel<String>()
		{
			@Override
			protected List<String> getSourceList(SectionInfo info)
			{
				LoginSettingsModel model = getModel(info);
				return model.getIpAddresses();
			}

			@Override
			protected void transform(SectionInfo info, SelectionsTableSelection selection, String thing,
				List<SectionRenderable> actions, int index)
			{
				selection.setName(new TextLabel(thing));
				actions.add(makeRemoveAction(DELETE_LABEL, new OverrideHandler(deleteFunc, thing)));
			}
		});
		ipAddressTable.setAddAction(addIpAddressLink);
		ipAddressTable.setNothingSelectedText(EMPTY_LABEL);
	}

	@Override
	protected TemplateResult setupTemplate(RenderEventContext info)
	{
		securityProvider.checkAuthorised();

		LoginSettingsModel model = getModel(info);

		if( !model.isLoaded() )
		{
			AutoLogin settings = getAutoLoginSettings();
			boolean enabledViaIp = settings.isEnabledViaIp();
			model.setShowInnerControls(enabledViaIp);
			model.setUserId(settings.getUserid());
			model.setUsername(settings.getUsername());

			enableSSLCheck.setChecked(info, settings.isLoginViaSSL());
			enableViaIpCheck.setChecked(info, enabledViaIp);
			disableAutoLoginCheck.setChecked(info, settings.isNotAutomatic());
			disallowUserEditCheck.setChecked(info, settings.isEditDetailsDisallowed());
			transientDRMCheck.setChecked(info, settings.isTransientDrmAcceptances());
			enableAnonACL.setChecked(info, settings.isEnableIpReferAcl());

			model.clearIpAddresses();
			for( String ipAddr : settings.getAddresses() )
			{
				addIpAddress(info, ipAddr);
			}

			loginNoticeField.setValue(info, settings.getLoginNotice());
			model.setLoaded(true);
		}
		else
		{
			model.setShowInnerControls(enableViaIpCheck.isChecked(info));
		}

		if( !Check.isEmpty(model.getUserId()) )
		{
			model.setUserLink(userLinkSection.createLink(info, model.getUserId()));
		}

		return new GenericTemplateResult(viewFactory.createNamedResult(BODY, "loginsettings.ftl", this));
	}

	/**
	 * @see com.tle.web.sections.equella.layout.OneColumnLayout#addBreadcrumbsAndTitle(com.tle.web.sections.SectionInfo,
	 *      com.tle.web.template.Decorations, com.tle.web.template.Breadcrumbs)
	 */
	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setTitle(TITLE_LABEL);
		crumbs.addToStart(SettingsUtils.getBreadcrumb());
	}

	private AutoLogin getAutoLoginSettings()
	{
		return configService.getProperties(new AutoLogin());
	}

	@EventHandlerMethod
	public void toggleEnabled(SectionInfo info)
	{
		boolean enableIsChecked = enableViaIpCheck.isChecked(info);
		getModel(info).setShowInnerControls(enableIsChecked);
	}

	@EventHandlerMethod
	public void selectLoginAsUser(SectionInfo info, String usersJson) throws Exception
	{
		SelectedUser selectedUser = SelectUserDialog.userFromJsonString(usersJson);
		if( selectedUser != null )
		{
			String userId = selectedUser.getUuid();
			UserBean userBean = userService.getInformationForUser(userId);
			LoginSettingsModel model = getModel(info);
			model.setUserId(userId);
			model.setUsername(userBean.getUsername());
			model.setLoaded(true);
		}
	}

	@EventHandlerMethod
	public void addIpAddressToSectionList(SectionInfo info, String enteredString)
	{
		if( !Check.isEmpty(enteredString) )
		{
			// We should be dealing with invalid content in the dialog
			Pattern wildCardNumericIp = Pattern.compile(WILD_IP_REGEX);
			boolean okWildcardNUmericIP = wildCardNumericIp.matcher(enteredString).matches();
			if( okWildcardNUmericIP )
			{
				addIpAddress(info, enteredString);
				getModel(info).setLoaded(true);
			}
		}
	}

	@EventHandlerMethod
	public void removeIpAddress(SectionInfo info, String key)
	{
		getModel(info).getIpAddresses().remove(key);
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		if( saveLoginSettings(info) )
		{
			receiptService.setReceipt(SAVE_RECEIPT_LABEL);
			getModel(info).setLoaded(false);
		}
		else
		{
			info.preventGET();
		}
	}

	private boolean saveLoginSettings(SectionInfo info)
	{
		AutoLogin settings = getAutoLoginSettings();
		LoginSettingsModel model = getModel(info);

		boolean viaIP = enableViaIpCheck.isChecked(info);
		String userId = model.getUserId();
		String username = model.getUsername();
		List<String> newAddressess = model.getIpAddresses();

		if( viaIP )
		{
			if( Check.isEmpty(userId) || Check.isEmpty(username) )
			{
				model.addError("user", CurrentLocale.get(INVALID_USER));
			}

			if( Check.isEmpty(newAddressess) )
			{
				model.addError("iplist", CurrentLocale.get(INVALID_IP_LIST));
			}
		}

		if( model.getErrors().size() == 0 )
		{
			settings.setEnabledViaIp(viaIP);
			settings.setLoginViaSSL(enableSSLCheck.isChecked(info));
			settings.setUserid(userId);
			settings.setUsername(username);
			settings.setNotAutomatic(disableAutoLoginCheck.isChecked(info));
			settings.setEditDetailsDisallowed(disallowUserEditCheck.isChecked(info));
			settings.setTransientDrmAcceptances(transientDRMCheck.isChecked(info));
			settings.setEnableIpReferAcl(enableAnonACL.isChecked(info));

			List<String> ipAddesses = settings.getAddresses();

			if( !Check.isEmpty(newAddressess) )
			{
				ipAddesses.clear();
				ipAddesses.addAll(newAddressess);
			}
			settings.setLoginNotice(loginNoticeField.getValue(info));
			model.setLoaded(false);
			configService.setProperties(settings);
			userService.refreshSettings();
			return true;
		}

		return false;
	}

	public void addIpAddress(SectionInfo info, String ipAddress)
	{
		getModel(info).addIpAddress(ipAddress);
	}

	public Checkbox getEnableSSLCheck()
	{
		return enableSSLCheck;
	}

	public Checkbox getEnableViaIpCheck()
	{
		return enableViaIpCheck;
	}

	public SelectUserDialog getSelectUserDialog()
	{
		return selectUserDialog;
	}

	public Checkbox getDisableAutoLoginCheck()
	{
		return disableAutoLoginCheck;
	}

	public Checkbox getDisallowUserEditCheck()
	{
		return disallowUserEditCheck;
	}

	public Checkbox getTransientDRMCheck()
	{
		return transientDRMCheck;
	}

	public boolean hasIpAddresses(SectionInfo info)
	{
		List<String> ipAddresses = getModel(info).getIpAddresses();
		return !Check.isEmpty(ipAddresses);
	}

	public TextField getLoginNoticeField()
	{
		return loginNoticeField;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new LoginSettingsModel();
	}

	@Override
	public Class<LoginSettingsModel> getModelClass()
	{
		return LoginSettingsModel.class;
	}

	public SelectionsTable getIpAddressTable()
	{
		return ipAddressTable;
	}

	public static class LoginSettingsModel extends OneColumnLayout.OneColumnLayoutModel
	{
		private boolean showInnerControls;
		@Bookmarked
		private boolean loaded;
		@Bookmarked
		private String userId;
		@Bookmarked
		private String username;
		private HtmlLinkState userLink;
		@Bookmarked
		private List<String> ipAddresses;

		private final Map<String, String> errors = new HashMap<String, String>();

		public boolean isShowInnerControls()
		{
			return showInnerControls;
		}

		public void setShowInnerControls(boolean showInnerControls)
		{
			this.showInnerControls = showInnerControls;
		}

		public boolean isLoaded()
		{
			return loaded;
		}

		public void setLoaded(boolean loaded)
		{
			this.loaded = loaded;
		}

		public String getUserId()
		{
			return userId;
		}

		public void setUserId(String userId)
		{
			this.userId = userId;
		}

		public String getUsername()
		{
			return username;
		}

		public void setUsername(String username)
		{
			this.username = username;
		}

		public HtmlLinkState getUserLink()
		{
			return userLink;
		}

		public void setUserLink(HtmlLinkState userLink)
		{
			this.userLink = userLink;
		}

		public List<String> getIpAddresses()
		{
			return ipAddresses;
		}

		public void setIpAddresses(List<String> ipAddresses)
		{
			this.ipAddresses = ipAddresses;
		}

		public void addIpAddress(String ipAddress)
		{
			if( ipAddresses == null )
			{
				ipAddresses = new ArrayList<String>();
			}
			ipAddresses.add(ipAddress);
		}

		public void clearIpAddresses()
		{
			if( ipAddresses != null )
			{
				ipAddresses.clear();
			}
		}

		public Map<String, String> getErrors()
		{
			return errors;
		}

		public void addError(String key, String value)
		{
			this.errors.put(key, value);
		}
	}

	public Checkbox getEnableAnonACL()
	{
		return enableAnonACL;
	}
}
