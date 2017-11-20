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

package com.tle.web.lti.consumers.section;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.lti.consumers.LtiConsumerConstants;
import com.tle.common.lti.consumers.LtiConsumerConstants.UnknownUser;
import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.common.recipientselector.formatter.ExpressionFormatter;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.Bind;
import com.tle.core.lti.consumers.service.LtiConsumerService;
import com.tle.core.lti.consumers.service.session.LtiConsumerEditingBean;
import com.tle.core.services.user.UserService;
import com.tle.web.entities.section.AbstractEntityEditor;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.lti.consumers.section.LtiConsumerEditorSection.LtiConsumerEditorModel;
import com.tle.web.recipientselector.ExpressionSelectorDialog;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.SelectionsTable;
import com.tle.web.sections.equella.component.model.DynamicSelectionsTableModel;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.equella.listmodel.EnumListModel;
import com.tle.web.sections.equella.utils.SelectGroupDialog;
import com.tle.web.sections.equella.utils.SelectRoleDialog;
import com.tle.web.sections.equella.utils.SelectedGroup;
import com.tle.web.sections.equella.utils.SelectedRole;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;

@Bind
public class LtiConsumerEditorSection
	extends
		AbstractEntityEditor<LtiConsumerEditingBean, LtiConsumer, LtiConsumerEditorModel>
{
	private static enum RoleTypes
	{
		INSTRUCTOR, UNKNOWN;
	}

	@PlugKey("editor.allowedselector.title")
	private static Label EXPRESSION_TITLE;
	@PlugKey("editor.unknown.")
	private static String UKNOWN_USER_PFX;
	@PlugKey("editor.table.remove")
	private static Label TABLE_REMOVE_ACTION;
	@PlugKey("editor.table.roles.none")
	private static Label TABLE_NO_ROLES;
	@PlugKey("editor.table.groups.none")
	private static Label TABLE_NO_GROUPS;
	@PlugKey("editor.error.nokey")
	private static Label ERROR_KEY;
	@PlugKey("editor.error.nosecret")
	private static Label ERROR_SECRET;
	@PlugKey("editor.error.duplicate.key")
	private static Label ERROR_DUPLICATE_KEY;
	@PlugKey("editor.error.duplicate.secret")
	private static Label ERROR_DUPLICATE_SECRET;
	@PlugKey("editor.error.custom.norole")
	private static Label ERROR_NO_ROLE_SELECTED;
	@PlugKey("editor.role.instructor.arrow")
	private static String INSTRUCTOR_ARROW;
	@PlugKey("editor.role.unknown.arrow")
	private static String UNKNOWN_ARROW;

	@Inject
	private LtiConsumerService consumerService;
	@ViewFactory
	private FreemarkerFactory view;
	@Inject
	private ComponentFactory componentFactory;
	@AjaxFactory
	protected AjaxGenerator ajax;
	@EventFactory
	private EventGenerator events;
	@Inject
	private UserService userService;

	@Component
	private TextField consumerKeyField;
	@Component
	private TextField consumerSecretField;
	@Component
	private TextField prefixField;
	@Component
	private TextField postfixField;
	@Component
	private SingleSelectionList<UnknownUser> unknownUserList;
	@Component
	private SelectionsTable instructorRolesTable;
	@Component
	@PlugKey("editor.table.roles.add")
	private Link addInstructorRole;
	@Component
	private SelectionsTable otherRolesTable;
	@Component
	@PlugKey("editor.table.roles.add")
	private Link addOtherRole;
	@Component
	private SelectionsTable unknownUserGroupsTable;
	@Component
	@PlugKey("editor.table.groups.add")
	private Link addUnknownUserGroup;
	@Component
	private TextField customRoleField;
	@Component
	private SelectionsTable customRolesTable;
	@Inject
	private SelectRoleDialog customRoleDialog;

	@Inject
	private ExpressionSelectorDialog allowedSelector;
	@Inject
	private SelectRoleDialog selectInstructorDialog;
	@Inject
	private SelectRoleDialog selectOtherDialog;
	@Inject
	private SelectGroupDialog unknowUserGroupDialog;

	private JSCallable removeInstructorRoleFunc;
	private JSCallable removeOtherRoleFunc;
	private JSCallable removeGroupFunc;
	private JSCallable removeCustomRolesFunc;

	@Override
	protected AbstractEntityService<LtiConsumerEditingBean, LtiConsumer> getEntityService()
	{
		return consumerService;
	}

	@Override
	protected LtiConsumer createNewEntity(SectionInfo info)
	{
		return new LtiConsumer();
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		allowedSelector.setOkCallback(
			ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("expressionCallback"), "allowed"));
		componentFactory.registerComponent(id, "allowedSelector", tree, allowedSelector);
		allowedSelector.setTitle(EXPRESSION_TITLE);

		selectInstructorDialog.setAjax(true);
		selectInstructorDialog.setMultipleRoles(true);
		selectInstructorDialog.setOkCallback(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("instructorRoleCallback"), "instructorrole"));
		componentFactory.registerComponent(id, "selectInstructor", tree, selectInstructorDialog);

		addInstructorRole.setClickHandler(selectInstructorDialog.getOpenFunction());

		instructorRolesTable.setSelectionsModel(new RoleTableModel(RoleTypes.INSTRUCTOR));
		instructorRolesTable.setNothingSelectedText(TABLE_NO_ROLES);
		instructorRolesTable.setAddAction(addInstructorRole);

		removeInstructorRoleFunc = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("removeRole"),
			"instructorrole");

		selectOtherDialog.setAjax(true);
		selectOtherDialog.setMultipleRoles(true);
		selectOtherDialog.setOkCallback(
			ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("otherRoleCallback"), "otherrole"));
		componentFactory.registerComponent(id, "selectOther", tree, selectOtherDialog);

		addOtherRole.setClickHandler(selectOtherDialog.getOpenFunction());

		otherRolesTable.setSelectionsModel(new RoleTableModel(RoleTypes.UNKNOWN));
		otherRolesTable.setNothingSelectedText(TABLE_NO_ROLES);
		otherRolesTable.setAddAction(addOtherRole);

		removeOtherRoleFunc = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("removeRole"),
			"otherrole");

		unknownUserList.setListModel(
			new EnumListModel<LtiConsumerConstants.UnknownUser>(UKNOWN_USER_PFX, true, UnknownUser.values()));
		unknownUserList.addChangeEventHandler(ajax.getAjaxUpdateDomFunction(tree, this, null, "unknownusergroup"));

		unknowUserGroupDialog.setAjax(true);
		unknowUserGroupDialog.setMultipleGroups(true);
		unknowUserGroupDialog.setOkCallback(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("unknownUserGroupCallback"), "unknownusergroup"));
		componentFactory.registerComponent(id, "selectGroup", tree, unknowUserGroupDialog);

		addUnknownUserGroup.setClickHandler(unknowUserGroupDialog.getOpenFunction());

		unknownUserGroupsTable.setSelectionsModel(new GroupsTableModel());
		unknownUserGroupsTable.setNothingSelectedText(TABLE_NO_GROUPS);
		unknownUserGroupsTable.setAddAction(addUnknownUserGroup);

		removeGroupFunc = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("removeGroup"),
			"unknownusergroup");

		customRoleDialog.setAjax(true);
		customRoleDialog.setMultipleRoles(true);
		customRoleDialog.setOkCallback(
			ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("customRolesCallback"), "customrole"));
		componentFactory.registerComponent(id, "selectCustom", tree, customRoleDialog);

		customRolesTable.setSelectionsModel(new CustomRoleTableModel());
		customRolesTable.setNothingSelectedText(TABLE_NO_ROLES);

		removeCustomRolesFunc = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("removeCustom"),
			"customrole");

		customRoleField.setAutoCompleteCallback(ajax.getAjaxFunction("customRolesAutocomplete"));

	}

	@Override
	protected SectionRenderable renderFields(RenderEventContext context,
		EntityEditingSession<LtiConsumerEditingBean, LtiConsumer> session)
	{
		String allowedExpression = allowedSelector.getExpression(context);
		if( allowedExpression == null )
		{
			allowedExpression = Recipient.EVERYONE.getPrefix();
		}
		allowedSelector.setExpression(context, allowedExpression);
		LtiConsumerEditorModel model = getModel(context);
		model.setPrettyExpression(new ExpressionFormatter(userService).convertToInfix(allowedExpression));
		customRoleField.setValue(context, null);
		return view.createResult("editconsumer.ftl", context);
	}

	@AjaxMethod
	public String[] customRolesAutocomplete(SectionInfo info)
	{
		return LtiConsumerConstants.CUSTOM_ROLES_AUTOCOMPLETE;
	}

	@EventHandlerMethod
	public void expressionCallback(SectionInfo info, String selectorId, String expression)
	{
		LtiConsumerEditingBean bean = getEntityService().loadSession(getModel(info).getSessionId()).getBean();
		if( Check.isEmpty(expression) )
		{
			expression = Recipient.EVERYONE.getPrefix();
		}
		bean.setAllowedExpression(expression);
	}

	@EventHandlerMethod
	public void instructorRoleCallback(SectionInfo info, String rolesJson)
	{
		LtiConsumerEditingBean bean = getEntityService().loadSession(getModel(info).getSessionId()).getBean();
		Set<String> selectedRoles = SelectRoleDialog.rolesFromJsonString(rolesJson).stream().map(sr -> sr.getUuid())
			.collect(Collectors.toSet());
		if( bean.getInstructorRoles() == null )
		{
			bean.setInstructorRoles(selectedRoles);
		}
		else
		{
			bean.getInstructorRoles().addAll(selectedRoles);
		}
	}

	@EventHandlerMethod
	public void otherRoleCallback(SectionInfo info, String rolesJson)
	{
		LtiConsumerEditingBean bean = getEntityService().loadSession(getModel(info).getSessionId()).getBean();
		Set<String> selectedRoles = SelectRoleDialog.rolesFromJsonString(rolesJson).stream().map(sr -> sr.getUuid())
			.collect(Collectors.toSet());
		if( bean.getOtherRoles() == null )
		{
			bean.setOtherRoles(selectedRoles);
		}
		else
		{
			bean.getOtherRoles().addAll(selectedRoles);
		}
	}

	@EventHandlerMethod
	public void unknownUserGroupCallback(SectionInfo info, String groupsJson)
	{
		LtiConsumerEditingBean bean = getEntityService().loadSession(getModel(info).getSessionId()).getBean();
		Set<String> selectedGroups = SelectGroupDialog.groupsFromJsonString(groupsJson).stream().map(sg -> sg.getUuid())
			.collect(Collectors.toSet());
		if( bean.getUnknownGroups() == null )
		{
			bean.setUnknownGroups(selectedGroups);
		}
		else
		{
			bean.getUnknownGroups().addAll(selectedGroups);
		}
	}

	@EventHandlerMethod
	public void customRolesCallback(SectionInfo info, String rolesJson)
	{
		EntityEditingSession<LtiConsumerEditingBean, LtiConsumer> session = getEntityService()
			.loadSession(getModel(info).getSessionId());
		String ltiRole = customRoleField.getValue(info);
		if( Check.isEmpty(ltiRole) )
		{
			Map<String, Object> validationErrors = session.getValidationErrors();
			validationErrors.put("nocustomrole", ERROR_NO_ROLE_SELECTED);
			return;
		}
		LtiConsumerEditingBean bean = session.getBean();

		Set<Pair<String, String>> roleMappings = SelectGroupDialog.groupsFromJsonString(rolesJson).stream()
			.map(sg -> new Pair<String, String>(ltiRole, sg.getUuid())).collect(Collectors.toSet());
		if( bean.getCustomRoles() == null )
		{
			bean.setCustomRoles(roleMappings);
		}
		else
		{
			bean.getCustomRoles().addAll(roleMappings);
		}
	}

	@EventHandlerMethod
	public void removeCustom(SectionInfo info, String ltiRole, String roleUuid)
	{
		LtiConsumerEditingBean bean = getEntityService().loadSession(getModel(info).getSessionId()).getBean();

		Set<Pair<String, String>> customRoles = bean.getCustomRoles();
		if( customRoles.size() > 1 )
		{
			Pair<String, String> filtered = customRoles.stream()
				.filter(crm -> crm.getFirst().equals(ltiRole) && crm.getSecond().equals(roleUuid)).findFirst().get();
			customRoles.remove(filtered);
		}
		else
		{
			bean.setCustomRoles(null);
		}
	}

	@EventHandlerMethod
	public void removeRole(SectionInfo info, SelectedRole remove, RoleTypes type)
	{
		LtiConsumerEditingBean bean = getEntityService().loadSession(getModel(info).getSessionId()).getBean();
		switch( type )
		{
			case INSTRUCTOR:
				bean.getInstructorRoles().remove(remove.getUuid());
				break;
			case UNKNOWN:
				bean.getOtherRoles().remove(remove.getUuid());
				break;
		}

	}

	@EventHandlerMethod
	public void removeGroup(SectionInfo info, SelectedGroup remove)
	{
		LtiConsumerEditingBean bean = getEntityService().loadSession(getModel(info).getSessionId()).getBean();
		bean.getUnknownGroups().remove(remove.getUuid());
	}

	@Override
	protected void loadFromSession(SectionInfo info, EntityEditingSession<LtiConsumerEditingBean, LtiConsumer> session)
	{
		final LtiConsumerEditingBean bean = session.getBean();
		consumerKeyField.setValue(info,
			bean.getConsumerKey() == null ? UUID.randomUUID().toString() : bean.getConsumerKey());
		consumerSecretField.setValue(info,
			bean.getConsumerSecret() == null ? UUID.randomUUID().toString() : bean.getConsumerSecret());
		prefixField.setValue(info, bean.getPrefix());
		postfixField.setValue(info, bean.getPostfix());
		allowedSelector.setExpression(info, bean.getAllowedExpression());
		LtiConsumerEditorModel model = getModel(info);
		if( !Check.isEmpty(bean.getInstructorRoles()) )
		{
			HashSet<SelectedRole> beanRoles = new HashSet<SelectedRole>();
			for( String uuid : bean.getInstructorRoles() )
			{
				RoleBean roleInfo = userService.getInformationForRole(uuid);
				beanRoles.add(new SelectedRole(roleInfo.getUniqueID(), roleInfo.getName()));
			}
			model.setSelectedInstructorRoles(beanRoles);
		}
		if( !Check.isEmpty(bean.getOtherRoles()) )
		{
			HashSet<SelectedRole> beanRoles = new HashSet<SelectedRole>();
			for( String uuid : bean.getOtherRoles() )
			{
				RoleBean roleInfo = userService.getInformationForRole(uuid);
				beanRoles.add(new SelectedRole(roleInfo.getUniqueID(), roleInfo.getName()));
			}
			model.setSelectedOtherRoles(beanRoles);
		}
		if( !Check.isEmpty(bean.getCustomRoles()) )
		{
			HashSet<CustomRoleMapping> customRoles = new HashSet<CustomRoleMapping>();
			for( Pair<String, String> entry : bean.getCustomRoles() )
			{
				RoleBean roleInfo = userService.getInformationForRole(entry.getSecond());
				customRoles.add(new CustomRoleMapping(entry.getFirst(),
					new SelectedRole(roleInfo.getUniqueID(), roleInfo.getName())));
			}
			model.setCustomRoleMappings(customRoles);
		}

		int unknownUserStrat = bean.getUnknownUser();
		if( unknownUserStrat >= 0 && unknownUserStrat < UnknownUser.values().length )
		{
			unknownUserList.setSelectedValue(info, UnknownUser.values()[unknownUserStrat]);
			if( unknownUserStrat == UnknownUser.CREATE.getValue() )
			{
				model.setSelectGroups(true);
			}
		}
		else
		{
			unknownUserList.setSelectedValue(info, UnknownUser.DENY);
		}

		if( model.isSelectGroups() && !Check.isEmpty(bean.getUnknownGroups()) )
		{
			HashSet<SelectedGroup> beanGroups = new HashSet<SelectedGroup>();
			for( String uuid : bean.getUnknownGroups() )
			{
				GroupBean beanInfo = userService.getInformationForGroup(uuid);
				beanGroups.add(new SelectedGroup(beanInfo.getUniqueID(), beanInfo.getName()));
			}
			model.setSelectedUnknownUserGroups(beanGroups);
		}
	}

	@Override
	protected void validate(SectionInfo info, EntityEditingSession<LtiConsumerEditingBean, LtiConsumer> session)
	{
		Map<String, Object> validationErrors = session.getValidationErrors();
		if( Check.isEmpty(consumerKeyField.getValue(info)) )
		{
			validationErrors.put("key", ERROR_KEY);
		}
		else
		{
			LtiConsumer consumer = consumerService.findByConsumerKey(consumerKeyField.getValue(info));
			if( consumer != null && !consumer.getUuid().equals(session.getBean().getUuid()) )
			{
				validationErrors.put("key", ERROR_DUPLICATE_KEY);
			}
		}
		if( Check.isEmpty(consumerSecretField.getValue(info)) )
		{
			validationErrors.put("secret", ERROR_SECRET);
		}

	}

	@Override
	protected void saveToSession(SectionInfo info, EntityEditingSession<LtiConsumerEditingBean, LtiConsumer> session,
		boolean validate)
	{
		LtiConsumerEditingBean bean = session.getBean();
		bean.setConsumerKey(consumerKeyField.getValue(info));
		bean.setConsumerSecret(consumerSecretField.getValue(info));
		bean.setPrefix(prefixField.getValue(info));
		bean.setPostfix(postfixField.getValue(info));
		bean.setAllowedExpression(allowedSelector.getExpression(info));
		bean.setUnknownUser(unknownUserList.getSelectedValue(info).getValue());
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new LtiConsumerEditorModel();
	}

	public class LtiConsumerEditorModel
		extends
			AbstractEntityEditor<LtiConsumerEditingBean, LtiConsumer, LtiConsumerEditorModel>.AbstractEntityEditorModel
	{
		private String prettyExpression;
		private Set<SelectedRole> selectedInstructorRoles;
		private Set<SelectedRole> selectedOtherRoles;
		private boolean selectGroups;
		private Set<SelectedGroup> selectedUnknownUserGroups;
		private Set<CustomRoleMapping> customRoleMappings;

		public String getPrettyExpression()
		{
			return prettyExpression;
		}

		public Set<SelectedRole> getSelectedInstructorRoles()
		{
			return selectedInstructorRoles;
		}

		public void setSelectedInstructorRoles(Set<SelectedRole> selectedInstructorRoles)
		{
			this.selectedInstructorRoles = selectedInstructorRoles;
		}

		public Set<SelectedRole> getSelectedOtherRoles()
		{
			return selectedOtherRoles;
		}

		public void setSelectedOtherRoles(Set<SelectedRole> selectedOtherRoles)
		{
			this.selectedOtherRoles = selectedOtherRoles;
		}

		public void setPrettyExpression(String prettyExpression)
		{
			this.prettyExpression = prettyExpression;
		}

		public Set<SelectedGroup> getSelectedUnknownUserGroups()
		{
			return selectedUnknownUserGroups;
		}

		public void setSelectedUnknownUserGroups(Set<SelectedGroup> selectedUnknownUserGroups)
		{
			this.selectedUnknownUserGroups = selectedUnknownUserGroups;
		}

		public boolean isSelectGroups()
		{
			return selectGroups;
		}

		public void setSelectGroups(boolean selectGroups)
		{
			this.selectGroups = selectGroups;
		}

		public Set<CustomRoleMapping> getCustomRoleMappings()
		{
			return customRoleMappings;
		}

		public void setCustomRoleMappings(Set<CustomRoleMapping> customRoleMappings)
		{
			this.customRoleMappings = customRoleMappings;
		}

	}

	private class RoleTableModel extends DynamicSelectionsTableModel<SelectedRole>
	{
		final RoleTypes type;

		public RoleTableModel(RoleTypes type)
		{
			this.type = type;
		}

		@Override
		protected Collection<SelectedRole> getSourceList(SectionInfo info)
		{
			switch( type )
			{
				case INSTRUCTOR:
					return getModel(info).getSelectedInstructorRoles();
				case UNKNOWN:
					return getModel(info).getSelectedOtherRoles();
			}
			return null;
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, SelectedRole role,
			List<SectionRenderable> actions, int index)
		{
			switch( type )
			{
				case INSTRUCTOR:
					selection.setViewAction(new LabelRenderer(new KeyLabel(INSTRUCTOR_ARROW, role.getDisplayName())));
					actions.add(makeRemoveAction(TABLE_REMOVE_ACTION,
						new OverrideHandler(removeInstructorRoleFunc, role, type)));
					break;
				case UNKNOWN:
					selection.setViewAction(new LabelRenderer(new KeyLabel(UNKNOWN_ARROW, role.getDisplayName())));
					actions.add(
						makeRemoveAction(TABLE_REMOVE_ACTION, new OverrideHandler(removeOtherRoleFunc, role, type)));
			}

		}
	}

	private class CustomRoleMapping
	{
		private String ltiRole;
		private SelectedRole equellaRole;

		public CustomRoleMapping(String ltiRole, SelectedRole equellaRole)
		{
			this.ltiRole = ltiRole;
			this.equellaRole = equellaRole;
		}

		public String getLtiRole()
		{
			return ltiRole;
		}

		public SelectedRole getEquellaRole()
		{
			return equellaRole;
		}

	}

	private class CustomRoleTableModel extends DynamicSelectionsTableModel<CustomRoleMapping>
	{

		@Override
		protected Collection<CustomRoleMapping> getSourceList(SectionInfo info)
		{
			return getModel(info).getCustomRoleMappings();
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, CustomRoleMapping thing,
			List<SectionRenderable> actions, int index)
		{
			selection.setViewAction(
				new LabelRenderer(new TextLabel(thing.getLtiRole() + " ⇨ " + thing.getEquellaRole().getDisplayName())));
			actions.add(makeRemoveAction(TABLE_REMOVE_ACTION,
				new OverrideHandler(removeCustomRolesFunc, thing.getLtiRole(), thing.getEquellaRole().getUuid())));
		}

	}

	private class GroupsTableModel extends DynamicSelectionsTableModel<SelectedGroup>
	{

		@Override
		protected Collection<SelectedGroup> getSourceList(SectionInfo info)
		{
			return getModel(info).getSelectedUnknownUserGroups();
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, SelectedGroup group,
			List<SectionRenderable> actions, int index)
		{
			selection.setViewAction(new LabelRenderer(new TextLabel(group.getDisplayName())));
			actions.add(makeRemoveAction(TABLE_REMOVE_ACTION, new OverrideHandler(removeGroupFunc, group)));
		}
	}

	public TextField getConsumerKeyField()
	{
		return consumerKeyField;
	}

	public TextField getConsumerSecretField()
	{
		return consumerSecretField;
	}

	public TextField getPrefixField()
	{
		return prefixField;
	}

	public TextField getPostfixField()
	{
		return postfixField;
	}

	public ExpressionSelectorDialog getAllowedSelector()
	{
		return allowedSelector;
	}

	public SingleSelectionList<UnknownUser> getUnknownUserList()
	{
		return unknownUserList;
	}

	public SelectionsTable getInstructorRolesTable()
	{
		return instructorRolesTable;
	}

	public SelectionsTable getOtherRolesTable()
	{
		return otherRolesTable;
	}

	public SelectionsTable getUnknownUserGroupsTable()
	{
		return unknownUserGroupsTable;
	}

	public TextField getCustomRoleField()
	{
		return customRoleField;
	}

	public SelectRoleDialog getCustomRoleDialog()
	{
		return customRoleDialog;
	}

	public SelectionsTable getCustomRolesTable()
	{
		return customRolesTable;
	}
}
