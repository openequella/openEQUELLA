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

package com.tle.web.sections.equella.utils;

import static com.tle.web.sections.js.generic.statement.FunctionCallStatement.jscall;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import net.sf.json.JSONArray;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.dialog.AbstractOkayableDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.CloseWindowResult;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.dialog.model.DialogModel;

/**
 * Note: the okCallback is event handler of signature: handler(SectionInfo info,
 * String usersJson) where usersJson is an array of
 * SelectUserDialog.SelectedUser objects in JSON format. Use
 * SelectUserDialog.userFromJsonString(String) or
 * SelectUserDialog.usersFromJsonString(String) to deserialise the string.
 * 
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
@NonNullByDefault
public class SelectUserDialog extends AbstractOkayableDialog<SelectUserDialog.Model>
{
	static
	{
		PluginResourceHandler.init(SelectUserDialog.class);
	}

	private static final int WIDTH = 550;

	private CurrentUsersCallback currentUsersCallback;
	@Inject
	protected SelectUserSection section;
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@PlugKey("utils.selectuserdialog.default.title")
	private static Label LABEL_DEFAULT_TITLE;

	private Label title = LABEL_DEFAULT_TITLE;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		setAjax(true);
		tree.registerSubInnerSection(section, id);
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return title;
	}

	public static Label getTitleLabel()
	{
		return LABEL_DEFAULT_TITLE;
	}

	@Override
	public void showDialog(SectionInfo info)
	{
		super.showDialog(info);
		if( currentUsersCallback != null )
		{
			section.setSelections(info, currentUsersCallback.getCurrentSelectedUsers(info));
		}
		else
		{
			section.setSelections(info, null);
		}
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		getModel(context).setInnerContents(renderSection(context, section));

		return viewFactory.createResult("utils/selectuserdialog.ftl", this);
	}

	@Override
	public String getWidth()
	{
		return WIDTH + "px";
	}

	@Override
	protected JSHandler createOkHandler(SectionTree tree)
	{
		return events.getNamedHandler("returnResults");
	}

	@EventHandlerMethod
	public void returnResults(SectionInfo info)
	{
		section.addUsers(info);
		final List<SelectedUser> selections = section.getSelections(info);
		Object[] array;
		if( selections != null )
		{
			array = selections.toArray();
		}
		else
		{
			array = new SelectedUser[]{};
		}
		String users = JSONArray.fromObject(array).toString();

		info.getRootRenderContext().setRenderedBody(
			new CloseWindowResult(jscall(getCloseFunction()), jscall(getOkCallback(), users)));
	}

	@Override
	public Model instantiateDialogModel(@Nullable SectionInfo info)
	{
		return new Model();
	}

	public interface CurrentUsersCallback
	{
		List<SelectedUser> getCurrentSelectedUsers(SectionInfo info);
	}

	public void setUsersCallback(CurrentUsersCallback usersCallback)
	{
		currentUsersCallback = usersCallback;
	}

	@SuppressWarnings({"unchecked", "deprecation"})
	public static List<SelectedUser> usersFromJsonString(String usersJson)
	{
		return JSONArray.toList(JSONArray.fromObject(usersJson), SelectedUser.class);
	}

	@Nullable
	public static SelectedUser userFromJsonString(String usersJson)
	{
		final List<SelectedUser> users = usersFromJsonString(usersJson);
		if( Check.isEmpty(users) )
		{
			return null;
		}
		return users.get(0);
	}

	@Override
	protected Label getOkLabel()
	{
		final boolean multiple = section.isMultipleUsers();
		final String okeyDokey = (multiple ? "com.tle.web.sections.equella.utils.selectuserdialog.selecttheseusers"
			: "com.tle.web.sections.equella.utils.selectuserdialog.selectthisuser");

		return new KeyLabel(okeyDokey);
	}

	public void setMultipleUsers(boolean b)
	{
		section.setMultipleUsers(b);
	}

	public void setGroupFilter(Set<String> filter)
	{
		section.setGroupFilter(filter);
	}

	public static class Model extends DialogModel
	{
		private SectionRenderable innerContents;

		public SectionRenderable getInnerContents()
		{
			return innerContents;
		}

		public void setInnerContents(SectionRenderable innerContents)
		{
			this.innerContents = innerContents;
		}
	}

	public void setTitle(Label title)
	{
		this.title = title;
	}

	public void setPrompt(Label prompt)
	{
		section.setPrompt(prompt);
	}
}
