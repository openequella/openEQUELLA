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

package com.tle.web.recipientselector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.Pair;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.recipientselector.ExpressionFormatter;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.common.security.expressions.PostfixExpressionParser;
import com.tle.common.security.expressions.PostfixExpressionParser.BooleanOp;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserService;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.recipientselector.SelectionDisplayTreeNode.ExpressionSelection;
import com.tle.web.recipientselector.SelectionDisplayTreeNode.SelectGroupResultOption;
import com.tle.web.recipientselector.SelectionDisplayTreeNode.SelectRoleResultOption;
import com.tle.web.recipientselector.SelectionDisplayTreeNode.SelectUserResultOption;
import com.tle.web.recipientselector.tree.SelectionExpressionTreeNode;
import com.tle.web.recipientselector.tree.SelectionGroupingTreeNode;
import com.tle.web.recipientselector.tree.SelectionGroupingTreeNode.Grouping;
import com.tle.web.recipientselector.tree.SelectionTreeNode;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.GroupSearchModel;
import com.tle.web.sections.equella.utils.RoleSearchModel;
import com.tle.web.sections.equella.utils.SelectGroupSection;
import com.tle.web.sections.equella.utils.SelectedGroup;
import com.tle.web.sections.equella.utils.UserLinkSection;
import com.tle.web.sections.equella.utils.UserLinkService;
import com.tle.web.sections.equella.utils.UserSearchModel;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.events.js.SubmitValuesFunction;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.jquery.libraries.effects.JQueryUIEffects;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.sections.standard.IpAddressInput;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.MappedLists;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.sections.standard.model.SimpleOption;

/**
 * @author Peng
 */
