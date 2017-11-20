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

package com.tle.admin.courseinfo;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.KeyListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.dytech.edge.common.Constants;
import com.dytech.gui.JSmartTextField;
import com.dytech.gui.TableLayout;
import com.tle.admin.Driver;
import com.tle.admin.baseentity.BaseEntityEditor.AbstractDetailsTab;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.common.DateSelector;
import com.tle.admin.gui.common.JNameValuePanel;
import com.tle.admin.gui.i18n.I18nTextArea;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.NameValue;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.recipientselector.SingleUserSelector;
import com.tle.core.remoting.RemoteCourseInfoService;
import com.tle.core.remoting.RemoteUserService;
import com.tle.i18n.BundleCache;

/**
 * @author Nicholas Read
 */
public class DetailsTab extends BaseEntityTab<CourseInfo> implements AbstractDetailsTab<CourseInfo>
{
	private I18nTextField name;
	private com.tle.admin.gui.i18n.I18nTextArea description;
	private SingleUserSelector owner;
	private RemoteCourseInfoService courseInfoService;
	private JSmartTextField code;
	private DateSelector startDate;
	private DateSelector endDate;
	private JSpinner students;
	private JComboBox citations;
	private JComboBox courseType;
	private JTextField departmentName;
	private JCheckBox archived;

	public DetailsTab()
	{
		super();
	}

	@Override
	public void setDriver(Driver driver)
	{
		super.setDriver(driver);
		courseInfoService = driver.getClientService().getService(RemoteCourseInfoService.class);
	}

	@Override
	public void init(Component parent)
	{
		setupGUI();
	}

	@Override
	public String getTitle()
	{
		return getString("detailstab.title"); //$NON-NLS-1$
	}

	private void setupGUI()
	{
		JNameValuePanel panel1 = new JNameValuePanel();

		name = new I18nTextField(BundleCache.getLanguages());
		description = new I18nTextArea(BundleCache.getLanguages());

		owner = new SingleUserSelector(clientService.getService(RemoteUserService.class));

		// http://jira.pearsoncmg.com/jira/browse/EQ-1224
		code = new JSmartTextField(100);

		startDate = new DateSelector();
		endDate = new DateSelector();
		students = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
		citations = new JComboBox(courseInfoService.getAllCitations().toArray());
		courseType = new JComboBox(CourseInfo.COURSE_TYPES.toArray());
		departmentName = new JTextField();
		archived = new JCheckBox();

		panel1.addTextAndComponent(getKey("courses.detailstab.name"), name); //$NON-NLS-1$
		panel1.addTextAndComponent(getKey("detailstab.desc"), description); //$NON-NLS-1$
		panel1.addTextAndComponent(getKey("detailstab.department"), departmentName); //$NON-NLS-1$
		panel1.addTextAndComponent(getKey("courses.detailstab.owner"), owner); //$NON-NLS-1$
		panel1.addTextAndComponent(getKey("detailstab.code"), code); //$NON-NLS-1$
		panel1.addTextAndComponent(getKey("detailstab.startdate"), startDate); //$NON-NLS-1$
		panel1.addTextAndComponent(getKey("detailstab.enddate"), endDate); //$NON-NLS-1$
		panel1.addTextAndComponent(getKey("detailstab.students"), students); //$NON-NLS-1$
		panel1.addTextAndComponent(getKey("detailstab.citation"), citations); //$NON-NLS-1$
		panel1.addTextAndComponent(getKey("detailstab.coursetype"), courseType); //$NON-NLS-1$
		panel1.addTextAndComponent(getKey("detailstab.archived"), archived); //$NON-NLS-1$

		// Make sure things are readonly.
		if( state.isReadonly() )
		{
			name.setEnabled(false);
			description.setEnabled(false);
			owner.setEnabled(false);
			code.setEnabled(false);
			startDate.setEnabled(false);
			endDate.setEnabled(false);
			students.setEnabled(false);
			departmentName.setEnabled(false);
			courseType.setEditable(false);
		}

		final int[] rows = {TableLayout.FILL,};
		final int[] cols = {TableLayout.DOUBLE_FILL, TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols));
		add(panel1.getComponent(), new Rectangle(0, 0, 1, 1));
	}

	@Override
	public void addNameListener(KeyListener listener)
	{
		name.addKeyListener(listener);
	}

	@Override
	public void load()
	{
		final CourseInfo course = state.getEntity();

		name.load(course.getName());
		description.load(course.getDescription());
		owner.setUserId(course.getOwner());
		code.setText(course.getCode());
		citations.setSelectedItem(course.getCitation());
		startDate.setDate(course.getFrom());
		endDate.setDate(course.getUntil());
		students.getModel().setValue(course.getStudents());
		departmentName.setText(course.getDepartmentName());
		AppletGuiUtils.selectInJCombo(courseType,
			new NameValue(Constants.BLANK, Character.toString(course.getCourseType())));
		archived.setSelected(course.isDisabled());
	}

	@Override
	public void save()
	{
		final CourseInfo course = state.getEntity();

		course.setName(name.save());
		course.setDescription(description.save());

		course.setOwner(owner.getUser().getUniqueID());
		course.setCode(code.getText());
		Object citation = citations.getSelectedItem();
		if( citation != null )
		{
			course.setCitation(citation.toString());
		}
		course.setUntil(endDate.getDate());
		course.setFrom(startDate.getDate());
		course.setStudents((Integer) students.getModel().getValue());
		course.setCourseType(((NameValue) courseType.getSelectedItem()).getValue().charAt(0));
		course.setDepartmentName(departmentName.getText());
		course.setDisabled(archived.isSelected());
	}

	@Override
	public void validation() throws EditorException
	{
		if( name.isCompletelyEmpty() )
		{
			throw new EditorException(getString("courses.detailstab.supplyname")); //$NON-NLS-1$
		}

		String code1 = code.getText();
		if( code1.length() == 0 )
		{
			throw new EditorException(getString("detailstab.supplycode")); //$NON-NLS-1$
		}

		if( startDate.getDate() != null && endDate.getDate() != null && !endDate.getDate().after(startDate.getDate()) )
		{
			throw new EditorException(getString("detailstab.daterange")); //$NON-NLS-1$
		}

		if( owner.getUser() == null )
		{
			throw new EditorException(getString("courses.detailstab.noowner")); //$NON-NLS-1$
		}
	}
}
