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

package com.tle.web.wizard.standard.controls.js;

import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.jquery.libraries.JQueryDraggable;
import com.tle.web.sections.jquery.libraries.JQueryDroppable;
import com.tle.web.sections.jquery.libraries.JQueryEquellaUtils;
import com.tle.web.sections.jquery.libraries.JQueryFancyBox;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.expression.CombinedExpression;
import com.tle.web.sections.js.generic.expression.ConstructorCallExpression;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.expression.PropertyExpression;
import com.tle.web.sections.js.generic.expression.ScriptExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.statement.AssignStatement;
import com.tle.web.sections.js.generic.statement.DeclarationStatement;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.TextField;
import com.tle.web.viewitem.treeviewer.js.TreeLibrary;

@SuppressWarnings("nls")
public final class TreeNavControlLibrary
{
	private static final IncludeFile INCLUDE = new IncludeFile(ResourcesService.getResourceHelper(
		TreeNavControlLibrary.class).url("scripts/treenavctl.js"), JQueryEquellaUtils.PRERENDER,
		JQueryDraggable.PRERENDER, JQueryDroppable.PRERENDER, TreeLibrary.INCLUDE, JQueryFancyBox.PRERENDER);
	private static final JSCallable SETUP_TREE_FUNC = new ExternallyDefinedFunction("setupTree", INCLUDE);
	private static final JSCallable SUBMIT_TREE_FUNC = new ExternallyDefinedFunction("submitTree", INCLUDE);
	private static final JSCallable TREE_NAV_CONSTRUCTOR = new ExternallyDefinedFunction("TreeNavControl", INCLUDE);

	public static final ScriptVariable THE_TREE = new ScriptVariable("tctl");

	public static final JSCallable CREATE_NODE = new ExternallyDefinedFunction("createTreeNode", INCLUDE);
	public static final JSCallable REMOVE_NODE = new ExternallyDefinedFunction("removeTreeNode", INCLUDE);
	public static final JSCallable MOVE_NODE_UP = new ExternallyDefinedFunction("moveTreeNodeUp", INCLUDE);
	public static final JSCallable MOVE_NODE_DOWN = new ExternallyDefinedFunction("moveTreeNodeDown", INCLUDE);

	/**
	 * @param info
	 * @param treeDef get from
	 *            JSONArray.fromObject(AbstractTreeViewerSection.addChildNodes(
	 *            etc...))
	 */
	public static void setupTree(RenderContext info, String treeDef, boolean disabled, TextField nodeDisplayName,
		Button tabAddButton, TextField popupTabName, Button popupSaveButton, Button popupCancelButton,
		ObjectExpression callbacks, ObjectExpression controls, Label tabRemoveConfirmMessage)
	{
		info.getPreRenderContext().addStatements(new DeclarationStatement(THE_TREE));

		info.getBody().addEventStatements(JSHandler.EVENT_PRESUBMIT,
			new FunctionCallStatement(SUBMIT_TREE_FUNC, THE_TREE));
		JQueryCore.appendReady(
			info,
			getTreeCode(treeDef, disabled, sel(nodeDisplayName), sel(tabAddButton), sel(popupTabName),
				sel(popupSaveButton), sel(popupCancelButton), callbacks, controls, tabRemoveConfirmMessage));
	}

	private static JSExpression sel(final ElementId comp)
	{
		return new JQuerySelector(comp);
	}

	private static JSStatements getTreeCode(String treeDef, boolean disabled, JSExpression tabDisplayName,
		JSExpression tabAddButtonExpr, JSExpression popupTabName, JSExpression popupSaveButtonExpr,
		JSExpression popupCancelButtonExpr, ObjectExpression callbacks, ObjectExpression controls,
		Label tabRemoveConfirmMessage)
	{
		final StatementBlock s = new StatementBlock();
		s.addStatements(new AssignStatement(THE_TREE, new ConstructorCallExpression(TREE_NAV_CONSTRUCTOR,
			tabRemoveConfirmMessage, tabDisplayName, tabAddButtonExpr, popupTabName, popupSaveButtonExpr,
			popupCancelButtonExpr)));
		s.addStatements(new AssignStatement(new CombinedExpression(THE_TREE, new PropertyExpression("disabled")),
			disabled));
		s.addStatements(new FunctionCallStatement(SETUP_TREE_FUNC, THE_TREE, new ScriptExpression(treeDef), callbacks,
			controls));
		return s;
	}

	private TreeNavControlLibrary()
	{
		throw new Error();
	}
}
