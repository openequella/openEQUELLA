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

package com.dytech.edge.admin.script.basicmodel;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTextField;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.admin.script.basicmodel.ScriptTargetDialog.SelectionType;
import com.dytech.edge.admin.script.options.ScriptOptions;
import com.tle.admin.helper.GeneralDialog;
import com.tle.admin.schema.SchemaModel;
import com.tle.admin.schema.SchemaNode;
import com.tle.admin.schema.TargetChooser;
import com.tle.common.i18n.CurrentLocale;

public class ScriptTargetChooser extends TargetChooser
{
	private static final long serialVersionUID = 1L;

	private final ScriptOptions options;
	private ScriptTargetDialog dialog;
	private JTextField target;
	private SchemaNode node;
	private SelectionType type;

	public ScriptTargetChooser(SchemaModel model, ScriptOptions options)
	{
		super(model, null);
		this.options = options;

		createGUI();
	}

	public SelectionType getType()
	{
		return type;
	}

	public String getSchemaXpath()
	{
		if( node != null )
		{
			return node.getXmlPath();
		}
		else
		{
			return ""; //$NON-NLS-1$
		}
	}

	public SchemaNode getSchemaNode()
	{
		return node;
	}

	protected ScriptOptions getScriptOptionModel()
	{
		return options;
	}

	public void loadSchema(PropBagEx schema)
	{
		getSchemaModel().loadSchema(schema);
	}

	private void createGUI()
	{
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		setLayout(gridbag);
		setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

		target = new JTextField();
		target.setEditable(false);
		target.setMinimumSize(new Dimension(150, 20));
		target.setPreferredSize(new Dimension(150, 20));
		target.setMaximumSize(new Dimension(150, 20));
		c.gridx = 0;
		c.weightx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(target, c);
		add(target);

		JButton search = new JButton("..."); //$NON-NLS-1$
		search.setFont(new Font("Sans Serif", Font.PLAIN, 8)); //$NON-NLS-1$
		search.addActionListener(new SearchHandler());
		search.setMinimumSize(new Dimension(18, 20));
		search.setPreferredSize(new Dimension(18, 20));
		search.setMaximumSize(new Dimension(18, 20));
		c.gridx = 1;
		c.weightx = 0;
		gridbag.setConstraints(search, c);
		add(search);
	}

	public void setType(SelectionType newType, Object... extras)
	{
		type = newType;
		if( type == null )
		{
			target.setText(""); //$NON-NLS-1$
			return;
		}

		switch( type )
		{
			case ITEM_STATUS:
				target.setText(CurrentLocale.get("com.dytech.edge.admin.script.target.status")); //$NON-NLS-1$
				break;

			case MODERATION:
				target.setText(CurrentLocale.get("com.dytech.edge.admin.script.target.moderator")); //$NON-NLS-1$
				break;

			case USER_TYPE:
				target.setText(CurrentLocale.get("com.dytech.edge.admin.script.target.role")); //$NON-NLS-1$
				break;

			case WORKFLOW_STEP:
				target.setText(CurrentLocale.get("com.dytech.edge.admin.script.target.workflow")); //$NON-NLS-1$
				break;

			case SCHEMA_ITEM:
				SchemaNode schemaNode = null;
				if( extras[0] instanceof SchemaNode )
				{
					schemaNode = (SchemaNode) extras[0];
				}
				else if( extras[0] instanceof String )
				{
					schemaNode = getSchemaModel().getNode((String) extras[0]);
				}
				else
				{
					throw new IllegalArgumentException("Requires extra parameter of type SchemaNode or String");
				}

				this.node = schemaNode;
				target.setText(schemaNode.getXmlPath());
				break;

			default:
				target.setText("");
		}
	}

	protected class SearchHandler implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if( dialog == null )
			{
				dialog = new ScriptTargetDialog(ScriptTargetChooser.this, getSchemaModel(), getScriptOptionModel());
			}

			dialog.showDialog();
			if( dialog.getResponse() == GeneralDialog.OK_RESPONSE )
			{
				SelectionType t = dialog.getType();
				if( t != null )
				{
					Object[] extras = null;
					if( t == SelectionType.SCHEMA_ITEM )
					{
						extras = new Object[]{dialog.getValue()};
					}
					setType(t, extras);
					fireTargedAdded(null);
				}
			}
		}
	}
}
