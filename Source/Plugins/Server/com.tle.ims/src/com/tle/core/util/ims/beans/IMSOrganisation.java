package com.tle.core.util.ims.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dytech.common.xml.XMLDataMappings;
import com.dytech.common.xml.mapping.ListMapping;
import com.tle.common.Utils;

public class IMSOrganisation extends IMSChild
{
	private static final long serialVersionUID = 1L;
	private static XMLDataMappings mappings;

	private List<IMSItem> items = new ArrayList<IMSItem>();

	public void addToXMLString(StringBuilder sbuf, Map<String, IMSResource> name)
	{
		sbuf.append("<wrapper type=\"1\">"); //$NON-NLS-1$
		sbuf.append(Utils.ent(getTitle()));

		for( IMSItem item : items )
		{
			item.addToXMLString(sbuf, name);
		}

		sbuf.append("</wrapper>"); //$NON-NLS-1$
	}

	/**
	 * @return Returns the items.
	 */
	public List<IMSItem> getItems()
	{
		return items;
	}

	public void setItems(List<IMSItem> items)
	{
		this.items = items;
	}

	@Override
	@SuppressWarnings("nls")
	public synchronized XMLDataMappings getMappings()
	{
		if( mappings == null )
		{
			mappings = new XMLDataMappings(super.getMappings());
			// Presumably the intent is to return the implementation class, so
			// we ignore Sonar's "loose coupling" warning
			mappings.addNodeMapping(new ListMapping("items", "item", ArrayList.class, IMSItem.class)); // NOSONAR
		}
		return mappings;
	}

	@Override
	public String getTitle()
	{
		// If there was no title on this org:
		String actTitle = super.getTitle();
		if( actTitle.length() == 0 )
		{
			if( items.size() == 1 )
			{
				// If there was only one item then use the item's name
				actTitle = items.get(0).getTitle();
			}
			else if( parent.getTitle().length() > 0 )
			{
				// If the manifest (parent) has a name then use the its title
				actTitle = parent.getTitle();
			}
			else if( items.size() > 0 )
			{
				// If there is at least one item then use the first item's name
				actTitle = items.get(0).getTitle();
			}
			else
			{
				actTitle = "Unnamed"; //$NON-NLS-1$
			}
		}
		return actTitle;
	}
}