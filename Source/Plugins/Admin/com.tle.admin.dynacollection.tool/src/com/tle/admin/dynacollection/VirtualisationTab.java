/*
 * Created on May 10, 2005
 */
package com.tle.admin.dynacollection;

import java.awt.Component;
import java.awt.GridLayout;

import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.gui.EditorException;
import com.tle.admin.search.searchset.virtualisation.VirtualisationEditor;
import com.tle.beans.entity.DynaCollection;
import com.tle.common.dynacollection.SearchSetAdapter;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class VirtualisationTab extends BaseEntityTab<DynaCollection>
{
	private VirtualisationEditor editor;

	@Override
	public void init(Component parent)
	{
		editor = new VirtualisationEditor(pluginService, clientService, "com.tle.admin.dynacollection.entityname",
			"com.tle.admin.dynacollection.virtualisationtab.renamingHelp");

		setLayout(new GridLayout(1, 1));
		add(editor);

		if( state.isReadonly() )
		{
			editor.setEnabled(false);
		}
	}

	@Override
	public String getTitle()
	{
		return CurrentLocale.get("com.tle.admin.dynacollection.virtualisationtab.title");
	}

	@Override
	public void load()
	{
		editor.load(new SearchSetAdapter(state.getEntity()));
	}

	@Override
	public void save()
	{
		editor.save(new SearchSetAdapter(state.getEntity()));
	}

	@Override
	public void validation() throws EditorException
	{
		editor.validation();
	}
}
