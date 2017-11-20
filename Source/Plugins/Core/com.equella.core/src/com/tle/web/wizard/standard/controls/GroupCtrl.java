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

package com.tle.web.wizard.standard.controls;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tle.core.guice.Bind;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.jquery.JQueryStatement;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.NotExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.AnonymousFunction;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.AssignStatement;
import com.tle.web.sections.js.generic.statement.DeclarationStatement;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.MappedBooleans;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.toggle.CheckboxRenderer;
import com.tle.web.wizard.WebWizardPage;
import com.tle.web.wizard.controls.CGroupCtrl;
import com.tle.web.wizard.controls.GroupsCtrl.ControlGroup;
import com.tle.web.wizard.controls.Item;
import com.tle.web.wizard.controls.WebControl;
import com.tle.web.wizard.controls.WebControlModel;
import com.tle.web.wizard.page.ControlResult;

/*
 * @author jmaginnis
 */
@Bind
public class GroupCtrl extends GroupWebControl<WebControlModel>
{
	private CGroupCtrl cgroup;
	@Component(stateful = false)
	private MappedBooleans selected;
	private final List<GroupRenderedGroup> renderedGroups = new ArrayList<GroupRenderedGroup>();

	@Override
	public Class<WebControlModel> getModelClass()
	{
		return WebControlModel.class;
	}

	@Override
	public void setWrappedControl(HTMLControl control)
	{
		cgroup = (CGroupCtrl) control;
		super.setWrappedControl(control);
	}

	@Override
	public void doEdits(SectionInfo info)
	{
		Set<String> checked = selected.getCheckedSet(info);
		setValues(checked.toArray(new String[checked.size()]));
		int i = 0;
		for( List<WebControl> controls : getWebGroups() )
		{
			Item oItem = cgroup.getItem(i);

			// only process the values if not disabled
			if( oItem.isSelected() )
			{
				processGroup(controls, info);
			}
			i++;
		}
	}

	@SuppressWarnings("nls")
	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		Set<String> checked = new HashSet<String>();
		List<Item> items = cgroup.getItems();
		for( Item item : items )
		{
			if( item.isSelected() )
			{
				checked.add(item.getValue());
			}
		}
		selected.setCheckedSet(context, checked);

		final ScriptVariable disVar = new ScriptVariable("dis");
		final StatementBlock disables = new StatementBlock();

		SimpleFunction onClick = new SimpleFunction("click" + getFormName(), StatementBlock.get(
			new DeclarationStatement(disVar), disables));

		WebWizardPage webWizardPage = getWebWizardPage();
		renderedGroups.clear();
		int index = 0;
		for( List<WebControl> controls : getWebGroups() )
		{
			final Item item = cgroup.getItems().get(index);
			HtmlBooleanState checkState = selected.getBooleanState(context, item.getValue());
			if( isEnabled() )
			{
				checkState.addReadyStatements(new JQueryStatement(checkState, new FunctionCallExpression("click",
					new AnonymousFunction(new FunctionCallStatement(onClick)))));
			}
			final CheckboxRenderer check = new CheckboxRenderer(checkState, cgroup.getCheckType());
			check.setNestedRenderable(new LabelRenderer(new TextLabel(item.getName())));

			// set disVar based on state of checkbox
			disables.addStatements(new AssignStatement(disVar, new NotExpression(check.createGetExpression())));
			final List<ControlResult> results = webWizardPage.renderChildren(context, controls, getSectionId() + "_"
				+ index, !checkState.isChecked());
			for( WebControl webControl : controls )
			{
				disables.addStatements(new FunctionCallStatement(webControl.getDisabler(context)
					.createDisableFunction(), disVar));
			}

			renderedGroups.add(new GroupRenderedGroup(results, check));
			index++;

		}
		TagState containerState = new TagState();
		for( GroupRenderedGroup gp : renderedGroups )
		{
			addDisabler(context, gp.check);
		}

		getModel(context).setDivContainer(new DivRenderer(containerState));
		addDisableablesForControls(context);
		return viewFactory.createResult("group.ftl", context);
	}

	@Override
	public void addNewGroup(ControlGroup group)
	{
		super.addNewGroup(group);

		List<List<WebControl>> webGroups = getWebGroups();
		List<WebControl> newGroup = webGroups.get(webGroups.size() - 1);
		for( WebControl g : newGroup )
		{
			g.setNested(true);
		}
	}

	public static final class GroupRenderedGroup extends RenderedGroup
	{
		private final CheckboxRenderer check;

		public GroupRenderedGroup(List<ControlResult> resultList, CheckboxRenderer check)
		{
			super(resultList);
			this.check = check;
		}

		public CheckboxRenderer getCheck()
		{
			return check;
		}
	}

	public List<GroupRenderedGroup> getRenderedGroups()
	{
		return renderedGroups;
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return null;
	}
}
