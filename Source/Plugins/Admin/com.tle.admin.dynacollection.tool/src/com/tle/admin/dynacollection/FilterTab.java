/*
 * Created on May 10, 2005
 */
package com.tle.admin.dynacollection;

import java.awt.Component;
import java.awt.GridLayout;

import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.gui.EditorException;
import com.tle.admin.search.searchset.SearchSetFilter;
import com.tle.beans.entity.DynaCollection;
import com.tle.common.applet.client.EntityCache;
import com.tle.common.dynacollection.SearchSetAdapter;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class FilterTab extends BaseEntityTab<DynaCollection>
{
	private final EntityCache cache;

	private SearchSetFilter filter;

	public FilterTab(EntityCache cache)
	{
		this.cache = cache;
	}

	@Override
	public void init(Component parent)
	{
		filter = new SearchSetFilter(cache, clientService);

		setLayout(new GridLayout(1, 1));
		add(filter);

		if( state.isReadonly() )
		{
			filter.setEnabled(false);
		}
	}

	@Override
	public String getTitle()
	{
		return CurrentLocale.get("com.tle.admin.dynacollection.filtertab.title"); //$NON-NLS-1$
	}

	@Override
	public void load()
	{
		filter.load(new SearchSetAdapter(state.getEntity()));
	}

	@Override
	public void save()
	{
		filter.save(new SearchSetAdapter(state.getEntity()));
	}

	@Override
	public void validation() throws EditorException
	{
		// Nothing to validate
	}
}
