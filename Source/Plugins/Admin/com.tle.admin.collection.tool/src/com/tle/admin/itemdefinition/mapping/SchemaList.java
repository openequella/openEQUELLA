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

package com.tle.admin.itemdefinition.mapping;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.ComponentHelper;
import com.dytech.gui.TableLayout;
import com.tle.admin.schema.SchemaModel;
import com.tle.admin.schema.SchemaNode;
import com.tle.admin.schema.SingleTargetChooser;
import com.tle.admin.schema.TargetListener;
import com.tle.beans.entity.itemdef.MetadataMapping;
import com.tle.beans.entity.itemdef.mapping.Literal;
import com.tle.beans.entity.itemdef.mapping.LiteralMapping;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Charles O'Farrell
 */
public class SchemaList extends JPanel implements ListSelectionListener, ActionListener
{
	private static final long serialVersionUID = 1L;

	private static final Log LOGGER = LogFactory.getLog(SchemaList.class);

	private SchemaModel schema;
	private JList list;
	private DefaultListModel model;
	private JButton add;
	private JButton edit;
	private JButton remove;

	public SchemaList(SchemaModel schema)
	{
		this.schema = schema;
		createGUI();
	}

	private void createGUI()
	{
		JLabel title = new JLabel(CurrentLocale.get("com.tle.admin.itemdefinition.mapping.schemalist.title")); //$NON-NLS-1$

		model = new DefaultListModel();
		list = new JList(model);

		list.addListSelectionListener(this);

		JScrollPane scroller = new JScrollPane(list);

		add = new JButton(CurrentLocale.get("com.tle.admin.add")); //$NON-NLS-1$
		edit = new JButton(CurrentLocale.get("com.tle.admin.edit")); //$NON-NLS-1$
		remove = new JButton(CurrentLocale.get("com.tle.admin.remove")); //$NON-NLS-1$

		add.addActionListener(this);
		edit.addActionListener(this);
		remove.addActionListener(this);

		final int height1 = title.getPreferredSize().height;
		final int height2 = add.getPreferredSize().height;
		final int width1 = remove.getPreferredSize().width;

		final int[] rows = {height1, TableLayout.FILL, height2,};
		final int[] cols = {width1, width1, width1,};

		setLayout(new TableLayout(rows, cols));
		add(title, new Rectangle(0, 0, 3, 1));
		add(scroller, new Rectangle(0, 1, 3, 1));
		add(add, new Rectangle(0, 2, 1, 1));
		add(edit, new Rectangle(1, 2, 1, 1));
		add(remove, new Rectangle(2, 2, 1, 1));

		updateButtons();
	}

	public void setChangeDetector(ChangeDetector detector)
	{
		detector.watch(model);
	}

	public void save(MetadataMapping mapping)
	{
		Collection<LiteralMapping> col = mapping.getLiteralMapping();
		col.clear();
		Enumeration<?> enumeration = model.elements();
		while( enumeration.hasMoreElements() )
		{
			ScriptedTarget target = (ScriptedTarget) enumeration.nextElement();
			if( !target.getRules().isEmpty() )
			{
				LiteralMapping targetXml = new LiteralMapping();
				targetXml.setValue(target.getTarget().toString());
				getLiteralsXml(targetXml, target);
				col.add(targetXml);
			}
		}
	}

	private void getLiteralsXml(LiteralMapping xml, ScriptedTarget target)
	{
		for( ScriptedRule rule : target.getRules() )
		{
			if( rule.getLiteral() != null )
			{
				Literal literal = new Literal();
				literal.setValue(rule.getLiteral());
				if( rule.getScript() != null )
				{
					literal.setScript(rule.getScript());
				}
				xml.getLiterals().add(literal);
			}
		}
	}

	public void load(MetadataMapping mapping)
	{
		if( mapping != null )
		{
			for( LiteralMapping targetXml : mapping.getLiteralMapping() )
			{
				SchemaNode node = schema.getNode(targetXml.getValue());
				if( node != null )
				{
					List<ScriptedRule> literals = new ArrayList<ScriptedRule>();

					ScriptedTarget target = new ScriptedTarget();
					target.setTarget(node);
					target.setRules(literals);

					loadLiterals(targetXml, target);

					model.addElement(target);
				}
				else
				{
					LOGGER.warn("Schema path could not be found: " + targetXml.getValue()); //$NON-NLS-1$
				}
			}
		}
		list.clearSelection();
	}

	private void loadLiterals(LiteralMapping xml, ScriptedTarget target)
	{
		for( Literal literal : xml.getLiterals() )
		{
			ScriptedRule rule = new ScriptedRule();
			rule.setLiteral(literal.getValue());
			rule.setScript(literal.getScript());

			target.getRules().add(rule);
		}
	}

	/**
	 * @param listener
	 */
	public void addListSelectionListener(ListSelectionListener listener)
	{
		list.addListSelectionListener(listener);
	}

	/**
	 * @param listener
	 */
	public void removeListSelectionListener(ListSelectionListener listener)
	{
		list.removeListSelectionListener(listener);
	}

