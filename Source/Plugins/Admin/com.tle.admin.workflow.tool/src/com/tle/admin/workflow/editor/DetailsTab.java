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

package com.tle.admin.workflow.editor;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import net.miginfocom.swing.MigLayout;

import com.dytech.gui.ChangeDetector;
import com.tle.admin.gui.i18n.I18nTextArea;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.SchemaModel;
import com.tle.admin.schema.SingleTargetChooser;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.beans.entity.Schema;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowItem.AutoAction;
import com.tle.common.workflow.node.WorkflowItem.MoveLive;
import com.tle.common.workflow.node.WorkflowItem.Priority;
import com.tle.core.remoting.RemoteSchemaService;
import com.tle.i18n.BundleCache;

/**
 * @author Nicholas Read
 */
public class DetailsTab extends JPanel
{
	private static final long serialVersionUID = 1L;

	private final SchemaModel schemaModel = new SchemaModel();
	private final RemoteSchemaService schemaService;

	private JLabel nameLabel;
	private I18nTextField name;

	private JLabel descriptionLabel;
	private I18nTextArea description;

	private JCheckBox escalation;
	private JLabel escalationDaysLabel;
	private JSpinner escalationDays;

	private JCheckBox definedDateCheck;

	private JCheckBox autoCheck;
	private ButtonGroup automatic;
	private JRadioButton autoReject;
	private JRadioButton autoAccept;
	private JLabel autoDaysLabel;
	private JSpinner autoDays;

	private JLabel priorityLabel;
	private JComboBox priority;

	private JCheckBox moveToLive;
	private ButtonGroup moveToLiveGroup;
	private JRadioButton moveToLiveArrival;
	private JRadioButton moveToLiveAccept;

	private JCheckBox rejectPoint;

	private JLabel schemaLabel;
	private JComboBox schemaList;
	private JLabel dynamicUserPathLabel;
	private SingleTargetChooser dynamicDueDate;

	public DetailsTab(ChangeDetector changeDetector, RemoteSchemaService schemaService)
	{
		this.schemaService = schemaService;
		setupGui();
		setupLayout();
		setupDefaults();
		setupChangeDetector(changeDetector);
	}