@SuppressWarnings("nls")
@Bind
public class ExpressionSelectorSection
	extends
		AbstractPrototypeSection<ExpressionSelectorSection.ExpressionSelectorModel>
	implements HtmlRenderer
{
	protected static final int SEARCH_LIMIT = 50;
	public static final String RESULTS_DIVID = "search-result-list";
	public static final String GROUP_FILTER_DIV = "groupfilter";
	public static final String SELECT_AREA_DIV = "select-area";
	public static final String SELECTED_AREA = "right-cloumn";
	public static final String OTHER_TYPE_DIV = "other-types";
	public static final String TREE_KEY = "expression-tree-key";
	public static final String DEFAULT_GROUPING = "grouping0";

	private static final PluginResourceHelper RESOURCES = ResourcesService
		.getResourceHelper(ExpressionSelectorSection.class);

	private static final IncludeFile INCLUDE = new IncludeFile(RESOURCES.url("scripts/select.js"));
	private static final JSCallable SETUP_CLICK = new ExternallyDefinedFunction("setupClick", INCLUDE);
	private static final JSCallable SELECT_RESULT = new ExternallyDefinedFunction("selectResult",
		JQueryUIEffects.TRANSFER, INCLUDE);
	private static final JSCallable SELECT_RESULTS = new ExternallyDefinedFunction("selectResults",
		JQueryUIEffects.TRANSFER, INCLUDE);
	private static final JSCallable SELECT_OTHER = new ExternallyDefinedFunction("selectOther",
		JQueryUIEffects.TRANSFER, INCLUDE);

	@Inject
	private UserSessionService userSessionService;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@AjaxFactory
	protected AjaxGenerator ajax;
	@EventFactory
	private EventGenerator events;

	@Component
	private MappedLists groupingSelections;

	@Component(name = "q", stateful = false)
	protected TextField query;
	@Component(name = "s")
	@PlugKey("selector.searchbutton")
	private Button search;

	@Component(name = "as")
	@PlugKey("selector.button.addselected")
	private Button addSelected;

	@Component(name = "ag")
	@PlugKey("selector.button.addgrouping")
	private Button addGroupingButton;

	@PlugKey("selector.validation.enterquery")
	private static Label ENTER_QUERY_LABEL;
	@PlugKey("selector.type.user")
	private static String STRING_USER;
	@PlugKey("selector.type.group")
	private static String STRING_GROUP;
	@PlugKey("selector.type.role")
	private static String STRING_ROLE;
	@PlugKey("selector.validation.invalid")
	private static String KEY_INVALID_QUERY;
	@PlugKey("selector.groupselect.title")
	private static Label GROUP_TITLE_LABEL;

	@PlugKey("selector.specialusersfinder.everyone")
	private static String STRING_EVERYONE;
	@PlugKey("selector.specialusersfinder.owner")
	private static String STRING_OWNER;
	@PlugKey("selector.specialusersfinder.loggedin")
	private static String STRING_LOGGEDIN;
	@PlugKey("selector.specialusersfinder.guest")
	private static String STRING_GUEST;
	@PlugKey("selector.specialusersfinder.tokenId")
	private static String STRING_SHARED_SECRET_ID;
	@PlugKey("selector.ipaddressfinder.addip")
	private static String STRING_IP_ADDRESS;
	@PlugKey("selector.ipaddressfinder.addreferrer")
	private static String STRING_REFERRER;

	@PlugKey("selector.ipaddressfinder.onlymatch")
	private static String STRING_REFFERRER_EXACT;
	@PlugKey("selector.ipaddressfinder.match")
	private static String STRING_REFERRER_PARTIAL;

	@Component(name = "type")
	private SingleSelectionList<NameValue> types;
	@Component(name = "otherType")
	private SingleSelectionList<NameValue> otherTypes;

	@Component(name = "tokenid")
	private SingleSelectionList<String> tokenIdList;
	@Component(name = "ip")
	private IpAddressInput ipAddress;
	@Component(name = "referrer")
	private TextField httpReferrer;

	@Component(name = "aob")
	@PlugKey("selector.button.addothers")
	private Button addOtherButton;

	@Component(name = "referreroptions")
	private SingleSelectionList<NameValue> referrerOptions;

	@PlugKey("selector.link.addgroup")
	@Component(name = "agl")
	private Link addGroupLink;
	@PlugKey("selector.link.editgroup")
	@Component(name = "egl")
	private Link editGroupLink;
	@PlugKey("selector.link.cleargroup")
	@Component(name = "cgl")
	private Link clearGroupLink;

	@PlugKey("selector.link.selectall")
	@Component(name = "sau")
	private Link selectAllLink;

	@PlugKey("selector.link.selectnone")
	@Component(name = "snu")
	private Link selectNoneLink;
	@Component(name = "sel")
	private TextField selected;
	private Set<String> groupFilter;
	private List<Label> groupFilterNames; // readonly

	@Inject
	private ComponentFactory componentFactory;
	@Inject
	private UserService userService;
	@Inject
	private UserLinkService userLinkService;
	@Inject
	private SelectGroupSection selectGroupSection;

	@Component(name = "ulist")
	private MultiSelectionList<UserBean> userList;
	@Component(name = "glist")
	private MultiSelectionList<GroupBean> groupList;
	@Component(name = "rlist")
	private MultiSelectionList<RoleBean> roleList;

	private ExpressionSelectorDialog dialog;
	private UserLinkSection userLinkSection;

	private UpdateDomFunction updateFunction;
	private UpdateDomFunction updateGroupingFunction;
	private UpdateDomFunction deleteGroupingFunction;
	private UpdateDomFunction deleteExpressionFunction;

	private SubmitValuesFunction openGroupSelectorFunc;
	private SubmitValuesFunction closeGroupSelectorFunc;

	private FunctionCallStatement setAllUserHandler;
	private FunctionCallStatement setNoneUserHandler;

	private FunctionCallStatement setAllGroupHandler;
	private FunctionCallStatement setNoneGroupHandler;

	private FunctionCallStatement setAllRoleHandler;
	private FunctionCallStatement setNoneRoleHandler;

	public enum TypeValue
	{
		USER, GROUP, ROLE
	}

	public enum OtherTypeValue
	{
		EVERYONE, OWNER, LOGGEDIN, GUEST, SHARE_SECRET, IP_ADDRESS, HTTP_REFERRER
	}

	public static OtherTypeValue getOtherTypeValue(String type)
	{
		for( OtherTypeValue value : OtherTypeValue.values() )
		{
			if( type.equals(value.toString()) )
			{
				return value;
			}
		}
		throw new IllegalArgumentException("Unparseable Type: " + type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		JSCallable replaceInPlace = ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE);

		SimpleHtmlListModel<NameValue> typeOptions = new SimpleHtmlListModel<NameValue>(
			new NameValue(CurrentLocale.get(STRING_USER), TypeValue.USER.toString()),
			new NameValue(CurrentLocale.get(STRING_GROUP), TypeValue.GROUP.toString()),
			new NameValue(CurrentLocale.get(STRING_ROLE), TypeValue.ROLE.toString()));
		types.setListModel(typeOptions);
		types.setAlwaysSelect(true);

		updateFunction = ajax.getAjaxUpdateDomFunction(tree, this, null,
			ajax.getEffectFunction(EffectType.REPLACE_WITH_LOADING), GROUP_FILTER_DIV, RESULTS_DIVID, SELECT_AREA_DIV);
		types.addChangeEventHandler(new OverrideHandler(updateFunction));

		SimpleHtmlListModel<NameValue> otherOptions = new SimpleHtmlListModel<NameValue>(
			new NameValue(CurrentLocale.get(STRING_EVERYONE), OtherTypeValue.EVERYONE.toString()),
			new NameValue(CurrentLocale.get(STRING_OWNER), OtherTypeValue.OWNER.toString()),
			new NameValue(CurrentLocale.get(STRING_LOGGEDIN), OtherTypeValue.LOGGEDIN.toString()),
			new NameValue(CurrentLocale.get(STRING_GUEST), OtherTypeValue.GUEST.toString()),
			new NameValue(CurrentLocale.get(STRING_SHARED_SECRET_ID), OtherTypeValue.SHARE_SECRET.toString()),
			new NameValue(CurrentLocale.get(STRING_IP_ADDRESS), OtherTypeValue.IP_ADDRESS.toString()),
			new NameValue(CurrentLocale.get(STRING_REFERRER), OtherTypeValue.HTTP_REFERRER.toString()));
		otherTypes.setListModel(otherOptions);
		otherTypes.setAlwaysSelect(true);

		otherTypes.addChangeEventHandler(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("setupOtherTypeDisabled"), replaceInPlace, OTHER_TYPE_DIV));

		SimpleHtmlListModel<NameValue> referrers = new SimpleHtmlListModel<NameValue>(
			new NameValue(CurrentLocale.get(STRING_REFFERRER_EXACT), "EXACT"),
			new NameValue(CurrentLocale.get(STRING_REFERRER_PARTIAL), "PARTIAL"));
		referrerOptions.setListModel(referrers);
		referrerOptions.setAlwaysSelect(true);

		openGroupSelectorFunc = events.getSubmitValuesFunction("openGroupSelector");
		addGroupLink.addClickStatements(new OverrideHandler(openGroupSelectorFunc));
		editGroupLink.addClickStatements(new OverrideHandler(openGroupSelectorFunc));

		clearGroupLink.setClickHandler(events.getNamedHandler("clearSelectedGroups"));

		closeGroupSelectorFunc = events.getSubmitValuesFunction("closeGroupSelector");

		userLinkSection = userLinkService.register(tree, id);
		userList = (MultiSelectionList<UserBean>) componentFactory.createMultiSelectionList(id, "ul", tree);
		setAllUserHandler = new FunctionCallStatement(userList.createSetAllFunction(), true);
		setNoneUserHandler = new FunctionCallStatement(userList.createSetAllFunction(), false);

		groupList = (MultiSelectionList<GroupBean>) componentFactory.createMultiSelectionList(id, "ul", tree);
		setAllGroupHandler = new FunctionCallStatement(groupList.createSetAllFunction(), true);
		setNoneGroupHandler = new FunctionCallStatement(groupList.createSetAllFunction(), false);

		roleList = (MultiSelectionList<RoleBean>) componentFactory.createMultiSelectionList(id, "ul", tree);
		setAllRoleHandler = new FunctionCallStatement(roleList.createSetAllFunction(), true);
		setNoneRoleHandler = new FunctionCallStatement(roleList.createSetAllFunction(), false);

		selectGroupSection.setMultipleGroups(true);
		tree.registerInnerSection(selectGroupSection, id);

		addGroupingButton.setClickHandler(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("addGrouping"), replaceInPlace, SELECTED_AREA));

		addSelected.setClickHandler(new OverrideHandler(SELECT_RESULTS, ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("addSelectedExpression"), replaceInPlace, SELECTED_AREA)));

		UpdateDomFunction ajaxUpdateDomFunction = ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("addOtherSelected"), replaceInPlace, SELECTED_AREA);

		OverrideHandler handler = new OverrideHandler(SELECT_OTHER, ajaxUpdateDomFunction);
		addOtherButton.setClickHandler(handler);

		updateGroupingFunction = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("updateGrouping"),
			replaceInPlace, SELECTED_AREA);

		deleteGroupingFunction = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("deleteGrouping"),
			replaceInPlace, SELECTED_AREA);
		deleteExpressionFunction = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("deleteExpression"),
			replaceInPlace, SELECTED_AREA);

		tokenIdList.setStyle("width: 250px");
	}

	private void getSharedSecretId(SectionInfo info)
	{
		SimpleHtmlListModel<String> list = new SimpleHtmlListModel<String>();
		List<String> tokenSecretIds = userService.getTokenSecretIds();
		list.addAll(tokenSecretIds);
		tokenIdList.setListModel(list);

		if( Check.isEmpty(tokenSecretIds) )
		{
			tokenIdList.setDisabled(info, true);
		}
		else
		{
			tokenIdList.setDisabled(info, false);
		}
	}

	@EventHandlerMethod
	public void setupOtherTypeDisabled(SectionInfo info)
	{
		tokenIdList.setDisabled(info, true);
		ipAddress.setDisabled(info, true);
		referrerOptions.setDisabled(info, true);
		httpReferrer.setDisabled(info, true);

		String selectedType = otherTypes.getSelectedValueAsString(info);
		if( selectedType.equals(OtherTypeValue.SHARE_SECRET.toString()) )
		{
			tokenIdList.setDisabled(info, false);
		}
		else if( selectedType.equals(OtherTypeValue.IP_ADDRESS.toString()) )
		{
			ipAddress.setDisabled(info, false);
		}
		else if( selectedType.equals(OtherTypeValue.HTTP_REFERRER.toString()) )
		{
			referrerOptions.setDisabled(info, false);
			httpReferrer.setDisabled(info, false);
		}
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		ExpressionSelectorModel model = getModel(context);

		String sel = selected.getValue(context);
		if( sel == null )
		{
			selected.setValue(context, DEFAULT_GROUPING);
		}

		String selectedTypeValue = types.getSelectedValueAsString(context);
		if( selectedTypeValue != null )
		{
			model.setTypeOption(selectedTypeValue);
		}
		else
		{
			model.setTypeOption(TypeValue.USER.toString());
		}

		if( model.getTypeOption().equals(TypeValue.USER.toString()) )
		{
			selectAllLink.setEventHandler(context, JSHandler.EVENT_CLICK, new OverrideHandler(setAllUserHandler));
			selectNoneLink.setEventHandler(context, JSHandler.EVENT_CLICK, new OverrideHandler(setNoneUserHandler));

			final UserSearchWithExclusionsListModel userListModel = new UserSearchWithExclusionsListModel(query,
				userService, groupFilter, SEARCH_LIMIT);
			userList.setListModel(userListModel);
		}
		else if( model.getTypeOption().equals(TypeValue.GROUP.toString()) )
		{
			selectAllLink.setEventHandler(context, JSHandler.EVENT_CLICK, new OverrideHandler(setAllGroupHandler));
			selectNoneLink.setEventHandler(context, JSHandler.EVENT_CLICK, new OverrideHandler(setNoneGroupHandler));

			final GroupSearchListModel groupListModel = new GroupSearchListModel(query, userService, groupFilter,
				SEARCH_LIMIT);
			groupList.setListModel(groupListModel);
		}
		else if( model.getTypeOption().equals(TypeValue.ROLE.toString()) )
		{
			selectAllLink.setEventHandler(context, JSHandler.EVENT_CLICK, new OverrideHandler(setAllRoleHandler));
			selectNoneLink.setEventHandler(context, JSHandler.EVENT_CLICK, new OverrideHandler(setNoneRoleHandler));

			final RoleSearchListModel roleListModel = new RoleSearchListModel(query, userService, SEARCH_LIMIT);
			roleList.setListModel(roleListModel);
		}

		if( !model.getTypeOption().equals(TypeValue.ROLE.toString()) )
		{
			model.setGroupFilterVisible(true);
		}
		else
		{
			model.setGroupFilterVisible(false);
		}

		if( !Check.isEmpty(groupFilter) && !Check.isEmpty(groupFilterNames) )
		{
			model.setHasGroupSelected(true);
		}
		else
		{
			model.setHasGroupSelected(false);
		}

		model.setShowSelectArea(userList.getListModel().getOptions(context).size() > 0
			|| groupList.getListModel().getOptions(context).size() > 0
			|| roleList.getListModel().getOptions(context).size() > 0);

		if( getModel(context).isShowGroupSelector() )
		{
			if( dialog != null )
			{
				final Button okButton = dialog.getOk();
				okButton.setLabel(context, GROUP_TITLE_LABEL);
				okButton.setClickHandler(context, new OverrideHandler(openGroupSelectorFunc));

				final Button cancelGroupSelectionButton = dialog.getCancelGroupSelectionButton();
				cancelGroupSelectionButton.setDisplayed(context, true);
				cancelGroupSelectionButton.setClickHandler(context, new OverrideHandler(closeGroupSelectorFunc));
			}
			return renderSection(context, selectGroupSection);
		}

		doValidationMessages(context);
		setupUserSessionValues(context);

		getSharedSecretId(context);
		setupOtherTypeDisabled(context);

		return viewFactory.createResult("selector.ftl", this);
	}

	protected void doValidationMessages(SectionInfo info)
	{
		final ExpressionSelectorModel model = getModel(info);
		final String theQuery = query.getValue(info);
		if( !Check.isEmpty(theQuery) && !validQuery(theQuery) )
		{
			model.setInvalidMessageKey(KEY_INVALID_QUERY);
		}
		else
		{
			model.setInvalidMessageKey(null);
		}
		model.setHasNoResults(isNoResults(info));
	}

	protected boolean isNoResults(SectionInfo info)
	{
		String type = types.getSelectedValueAsString(info);
		if( type.equals(TypeValue.USER.toString()) )
		{
			return (!Check.isEmpty(query.getValue(info)) && userList.getListModel().getOptions(info).size() == 0);
		}
		else if( type.equals(TypeValue.GROUP.toString()) )
		{
			return (!Check.isEmpty(query.getValue(info)) && groupList.getListModel().getOptions(info).size() == 0);
		}
		else if( type.equals(TypeValue.ROLE.toString()) )
		{
			return (!Check.isEmpty(query.getValue(info)) && roleList.getListModel().getOptions(info).size() == 0);
		}

		return false;
	}

	protected boolean validQuery(String query)
	{
		String q = Strings.nullToEmpty(query);
		for( int i = 0; i < q.length(); i++ )
		{
			if( Character.isLetterOrDigit(q.codePointAt(i)) )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);

		OverrideHandler handler = new OverrideHandler(updateFunction);
		handler.addValidator(Js.validator(Js.notEquals(query.createGetExpression(), Js.str("")))
			.setFailureStatements(Js.alert_s(ENTER_QUERY_LABEL)));
		search.setClickHandler(handler);
	}

	public void clearUserSession(SectionInfo info)
	{
		userSessionService.removeAttribute(TREE_KEY);
	}

	@EventHandlerMethod
	public void clearSelectedGroups(SectionInfo info)
	{
		groupFilter.clear();
		groupFilterNames.clear();
		getModel(info).setHasGroupSelected(false);
		selectGroupSection.getQuery().setValue(info, "");
	}

	@EventHandlerMethod
	public void addSelectedExpression(SectionInfo info)
	{
		SelectionGroupingTreeNode treeRoot = (SelectionGroupingTreeNode) userSessionService.getAttribute(TREE_KEY);
		String type = types.getSelectedValueAsString(info);
		SelectionGroupingTreeNode targetNode = getTargetNodeToAddExpression(info, treeRoot);

		if( type.equals(TypeValue.USER.toString()) )
		{
			for( UserBean user : userList.getSelectedValues(info) )
			{
				addExpressionToTree(Recipient.USER, user.getUniqueID(), treeRoot, targetNode);
			}
		}
		else if( type.equals(TypeValue.GROUP.toString()) )
		{
			for( GroupBean group : groupList.getSelectedValues(info) )
			{
				addExpressionToTree(Recipient.GROUP, group.getUniqueID(), treeRoot, targetNode);
			}
		}
		else if( type.equals(TypeValue.ROLE.toString()) )
		{
			for( RoleBean role : roleList.getSelectedValues(info) )
			{
				addExpressionToTree(Recipient.ROLE, role.getUniqueID(), treeRoot, targetNode);
			}
		}
	}

	@EventHandlerMethod
	public void addSingleExpression(SectionInfo info, String uuid)
	{
		SelectionGroupingTreeNode treeRoot = (SelectionGroupingTreeNode) userSessionService.getAttribute(TREE_KEY);
		String type = types.getSelectedValueAsString(info);
		SelectionGroupingTreeNode targetNode = getTargetNodeToAddExpression(info, treeRoot);

		if( type.equals(TypeValue.USER.toString()) )
		{
			addExpressionToTree(Recipient.USER, uuid, treeRoot, targetNode);
		}
		else if( type.equals(TypeValue.GROUP.toString()) )
		{
			addExpressionToTree(Recipient.GROUP, uuid, treeRoot, targetNode);
		}
		else if( type.equals(TypeValue.ROLE.toString()) )
		{
			addExpressionToTree(Recipient.ROLE, uuid, treeRoot, targetNode);
		}
	}

	private void addExpressionToTree(Recipient type, String uuid, SelectionGroupingTreeNode groupTree,
		SelectionGroupingTreeNode targetNode)
	{
		String recipient = SecurityConstants.getRecipient(type, uuid);
		if( !recipientExistInTree(recipient, groupTree) )
		{
			SelectionExpressionTreeNode expressionNode = new SelectionExpressionTreeNode(recipient);
			targetNode.getSelection().add(expressionNode);
		}
	}

	@EventHandlerMethod
	public void addOtherSelected(SectionInfo info)
	{
		SelectionGroupingTreeNode treeRoot = (SelectionGroupingTreeNode) userSessionService.getAttribute(TREE_KEY);
		String otherTpye = otherTypes.getSelectedValueAsString(info);
		SelectionGroupingTreeNode targetNode = getTargetNodeToAddExpression(info, treeRoot);

		String recipient = null;
		switch( getOtherTypeValue(otherTpye) )
		{
			case EVERYONE:
				recipient = SecurityConstants.getRecipient(Recipient.EVERYONE);
				break;

			case OWNER:
				recipient = SecurityConstants.getRecipient(Recipient.OWNER);
				break;

			case LOGGEDIN:
				recipient = SecurityConstants.getRecipient(Recipient.ROLE, SecurityConstants.LOGGED_IN_USER_ROLE_ID);
				break;

			case GUEST:
				recipient = SecurityConstants.getRecipient(Recipient.ROLE, SecurityConstants.GUEST_USER_ROLE_ID);
				break;

			case SHARE_SECRET:
				String id = tokenIdList.getSelectedValueAsString(info);
				if( id != null )
				{
					recipient = SecurityConstants.getRecipient(Recipient.TOKEN_SECRET_ID, id);
				}
				break;

			case IP_ADDRESS:
				String ip = ipAddress.getValue(info);
				if( !Check.isEmpty(ip) )
				{
					recipient = SecurityConstants.getRecipient(Recipient.IP_ADDRESS, ip);
				}
				break;

			case HTTP_REFERRER:
				String referrer = httpReferrer.getValue(info).trim();
				if( !Check.isEmpty(referrer) )
				{
					if( referrerOptions.getSelectedValueAsString(info).equals("PARTIAL") )
					{
						referrer = "*" + referrer + "*";
					}
					recipient = SecurityConstants.getRecipient(Recipient.HTTP_REFERRER, referrer);
				}
				break;
		}

		if( recipient != null )
		{
			if( !recipientExistInTree(recipient, treeRoot) )
			{
				SelectionExpressionTreeNode expressionNode = new SelectionExpressionTreeNode(recipient);
				targetNode.getSelection().add(expressionNode);
			}
		}

	}

	private SelectionGroupingTreeNode getTargetNodeToAddExpression(SectionInfo info, SelectionGroupingTreeNode treeRoot)
	{
		SelectionGroupingTreeNode targetNode = treeRoot;
		String selectedGroup = selected.getValue(info);

		if( selectedGroup != null )
		{
			SelectionGroupingTreeNode findGroupingNode = findGroupingNode(selectedGroup, treeRoot);
			targetNode = findGroupingNode == null ? treeRoot : findGroupingNode;
		}
		return targetNode;
	}

	private boolean recipientExistInTree(String recipient, SelectionGroupingTreeNode rootNode)
	{
		for( SelectionExpressionTreeNode expression : rootNode.getSelection() )
		{
			if( expression.getExpression().equals(recipient) )
			{
				return true;
			}
		}
		for( SelectionGroupingTreeNode group : rootNode.getChildren() )
		{
			if( recipientExistInTree(recipient, group) )
			{
				return true;
			}
		}
		return false;
	}

	@EventHandlerMethod
	public void openGroupSelector(SectionInfo info)
	{
		ExpressionSelectorModel model = getModel(info);
		if( model.isShowGroupSelector() )
		{
			model.setShowGroupSelector(false);
			selectGroupSection.addGroups(info);
			final List<SelectedGroup> selections = selectGroupSection.getSelections(info);

			groupFilter = new HashSet<String>();
			groupFilterNames = new ArrayList<Label>();

			if( selections != null )
			{
				for( SelectedGroup selectedGroup : selections )
				{
					groupFilter.add(selectedGroup.getUuid());
					groupFilterNames.add(new TextLabel(selectedGroup.getDisplayName()));
				}
				selectGroupSection.getSelections(info).clear();
			}
		}
		else
		{
			model.setShowGroupSelector(true);
		}
	}

	@EventHandlerMethod
	public void closeGroupSelector(SectionInfo info)
	{
		getModel(info).setShowGroupSelector(false);
	}

	public class UserSearchWithExclusionsListModel extends UserSearchModel
	{
		public UserSearchWithExclusionsListModel(TextField query, UserService userService, Set<String> groupFilter,
			int limit)
		{
			super(query, userService, groupFilter, limit);
		}

		@Override
		protected Iterable<UserBean> populateModel(SectionInfo info)
		{
			final Iterable<UserBean> users = super.populateModel(info);
			final Set<String> userExclusions = getModel(info).getUserExclusions();
			if( userExclusions != null && !userExclusions.isEmpty() )
			{
				final Iterator<UserBean> iterator = users.iterator();
				while( iterator.hasNext() )
				{
					final UserBean user = iterator.next();
					if( userExclusions.contains(user.getUniqueID()) )
					{
						iterator.remove();
					}
				}
			}
			return users;
		}

		@Override
		protected Option<UserBean> convertToOption(SectionInfo info, UserBean user)
		{
			HtmlLinkState state = new HtmlLinkState();
			state.addClass("add");
			state.setClickHandler(new OverrideHandler(SELECT_RESULT, Jq.$(state),
				events.getSubmitValuesFunction("addSingleExpression"), user.getUniqueID()));
			return new SelectUserResultOption(user, userLinkSection.createLinkFromBean(info, user), state);
		}
	}

	public class GroupSearchListModel extends GroupSearchModel
	{
		public GroupSearchListModel(TextField query, UserService userService, Set<String> groupFilter, int limit)
		{
			super(query, userService, groupFilter, limit);
		}

		@Override
		protected Option<GroupBean> convertToOption(SectionInfo info, GroupBean group)
		{
			HtmlLinkState state = new HtmlLinkState();
			state.addClass("add");
			state.setClickHandler(new OverrideHandler(SELECT_RESULT, Jq.$(state),
				events.getSubmitValuesFunction("addSingleExpression"), group.getUniqueID()));
			return new SelectGroupResultOption(group, state);
		}
	}

	public class RoleSearchListModel extends RoleSearchModel
	{

		public RoleSearchListModel(TextField query, UserService userService, int limit)
		{
			super(query, userService, limit);
		}

		@Override
		protected Option<RoleBean> convertToOption(SectionInfo info, RoleBean role)
		{
			HtmlLinkState state = new HtmlLinkState();
			state.addClass("add");
			state.setClickHandler(new OverrideHandler(SELECT_RESULT, Jq.$(state),
				events.getSubmitValuesFunction("addSingleExpression"), role.getUniqueID()));
			return new SelectRoleResultOption(role, state);
		}
	}

	public void setExpression(SectionInfo info, String expression)
	{
		getModel(info).setExpression(expression);
	}

	public String getExpression(SectionInfo info)
	{
		return getModel(info).getExpression();
	}

	public TextField getQuery()
	{
		return query;
	}

	public Button getSearch()
	{
		return search;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new ExpressionSelectorModel();
	}

	public MultiSelectionList<UserBean> getUserList()
	{
		return userList;
	}

	public MultiSelectionList<GroupBean> getGroupList()
	{
		return groupList;
	}

	public MultiSelectionList<RoleBean> getRoleList()
	{
		return roleList;
	}

	public SingleSelectionList<NameValue> getOtherTypes()
	{
		return otherTypes;
	}

	public SingleSelectionList<NameValue> getTypes()
	{
		return types;
	}

	public SingleSelectionList<NameValue> getReferrerOptions()
	{
		return referrerOptions;
	}

	public SingleSelectionList<String> getTokenIdList()
	{
		return tokenIdList;
	}

	public IpAddressInput getIpAddress()
	{
		return ipAddress;
	}

	public TextField getHttpReferrer()
	{
		return httpReferrer;
	}

	public Link getAddGroupLink()
	{
		return addGroupLink;
	}

	public Link getEditGroupLink()
	{
		return editGroupLink;
	}

	public Link getClearGroupLink()
	{
		return clearGroupLink;
	}

	public Link getSelectAllLink()
	{
		return selectAllLink;
	}

	public Link getSelectNoneLink()
	{
		return selectNoneLink;
	}

	public Button getAddSelected()
	{
		return addSelected;
	}

	public Button getAddGroupingButton()
	{
		return addGroupingButton;
	}

	public Button getAddOtherButton()
	{
		return addOtherButton;
	}

	public UpdateDomFunction getUpdateGroupFilterFunction()
	{
		return updateFunction;
	}

	public void setDialog(ExpressionSelectorDialog dialog)
	{
		this.dialog = dialog;
	}

	public List<Label> getGroupFilterNames()
	{
		return groupFilterNames;
	}

	public TextField getSelected()
	{
		return selected;
	}

	public static class ExpressionSelectorModel
	{
		@Bookmarked(name = "e", stateful = true)
		private String expression;
		@Bookmarked
		private boolean showGroupSelector;

		private Set<String> userExclusions;
		private String typeOption;
		private boolean groupFilterVisible;
		private boolean hasGroupSelected;
		private boolean showSelectArea;
		private boolean hasNoResults;
		private String invalidMessageKey;

		private SelectionDisplayTreeNode selectionDisplayTree;

		public SelectionDisplayTreeNode getSelectionDisplayTree()
		{
			return selectionDisplayTree;
		}

		public void setSelectionDisplayTree(SelectionDisplayTreeNode selectionDisplayTree)
		{
			this.selectionDisplayTree = selectionDisplayTree;
		}

		public boolean isShowSelectArea()
		{
			return showSelectArea;
		}

		public void setShowSelectArea(boolean showSelectArea)
		{
			this.showSelectArea = showSelectArea;
		}

		public boolean isHasGroupSelected()
		{
			return hasGroupSelected;
		}

		public void setHasGroupSelected(boolean hasGroupSelected)
		{
			this.hasGroupSelected = hasGroupSelected;
		}

		public boolean isShowGroupSelector()
		{
			return showGroupSelector;
		}

		public void setShowGroupSelector(boolean showGroupSelector)
		{
			this.showGroupSelector = showGroupSelector;
		}

		public String getExpression()
		{
			return expression;
		}

		public void setExpression(String expression)
		{
			this.expression = expression;
		}

		public Set<String> getUserExclusions()
		{
			return userExclusions;
		}

		public void setUserExclusions(Set<String> userExclusions)
		{
			this.userExclusions = userExclusions;
		}

		public String getTypeOption()
		{
			return typeOption;
		}

		public void setTypeOption(String typeOption)
		{
			this.typeOption = typeOption;
		}

		public boolean isGroupFilterVisible()
		{
			return groupFilterVisible;
		}

		public void setGroupFilterVisible(boolean groupFilterVisible)
		{
			this.groupFilterVisible = groupFilterVisible;
		}

		public boolean isHasNoResults()
		{
			return hasNoResults;
		}

		public void setHasNoResults(boolean hasNoResults)
		{
			this.hasNoResults = hasNoResults;
		}

		public String getInvalidMessageKey()
		{
			return invalidMessageKey;
		}

		public void setInvalidMessageKey(String invalidMessageKey)
		{
			this.invalidMessageKey = invalidMessageKey;
		}

	}

	@EventHandlerMethod
	public void addGrouping(SectionInfo info)
	{
		SelectionGroupingTreeNode groupTree = (SelectionGroupingTreeNode) userSessionService.getAttribute(TREE_KEY);
		SelectionGroupingTreeNode findGroupingNode = findGroupingNode(selected.getValue(info), groupTree);
		SelectionGroupingTreeNode newGrouping = new SelectionGroupingTreeNode(Grouping.MATCH_ANY);
		newGrouping.setNewGrouping(true);
		findGroupingNode.add(newGrouping);
	}

	private SelectionGroupingTreeNode addingIDsForGroupingNodes(SelectionGroupingTreeNode parent,
		SelectionGroupingTreeNode nodes, SectionInfo info)
	{
		nodes.setId("grouping" + SectionUtils.getPageUniqueId(info));
		nodes.setParent(parent);
		for( SelectionGroupingTreeNode group : nodes.getChildren() )
		{
			addingIDsForGroupingNodes(nodes, group, info);
		}
		return nodes;
	}

	public SelectionGroupingTreeNode findGroupingNode(String id, SelectionGroupingTreeNode rootNode)
	{
		if( rootNode.getId().equals(id) )
		{
			return rootNode;
		}

		for( SelectionGroupingTreeNode group : rootNode.getChildren() )
		{
			SelectionGroupingTreeNode node = findGroupingNode(id, group);
			if( node != null )
			{
				return node;
			}
		}
		return null;
	}

	public void setupUserSessionValues(SectionInfo info)
	{
		Object attribute = userSessionService.getAttribute(TREE_KEY);
		SelectionTreeNode node;
		if( attribute == null )
		{
			String expression = getModel(info).getExpression();
			Parser parser = new Parser();
			node = parser.convertToTreeNodes(expression == null ? "" : expression);
			userSessionService.setAttribute(TREE_KEY, node);
		}
		else
		{
			node = (SelectionTreeNode) attribute;
		}

		SelectionGroupingTreeNode groupingNodes = addingIDsForGroupingNodes(null, (SelectionGroupingTreeNode) node,
			info);
		SelectionDisplayTreeNode displayTree = new SelectionDisplayTreeNode();
		setupDisplayTreeNode(groupingNodes, displayTree, info);
		getModel(info).setSelectionDisplayTree(displayTree);
	}

	public void setupDisplayTreeNode(SelectionGroupingTreeNode selectionNode, SelectionDisplayTreeNode display,
		SectionInfo info)
	{
		for( SelectionGroupingTreeNode group : selectionNode.getChildren() )
		{
			SelectionDisplayTreeNode groupDisplay = new SelectionDisplayTreeNode();
			display.addChildren(groupDisplay);
			setupDisplayTreeNode(group, groupDisplay, info);
		}

		String groupingId = selectionNode.getId();
		boolean isNewAddGrouping = selectionNode.isNewGrouping();
		display.setGrouping(createDropdownList(info, selectionNode.getGrouping(), groupingId));
		display.setId(groupingId);
		display.getGrouping()
			.addReadyStatements(Js.call_s(SETUP_CLICK, display.getId(), Jq.$(selected), isNewAddGrouping));
		// highlight the new added grouping in js then set the value to false
		if( isNewAddGrouping )
		{
			selectionNode.setNewGrouping(false);
		}

		HtmlLinkState deleteLink = new HtmlLinkState();
		deleteLink.addClasses("unselect");
		deleteLink.setClickHandler(new OverrideHandler(deleteGroupingFunction, groupingId));
		display.setDelete(deleteLink);

		List<ExpressionSelection> expressions = new ArrayList<ExpressionSelection>();
		List<SelectionExpressionTreeNode> selections = selectionNode.getSelection();
		for( SelectionExpressionTreeNode selection : selections )
		{
			String expressionString = selection.getExpression();
			if( !Check.isEmpty(expressionString) )
			{
				String convertToInfix = new ExpressionFormatter(userService).convertToInfix(expressionString);
				ExpressionSelection expression = new ExpressionSelection(convertToInfix);
				HtmlLinkState delete = new HtmlLinkState();
				delete.addClasses("unselect");
				expression.setDeleteSelection(delete);
				delete.setClickHandler(new OverrideHandler(deleteExpressionFunction, expressionString, groupingId));
				expressions.add(expression);
			}
		}
		display.setExpression(expressions);
	}

	@EventHandlerMethod
	public void deleteGrouping(SectionInfo info, String groupingId)
	{
		SelectionGroupingTreeNode groupTree = (SelectionGroupingTreeNode) userSessionService.getAttribute(TREE_KEY);
		SelectionGroupingTreeNode node = findGroupingNode(groupingId, groupTree);
		SelectionGroupingTreeNode parent = node.getParent();
		if( parent != null )
		{
			parent.remove(node);
		}
	}

	@EventHandlerMethod
	public void deleteExpression(SectionInfo info, String expression, String groupingId)
	{
		SelectionGroupingTreeNode groupTree = (SelectionGroupingTreeNode) userSessionService.getAttribute(TREE_KEY);
		SelectionGroupingTreeNode node = findGroupingNode(groupingId, groupTree);
		List<SelectionExpressionTreeNode> selections = node.getSelection();

		SelectionExpressionTreeNode target = null;
		for( SelectionExpressionTreeNode selection : selections )
		{
			if( expression.equals(selection.getExpression()) )
			{
				target = selection;
			}
		}
		if( target != null )
		{
			selections.remove(target);
		}
	}

	@SuppressWarnings("rawtypes")
	public HtmlListState createDropdownList(SectionInfo info, Grouping grouping, String groupingId)
	{
		List<Option<?>> options = new ArrayList<Option<?>>();
		for( Grouping g : Grouping.values() )
		{
			if( !g.toString().equals(Grouping.TEMPORARY_NOT.toString()) )
			{
				if( groupingId.equals("grouping0") )
				{
					// the top level will not add MATCH_NONE
					if( !g.toString().equals(Grouping.MATCH_NONE.toString()) )
					{
						options.add(new SimpleOption(g.toString(), g.toString()));
					}
				}
				else
				{
					options.add(new SimpleOption(g.toString(), g.toString()));
				}
			}
		}

		HtmlListState listState = groupingSelections.getListState(info, groupingId, options, grouping.toString());
		listState.setStyle("width: 250px");
		listState.setSelectedValues(grouping.toString());
		listState.addEventStatements(JSHandler.EVENT_CHANGE, new OverrideHandler(updateGroupingFunction, groupingId));
		return listState;
	}

	@EventHandlerMethod
	public void updateGrouping(SectionInfo info, String groupingId)
	{
		SelectionGroupingTreeNode groupTree = (SelectionGroupingTreeNode) userSessionService.getAttribute(TREE_KEY);
		SelectionGroupingTreeNode targetNode = findGroupingNode(groupingId, groupTree);
		String selectedValue = groupingSelections.getSelectedValue(info, groupingId);
		if( selectedValue.equals(Grouping.MATCH_NONE.toString()) )
		{
			targetNode.setGrouping(Grouping.MATCH_NONE);
		}
		else if( selectedValue.equals(Grouping.MATCH_ALL.toString()) )
		{
			targetNode.setGrouping(Grouping.MATCH_ALL);
		}
		else
		{
			targetNode.setGrouping(Grouping.MATCH_ANY);
		}
	}

	public String convertTreeToExpression(SectionInfo info)
	{
		SelectionGroupingTreeNode groupTree = userSessionService.getAttribute(TREE_KEY);

		if( removeUnnecessaryNodes(groupTree) != null )
		{
			SelectionGroupingTreeNode cloneNode = cloneExpressionAndAddTemporaryNots(groupTree);

			StringBuilder buffer = new StringBuilder();
			getExpression(buffer, cloneNode);

			clearUserSession(info);
			if( buffer.toString().trim().equals("NOT") )
			{
				return "";
			}
			return buffer.toString();
		}
		return "";
	}

	private SelectionGroupingTreeNode removeUnnecessaryNodes(SelectionGroupingTreeNode root)
	{
		// Start by removing
		removeUnnecessaryChildNodesFromNode(root);

		final int childCount = root.getChildCount();
		if( childCount == 0 )
		{
			if( Check.isEmpty(root.getSelection()) )
			{
				return null;
			}
			else
			{
				return root;
			}
		}
		else
			if( childCount == 1
				&& (root.getGrouping() == Grouping.MATCH_ALL || root.getGrouping() == Grouping.MATCH_ANY) )
		{
			if( Check.isEmpty(root.getSelection()) )
			{
				root = root.getChildAt(0);
			}
		}
		return root;
	}

	private void removeUnnecessaryChildNodesFromNode(SelectionGroupingTreeNode node)
	{
		for( int i = node.getChildCount() - 1; i >= 0; i-- )
		{
			SelectionGroupingTreeNode child = node.getChildAt(i);

			removeUnnecessaryChildNodesFromNode(child);

			final int childChildCount = child.getChildCount();
			if( childChildCount == 0 )
			{
				if( Check.isEmpty(child.getSelection()) )
				{
					node.remove(child);
				}
				else
				{
					if( Check.isEmpty(node.getSelection()) )
					{
						if( node.getGrouping() == Grouping.MATCH_ALL || node.getGrouping() == Grouping.MATCH_ANY )
						{
							node.setGrouping(child.getGrouping());
						}

						for( SelectionExpressionTreeNode selection : child.getSelection() )
						{
							node.add(selection);
						}
						node.remove(child);
					}
				}
			}
			else
			{
				if( Check.isEmpty(node.getSelection()) )
				{
					if( node.getGrouping() == Grouping.MATCH_ALL || node.getGrouping() == Grouping.MATCH_ANY )
					{
						node.setGrouping(child.getGrouping());
					}

					for( SelectionExpressionTreeNode selection : child.getSelection() )
					{
						node.add(selection);
					}
					child.getSelection().clear();
					removeUnnecessaryChildNodesFromNode(child);
				}
			}
		}
	}

	private void getExpression(StringBuilder buffer, SelectionGroupingTreeNode node)
	{
		if( node == null )
		{
			return;
		}

		Grouping grouping = node.getGrouping();

		if( grouping == Grouping.TEMPORARY_NOT )
		{
			int i = 0;
			for( SelectionExpressionTreeNode selection : node.getSelection() )
			{
				if( !Check.isEmpty(selection.getExpression()) )
				{
					buffer.append(selection.getExpression());
					buffer.append(' ');
					if( i > 0 )
					{
						buffer.append(BooleanOp.OR_TOKEN.toString());
						buffer.append(' ');
					}
					i++;
				}
			}

			if( node.getChildCount() > 0 )
			{
				SelectionGroupingTreeNode child = node.getChildAt(0);
				getExpression(buffer, child);

				buffer.append(BooleanOp.NOT_TOKEN.toString());
				buffer.append(' ');
			}
		}
		else if( grouping == Grouping.MATCH_ALL || grouping == Grouping.MATCH_ANY )
		{
			BooleanOp op = grouping == Grouping.MATCH_ALL ? BooleanOp.AND_TOKEN : BooleanOp.OR_TOKEN;

			int i = 0;

			for( SelectionExpressionTreeNode selection : node.getSelection() )
			{
				if( !Check.isEmpty(selection.getExpression()) )
				{
					buffer.append(selection.getExpression());
					buffer.append(' ');

					if( i > 0 )
					{
						buffer.append(op.toString());
						buffer.append(' ');
					}
					i++;
				}
			}

			for( SelectionGroupingTreeNode child : node.getChildren() )
			{
				getExpression(buffer, child);

				buffer.append(op.toString());
				buffer.append(' ');
			}
		}
	}

	private SelectionGroupingTreeNode cloneExpressionAndAddTemporaryNots(SelectionGroupingTreeNode original)
	{
		SelectionGroupingTreeNode newNode = new SelectionGroupingTreeNode(original.getGrouping());
		for( SelectionExpressionTreeNode selection : original.getSelection() )
		{
			newNode.add(selection);
		}

		SelectionGroupingTreeNode nodeToAddChildrenTo = newNode;

		if( newNode.getGrouping() == Grouping.MATCH_NONE )
		{
			nodeToAddChildrenTo = new SelectionGroupingTreeNode(Grouping.MATCH_ANY);
			newNode.add(nodeToAddChildrenTo);
			newNode.setGrouping(Grouping.TEMPORARY_NOT);
		}

		for( SelectionGroupingTreeNode child : original.getChildren() )
		{
			nodeToAddChildrenTo.add(cloneExpressionAndAddTemporaryNots(child));
		}

		return newNode;
	}

	protected static class Parser extends PostfixExpressionParser<SelectionTreeNode>
	{
		public SelectionTreeNode convertToTreeNodes(String expression)
		{
			SelectionTreeNode root = getResult(expression);
			root = ensureRootIsGroup(root);
			removeTemporaryNots(root);

			return root;
		}

		@Override
		protected void doAndOperator(Stack<Pair<SelectionTreeNode, Integer>> operands)
		{
			doBinaryOperator(operands, Grouping.MATCH_ALL);
		}

		@Override
		protected void doOrOperator(Stack<Pair<SelectionTreeNode, Integer>> operands)
		{
			doBinaryOperator(operands, Grouping.MATCH_ANY);
		}

		protected void doBinaryOperator(Stack<Pair<SelectionTreeNode, Integer>> operands, Grouping grouping)
		{
			SelectionTreeNode second = operands.pop().getFirst();
			SelectionTreeNode first = operands.pop().getFirst();

			SelectionTreeNode parent = null;
			if( first instanceof SelectionGroupingTreeNode
				&& ((SelectionGroupingTreeNode) first).getGrouping() == grouping )
			{
				parent = first;
				((SelectionGroupingTreeNode) parent).add(second);
			}
			else
				if( second instanceof SelectionGroupingTreeNode
					&& ((SelectionGroupingTreeNode) second).getGrouping() == grouping )
			{
				parent = second;
				((SelectionGroupingTreeNode) parent).add(first);
			}
			else
			{
				parent = new SelectionGroupingTreeNode(grouping);
				((SelectionGroupingTreeNode) parent).add(first);
				((SelectionGroupingTreeNode) parent).add(second);
			}

			operands.push(new Pair<SelectionTreeNode, Integer>(parent, -1));
		}

		@Override
		protected void doNotOperator(Stack<Pair<SelectionTreeNode, Integer>> operands)
		{
			SelectionGroupingTreeNode node = new SelectionGroupingTreeNode(Grouping.TEMPORARY_NOT);
			node.insert(operands.pop().getFirst(), 0);
			operands.push(new Pair<SelectionTreeNode, Integer>(node, -1));
		}

		@Override
		protected SelectionTreeNode processOperand(String token)
		{
			return new SelectionExpressionTreeNode(token);
		}

		private SelectionTreeNode ensureRootIsGroup(SelectionTreeNode root)
		{
			if( !(root instanceof SelectionGroupingTreeNode) )
			{
				SelectionTreeNode child = root;
				root = new SelectionGroupingTreeNode(Grouping.MATCH_ANY);
				if( !Check.isEmpty(((SelectionExpressionTreeNode) child).getExpression()) )
				{
					((SelectionGroupingTreeNode) root).add((SelectionExpressionTreeNode) child);
				}
			}
			return root;
		}

		private void removeTemporaryNots(SelectionTreeNode node)
		{
			if( node instanceof SelectionGroupingTreeNode )
			{
				if( ((SelectionGroupingTreeNode) node).getGrouping() == Grouping.TEMPORARY_NOT )
				{
					((SelectionGroupingTreeNode) node).setGrouping(Grouping.MATCH_NONE);
					int childCount = ((SelectionGroupingTreeNode) node).getChildCount();
					if( childCount > 0 )
					{
						SelectionGroupingTreeNode child = ((SelectionGroupingTreeNode) node).getChildAt(0);
						if( child.getGrouping() == Grouping.MATCH_ANY )
						{
							for( int i = childCount - 1; i >= 0; i-- )
							{
								for( SelectionExpressionTreeNode expression : child.getSelection() )
								{
									((SelectionGroupingTreeNode) node).add(expression);
								}
							}
							// Remove child, but inherit the child's children
							((SelectionGroupingTreeNode) node).remove(child);
						}
					}
				}

				// Recurse
				for( SelectionGroupingTreeNode child : ((SelectionGroupingTreeNode) node).getChildren() )
				{
					removeTemporaryNots(child);
				}
			}
		}
	}

}
