package com.tle.core.soap;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.valuebean.UserBean;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemPack;
import com.tle.beans.user.TLEGroup;

/**
 * @author aholland
 */
public interface SoapXMLService
{
	PropBagEx convertCollectionToXML(final ItemDefinition collection);

	PropBagEx convertSchemaToXML(final Schema schema);

	/**
	 * @param pack
	 * @param everything Include badUrls, history and moderation
	 * @return
	 * @throws Exception
	 */
	PropBagEx convertItemPackToXML(final ItemPack pack, boolean everything);

	PropBagEx convertUserToXML(final UserBean user);

	PropBagEx convertGroupToXML(final TLEGroup group);
}
