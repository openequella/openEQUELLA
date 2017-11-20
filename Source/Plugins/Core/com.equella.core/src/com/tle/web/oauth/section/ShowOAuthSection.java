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

package com.tle.web.oauth.section;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.dytech.edge.exceptions.InUseException;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.oauth.beans.OAuthToken;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.oauth.OAuthConstants;
import com.tle.core.oauth.service.OAuthService;
import com.tle.core.security.TLEAclManager;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.SelectionsTable;
import com.tle.web.sections.equella.component.model.DynamicSelectionsTableModel;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.render.DateRendererFactory;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.render.WrappedLabel;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
@Bind
public class ShowOAuthSection extends AbstractPrototypeSection<ShowOAuthSection.ShowOAuthSectionModel>
	implements
		HtmlRenderer
{
	private static final String DIV_TOKENS = "tokensDiv";

	private static final String DIV_CLIENTS = "clientsDiv";

	@PlugKey("oauth.page.title")
	private static Label LABEL_PAGE_TITLE;

	@PlugKey("oauth.clients.link.add")
	private static Label LABEL_CLIENTS_LINK_ADD;
	@PlugKey("oauth.clients.none")
	private static Label LABEL_CLIENTS_NONE;
	@PlugKey("oauth.clients.confirm.delete")
	private static Label LABEL_CLIENTS_DELETE_CONFIRM;
	@PlugKey("oauth.clients.link.edit")
	private static Label LABEL_CLIENTS_LINK_EDIT;
	@PlugKey("oauth.clients.link.delete")
	private static Label LABEL_CLIENTS_LINK_DELETE;
	@PlugKey("oauth.clients.column.clientname")
	private static Label LABEL_CLIENTS_CLIENTNAME;
	@PlugKey("oauth.clients.column.clientid")
	private static Label LABEL_CLIENTS_CLIENTID;
	@PlugKey("oauth.clients.column.redirecturl")
	private static Label LABEL_CLIENTS_REDIRECTURL;

	@PlugKey("oauth.tokens.none")
	private static Label LABEL_TOKENS_NONE;
	@PlugKey("oauth.tokens.confirm.delete")
	private static Label LABEL_TOKENS_DELETE_CONFIRM;
	@PlugKey("oauth.tokens.link.delete")
	private static Label LABEL_TOKENS_LINK_DELETE;
	@PlugKey("oauth.tokens.column.token")
	private static Label LABEL_TOKENS_TOKEN;
	@PlugKey("oauth.tokens.column.username")
	private static Label LABEL_TOKENS_USERNAME;
	@PlugKey("oauth.tokens.column.clientid")
	private static Label LABEL_TOKENS_CLIENTID;
	@PlugKey("oauth.tokens.column.created")
	private static Label LABEL_TOKENS_CREATED;
	@PlugKey("oauth.clients.refuse.delete")
	private static String KEY_OAUTH_CLIENT_IN_USE;

	@Inject
	private OAuthService oauthService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private TLEAclManager aclService;
	@Inject
	private DateRendererFactory dateRendererFactory;

	@TreeLookup
	private OAuthClientEditorSection clientEditorSection;

	@Component(name = "c")
	private SelectionsTable clientTable;
	@Component(name = "ac")
	private Link addClientLink;
	@Component(name = "t")
	private SelectionsTable tokenTable;

	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;
	@ViewFactory
	private FreemarkerFactory viewFactory;

	private JSCallable editClientFunction;
	private JSCallable deleteClientFunction;
	private JSCallable deleteTokenFunction;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final ShowOAuthSectionModel model = getModel(context);
		model.setPageTitle(LABEL_PAGE_TITLE);

		final boolean canAdd = !aclService.filterNonGrantedPrivileges(OAuthConstants.PRIV_CREATE_OAUTH_CLIENT)
			.isEmpty();
		final boolean canEdit = !aclService.filterNonGrantedPrivileges(OAuthConstants.PRIV_EDIT_OAUTH_CLIENT).isEmpty();
		addClientLink.setDisplayed(context, canAdd);
		model.setShowClients(canAdd || canEdit);

		model.setShowTokens(!aclService.filterNonGrantedPrivileges(OAuthConstants.PRIV_ADMINISTER_OAUTH_TOKENS)
			.isEmpty());

		final GenericTemplateResult templateResult = new GenericTemplateResult();
		templateResult.addNamedResult(OneColumnLayout.BODY, viewFactory.createResult("oauth.ftl", this));
		return templateResult;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		addClientLink.setLabel(LABEL_CLIENTS_LINK_ADD);
		addClientLink.setClickHandler(events.getNamedHandler("newClient"));
		deleteClientFunction = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("deleteClient"),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), DIV_CLIENTS, DIV_TOKENS);
		editClientFunction = events.getSubmitValuesFunction("editClient");

		clientTable
			.setColumnHeadings(LABEL_CLIENTS_CLIENTNAME, LABEL_CLIENTS_CLIENTID, LABEL_CLIENTS_REDIRECTURL, null);
		clientTable.setColumnSorts(Sort.PRIMARY_ASC, Sort.SORTABLE_ASC);
		clientTable.setSelectionsModel(new OAuthClientsModel());
		clientTable.setNothingSelectedText(LABEL_CLIENTS_NONE);
		clientTable.setAddAction(addClientLink);

		deleteTokenFunction = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("deleteToken"),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), DIV_TOKENS);

		tokenTable.setColumnHeadings(LABEL_TOKENS_TOKEN, LABEL_TOKENS_USERNAME, LABEL_TOKENS_CLIENTID,
			LABEL_TOKENS_CREATED, null);
		tokenTable.setColumnSorts(Sort.NONE, Sort.SORTABLE_ASC, Sort.SORTABLE_ASC, Sort.PRIMARY_ASC);
		tokenTable.setSelectionsModel(new OAuthTokensModel());
		tokenTable.setNothingSelectedText(LABEL_TOKENS_NONE);
	}

	@EventHandlerMethod
	public void editClient(SectionInfo info, String uuid)
	{
		clientEditorSection.startEdit(info, uuid);
	}

	@EventHandlerMethod
	public void newClient(SectionInfo info)
	{
		clientEditorSection.createNew(info);
	}

	@EventHandlerMethod
	public void deleteClient(SectionInfo info, String uuid)
	{
		try
		{
			oauthService.delete(oauthService.getByUuid(uuid), true);
		}
		catch( InUseException iue ) // NOSONAR
		{
			// InUseException captures the properties string related to the
			// class name of the entity which uses this OauthClient
			String usingClassName = iue.getMessage();
			getModel(info).setInUseError(new KeyLabel(KEY_OAUTH_CLIENT_IN_USE, usingClassName));
		}
	}

	@EventHandlerMethod
	public void deleteToken(SectionInfo info, long id)
	{
		oauthService.deleteToken(id);
	}

	@Override
	public Class<ShowOAuthSectionModel> getModelClass()
	{
		return ShowOAuthSectionModel.class;
	}

	public SelectionsTable getClientTable()
	{
		return clientTable;
	}

	public SelectionsTable getTokenTable()
	{
		return tokenTable;
	}

	private class OAuthClientsModel extends DynamicSelectionsTableModel<OAuthClient>
	{
		@Override
		protected List<OAuthClient> getSourceList(SectionInfo info)
		{
			return oauthService.enumerateEditable();
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, OAuthClient client,
			List<SectionRenderable> actions, int index)
		{
			selection.setViewAction(new LabelRenderer(new BundleLabel(client.getName(), bundleCache)));

			// client id, redirect url
			final String clientid = client.getClientId();
			selection.addColumn(clientid).setSortData(clientid);

			final Label redirectUrl = new TextLabel(client.getRedirectUrl());
			selection.addColumn(new WrappedLabel(redirectUrl, 50, true));

			final String uuid = client.getUuid();
			actions.add(makeAction(LABEL_CLIENTS_LINK_EDIT, new OverrideHandler(editClientFunction, uuid)));
			if( oauthService.canDelete(client) )
			{
				actions.add(makeAction(LABEL_CLIENTS_LINK_DELETE, new OverrideHandler(deleteClientFunction, uuid)
					.addValidator(new Confirm(LABEL_CLIENTS_DELETE_CONFIRM))));
			}
		}
	}

	private class OAuthTokensModel extends DynamicSelectionsTableModel<OAuthToken>
	{
		@Override
		protected List<OAuthToken> getSourceList(SectionInfo info)
		{
			return oauthService.listAllTokens();
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, OAuthToken token,
			List<SectionRenderable> actions, int index)
		{
			selection.setViewAction(new LabelRenderer(new TextLabel(token.getToken())));

			final String username = token.getUsername();
			final String clientId = token.getClient().getClientId();
			final Date created = token.getCreated();

			selection.addColumn(username).setSortData(username);
			selection.addColumn(clientId).setSortData(clientId);
			selection.addColumn(dateRendererFactory.createDateRenderer(created)).setSortData(created);

			if( oauthService.canAdministerTokens() )
			{
				actions.add(makeRemoveAction(LABEL_TOKENS_LINK_DELETE,
					new OverrideHandler(deleteTokenFunction, token.getId()).addValidator(new Confirm(
						LABEL_TOKENS_DELETE_CONFIRM))));
			}
		}
	}

	public static class ShowOAuthSectionModel extends OneColumnLayout.OneColumnLayoutModel
	{
		private Label pageTitle;
		private boolean showClients;
		private boolean showTokens;
		private Label inUseError;

		public Label getPageTitle()
		{
			return pageTitle;
		}

		public void setPageTitle(Label pageTitle)
		{
			this.pageTitle = pageTitle;
		}

		public boolean isShowClients()
		{
			return showClients;
		}

		public void setShowClients(boolean showClients)
		{
			this.showClients = showClients;
		}

		public boolean isShowTokens()
		{
			return showTokens;
		}

		public void setShowTokens(boolean showTokens)
		{
			this.showTokens = showTokens;
		}

		public Label getInUseError()
		{
			return inUseError;
		}

		public void setInUseError(Label inUseError)
		{
			this.inUseError = inUseError;
		}
	}
}
