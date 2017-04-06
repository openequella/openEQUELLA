package com.tle.web.inplaceeditor;

import java.awt.Component;
import java.io.IOException;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import com.tle.common.gui.models.GenericListModel;

@SuppressWarnings("nls")
public abstract class AbstractListDialogOpener implements Opener
{
	@Override
	public void openWith(Component parent, String filepath, String mimetype) throws IOException
	{
		List<App> apps = getAppList(filepath, mimetype);
		JList<App> list = new JList<>(new GenericListModel<App>(apps));
		list.setCellRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus)
			{
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				setText(((App) value).getName());
				return this;
			}
		});

		int rv = JOptionPane.showOptionDialog(parent, new JScrollPane(list), "Open with...", JOptionPane.OK_OPTION,
			JOptionPane.QUESTION_MESSAGE, null, new Object[]{"Launch",}, null);
		if( rv != JOptionPane.OK_OPTION )
		{
			return;
		}

		int i = list.getSelectedIndex();
		if( i < 0 )
		{
			return;
		}

		executeApp(apps.get(i), filepath, mimetype);
	}

	protected abstract List<App> getAppList(String filepath, String mimetype) throws IOException;

	protected abstract void executeApp(App app, String filepath, String mimetype) throws IOException;

	public static class App
	{
		private final String name;
		private final String exec;

		public App(String name, String exec)
		{
			this.name = name;
			this.exec = exec;
		}

		public String getExec()
		{
			return exec;
		}

		public String getName()
		{
			return name;
		}
	}
}
