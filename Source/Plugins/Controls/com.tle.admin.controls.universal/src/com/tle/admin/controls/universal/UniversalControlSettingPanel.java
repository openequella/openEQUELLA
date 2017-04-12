package com.tle.admin.controls.universal;

import java.awt.Font;
import java.awt.LayoutManager;

import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import com.tle.admin.gui.common.DynamicChoicePanel;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.wizard.controls.universal.UniversalControl;
import com.tle.common.wizard.controls.universal.UniversalSettings;

/**
 * @author Aaron
 */
public abstract class UniversalControlSettingPanel extends DynamicChoicePanel<UniversalSettings>
{
	private int wizardType;
	private SchemaModel schemaModel;
	private UniversalControl control;
	private ClientService clientService;

	/**
	 * Uses a MigLayout("wrap 2, insets 10 15 20 5", "[][fill, grow]")
	 */
	protected UniversalControlSettingPanel()
	{
		super(new MigLayout("wrap 2, insets 10 15 20 5", "[][fill, grow]"));
		JLabel label = new JLabel(CurrentLocale.get(getTitleKey()));
		Font titleFont = label.getFont();
		label.setFont(new Font(titleFont.getName(), Font.BOLD, titleFont.getSize()));
		add(label, "span 2, gapbottom 10");
	}

	protected UniversalControlSettingPanel(LayoutManager layout)
	{
		super(layout);
	}

	protected abstract String getTitleKey();

	public void init(UniversalControl control, int wizardType, SchemaModel schemaModel, ClientService clientService)
	{
		this.control = control;
		this.wizardType = wizardType;
		this.schemaModel = schemaModel;
		this.clientService = clientService;
	}

	public int getWizardType()
	{
		return wizardType;
	}

	public SchemaModel getSchemaModel()
	{
		return schemaModel;
	}

	public UniversalControl getControl()
	{
		return control;
	}

	public ClientService getClientService()
	{
		return clientService;
	}
}
