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

package com.tle.admin.search.searchset.scripting;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTextField;

import com.dytech.devlib.PropBagEx;
import com.tle.admin.helper.GeneralDialog;
import com.tle.admin.schema.SchemaModel;
import com.tle.admin.schema.SchemaNode;
import com.tle.admin.schema.SchemaTree;
import com.tle.admin.schema.TargetChooser;
import com.tle.admin.schema.WhereTargetDialog;

public class WhereTargetChooser extends TargetChooser implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private WhereTargetDialog dialog;
	private SchemaNode node;
	private JTextField target;
	private JButton search;

	public WhereTargetChooser(SchemaModel model)
	{
		super(model, null);
		createGUI();
	}

	public String getSchemaItem()
	{
		return node.getXmlPath();
	}

	public void loadSchema(PropBagEx schema)
	{
		getSchemaModel().loadSchema(schema);
	}

	public void setSchemaItem(String path)
	{
		setSchemaItem(getSchemaModel().getNode(path));
	}

	public void setSchemaItem(SchemaNode node)
	{
		this.node = node;
		if( node == null )
		{
			target.setText("");
		}
		else
		{
			target.setText(node.getXmlPath());
		}
	}

	protected void createGUI()
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

		search = new JButton("...");
		search.setFont(new Font("Sans Serif", Font.PLAIN, 8));
		search.addActionListener(this);
		search.setMinimumSize(new Dimension(18, 20));
		search.setPreferredSize(new Dimension(18, 20));
		search.setMaximumSize(new Dimension(18, 20));

		c.gridx = 1;
		c.weightx = 0;
		gridbag.setConstraints(search, c);
		add(search);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == search )
		{
			if( dialog == null )
			{
				dialog = new WhereTargetDialog(this, new SchemaTree(getSchemaModel(), true), true);
				dialog.setNonLeafSelection(true);
			}
			dialog.showDialog();

			if( dialog.getResponse() == GeneralDialog.OK_RESPONSE )
			{
				SchemaNode newNode = (SchemaNode) dialog.getValue();

				if( node != null )
				{
					fireTargedRemoved(node.getXmlPath());
				}

				node = newNode;
				if( node == null )
				{
					target.setText("");
				}
				else
				{
					String xpath = node.getXmlPath();
					target.setText(xpath);
					fireTargedAdded(xpath);
				}
			}
		}
	}
}
