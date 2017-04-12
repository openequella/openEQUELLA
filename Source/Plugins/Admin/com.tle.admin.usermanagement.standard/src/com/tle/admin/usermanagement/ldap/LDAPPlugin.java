package com.tle.admin.usermanagement.ldap;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tle.admin.plugin.GeneralPlugin;
import com.tle.beans.usermanagement.standard.LDAPSettings;
import com.tle.common.i18n.CurrentLocale;

public class LDAPPlugin extends GeneralPlugin<LDAPSettings> implements ChangeListener
{
	private static final Log LOGGER = LogFactory.getLog(LDAPPlugin.class);

	private LDAPGeneralSettings generalSettings;
	private LDAPMappingPanel mapping;
	private JTabbedPane tabbed;
	protected LDAPSettings settings;

	protected int selectedTabIndex;
	protected AbstractLDAPPanel selected;
	protected List<AbstractLDAPPanel> plugins = new ArrayList<AbstractLDAPPanel>();

	public LDAPPlugin()
	{
		super();
	}

	@Override
	public void init()
	{
		generalSettings = new LDAPGeneralSettings();
		mapping = new LDAPMappingPanel(clientService);
		tabbed = new JTabbedPane();
		setup();
	}

	private void addPlugin(AbstractLDAPPanel plugin)
	{
		JComponent component = plugin.getComponent();
		if( plugin.needsScrollPane() )
		{
			component = new JScrollPane(component);
		}
		tabbed.add(component, plugin.getName());
		plugins.add(plugin);
	}

	private void setup()
	{
		addPlugin(generalSettings);
		addPlugin(mapping);

		selectedTabIndex = 0;
		tabbed.addChangeListener(this);

		selected = generalSettings;
		addFillComponent(tabbed);
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		int index = tabbed.getSelectedIndex();
		if( index == selectedTabIndex )
		{
			return;
		}

		AbstractLDAPPanel comp = plugins.get(index);
		try
		{
			selected.applySettings();
			comp.showPanel();
			selected = comp;
			selectedTabIndex = index;
		}
		catch( Exception e1 )
		{
			LOGGER.error("Error applying LDAP settings", e1);
			JOptionPane.showMessageDialog(tabbed, CurrentLocale.get("com.tle.admin.usermanagement.ldapplugin.error")); //$NON-NLS-1$
			tabbed.setSelectedIndex(selectedTabIndex);
		}
	}

	@Override
	public void load(LDAPSettings ls)
	{
		this.settings = ls;
		generalSettings.load(ls);
		mapping.load(ls);
	}

	@Override
	public boolean save(LDAPSettings ls)
	{
		generalSettings.save(ls);
		mapping.save(ls);
		return true;
	}
}
