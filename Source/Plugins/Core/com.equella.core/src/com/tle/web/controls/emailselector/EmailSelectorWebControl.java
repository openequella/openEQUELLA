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

package com.tle.web.controls.emailselector;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.dytech.edge.common.Constants;
import com.dytech.edge.wizard.beans.control.CustomControl;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.tle.common.Check;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.common.wizard.controls.emailselector.EmailSelectorControl;
import com.tle.core.email.EmailService;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserService;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.SelectionsTable;
import com.tle.web.sections.equella.component.model.DynamicSelectionsTableModel;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.equella.utils.SelectUserDialog;
import com.tle.web.sections.equella.utils.SelectedUser;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.wizard.controls.AbstractWebControl;
import com.tle.web.wizard.controls.CCustomControl;
import com.tle.web.wizard.controls.WebControlModel;

@SuppressWarnings("nls")
@Bind
public class EmailSelectorWebControl extends AbstractWebControl<EmailSelectorWebControl.EmailSelectorWebControlModel>
{
	@PlugKey("prompt.selectsingle")
	private static Label LABEL_PROMPTSINGLE;
	@PlugKey("prompt.selectmultiple")
	private static Label LABEL_PROMPTMULTIPLE;
	@PlugKey("title.selectsingle")
	private static Label LABEL_TITLESINGLE;
	@PlugKey("title.selectmultiple")
	private static Label LABEL_TITLEMULTIPLE;

	@PlugKey("confirmremove")
	private static String KEY_CONFIRM;
	@PlugKey("currentlyselectedstuff.remove")
	private static Label LABEL_REMOVE;

	private EmailSelectorControl definitionControl;
	private CCustomControl storageControl;

	@ViewFactory(name="wizardFreemarkerFactory")
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@Inject
	private ComponentFactory componentFactory;
	@Inject
	private SelectUserDialog selectUserDialog;
	@Inject
	private UserService userService;
	@Inject
	private EmailService emailService;

	@Component(name = "e")
	private TextField email;
	@Component(name = "a")
	@PlugKey("button.addemail")
	private Button addEmail;
	@Component
	private SelectionsTable selectedTable;

	private JSCallable removeFunction;

	@Override
	public void setWrappedControl(final HTMLControl control)
	{
		definitionControl = new EmailSelectorControl((CustomControl) control.getControlBean());
		storageControl = (CCustomControl) control;
		super.setWrappedControl(control);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		final EmailSelectorWebControlModel model = getModel(context);

		boolean allowAdd = (isEmpty() || definitionControl.isSelectMultiple());
		model.setAllowAdd(allowAdd);

		addDisablers(context, email, allowAdd ? selectUserDialog.getOpener() : null, allowAdd ? addEmail : null);

		return viewFactory.createResult("emailselectorwebcontrol.ftl", context);
	}

	@Override
	public void registered(final String id, final SectionTree tree)
	{
		super.registered(id, tree);

		final boolean multiple = definitionControl.isSelectMultiple();

		selectUserDialog.setMultipleUsers(multiple);
		selectUserDialog.setTitle(multiple ? LABEL_TITLEMULTIPLE : LABEL_TITLESINGLE);
		selectUserDialog.setPrompt(multiple ? LABEL_PROMPTMULTIPLE : LABEL_PROMPTSINGLE);
		selectUserDialog.setOkCallback(getReloadFunction(true, events.getEventHandler("select")));

		componentFactory.registerComponent(id, "s", tree, selectUserDialog);

		addEmail.setClickHandler(getReloadFunction(true, events.getEventHandler("addEmail")));

		removeFunction = getReloadFunction(true, events.getEventHandler("removeEmail"));

		selectedTable.setSelectionsModel(new EmailsModel());
	}

	@Override
	public boolean isEmpty()
	{
		return storageControl.getValues().size() == 0;
	}

	@EventHandlerMethod
	public void select(final SectionInfo info, List<SelectedUser> users) throws Exception
	{
		// no sneaky backdoor additions
		if( !definitionControl.isSelectMultiple() && !isEmpty() )
		{
			return;
		}
		getModel(info).setSelectedUsers(users);
	}

	@EventHandlerMethod
	public void removeEmail(final SectionInfo info, final String emailAddress) throws Exception
	{
		storageControl.getValues().remove(emailAddress);
	}

	@EventHandlerMethod
	public void addEmail(SectionInfo info)
	{
		final String address = email.getValue(info);
		final List<String> values = storageControl.getValues();
		if( !values.contains(address) && validateEmail(info, address) )
		{
			values.add(address);
			email.setValue(info, Constants.BLANK);
		}
	}

	private boolean validateEmail(SectionInfo info, String address)
	{
		if( !emailService.isValidAddress(address) )
		{
			getModel(info).setWarning("error.invalidemail");
			return false;
		}
		return true;
	}

	@Override
	public void doEdits(SectionInfo info)
	{
		final EmailSelectorWebControlModel model = getModel(info);
		if( model.getSelectedUsers() != null )
		{
			final Collection<String> selectedUuids = Collections2.transform(model.getSelectedUsers(),
				new Function<SelectedUser, String>()
				{
					@Override
					public String apply(SelectedUser user)
					{
						return user.getUuid();
					}
				});

			final List<String> controlValues = storageControl.getValues();
			for( UserBean ub : userService.getInformationForUsers(selectedUuids).values() )
			{
				String emailAddress = ub.getEmailAddress();
				if( Check.isEmpty(emailAddress) )
				{
					model.setWarning("warning.noemails");
				}
				else if( !controlValues.contains(emailAddress) )
				{
					controlValues.add(emailAddress);
				}
			}
		}
	}

	@Override
	public Class<EmailSelectorWebControlModel> getModelClass()
	{
		return EmailSelectorWebControlModel.class;
	}

	public SelectUserDialog getSelectUserDialog()
	{
		return selectUserDialog;
	}

	public TextField getEmail()
	{
		return email;
	}

	public Button getAddEmailButton()
	{
		return addEmail;
	}

	public SelectionsTable getSelectedTable()
	{
		return selectedTable;
	}

	private class EmailsModel extends DynamicSelectionsTableModel<String>
	{
		@Override
		protected List<String> getSourceList(SectionInfo info)
		{
			return storageControl.getValues();
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, String emailAddress,
			List<SectionRenderable> actions, int index)
		{
			selection.setName(new TextLabel(emailAddress));
			if( isEnabled() )
			{
				final JSHandler removeHandler = new OverrideHandler(removeFunction, emailAddress);
				removeHandler.addValidator(new Confirm(new KeyLabel(KEY_CONFIRM, emailAddress)));
				actions.add(makeRemoveAction(LABEL_REMOVE, removeHandler));
			}
		}
	}

	public static class EmailSelectorWebControlModel extends WebControlModel
	{
		@Bookmarked(name = "w", stateful = false)
		private String warning;
		private boolean allowAdd;
		/**
		 * For shoving de-JSONed results into.
		 */
		private List<SelectedUser> selectedUsers;

		public String getWarning()
		{
			return warning;
		}

		public void setWarning(String warning)
		{
			this.warning = warning;
		}

		public boolean isAllowAdd()
		{
			return allowAdd;
		}

		public void setAllowAdd(boolean allowAdd)
		{
			this.allowAdd = allowAdd;
		}

		public List<SelectedUser> getSelectedUsers()
		{
			return selectedUsers;
		}

		public void setSelectedUsers(final List<SelectedUser> selectedUsers)
		{
			this.selectedUsers = selectedUsers;
		}
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return email;
	}
}
