package com.tle.admin.controls.kaltura.universal;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

import com.dytech.common.text.NumberStringComparator;
import com.tle.admin.controls.universal.UniversalControlSettingPanel;
import com.tle.admin.controls.universal.UniversalPanelValidator;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.applet.client.ClientService;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.kaltura.admin.control.KalturaSettings;
import com.tle.common.kaltura.admin.control.KalturaSettings.KalturaOption;
import com.tle.common.kaltura.entity.KalturaServer;
import com.tle.common.kaltura.service.RemoteKalturaService;
import com.tle.common.wizard.controls.universal.UniversalControl;
import com.tle.common.wizard.controls.universal.UniversalSettings;
import com.tle.i18n.BundleCache;

@SuppressWarnings("nls")
public class KalturaSettingsPanel extends UniversalControlSettingPanel implements UniversalPanelValidator
{
	private static final long serialVersionUID = 1L;

	private JLabel serverLabel;
	private JComboBox serverSelect;
	private JRadioButton radioNone;
	private JRadioButton radioUpload;
	private JRadioButton radioExisting;

	public KalturaSettingsPanel()
	{
		super();
		createGui();
	}

	private void createGui()
	{
		serverLabel = new JLabel(s("server"));
		serverSelect = new JComboBox();

		add(serverLabel);
		add(serverSelect);

		add(new JLabel(s("restrictions.label")));
		ButtonGroup group = new ButtonGroup();

		radioNone = new JRadioButton(s("restrictions.none"), true);
		radioUpload = new JRadioButton(s("restrictions.upload"));
		radioExisting = new JRadioButton(s("restrictions.existing"));

		group.add(radioNone);
		group.add(radioUpload);
		group.add(radioExisting);

		add(radioNone);
		add(radioUpload, "skip 1");
		add(radioExisting, "skip 1");
	}

	@Override
	public void init(UniversalControl control, int wizardType, SchemaModel schemaModel,
		com.tle.common.applet.client.ClientService clientService)
	{
		super.init(control, wizardType, schemaModel, clientService);

		List<BaseEntityLabel> kalturaServers = getClientService().getService(RemoteKalturaService.class).listAll();
		List<NameValue> nameValues = BundleCache.getNameUuidValues(kalturaServers);
		Collections.sort(nameValues, new NumberStringComparator<NameValue>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String convertToString(NameValue nv)
			{
				return nv.getName();
			}
		});
		AppletGuiUtils.addItemsToJCombo(serverSelect, nameValues);
	}

	@Override
	protected String getTitleKey()
	{
		return "com.tle.admin.controls.kaltura.settings.title";
	}

	private String s(String postfix)
	{
		return CurrentLocale.get("com.tle.admin.controls.kaltura.settings." + postfix);
	}

	@Override
	public void load(UniversalSettings state)
	{
		final KalturaSettings settings = new KalturaSettings(state.getWrapped());
		AppletGuiUtils.selectInJCombo(serverSelect, new NameValue("", settings.getServerUuid()), 0);

		String restriction = settings.getRestriction();
		if( restriction == null )
		{
			radioNone.setSelected(true);
		}
		else if( restriction.equals(KalturaOption.UPLOAD.name()) )
		{
			radioUpload.setSelected(true);
		}
		else
		{
			radioExisting.setSelected(true);
		}
	}

	@Override
	public void afterLoad(UniversalSettings state)
	{
		final KalturaSettings settings = new KalturaSettings(state.getWrapped());

		if( !AppletGuiUtils.isInJCombo(serverSelect, new NameValue("", settings.getServerUuid())) )
		{
			changeDetector.forceChange(serverSelect);
		}
	}

	@Override
	public void removeSavedState(UniversalSettings state)
	{
		// Nothing to do here
	}

	@Override
	public void save(UniversalSettings state)
	{
		final KalturaSettings settings = new KalturaSettings(state.getWrapped());
		NameValue nv = (NameValue) serverSelect.getSelectedItem();
		String selectedItem = "";
		if( nv != null )
		{
			selectedItem = nv.getValue();
		}
		settings.setServerUuid(selectedItem);

		if( radioNone.isSelected() )
		{
			settings.setRestriction(null);
		}

		if( radioUpload.isSelected() )
		{
			settings.setRestriction(KalturaOption.UPLOAD.name());

		}

		if( radioExisting.isSelected() )
		{
			settings.setRestriction(KalturaOption.EXISTING.name());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() instanceof JCheckBox )
		{
			JCheckBox box = (JCheckBox) e.getSource();

			if( box.isSelected() && Check.isEmpty(getClientService().getService(RemoteKalturaService.class).listAll()) )
			{
				box.setSelected(false);
				JOptionPane.showMessageDialog(box.getParent(), s("noservers"));
			}
		}
	}

	@Override
	public String doValidation(UniversalControl control, ClientService clientService)
	{
		KalturaSettings ks = new KalturaSettings(control);

		RemoteKalturaService kalturaService = clientService.getService(RemoteKalturaService.class);

		KalturaServer server = kalturaService.getByUuid(ks.getServerUuid());

		if( server == null )
		{
			return s("invalid");
		}

		return null;
	}

	@Override
	public String getValidatorType()
	{
		return "kalturaHandler";
	}
}
