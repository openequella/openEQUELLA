package com.dytech.edge.ejb.helpers.metadata.mapping;

import java.util.Map;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.itemdef.mapping.IMSMapping.MappingType;

/**
 * @author aholland
 */
public class GenericPackageMapping extends Mapping
{
	private final PropBagEx xml;

	public GenericPackageMapping(String path, PropBagEx xml)
	{
		super(path, "", MappingType.COMPOUND, false); //$NON-NLS-1$
		this.xml = xml;
	}

	@Override
	public void update(PropBagEx item)
	{
		PropBagEx sub = item.newSubtree(getPath());
		sub.appendChildren("", xml); //$NON-NLS-1$

		// We need to do this to get attributes from the root node
		// (Just in case - but should be rare)
		for( Map.Entry<String, String> entry : xml.getAttributesForNode("").entrySet() )
		{
			sub.setNode("/@" + entry.getKey(), entry.getValue());
		}
	}
}
