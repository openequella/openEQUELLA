/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.soap.service;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemPack;
import com.tle.beans.user.TLEGroup;
import com.tle.common.usermanagement.user.valuebean.UserBean;

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
