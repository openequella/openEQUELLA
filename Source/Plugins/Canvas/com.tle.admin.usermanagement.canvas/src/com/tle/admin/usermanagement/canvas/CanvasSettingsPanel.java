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

package com.tle.admin.usermanagement.canvas;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.Changeable;
import com.google.common.base.Strings;
import com.tle.beans.usermanagement.canvas.CanvasWrapperSettings;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class CanvasSettingsPanel extends JPanel implements Changeable, MouseListener
{
	private static final long serialVersionUID = 5306265445292956845L;

	private static final String CANVAS_SIGNUP_URL = "http://instructure.github.io/";

	protected JCheckBox bypassLogon;
	protected JTextField canvasUrl;
	protected JTextField clientId;
	protected JPasswordField secret;
	protected JLabel preamble;

	protected ChangeDetector changeDetector;

	public CanvasSettingsPanel()
	{
		changeDetector = new ChangeDetector();
		setupGui();
	}

	protected void setupGui()
	{
		final JLabel canvasUrlLabel = new JLabel(getString("label.canvasurl"));
		final JLabel clientIdLabel = new JLabel(getString("label.clientid"));
		final JLabel secretLabel = new JLabel(getString("label.secret"));
		preamble = new JLabel(getString("preamble", CANVAS_SIGNUP_URL));
		preamble.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		preamble.addMouseListener(this);

		canvasUrl = new JTextField(20);
		clientId = new JTextField(20);
		secret = new JPasswordField(20);
		bypassLogon = new JCheckBox();
		bypassLogon.setText(getString("label.bypasslogon"));

		final LayoutManager layout = new MigLayout("wrap", "[fill][fill,grow]");
		setLayout(layout);

		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		add(preamble, "span 2, gapbottom 20");
		add(canvasUrlLabel);
		add(canvasUrl);
		add(clientIdLabel);
		add(clientId);
		add(secretLabel);
		add(secret);
		add(bypassLogon, "span 2");

		validate();
	}

	private String getString(String key, Object... args)
	{
		return CurrentLocale.get("com.tle.admin.usermanagement.canvas.generalsettings." + key, args);
	}

	public void load(CanvasWrapperSettings settings)
	{
		String canvasUrlVal = settings.getCanvasUrl();
		if( Strings.isNullOrEmpty(canvasUrlVal) )
		{
			canvasUrlVal = "https://canvas.instructure.com/";
		}
		canvasUrl.setText(canvasUrlVal);
		clientId.setText(settings.getClientId());
		secret.setText(settings.getClientSecret());
		bypassLogon.setSelected(settings.isBypassLogonPage());
	}

	public void save(CanvasWrapperSettings settings)
	{
		settings.setCanvasUrl(canvasUrl.getText());
		settings.setClientId(clientId.getText());
		settings.setClientSecret(new String(secret.getPassword()));
		settings.setBypassLogonPage(bypassLogon.isSelected());
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

	@Override
	public void mouseClicked(MouseEvent e)
	{
		if( e.getSource() == preamble )
		{
			try
			{
				Desktop desktop = java.awt.Desktop.getDesktop();
				URI uri = new java.net.URI(CANVAS_SIGNUP_URL);
				desktop.browse(uri);
			}
			catch( Exception ex )
			{
				ex.printStackTrace();
				JOptionPane.showMessageDialog(null, "Unable to open link in the system browser",
					"Could not follow link", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		// Nothing
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		// Nothing
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
		// Nothing
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
		// Nothing
	}
}
