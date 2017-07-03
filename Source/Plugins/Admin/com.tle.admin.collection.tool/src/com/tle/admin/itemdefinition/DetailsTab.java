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

package com.tle.admin.itemdefinition;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.baseentity.BaseEntityEditor.AbstractDetailsTab;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.i18n.I18nTextArea;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.SchemaTransform;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.client.gui.JLinkButton;
import com.tle.common.Check;
import com.tle.common.Format;
import com.tle.common.NameValue;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.filesystem.remoting.RemoteFileSystemService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.recipientselector.SingleUserSelector;
import com.tle.common.workflow.RemoteWorkflowService;
import com.tle.common.workflow.Workflow;
import com.tle.core.remoting.RemoteItemDefinitionService;
import com.tle.core.remoting.RemoteSchemaService;
import com.tle.core.remoting.RemoteUserService;
import com.tle.i18n.BundleCache;

@SuppressWarnings("nls")
public class DetailsTab extends AbstractItemdefTab implements ActionListener, AbstractDetailsTab<ItemDefinition>
{
	private I18nTextField itemName;
	private I18nTextArea description;
	private SingleUserSelector owner;

	protected int currentSchemaIndex = -1;
	private JComboBox schemaList;

	private JComboBox reviewPeriod;
	protected JComboBox workflowMapping;
	private JComboBox wizardCategory;
	private JLinkButton wizardCategoryAdd;
	private JCheckBox denyDirectContribution;

	private JComboBox scormPackagingTransformation;
	private JComboBox<NameValue> filestoreList;
	private boolean advancedFilestore;
	private Collection<NameValue> filestores;

	private RemoteItemDefinitionService itemdefService;
	private RemoteFileSystemService remoteFileSystemService;

	@Override
	public void init(Component parent)
	{
		setupGUI();
	}

	@Override
	public void setDriver(Driver driver)
	{
		super.setDriver(driver);
		itemdefService = clientService.getService(RemoteItemDefinitionService.class);
		remoteFileSystemService = clientService.getService(RemoteFileSystemService.class);
		advancedFilestore = remoteFileSystemService.isAdvancedFilestore();
		filestores = getFilestores();
	}

	@Override
	public void validation() throws EditorException
	{
		if( itemName.isCompletelyEmpty() )
		{
			throw new EditorException(s("supplyname"));
		}
		else if( owner.getUser() == null )
		{
			throw new EditorException(s("noowner"));
		}
		else if( schemaList.getSelectedIndex() < 0 )
		{
			throw new EditorException(s("selectschema"));
		}
		else if( wizardCategory.getSelectedItem() == null )
		{
			throw new EditorException(s("choose"));
		}
	}

