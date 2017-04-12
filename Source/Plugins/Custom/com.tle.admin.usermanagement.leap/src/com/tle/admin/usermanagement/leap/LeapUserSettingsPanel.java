package com.tle.admin.usermanagement.leap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.Changeable;
import com.tle.beans.usermanagement.leap.wrapper.LeapUserWrapperSettings;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author aholland
 */
public class LeapUserSettingsPanel extends JPanel implements Changeable
{
	private static final long serialVersionUID = 5306265445292956845L;

	protected JTextField endpointUrl;
	protected ChangeDetector changeDetector;

	public LeapUserSettingsPanel()
	{
		changeDetector = new ChangeDetector();
		setupGui();
	}

	protected void setupGui()
	{
		final JLabel endpointUrlLabel = new JLabel(getString("endpointurl"));

		endpointUrl = new JTextField();

		setLayout(new MigLayout("fillx,wrap 2", "[align label][fill,grow]"));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		add(endpointUrlLabel);
		add(endpointUrl);

		validate();
	}

	private String getString(String key)
	{
		return CurrentLocale.get("com.tle.admin.usermanagement.leap.generalsettings." + key);
	}

	public void load(LeapUserWrapperSettings settings)
	{
		endpointUrl.setText(settings.getEndpointUrl());
	}

	public void save(LeapUserWrapperSettings settings)
	{
		settings.setEndpointUrl(endpointUrl.getText());
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