	/**
	 * @return The currently selected ScriptedTarget, else null.
	 */
	public ScriptedTarget getSelection()
	{
		int index = list.getSelectedIndex();
		if( index >= 0 )
		{
			return (ScriptedTarget) model.get(index);
		}
		else
		{
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(
	 * javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		if( e.getSource() == list )
		{
			updateButtons();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == add )
		{
			onAdd();
		}
		else if( e.getSource() == remove )
		{
			onRemove();
		}
		else if( e.getSource() == edit )
		{
			onEdit();
		}
	}

	/**
	 * Updates the state of the buttons to ensure that only the correct
	 * operations for the given state can occur.
	 */
	private void updateButtons()
	{
		int index = list.getSelectedIndex();
		boolean selection = index >= 0;

		edit.setEnabled(selection);
		remove.setEnabled(selection);
	}

	/**
	 * Invoke this to add a new schema node
	 */
	private void onAdd()
	{
		SchemaPicker picker = new SchemaPicker();
		SchemaNode node = picker.showSchemaPicker(this);
		if( node != null )
		{
			ScriptedTarget target = new ScriptedTarget();
			target.setTarget(node);
			model.addElement(target);
			list.setSelectedIndex(model.getSize() - 1);
		}
	}

	/**
	 * Invoke this to edit a schema node
	 */
	private void onEdit()
	{
		int index = list.getSelectedIndex();
		if( index >= 0 )
		{
			ScriptedTarget target = (ScriptedTarget) model.get(index);

			SchemaPicker picker = new SchemaPicker();
			SchemaNode node = picker.showSchemaPicker(this, target.getTarget().getXmlPath());
			if( node != null )
			{
				target.setTarget(node);
				model.set(index, target);

				list.clearSelection();
				list.setSelectedIndex(index);
			}
		}
	}

	/**
	 * Invoke this to remove a schema node
	 */
	private void onRemove()
	{
		int index = list.getSelectedIndex();
		if( index >= 0 )
		{
			ScriptedTarget target = (ScriptedTarget) model.get(index);
			int result = JOptionPane.showConfirmDialog(this,
				CurrentLocale.get("com.tle.admin.itemdefinition.mapping.schemalist.confirm", target.toString()), //$NON-NLS-1$
				"Remove?", JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
			if( result == JOptionPane.YES_OPTION )
			{
				list.clearSelection();
				model.remove(index);
				target.destroy();
			}
		}
	}

	private class SchemaPicker implements TargetListener, ActionListener
	{
		private static final int DIALOG_WIDTH = 300;

		private JDialog dialog;
		private JPanel content;
		private SingleTargetChooser chooser;
		private JLabel alreadyChosen;
		private JButton ok;
		private JButton cancel;
		private SchemaNode result;

		public SchemaPicker()
		{
			setupGui();
		}

		public SchemaNode showSchemaPicker(Component parent)
		{
			return showSchemaPicker(parent, ""); //$NON-NLS-1$
		}

		public SchemaNode showSchemaPicker(Component parent, String target)
		{
			chooser.setTarget(target);

			dialog = ComponentHelper.createJDialog(parent);
			dialog.setModal(true);
			dialog.setTitle(CurrentLocale.get("com.tle.admin.itemdefinition.mapping.schemalist.picker")); //$NON-NLS-1$
			dialog.getContentPane().add(content);

			dialog.pack();
			dialog.setSize(DIALOG_WIDTH, dialog.getSize().height);
			ComponentHelper.centreOnScreen(dialog);

			dialog.setVisible(true);

			return result;
		}

		private void setupGui()
		{
			JLabel instruction = new JLabel(CurrentLocale.get("com.tle.admin.itemdefinition.mapping.schemalist.choose")); //$NON-NLS-1$

			alreadyChosen = new JLabel(CurrentLocale.get("com.tle.admin.itemdefinition.mapping.schemalist.selected")); //$NON-NLS-1$
			alreadyChosen.setForeground(Color.RED);

			chooser = new SingleTargetChooser(schema, null);
			chooser.addTargetListener(this);

			ok = new JButton(CurrentLocale.get("com.tle.admin.ok")); //$NON-NLS-1$
			cancel = new JButton(CurrentLocale.get("com.tle.admin.cancel")); //$NON-NLS-1$

			ok.addActionListener(this);
			cancel.addActionListener(this);

			final int height1 = instruction.getPreferredSize().height;
			final int height2 = chooser.getPreferredSize().height;
			final int height3 = cancel.getPreferredSize().height;
			final int width1 = cancel.getPreferredSize().width;

			final int[] rows = {height1, height2, height1, height3,};
			final int[] cols = {TableLayout.FILL, width1, width1,};

			content = new JPanel(new TableLayout(rows, cols));
			content.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			content.add(instruction, new Rectangle(0, 0, 3, 1));
			content.add(chooser, new Rectangle(0, 1, 3, 1));
			content.add(alreadyChosen, new Rectangle(0, 2, 3, 1));
			content.add(ok, new Rectangle(1, 3, 1, 1));
			content.add(cancel, new Rectangle(2, 3, 1, 1));

			updateGui(chooser.getTarget());
		}

		private void updateGui(String target)
		{
			boolean empty = true;
			boolean unique = true;

			if( target != null )
			{
				empty = false;

				Enumeration<?> enumeration = model.elements();
				while( unique && enumeration.hasMoreElements() )
				{
					ScriptedTarget element = (ScriptedTarget) enumeration.nextElement();
					if( element.getTarget().getXmlPath().equals(target) )
					{
						unique = false;
					}
				}
			}

			ok.setEnabled(!empty && unique);
			alreadyChosen.setVisible(!empty && !unique);
		}

		/*
		 * (non-Javadoc)
		 * @see com.dytech.edge.admin.schema.TargetListener#targetAdded(
		 * com.dytech.edge.admin.schema.SchemaNode)
		 */
		@Override
		public void targetAdded(String target)
		{
			updateGui(target);
		}

		/*
		 * (non-Javadoc)
		 * @see com.dytech.edge.admin.schema.TargetListener#targetRemoved(
		 * com.dytech.edge.admin.schema.SchemaNode)
		 */
		@Override
		public void targetRemoved(String target)
		{
			// Don't care about this event.
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if( e.getSource() == ok )
			{
				result = chooser.getSchemaNode();
			}
			else if( e.getSource() == cancel )
			{
				result = null;
			}
			dialog.dispose();
		}
	}
}
