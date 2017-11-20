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

package com.tle.core.soap.service.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.google.common.base.Strings;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemPack;
import com.tle.beans.user.TLEGroup;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.guice.Bind;
import com.tle.core.item.helper.ItemHelper;
import com.tle.core.item.helper.ItemHelper.ItemHelperSettings;
import com.tle.core.soap.service.SoapXMLService;

/**
 * @author aholland
 */
@Singleton
@SuppressWarnings("nls")
@Bind(SoapXMLService.class)
public class SoapXMLServiceImpl implements SoapXMLService
{
	@Inject
	private ItemHelper itemHelper;

	@Override
	public PropBagEx convertItemPackToXML(final ItemPack pack, boolean everything)
	{
		return itemHelper.convertToXml(pack, new ItemHelperSettings(everything));
	}

	@Override
	public PropBagEx convertCollectionToXML(final ItemDefinition collection)
	{
		PropBagEx collectionXML = new PropBagEx().newSubtree("itemdef");
		collectionXML.setNode("id", collection.getId());
		collectionXML.setNode("uuid", collection.getUuid());
		collectionXML.setNode("name", CurrentLocale.get(collection.getName()));
		collectionXML.setNode("system", collection.isSystemType());
		Schema schema = collection.getSchema();
		if( schema != null )
		{
			collectionXML.setNode("schemaUuid", schema.getUuid());
		}
		return collectionXML;
	}

	@Override
	public PropBagEx convertUserToXML(final UserBean user)
	{
		PropBagEx userXML = new PropBagEx().newSubtree("user");
		userXML.setNode("uuid", user.getUniqueID());
		userXML.setNode("username", user.getUsername());
		userXML.setNode("firstName", Strings.nullToEmpty(user.getFirstName()));
		userXML.setNode("lastName", Strings.nullToEmpty(user.getLastName()));
		userXML.setNode("email", Strings.nullToEmpty(user.getEmailAddress()));
		return userXML;
	}

	@Override
	public PropBagEx convertSchemaToXML(final Schema schema)
	{
		PropBagEx schemaXML = new PropBagEx().newSubtree("schema");
		schemaXML.setNode("uuid", schema.getUuid());
		schemaXML.setNode("itemNamePath", schema.getItemNamePath());
		schemaXML.setNode("itemDescriptionPath", schema.getItemDescriptionPath());
		schemaXML.setNode("name", CurrentLocale.get(schema.getName()));
		schemaXML.setNode("description", CurrentLocale.get(schema.getDescription()));
		return schemaXML;
	}

	@Override
	public PropBagEx convertGroupToXML(TLEGroup group)
	{
		PropBagEx groupXML = new PropBagEx().newSubtree("group");
		groupXML.setNode("uuid", group.getUuid());
		groupXML.setNode("name", group.getName());
		return groupXML;
	}
}
