package com.tle.admin.tools.common;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.java.plugin.registry.Extension;

import com.tle.beans.entity.BaseEntity;
import com.tle.common.EntityPack;
import com.tle.common.i18n.CurrentLocale;

@SuppressWarnings("nls")
public abstract class BaseEntityWithAddDialogTool<T extends BaseEntity> extends BaseEntityTool<T>
{
	private final SortedMap<String, String> plugins = new TreeMap<String, String>();
	private final Map<String, Extension> tools = new HashMap<String, Extension>();

	public BaseEntityWithAddDialogTool(Class<T> entityClass, String entityType) throws Exception
	{
		super(entityClass, entityType);
	}

	@Override
	public void setup(Set<String> grantedPrivileges, String toolName)
	{
		super.setup(grantedPrivileges, toolName);

		Collection<Extension> extensions = driver.getPluginService().getConnectedExtensions(
			"com.tle.admin.fedsearch.tool", "configUI");
		for( Extension extension : extensions )
		{
			String type = extension.getParameter("type").valueAsString();
			String name = extension.getParameter("name").valueAsString();
			plugins.put(name, type);
			tools.put(type, extension);
		}
	}

	@SuppressWarnings("unchecked")
	public <U> U getToolInstance(String type)
	{
		Extension extension = tools.get(type);
		return (U) driver.getPluginService().getBean(extension.getDeclaringPluginDescriptor(),
			extension.getParameter("class").valueAsString());
	}

	@Override
	protected EntityPack<T> create()
	{
		PluginDialog plugin = new PluginDialog();
		JOptionPane.showMessageDialog(parentFrame, plugin,
			CurrentLocale.get("com.tle.admin.gui.searchtool.createdialog.title"), JOptionPane.QUESTION_MESSAGE); //$NON-NLS-1$
		String tool = plugin.getSelectedTool();
		EntityPack<T> pack = null;
		if( tool != null )
		{
			pack = super.create();
			setTypeForCreate(pack.getEntity(), plugins.get(tool));
		}
		return pack;
	}

	protected abstract void setTypeForCreate(T entity, String type);

	private class PluginDialog extends JPanel
	{
		private JList<String> list;
		private DefaultListModel<String> listModel;

		public PluginDialog()
		{
			listModel = new DefaultListModel<>();

			list = new JList<>();
			list.setModel(listModel);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			for( String name : plugins.keySet() )
			{
				listModel.addElement(name);
			}

			setLayout(new BorderLayout());
			add(new JScrollPane(list));
		}

		public String getSelectedTool()
		{
			return list.getSelectedValue();
		}
	}
}
