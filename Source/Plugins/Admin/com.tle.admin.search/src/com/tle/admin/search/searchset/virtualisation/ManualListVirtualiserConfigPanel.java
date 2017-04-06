package com.tle.admin.search.searchset.virtualisation;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import com.dytech.common.xml.TLEXStream;
import com.dytech.gui.JShuffleList;
import com.tle.admin.gui.common.DynamicChoicePanel;
import com.tle.common.Check;
import com.tle.common.search.searchset.SearchSet;

public class ManualListVirtualiserConfigPanel extends DynamicChoicePanel<SearchSet>
{
	private JShuffleList<String> list;
	private static TLEXStream xstream = new TLEXStream();

	public ManualListVirtualiserConfigPanel()
	{
		super(new GridLayout(1, 1));

		list = JShuffleList.newDefaultShuffleList(false);
		add(list);

		changeDetector.watch(list.getModel());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void load(SearchSet searchSet)
	{
		String v = searchSet.getAttribute(getId());
		if( !Check.isEmpty(v) )
		{
			list.addItems((List<String>) xstream.fromXML(v));
		}
	}

	@Override
	public void save(SearchSet searchSet)
	{
		searchSet.setAttribute(getId(), xstream.toXML(new ArrayList<String>(list.getItems())));
	}

	@Override
	public void removeSavedState(SearchSet searchSet)
	{
		searchSet.removeAttribute(getId());
	}
}