	@Override
	public String getTitle()
	{
		return s("title");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.admin.baseentity.BaseEntityEditor.AbstractDetailsTab#addNameListener
	 * (java.awt.event.KeyListener)
	 */
	@Override
	public void addNameListener(KeyListener listener)
	{
		itemName.addKeyListener(listener);
	}

	@Override
	public void save()
	{
		final ItemDefinition itemDef = state.getEntity();

		itemDef.setName(itemName.save());
		itemDef.setDescription(description.save());
		itemDef.setOwner(owner.getUser().getUniqueID());
		itemDef.setReviewperiod(Integer.valueOf(((NameValue) reviewPeriod.getSelectedItem()).getValue()).intValue());

		String wizCat = null;
		if( wizardCategory.getSelectedIndex() > 0 )
		{
			wizCat = wizardCategory.getSelectedItem().toString();
		}
		itemDef.setWizardcategory(wizCat);
		itemDef.setDenyDirectContribution(denyDirectContribution.isSelected());

		NameValue selectedSchema = (NameValue) schemaList.getSelectedItem();
		itemDef.setSchema(new Schema(Long.parseLong(selectedSchema.getValue())));

		String scormTransform = null;
		if( scormPackagingTransformation.getSelectedIndex() > 0 )
		{
			scormTransform = (String) scormPackagingTransformation.getSelectedItem();
		}
		itemDef.setScormPackagingTransformation(scormTransform);

		Workflow workflow = null;
		if( workflowMapping.getSelectedIndex() > 0 )
		{
			NameValue nv = (NameValue) workflowMapping.getSelectedItem();
			workflow = new Workflow();
			workflow.setId(Long.parseLong(nv.getValue()));
		}
		itemDef.setWorkflow(workflow);

		if( filestoreList != null )
		{
			itemDef.setAttribute(RemoteItemDefinitionService.ATTRIBUTE_KEY_FILESTORE,
				((NameValue) filestoreList.getSelectedItem()).getValue());
		}
	}

	@Override
	public void load()
	{
		final ItemDefinition itemDef = state.getEntity();

		itemName.load(itemDef.getName());
		description.load(itemDef.getDescription());
		owner.setUserId(itemDef.getOwner());

		Schema schema1 = itemDef.getSchema();
		currentSchemaIndex = -1;
		if( schema1 != null )
		{
			schemaList.removeActionListener(this);

			NameValue temp = new NameValue(null, Long.toString(schema1.getId()));
			currentSchemaIndex = AppletGuiUtils.selectInJCombo(schemaList, temp, 0);

			schemaList.addActionListener(this);
		}
		switchSchemas(currentSchemaIndex);

		scormPackagingTransformation.setSelectedItem(itemDef.getScormPackagingTransformation());
		if( scormPackagingTransformation.getSelectedIndex() < 0 )
		{
			scormPackagingTransformation.setSelectedIndex(0);
		}

		Workflow workflow = itemDef.getWorkflow();
		long workflowId = 0;
		if( workflow != null )
		{
			workflowId = workflow.getId();
		}
		NameValue temp = new NameValue(null, Long.toString(workflowId));
		AppletGuiUtils.selectInJCombo(workflowMapping, temp, 0);

		String wizCat = itemDef.getWizardcategory();
		if( wizCat != null )
		{
			wizardCategory.setSelectedItem(wizCat);
		}
		else
		{
			wizardCategory.setSelectedIndex(0);
		}
		denyDirectContribution.setSelected(itemDef.isDenyDirectContribution());

		AppletGuiUtils
			.selectInJCombo(reviewPeriod, new NameValue(null, Integer.toString(itemDef.getReviewperiod())), 0);

		if( filestoreList != null )
		{
			String filestoreId = itemDef.getAttribute(RemoteItemDefinitionService.ATTRIBUTE_KEY_FILESTORE);
			if( filestoreId == null )
			{
				filestoreId = RemoteFileSystemService.DEFAULT_FILESTORE_ID;
			}
			AppletGuiUtils.selectInJCombo(filestoreList, new NameValue(null, filestoreId), 0);
		}

		updateGUI();
	}

	public void updateGUI()
	{
		reviewPeriod.setEnabled(workflowMapping.getSelectedIndex() > 0);
	}

	protected void setupGUI()
	{
		JLabel nameLabel = new JLabel(s("name"));
		JLabel descriptionLabel = new JLabel(s("desc"));
		JLabel ownerLabel = new JLabel(s("owner"));
		JLabel schemaLabel = new JLabel(s("schema"));
		JLabel scormLabel = new JLabel(s("allow"));
		JLabel wizCatLabel = new JLabel(s("selectwiz"));
		JLabel moderationLabel = new JLabel(s("map"));
		JLabel reviewLabel = new JLabel(s("selectreview"));
		JLabel filestoreLabel = new JLabel(s("filestore"));

		descriptionLabel.setVerticalAlignment(SwingConstants.TOP);
		descriptionLabel.setVerticalTextPosition(SwingConstants.TOP);

		itemName = new I18nTextField(BundleCache.getLanguages());
		nameLabel.setLabelFor(itemName);

		description = new I18nTextArea(BundleCache.getLanguages());
		descriptionLabel.setLabelFor(description);

		owner = new SingleUserSelector(clientService.getService(RemoteUserService.class));
		ownerLabel.setLabelFor(owner);

		schemaList = new JComboBox();
		List<NameValue> schemas = getSchemaList();
		AppletGuiUtils.addItemsToJCombo(schemaList, schemas);
		if( schemas.size() > 0 )
		{
			schemaList.setSelectedIndex(0);
		}
		schemaList.addActionListener(this);
		schemaLabel.setLabelFor(schemaList);

		scormPackagingTransformation = new JComboBox();
		scormLabel.setLabelFor(scormPackagingTransformation);

		workflowMapping = new JComboBox();
		NameValue nv = new NameValue(s("none"), ""); //$NON-NLS-2$
		workflowMapping.insertItemAt(nv, 0);
		workflowMapping.setSelectedIndex(0);
		workflowMapping.addActionListener(this);
		AppletGuiUtils.addItemsToJCombo(workflowMapping, getWorkflows());
		moderationLabel.setLabelFor(workflowMapping);

		reviewPeriod = new JComboBox();
		AppletGuiUtils.addItemsToJCombo(reviewPeriod, getReviewPeriods());
		reviewLabel.setLabelFor(reviewPeriod);

		wizardCategory = new JComboBox();
		NameValue noWiz = new NameValue(s("none"), ""); //$NON-NLS-2$
		wizardCategory.insertItemAt(noWiz, 0);
		wizardCategory.setSelectedIndex(0);
		AppletGuiUtils.addItemsToJCombo(wizardCategory, getCategories());
		wizCatLabel.setLabelFor(wizardCategory);
		denyDirectContribution = new JCheckBox(s("direct"));

		wizardCategoryAdd = new JLinkButton(s("add"));
		wizardCategoryAdd.addActionListener(this);

		if( advancedFilestore )
		{
			if( filestores.size() > 1 )
			{
				filestoreList = new JComboBox<>();
				AppletGuiUtils.addItemsToJCombo(filestoreList, filestores);
				//Make it locked on existing collections
				if( state.getEntity().getId() != 0 )
				{
					filestoreList.setEnabled(false);
				}
			}
		}

		final int height1 = itemName.getPreferredSize().height;
		final int height2 = owner.getPreferredSize().height;

		final int width1 = scormLabel.getPreferredSize().width;
		final int width2 = wizCatLabel.getPreferredSize().width;
		final int width3 = schemaLabel.getPreferredSize().width;
		final int width4 = Math.max(width1, Math.max(width2, width3));
		final int width5 = wizardCategoryAdd.getPreferredSize().width;
		final int gap = 10;

		final int columns[] = {width4, TableLayout.DOUBLE_FILL, width5, TableLayout.FILL, TableLayout.FILL,};
		final int rows[] = {height1, height1 * 3, height2, gap, height1, height1, gap, height1, height1, height1,
				height1, height1, gap, TableLayout.FILL,};

		setLayout(new TableLayout(rows, columns, 5, 5));

		int row = 0;
		add(nameLabel, new Rectangle(0, row, 1, 1));
		add(itemName, new Rectangle(1, row++, 3, 1));

		add(descriptionLabel, new Rectangle(0, row, 1, 1));
		add(description, new Rectangle(1, row++, 3, 1));

		add(ownerLabel, new Rectangle(0, row, 1, 1));
		add(owner, new Rectangle(1, row++, 3, 1));

		row++;

		add(schemaLabel, new Rectangle(0, row, 1, 1));
		add(schemaList, new Rectangle(1, row++, 1, 1));

		add(scormLabel, new Rectangle(0, row, 1, 1));
		add(scormPackagingTransformation, new Rectangle(1, row++, 1, 1));

		row++;

		add(moderationLabel, new Rectangle(0, row, 1, 1));
		add(workflowMapping, new Rectangle(1, row++, 1, 1));

		add(reviewLabel, new Rectangle(0, row, 1, 1));
		add(reviewPeriod, new Rectangle(1, row++, 1, 1));

		add(wizCatLabel, new Rectangle(0, row, 1, 1));
		add(wizardCategory, new Rectangle(1, row, 1, 1));
		add(wizardCategoryAdd, new Rectangle(2, row++, 1, 1));
		add(denyDirectContribution, new Rectangle(1, row++, 1, 1));

		if( filestoreList != null )
		{
			add(filestoreLabel, new Rectangle(0, row, 1, 1));
			add(filestoreList, new Rectangle(1, row++, 1, 1));
		}

		updateGUI();
		switchSchemas(schemaList.getSelectedIndex());
	}

	private static String s(String keyPart)
	{
		return CurrentLocale.get("com.tle.admin.itemdefinition.detailstab." + keyPart);
	}

	protected List<NameValue> getSchemaList()
	{
		try
		{
			List<BaseEntityLabel> schemas = clientService.getService(RemoteSchemaService.class).listAll();
			List<NameValue> nameValues = BundleCache.getNameValues(schemas);
			Collections.sort(nameValues, Format.NAME_VALUE_COMPARATOR);
			return nameValues;
		}
		catch( Exception ex )
		{
			Driver.displayError(null, "schema/enumerating", ex);
			LOGGER.error("Error enumerating schemas", ex);
			return null;
		}
	}

	protected List<String> getCategories()
	{
		try
		{
			List<String> list = new ArrayList<String>(itemdefService.enumerateCategories());
			Collections.sort(list, Format.STRING_COMPARATOR);
			return list;
		}
		catch( Exception ex )
		{
			Driver.displayError(null, "wizard/listCategories", ex);
			LOGGER.error("Error enumerating wizard categories", ex);
			return null;
		}
	}

	protected Collection<NameValue> getWorkflows()
	{
		try
		{
			List<BaseEntityLabel> results = clientService.getService(RemoteWorkflowService.class).listAll();
			List<NameValue> nameValues = BundleCache.getNameValues(results);
			Collections.sort(nameValues, Format.NAME_VALUE_COMPARATOR);
			return nameValues;
		}
		catch( Exception ex )
		{
			Driver.displayError(null, "workflow/enumerating", ex);
			LOGGER.error("Error enumerating workflows", ex);
			return null;
		}
	}

	protected Collection<NameValue> getFilestores()
	{
		try
		{
			return remoteFileSystemService.listFilestores();
		}
		catch( Exception ex )
		{
			Driver.displayError(null, "filestores/enumerating", ex);
			LOGGER.error("Error enumerating filestores", ex);
			return null;
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
		if( e.getSource() == wizardCategoryAdd )
		{
			while( true )
			{
				String result = JOptionPane.showInputDialog(getComponent(), s("enter"), s("newwiz"),
					JOptionPane.QUESTION_MESSAGE);

				if( result == null )
				{
					return;
				}
				else if( Check.isEmpty(result) )
				{
					Driver.displayInformation(getComponent(), "You must enter a name for the new wizard category");
				}
				else if( result.trim().length() > 100 )
				{
					Driver.displayInformation(getComponent(),
						"The name of a wizard category must not exceed 100 characters");
				}
				else
				{
					wizardCategory.setSelectedIndex(-1);
					wizardCategory.setSelectedItem(result);
					if( wizardCategory.getSelectedIndex() < 0 )
					{
						wizardCategory.addItem(result);
						wizardCategory.setSelectedItem(result);
					}
					return;
				}
			}
		}
		else if( e.getSource() == workflowMapping )
		{
			GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
			{
				@Override
				public Object construct() throws Exception
				{
					String value = ((NameValue) workflowMapping.getSelectedItem()).getValue();
					long id = Check.isEmpty(value) ? 0 : Long.parseLong(value);
					options.setCurrentWorkflow(id);
					return null;
				}

				@Override
				public void exception()
				{
					LOGGER.error("Error loading workflow", getException());
					JOptionPane.showMessageDialog(panel, "Error loading workflow");
				}
			};

			worker.setComponent(panel);
			worker.start();

			updateGUI();
		}
		else if( e.getSource() == schemaList )
		{
			final int selected = schemaList.getSelectedIndex();
			final int old = currentSchemaIndex;
			if( selected == currentSchemaIndex )
			{
				return;
			}

			if( currentSchemaIndex > -1 )
			{
				int result = JOptionPane.showConfirmDialog(panel, s("confirm"), s("sure"), JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);

				if( result != JOptionPane.YES_OPTION )
				{
					schemaList.setSelectedIndex(currentSchemaIndex);
					return;
				}
				currentSchemaIndex = selected;
			}

			GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
			{
				@Override
				public Object construct() throws Exception
				{
					switchSchemas(selected);
					return null;
				}

				@Override
				public void finished()
				{
					if( currentSchemaIndex > -1 )
					{
						JOptionPane.showMessageDialog(panel, s("schemachanged"));
					}
				}

				@Override
				public void exception()
				{
					currentSchemaIndex = old;
					LOGGER.error("Error loading schema", getException());
					JOptionPane.showMessageDialog(panel, s("error"));
				}
			};

			worker.setComponent(panel);
			worker.start();
		}
	}

	void switchSchemas(final int newIndex)
	{
		scormPackagingTransformation.removeAllItems();
		scormPackagingTransformation.addItem(s("notrans"));
		scormPackagingTransformation.setSelectedIndex(0);

		NameValue schemaPair = (NameValue) schemaList.getItemAt(newIndex);
		if( schemaPair != null )
		{
			Schema schemaBean = clientService.getService(RemoteSchemaService.class).get(
				Long.parseLong(schemaPair.getValue()));
			schema.loadSchema(schemaBean.getDefinitionNonThreadSafe());

			for( SchemaTransform transform : schemaBean.getExportTransforms() )
			{
				scormPackagingTransformation.addItem(transform.getType());
			}
		}
	}

	private static List<NameValue> getReviewPeriods()
	{
		final double daysPerYear = 365.25;
		final long daysPerMonth = 30;
		final long daysPerWeek = 7;

		List<NameValue> result = new ArrayList<NameValue>();
		result.add(getReviewPeriod("5years", 5 * daysPerYear));
		result.add(getReviewPeriod("3years", 3 * daysPerYear));
		result.add(getReviewPeriod("2years", 2 * daysPerYear));
		result.add(getReviewPeriod("1year", daysPerYear));
		result.add(getReviewPeriod("6months", 6 * daysPerMonth));
		result.add(getReviewPeriod("3months", 3 * daysPerMonth));
		result.add(getReviewPeriod("1month", daysPerMonth));
		result.add(getReviewPeriod("2weeks", 2 * daysPerWeek));
		result.add(getReviewPeriod("1week", daysPerWeek));
		result.add(getReviewPeriod("never", Integer.MIN_VALUE));
		return result;
	}

	private static NameValue getReviewPeriod(String key, double time)
	{
		return new NameValue(s(key), Long.toString((long) time));
	}
}
