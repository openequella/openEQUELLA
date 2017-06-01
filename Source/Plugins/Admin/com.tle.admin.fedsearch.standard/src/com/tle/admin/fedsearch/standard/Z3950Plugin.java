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

package com.tle.admin.fedsearch.standard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagIterator;
import com.dytech.edge.admin.wizard.TripleShuffleList;
import com.dytech.edge.wizard.beans.control.WizardControlItem;
import com.dytech.gui.JNumberTextField;
import com.dytech.gui.TableLayout;
import com.tle.admin.fedsearch.SearchPlugin;
import com.tle.admin.gui.EditorException;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.beans.search.Z3950Settings;
import com.tle.beans.search.Z3950Settings.AttributeProfile;
import com.tle.beans.search.Z3950Settings.RecordFormat;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.core.remoting.RemoteZ3950Service;

public class Z3950Plugin extends SearchPlugin<Z3950Settings>
{
	private static final int DEFAULT_PORT = 210;

	private JTextField hostField;
	private JTextField databaseField;
	private JTextField usernameField;
	private JPasswordField passwordField;
	private SpinnerNumberModel portSpinnerModel;

	// private JEntityFileUpload xsltFile;
	private JCheckBox advancedBox;

	// private JComboBox displayRecordSchema;
	private JComboBox<NameValue> importRecordSchema;

	private JButton defaultOpener;
	private Z3950AttributesEditor standardAttributes;
	private SearchTripleShuffleList advancedSearchFields;

	protected Map<NameValue, List<String>> transformsPerCollection = new HashMap<NameValue, List<String>>();

	public Z3950Plugin()
	{
		super(Z3950Settings.class);
	}

