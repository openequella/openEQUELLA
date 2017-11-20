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

package com.tle.admin.workflow;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import com.tle.beans.entity.LanguageBundle;
import com.tle.common.Pair;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.workflow.node.DecisionNode;
import com.tle.common.workflow.node.ParallelNode;
import com.tle.common.workflow.node.ScriptNode;
import com.tle.common.workflow.node.SerialNode;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;

public class WorkflowCellRenderer implements ListCellRenderer, TreeCellRenderer
{
	private Map<Class<? extends WorkflowNode>, Pair<String, Icon>> nameAndIcon = new HashMap<Class<? extends WorkflowNode>, Pair<String, Icon>>();

	private static final String DECISION_ICON = "/icons/decision.gif";
	private static final String PARALLEL_ICON = "/icons/parallel.gif";
	private static final String SERIAL_ICON = "/icons/serial.gif";
	private static final String ITEM_ICON = "/icons/workflowitem.gif";
	private static final String SCRIPT_ICON = "/icons/script.gif";

	private DefaultTreeCellRenderer treer;
	private DefaultListCellRenderer listr;
	private String defaultString;

	public WorkflowCellRenderer()
	{
		treer = new DefaultTreeCellRenderer();
		listr = new DefaultListCellRenderer();

		add(DecisionNode.class, "descision", DECISION_ICON);
		add(ParallelNode.class, "parallel", PARALLEL_ICON);
		add(SerialNode.class, "serial", SERIAL_ICON);
		add(WorkflowItem.class, "workflow", ITEM_ICON);
		add(ScriptNode.class, "script", SCRIPT_ICON);
		defaultString = CurrentLocale.get("com.tle.admin.workflow.tree.workflowcellrenderer.unnamed");
	}

	private void add(Class<? extends WorkflowNode> klass, String key, String iconPath)
	{
		String name = CurrentLocale.get("com.tle.admin.workflow.stepdialog." + key);
		Icon icon = new ImageIcon(WorkflowCellRenderer.class.getResource(iconPath));
		nameAndIcon.put(klass, new Pair<String, Icon>(name, icon));
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
		boolean leaf, int row, boolean hasFocus1)
	{
		JLabel label = (JLabel) treer.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus1);

		setup(label, value);
		return label;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
		boolean cellHasFocus)
	{
		JLabel label = (JLabel) listr.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		setup(label, value);
		return label;
	}

	@SuppressWarnings({"unchecked", "cast"})
	private void setup(JLabel label, Object value)
	{
		Class<? extends WorkflowNode> klass = null;
		LanguageBundle name = null;

		if( value instanceof Class )
		{
			klass = (Class<? extends WorkflowNode>) value;
		}
		else
		{
			WorkflowNode node = (WorkflowNode) value;
			klass = node.getClass();
			name = node.getName();
		}

		Pair<String, Icon> defaults = nameAndIcon.get(klass);

		if( name != null )
		{
			label.setText(CurrentLocale.get(name, defaultString));
		}
		else
		{
			label.setText(defaults.getFirst());
		}

		label.setIcon(defaults.getSecond());
	}
}
