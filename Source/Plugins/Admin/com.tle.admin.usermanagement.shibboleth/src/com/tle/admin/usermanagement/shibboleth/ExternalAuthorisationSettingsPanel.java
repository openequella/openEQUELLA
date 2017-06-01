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

package com.tle.admin.usermanagement.shibboleth;

import static com.tle.beans.usermanagement.shibboleth.wrapper.ExternalAuthorisationWrapperSettings.USAGE_ENV_VAR;
import static com.tle.beans.usermanagement.shibboleth.wrapper.ExternalAuthorisationWrapperSettings.USAGE_HTTP_HEADER;
import static com.tle.beans.usermanagement.shibboleth.wrapper.ExternalAuthorisationWrapperSettings.USAGE_REMOTE_USER;

import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.Changeable;
import com.dytech.gui.TableLayout;
import com.tle.beans.usermanagement.shibboleth.wrapper.ExternalAuthorisationWrapperSettings;
import com.tle.client.gui.JRadioGroup;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author aholland
 */
public class ExternalAuthorisationSettingsPanel extends JPanel implements Changeable
{
	private static final long serialVersionUID = 5306265445292956845L;

	protected JTextField httpHeaderName;
	protected JTextField environmentVarName;
	protected JTextField logoutUrl;

	protected ChangeDetector changeDetector;

	private final ButtonGroup buttonGroup = new ButtonGroup();
	protected JRadioGroup useRemoteUser;
	protected JRadioGroup useHTTPHeader;
	protected JRadioGroup useEnvironmentVariable;

	public ExternalAuthorisationSettingsPanel()
	{
		changeDetector = new ChangeDetector();
		setupGui();
	}

	protected void setupGui()
	{
		final JLabel httpHeaderNameLabel = new JLabel(getString("httpheadername")); //$NON-NLS-1$
		final JLabel environmentVarNameLabel = new JLabel(getString("environmentvarname")); //$NON-NLS-1$
		final JLabel preamble = new JLabel(getString("preamble")); //$NON-NLS-1$
		final JLabel logoutUrlLabel = new JLabel(getString("logouturl")); //$NON-NLS-1$

		httpHeaderName = new JTextField(20);
		environmentVarName = new JTextField(20);
		logoutUrl = new JTextField(20);

		final int height1 = logoutUrl.getPreferredSize().height;
		final TableLayout innerLayout = new TableLayout(new int[]{height1, height1},
			new int[]{
					Math.max(environmentVarNameLabel.getPreferredSize().width,
						httpHeaderNameLabel.getPreferredSize().width), TableLayout.FILL}, 5, 5);

		final Rectangle radioPos = new Rectangle(0, 0, 2, 1);
		useRemoteUser = new JRadioGroup(getString("useremoteuser"), innerLayout, radioPos); //$NON-NLS-1$
		buttonGroup.add(useRemoteUser.getButton());
		useHTTPHeader = new JRadioGroup(getString("usehttpheader"), innerLayout, radioPos); //$NON-NLS-1$
		buttonGroup.add(useHTTPHeader.getButton());
		useEnvironmentVariable = new JRadioGroup(getString("useenvironmentvar"), innerLayout, radioPos); //$NON-NLS-1$
		buttonGroup.add(useEnvironmentVariable.getButton());

		final int width1 = logoutUrlLabel.getPreferredSize().width;
		final int width2 = logoutUrl.getPreferredSize().width;
		final int[] rows = {preamble.getPreferredSize().height * 2, height1, height1, height1, height1, height1,
				height1, height1, height1, height1, height1, TableLayout.PREFERRED};
		final int[] columns = {width1, width2, TableLayout.FILL};
		setLayout(new TableLayout(rows, columns, 5, 5));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		int row = 0;
		add(preamble, new Rectangle(0, row, 3, 1));

		// Advance the value of row as we refer to it ...
		++row;
		add(useRemoteUser, new Rectangle(0, row, 3, 1));

		useHTTPHeader.add(httpHeaderNameLabel, new Rectangle(0, 1, 1, 1));
		useHTTPHeader.add(httpHeaderName, new Rectangle(1, 1, 1, 1));
		row += 2;
		add(useHTTPHeader, new Rectangle(0, row, 3, 2));

		useEnvironmentVariable.add(environmentVarNameLabel, new Rectangle(0, 1, 1, 1));
		useEnvironmentVariable.add(environmentVarName, new Rectangle(1, 1, 1, 1));
		row += 3;
		add(useEnvironmentVariable, new Rectangle(0, row, 3, 2));

		row += 3;
		add(logoutUrlLabel, new Rectangle(0, row, 1, 1));

		// .. except this time
		add(logoutUrl, new Rectangle(1, row, 2, 1));

		useRemoteUser.setEnabled(false);
		useHTTPHeader.setEnabled(false);
		useEnvironmentVariable.setEnabled(false);

		validate();
	}

	private String getString(String key)
	{
		return CurrentLocale.get("com.tle.admin.usermanagement.shibboleth.generalsettings." + key); //$NON-NLS-1$
	}

	public void load(ExternalAuthorisationWrapperSettings settings)
	{
		httpHeaderName.setText(settings.getHttpHeaderName());
		environmentVarName.setText(settings.getEnvironmentVarName());
		logoutUrl.setText(settings.getLogoutUrl());

		final String usage = settings.getUsageType();
		if( Check.isEmpty(usage) )
		{
			// default to remoteUser
			useRemoteUser.setSelected(true);
		}
		else
		{
			useRemoteUser.setSelected(USAGE_REMOTE_USER.equals(usage));
			useEnvironmentVariable.setSelected(USAGE_ENV_VAR.equals(usage));
			useHTTPHeader.setSelected(USAGE_HTTP_HEADER.equals(usage));
		}
	}

	public void save(ExternalAuthorisationWrapperSettings settings)
	{
		settings.setHttpHeaderName(httpHeaderName.getText());
		settings.setEnvironmentVarName(environmentVarName.getText());
		settings.setLogoutUrl(logoutUrl.getText());

		if( useHTTPHeader.isSelected() )
		{
			settings.setUsageType(USAGE_HTTP_HEADER);
		}
		else if( useEnvironmentVariable.isSelected() )
		{
			settings.setUsageType(USAGE_ENV_VAR);
		}
		else
		{
			settings.setUsageType(USAGE_REMOTE_USER);
		}
	}

	@Override
	public void clearChanges()
	{
		changeDetector.clearChanges();
	}

	@Override
	public boolean hasDetectedChanges()
	{
		return changeDetector.hasDetectedChanges();
	}
}
