package com.tle.admin.controls.itunesu.universal;

import javax.swing.JLabel;
import javax.swing.JTextField;

import com.tle.admin.controls.universal.UniversalControlSettingPanel;
import com.tle.admin.controls.universal.UniversalPanelValidator;
import com.tle.common.Check;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.wizard.controls.universal.UniversalControl;
import com.tle.common.wizard.controls.universal.UniversalSettings;
import com.tle.common.wizard.controls.universal.handlers.ITunesUSettings;

@SuppressWarnings("nls")
public class ITunesUSettingsPanel extends UniversalControlSettingPanel implements UniversalPanelValidator
{
	private final JTextField institutionId;

	public ITunesUSettingsPanel()
	{
		super(); // thanks for asking
		JLabel institutionIdLabel = new JLabel(CurrentLocale.get("com.tle.admin.controls.itunesu.institutionid"));
		institutionId = new JTextField();
		add(institutionIdLabel);
		add(institutionId);
	}

	@Override
	protected String getTitleKey()
	{
		return "com.tle.admin.controls.itunesu.settings.title";
	}

	@Override
	public void load(UniversalSettings state)
	{
		institutionId.setText(new ITunesUSettings(state).getInstitutionId());
	}

	@Override
	public void removeSavedState(UniversalSettings state)
	{
		// Nothing
	}

	@Override
	public void save(UniversalSettings state)
	{
		new ITunesUSettings(state).setInstitutionId(institutionId.getText());
	}

	private String s(String postfix)
	{
		return CurrentLocale.get("com.tle.admin.controls.itunesu.settings." + postfix);
	}

	@Override
	public String doValidation(UniversalControl control, ClientService clientService)
	{
		ITunesUSettings is = new ITunesUSettings(control);
		if( Check.isEmpty(is.getInstitutionId()) )
		{
			return s("noinstitutionid");
		}
		return null;
	}

	@Override
	public String getValidatorType()
	{
		return "iTunesUHandler";
	}

}
