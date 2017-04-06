package com.tle.admin.taxonomy.tool;

import java.awt.LayoutManager;

import javax.swing.JComponent;

import com.dytech.gui.Changeable;
import com.tle.admin.baseentity.DynamicTabService;
import com.tle.admin.gui.common.DynamicChoicePanel;
import com.tle.common.applet.client.ClientService;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.core.plugins.PluginService;

public abstract class DataSourceChoice extends DynamicChoicePanel<Taxonomy>
{
	private JComponent existingTab;
	private DynamicTabService dynamicTabService;
	private ClientService clientService;
	private PluginService pluginService;
	private boolean readonly;
	private String entityUuid;

	public DataSourceChoice()
	{
		super();
	}

	public DataSourceChoice(LayoutManager lm)
	{
		super(lm);
	}

	protected void addTab(final String tabName, final JComponent comp)
	{
		if( tabName != null && comp != null )
		{
			existingTab = comp;
			dynamicTabService.addTab(comp, tabName, 1);
		}
	}

	@Override
	public void choiceDeselected()
	{
		if( existingTab != null )
		{
			dynamicTabService.removeTab(existingTab);
		}
	}

	@Override
	public boolean hasDetectedChanges()
	{
		if( super.hasDetectedChanges() )
		{
			return true;
		}

		if( existingTab instanceof Changeable )
		{
			Changeable ch = (Changeable) existingTab;
			return ch.hasDetectedChanges();
		}

		return false;
	}

	@Override
	public void clearChanges()
	{
		super.clearChanges();
		if( existingTab instanceof Changeable )
		{
			Changeable ch = (Changeable) existingTab;
			ch.clearChanges();
		}
	}

	public ClientService getClientService()
	{
		return clientService;
	}

	public void setDynamicTabService(DynamicTabService dynamicTabService)
	{
		this.dynamicTabService = dynamicTabService;
	}

	public PluginService getPluginService()
	{
		return pluginService;
	}

	public void setClientService(ClientService clientService)
	{
		this.clientService = clientService;
	}

	public void setPluginService(PluginService pluginService)
	{
		this.pluginService = pluginService;
	}

	public boolean isReadonly()
	{
		return readonly;
	}

	public void setReadOnly(boolean readonly)
	{
		this.readonly = readonly;
	}

	public String getEntityUuid()
	{
		return entityUuid;
	}

	public void setEntityUuid(String entityUuid)
	{
		this.entityUuid = entityUuid;
	}

	public void afterSave()
	{
		// Nothing by default
	}
}