	@SuppressWarnings("nls")
	private void setupGui()
	{
		nameLabel = new JLabel(s("name"));
		name = new I18nTextField(BundleCache.getLanguages());

		descriptionLabel = new JLabel(s("desc"));
		description = new I18nTextArea(BundleCache.getLanguages());
		description.setTextRows(5);

		escalation = new JCheckBox(s("timeperiod"));
		escalation.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				enableTimePeriod(escalation.isSelected());
			}
		});
		escalationDays = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));
		escalationDaysLabel = new JLabel(s("days"));

		definedDateCheck = new JCheckBox(s("defined"));
		definedDateCheck.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				enableSchemaDate(definedDateCheck.isSelected());
			}
		});

		autoCheck = new JCheckBox(s("auto.within"));
		autoCheck.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				enableAuto(autoCheck.isSelected() && escalation.isSelected());
			}
		});
		autoDaysLabel = new JLabel(s("auto.days"));
		autoDays = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));

		automatic = new ButtonGroup();
		autoReject = new JRadioButton(s("auto.reject"));
		autoAccept = new JRadioButton(s("auto.accept"));
		automatic.add(autoReject);
		automatic.add(autoAccept);

		moveToLive = new JCheckBox(s("move"));
		moveToLive.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				enableMoveToLive(moveToLive.isSelected());
			}
		});

		priorityLabel = new JLabel(s("priority"));
		priority = new JComboBox(Priority.values());

		moveToLiveGroup = new ButtonGroup();
		moveToLiveArrival = new JRadioButton(s("move.arrival"));
		moveToLiveAccept = new JRadioButton(s("move.acceptance"));
		moveToLiveGroup.add(moveToLiveArrival);
		moveToLiveGroup.add(moveToLiveAccept);

		rejectPoint = new JCheckBox(s("rejectpoint"));

		dynamicUserPathLabel = new JLabel(s("target")); //$NON-NLS-1$
		dynamicDueDate = new SingleTargetChooser(schemaModel, null);

		schemaLabel = new JLabel(s("schema")); //$NON-NLS-1$
		schemaList = new JComboBox();
		schemaList.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				final String uuid = ((NameValue) schemaList.getSelectedItem()).getValue();
				final Schema selected = schemaService.get(schemaService.identifyByUuid(uuid));
				schemaModel.loadSchema(selected.getDefinitionNonThreadSafe());
			}
		});

		for( final NameValue schema : BundleCache.getNameUuidValues(schemaService.listAll()) )
		{
			schemaList.addItem(schema);
		}
	}

	@SuppressWarnings("nls")
	private void setupLayout()
	{
		setLayout(new MigLayout("wrap 3", "[][][fill,grow]"));
		setBorder(AppletGuiUtils.DEFAULT_BORDER);

		add(nameLabel);
		add(name, "span 2, grow");

		add(descriptionLabel, "aligny top");
		add(description, "span 2, grow");

		add(priorityLabel);
		add(priority, "span 2, width 150");

		add(escalation, "split 3, span 3");
		add(escalationDays);
		add(escalationDaysLabel);

		add(definedDateCheck, "skip, span 2");
		add(schemaLabel, "skip, gapleft 20");
		add(schemaList, "span 2, grow");
		add(dynamicUserPathLabel, "skip, gapleft 20");
		add(dynamicDueDate, "span 2, grow");

		add(autoCheck, "skip, split 3, span 3");
		add(autoDays);
		add(autoDaysLabel);
		add(autoReject, "skip, gapleft 20, span 2");
		add(autoAccept, "skip, gapleft 20, span 2");

		add(moveToLive, "span 3");
		add(moveToLiveArrival, "skip, span 2");
		add(moveToLiveAccept, "skip, span 2");

		add(rejectPoint, "span 3");
	}

	private void setupDefaults()
	{
		enableTimePeriod(false);
		enableSchemaDate(false);
		enableAuto(false);
		enableMoveToLive(false);

		priority.setSelectedItem(Priority.NORMAL);
		moveToLiveAccept.setSelected(true);
		autoReject.setSelected(true);
	}

	private void setupChangeDetector(ChangeDetector changeDetector)
	{
		changeDetector.watch(name);
		changeDetector.watch(description);
		changeDetector.watch(escalation);
		changeDetector.watch(escalationDays.getModel());
		changeDetector.watch(moveToLive);
		changeDetector.watch(rejectPoint);
	}

	public void load(WorkflowItem item)
	{
		name.load(item.getDisplayName());
		description.load(item.getDescription());
		escalation.setSelected(item.isEscalate());
		if( item.isEscalate() )
		{
			escalationDays.setValue(item.getEscalationdays());
		}

		MoveLive movelive = item.getMovelive();
		moveToLive.setSelected(movelive != MoveLive.NO);
		moveToLiveArrival.setSelected(movelive == MoveLive.ARRIVAL);
		moveToLiveAccept.setSelected(movelive == MoveLive.ACCEPTED || movelive == MoveLive.NO);
		rejectPoint.setSelected(item.isRejectPoint());
		AutoAction autoAction = item.getAutoAction();
		boolean hasAction = autoAction != AutoAction.NONE;
		autoCheck.setSelected(hasAction);
		if( hasAction )
		{
			autoDays.setValue(item.getActionDays());
		}
		autoAccept.setSelected(autoAction == AutoAction.ACCEPT);
		autoReject.setSelected(autoAction == AutoAction.REJECT);

		int priVal = item.getPriority();
		for( int i = 0; i < priority.getItemCount(); i++ )
		{
			final int val = ((Priority) priority.getItemAt(i)).intValue();
			if( val == priVal )
			{
				priority.setSelectedIndex(i);
				break;
			}
		}
		boolean found = false;
		final String itemSchemaUuid = item.getDueDateSchemaUuid();
		for( int i = 0; i < schemaList.getItemCount(); i++ )
		{
			final String uuid = ((NameValue) schemaList.getItemAt(i)).getValue();
			if( uuid != null && uuid.equals(itemSchemaUuid) )
			{
				schemaList.setSelectedIndex(i);
				found = true;
				break;
			}
		}
		if( !found )
		{
			schemaList.setSelectedIndex(0);
		}
		String dueDatePath = item.getDueDatePath();
		if( !Check.isEmpty(dueDatePath) )
		{
			dynamicDueDate.setTarget(dueDatePath);
			definedDateCheck.setSelected(true);
		}
		else
		{
			definedDateCheck.setSelected(false);
		}
	}

	public void save(WorkflowItem item)
	{
		LanguageBundle nameBundle = name.save();
		// Db restrictions mean that task names need to be >100 char
		Map<String, LanguageString> strings = nameBundle.getStrings();
		for( Entry<String, LanguageString> entry : strings.entrySet() )
		{
			LanguageString currentString = entry.getValue();
			if( currentString.getText().length() > 100 )
			{
				currentString.setText(currentString.getText().substring(0, 100));
				strings.put(entry.getKey(), currentString);
			}
		}
		item.setName(nameBundle);
		item.setDescription(description.save());
		item.setEscalate(escalation.isSelected());
		item.setEscalationdays((Integer) escalationDays.getValue());
		item.setMovelive(moveToLive.isSelected() ? (moveToLiveArrival.isSelected() ? MoveLive.ARRIVAL
			: MoveLive.ACCEPTED) : MoveLive.NO);
		item.setAutoAction(autoCheck.isSelected() ? (autoAccept.isSelected() ? AutoAction.ACCEPT : AutoAction.REJECT)
			: AutoAction.NONE);
		item.setActionDays((Integer) autoDays.getValue());
		item.setRejectPoint(rejectPoint.isSelected());
		item.setPriority(((Priority) priority.getSelectedItem()).intValue());
		if( definedDateCheck.isSelected() )
		{
			item.setDueDatePath(dynamicDueDate.getTarget());
			item.setDueDateSchemaUuid(((NameValue) schemaList.getSelectedItem()).getValue());
		}
		else
		{
			item.setDueDatePath(null);
		}
	}

	private String s(String keyPart)
	{
		return CurrentLocale.get("com.tle.admin.workflow.editor.detailstab." + keyPart); //$NON-NLS-1$
	}

	private void enableTimePeriod(boolean b)
	{
		escalationDays.setEnabled(b);
		definedDateCheck.setEnabled(b);
		autoCheck.setEnabled(b);
		enableSchemaDate(b && definedDateCheck.isSelected());
		enableAuto(b && autoCheck.isSelected());
	}

	private void enableAuto(boolean b)
	{
		autoReject.setEnabled(b);
		autoAccept.setEnabled(b);
		autoDays.setEnabled(b);
	}

	private void enableSchemaDate(boolean b)
	{
		schemaList.setEnabled(b);
		dynamicDueDate.setEnabled(b);
	}

	private void enableMoveToLive(boolean b)
	{
		moveToLiveAccept.setEnabled(b);
		moveToLiveArrival.setEnabled(b);
	}
}