	@Override
	public void initGUI()
	{
		hostField = new JTextField();
		databaseField = new JTextField();
		usernameField = new JTextField();
		passwordField = new JPasswordField();

		portSpinnerModel = new SpinnerNumberModel(DEFAULT_PORT, 1, 32765, 1);
		JSpinner portField = new JSpinner(portSpinnerModel);
		importRecordSchema = new JComboBox<NameValue>();
		populateRecordSchema(importRecordSchema);

		defaultOpener = new JButton(s("button.select"));
		defaultOpener.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if( e.getSource() == defaultOpener )
				{
					DefaultsDialog dialog = new DefaultsDialog();
					JOptionPane.showMessageDialog(panel, dialog, s("dialog.title"), JOptionPane.QUESTION_MESSAGE);
					loadDefaultAttributes(AttributeProfile.valueOf(dialog.getSelectedDefault()));
				}
			}
		});

		standardAttributes = new Z3950AttributesEditor();

		advancedBox = new JCheckBox(); //$NON-NLS-1$
		advancedSearchFields = new SearchTripleShuffleList(s("fields.name"), s("fields.attributes"));

		panel.add(new JLabel(s("host")));
		panel.add(hostField);
		panel.add(new JLabel(s("port")));
		panel.add(portField);
		panel.add(new JLabel(s("database")));
		panel.add(databaseField);
		panel.add(new JLabel(s("username")));
		panel.add(usernameField);
		panel.add(new JLabel(s("password")));
		panel.add(passwordField);
		panel.add(new JLabel(s("importRecordSchema")));
		panel.add(importRecordSchema);
		panel.add(new JSeparator(), "span 2, gapbottom 20");
		panel.add(new JLabel(s("default")));
		panel.add(defaultOpener, "width 75!");
		panel.add(new JLabel(s("attributes.standard")));
		panel.add(standardAttributes);
		panel.add(new JSeparator(), "span 2, gapbottom 20");
		panel.add(new JLabel(s("advanced")));
		panel.add(advancedBox);
		panel.add(new JLabel(s("fields")));
		panel.add(advancedSearchFields);
	}

	private void loadDefaultAttributes(AttributeProfile profile)
	{
		if( profile == null )
		{
			profile = AttributeProfile.EQUELLA;
		}
		RemoteZ3950Service z3950Service = getClientService().getService(RemoteZ3950Service.class);
		advancedSearchFields.clear();
		standardAttributes.setAttributes(1016, 3, 3, 2, 100, 1);
		for( NameValue fields : z3950Service.listDefaultFields(profile) )
		{
			LanguageBundle bundle = new LanguageBundle();
			LangUtils.createLanguageString(bundle, CurrentLocale.getLocale(), fields.getFirst());
			advancedSearchFields.addItem(new WizardControlItem(bundle, fields.getSecond()));
		}

	}

	private static String s(String keyPart)
	{
		return CurrentLocale.get("com.tle.admin.fedsearch.standard.z3950plugin." + keyPart); //$NON-NLS-1$
	}

	private void populateRecordSchema(JComboBox<NameValue> schemaCombo)
	{
		for( RecordFormat format : RecordFormat.values() )
		{
			String uri = format.getUri();
			String name = format.getName() + " - " + uri; //$NON-NLS-1$
			schemaCombo.addItem(new NameValue(name, uri));
		}
	}

	@Override
	public void load(final Z3950Settings settings)
	{
		hostField.setText(settings.getHost());
		databaseField.setText(settings.getDatabase());
		portSpinnerModel.setValue(settings.getPort());
		advancedBox.setSelected(settings.isAdvanced());
		usernameField.setText(settings.getUsername());
		passwordField.setText(settings.getPassword());
		selectRecordSchema(importRecordSchema, settings.getImportRecordSchema());
		if( !Check.isEmpty(settings.getStandardAttributes()) )
		{
			setAttributeField(standardAttributes, settings.getStandardAttributes());
			if( !Check.isEmpty(settings.getAdvancedSearchFields()) )
			{
				PropBagEx fieldsXml = new PropBagEx(settings.getAdvancedSearchFields());
				PropBagIterator entryIterator = fieldsXml.iterator("entry");
				while( entryIterator.hasNext() )
				{
					PropBagEx entryXml = entryIterator.next();
					advancedSearchFields.addItem(new WizardControlItem(LangUtils.getBundleFromXml(entryXml
						.getSubtree("name")), entryXml.getNode("value")));
				}
			}
		}
		else
		{
			loadDefaultAttributes(AttributeProfile.EQUELLA);
		}
	}

	@Override
	public void validation() throws EditorException
	{
		super.validation();
		if( !standardAttributes.attributesValid() )
		{
			throw new EditorException(s("error.standard"));
		}
		if( advancedBox.isSelected() && Check.isEmpty(advancedSearchFields.getItems()) )
		{
			throw new EditorException(s("error.nofields"));
		}
	}

	private void selectRecordSchema(JComboBox<NameValue> schemaCombo, String schemaUri)
	{
		AppletGuiUtils.selectInJCombo(schemaCombo, new NameValue(null, schemaUri));
	}

	private void setAttributeField(Z3950AttributesEditor editor, String attributes)
	{
		if( !Check.isEmpty(attributes) )
		{
			String[] atts = attributes.split("\\.");
			editor.setAttributes(Integer.parseInt(atts[0]), Integer.parseInt(atts[1]), Integer.parseInt(atts[2]),
				Integer.parseInt(atts[3]), Integer.parseInt(atts[4]), Integer.parseInt(atts[5]));
		}
	}

	@Override
	public void save(Z3950Settings settings)
	{
		settings.setHost(hostField.getText());
		settings.setDatabase(databaseField.getText());
		settings.setPort(portSpinnerModel.getNumber().intValue());
		settings.setAdvanced(advancedBox.isSelected());
		settings.setUsername(usernameField.getText());
		settings.setPassword(new String(passwordField.getPassword()));
		settings.setStandardAttributes(standardAttributes.getText());
		settings.setImportRecordSchema(((NameValue) importRecordSchema.getSelectedItem()).getValue());
		PropBagEx fieldsXml = new PropBagEx();
		for( WizardControlItem field : advancedSearchFields.getItems() )
		{
			PropBagEx node = fieldsXml.newSubtree("entry");
			node.createNode("value", field.getValue());
			node.appendChildren("/name", new PropBagEx(LangUtils.getBundleAsXmlString(field.getName())));
		}
		settings.setAdvancedSearchFields(fieldsXml.toString());
	}

	private class Z3950AttributesEditor extends JPanel implements KeyListener
	{
		private static final long serialVersionUID = 1L;

		private JNumberTextField use;
		private JNumberTextField relation;
		private JNumberTextField position;
		private JNumberTextField structure;
		private JNumberTextField truncation;
		private JNumberTextField completeness;

		public Z3950AttributesEditor()
		{
			JLabel dot = new JLabel(".");
			// http://www.loc.gov/z3950/agency/defns/bib1.html
			use = new JNumberTextField(10000);// 1-63..1k-1.225k..5k-10k
			relation = new JNumberTextField(150);// 1-6..100-104
			position = new JNumberTextField(5);// 1-3
			structure = new JNumberTextField(150);// 1-6..100-108
			truncation = new JNumberTextField(150);// 1-3...100-104
			completeness = new JNumberTextField(5); // 1-3

			use.addKeyListener(this);
			relation.addKeyListener(this);
			position.addKeyListener(this);
			structure.addKeyListener(this);
			truncation.addKeyListener(this);
			completeness.addKeyListener(this);

			int height = use.getPreferredSize().height;
			int fieldWidth = 50;
			int dotWidth = dot.getPreferredSize().width;

			final int[] rows = {height};
			final int[] cols = {fieldWidth, dotWidth, fieldWidth, dotWidth, fieldWidth, dotWidth, fieldWidth, dotWidth,
					fieldWidth, dotWidth, fieldWidth};

			setLayout(new TableLayout(rows, cols));
			add(use, new Rectangle(0, 0, 1, 1));
			add(dot, new Rectangle(1, 0, 1, 1));
			add(relation, new Rectangle(2, 0, 1, 1));
			add(new JLabel("."), new Rectangle(3, 0, 1, 1));
			add(position, new Rectangle(4, 0, 1, 1));
			add(new JLabel("."), new Rectangle(5, 0, 1, 1));
			add(structure, new Rectangle(6, 0, 1, 1));
			add(new JLabel("."), new Rectangle(7, 0, 1, 1));
			add(truncation, new Rectangle(8, 0, 1, 1));
			add(new JLabel("."), new Rectangle(9, 0, 1, 1));
			add(completeness, new Rectangle(10, 0, 1, 1));

		}

		@Override
		public void setEnabled(boolean enabled)
		{
			super.setEnabled(enabled);
			for( Component c : getComponents() )
			{
				c.setEnabled(enabled);
			}
		}

		public void setAttributes(int use, int relation, int position, int structure, int truncation, int completeness)
		{
			this.use.setText(String.valueOf(use));
			this.relation.setText(String.valueOf(relation));
			this.position.setText(String.valueOf(position));
			this.structure.setText(String.valueOf(structure));
			this.truncation.setText(String.valueOf(truncation));
			this.completeness.setText(String.valueOf(completeness));
		}

		public void clear()
		{
			use.clear();
			relation.clear();
			position.clear();
			structure.clear();
			truncation.clear();
			completeness.clear();
		}

		public boolean attributesValid()
		{
			boolean valid = true;
			valid &= use.getNumber() > 0;
			valid &= relation.getNumber() > 0;
			valid &= position.getNumber() > 0;
			valid &= structure.getNumber() > 0;
			valid &= truncation.getNumber() > 0;
			valid &= completeness.getNumber() > 0;
			return valid;
		}

		public String getText()
		{
			StringBuilder builder = new StringBuilder();
			builder.append(use.getNumber());
			builder.append('.');
			builder.append(relation.getNumber());
			builder.append('.');
			builder.append(position.getNumber());
			builder.append('.');
			builder.append(structure.getNumber());
			builder.append('.');
			builder.append(truncation.getNumber());
			builder.append('.');
			builder.append(completeness.getNumber());

			return builder.toString();
		}

		@Override
		public void keyTyped(KeyEvent e)
		{
			// We don't care about this event
		}

		@Override
		public void keyPressed(KeyEvent e)
		{
			// We don't care about this event
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			// TODO could put validation here
		}

	}

	private class SearchTripleShuffleList extends TripleShuffleList
	{
		private Z3950AttributesEditor attributeEditor;

		public SearchTripleShuffleList(String firstText, String secondText)
		{
			super(firstText, secondText);
		}

		@Override
		public boolean isThreeColumn()
		{
			return false;
		}

		@Override
		public boolean tableEditable()
		{
			return false;
		}

		@Override
		protected void setupGUI(String t1, String t2)
		{
			super.setupGUI(t1, t2);
			attributeEditor = new Z3950AttributesEditor();
			super.remove(secondField);
			super.add(attributeEditor, new Rectangle(2, 1, 1, 1));
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			table.editingCanceled(new ChangeEvent(e.getSource()));

			if( e.getSource() == add )
			{
				if( !firstField.isCompletelyEmpty() && attributeEditor.attributesValid() )
				{
					WizardControlItem item = new WizardControlItem();
					item.setName(firstField.save());
					item.setValue(attributeEditor.getText());
					model.addItem(item);
					firstField.load(null);
					attributeEditor.clear();
				}
			}
			else if( e.getSource() == remove )
			{
				int[] rows = table.getSelectedRows();
				for( int i = rows.length - 1; i >= 0; i-- )
				{
					model.removeRow(rows[i]);
				}
			}
			else if( e.getSource() == up )
			{
				if( table.getSelectedRowCount() != 0 )
				{
					final int index = table.getSelectedRow();
					if( index > 0 )
					{
						WizardControlItem item = model.removeRow(index);
						model.insertItem(index - 1, item);
						table.updateUI();
						table.setRowSelectionInterval(index - 1, index - 1);
					}
				}
			}
			else if( e.getSource() == down )
			{
				if( table.getSelectedRowCount() != 0 )
				{
					final int index = table.getSelectedRow();
					if( index < model.getRowCount() - 1 )
					{
						WizardControlItem item = model.removeRow(index);
						model.insertItem(index + 1, item);
						table.updateUI();
						table.setRowSelectionInterval(index + 1, index + 1);
					}
				}
			}
			else if( e.getSource() == sort )
			{
				List<WizardControlItem> items = new ArrayList<WizardControlItem>(getItems());
				Collections.sort(items, new Comparator<WizardControlItem>()
				{
					@Override
					public int compare(WizardControlItem i1, WizardControlItem i2)
					{
						return getName(i1).compareToIgnoreCase(getName(i2));
					}

					private String getName(WizardControlItem i)
					{
						Map<String, LanguageString> strings = i.getName().getStrings();
						if( strings != null )
						{
							Iterator<LanguageString> it = strings.values().iterator();
							if( it.hasNext() )
							{
								return it.next().getText();
							}
						}
						return ""; //$NON-NLS-1$
					}
				});
				setItems(items);
			}
		}

	}

	private class DefaultsDialog extends JPanel
	{
		private JList<NameValue> list;
		private DefaultListModel<NameValue> listModel;

		public DefaultsDialog()
		{
			listModel = new DefaultListModel<NameValue>();
			list = new JList<NameValue>();
			list.setModel(listModel);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			for( AttributeProfile profile : AttributeProfile.values() )
			{
				listModel.addElement(new NameValue(profile.getName(), profile.toString()));
			}

			setLayout(new BorderLayout());
			add(new JScrollPane(list));
		}

		public String getSelectedDefault()
		{
			return list.getSelectedValue().getValue();
		}
	}
}
