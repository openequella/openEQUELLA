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

package com.tle.web.service.oai.legacy;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import javax.inject.Inject;

import ORG.oclc.oai.server.catalog.RecordFactory;
import ORG.oclc.oai.server.crosswalk.Crosswalks;
import ORG.oclc.oai.server.verb.ServerVerb;

import com.dytech.edge.common.valuebean.ItemKey;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;

@Deprecated
public class XMLRecordFactory extends RecordFactory
{
	@Inject
	private TLECrosswalk cw;

	public XMLRecordFactory(Properties properties)
	{
		super(properties);
	}

	@Override
	public Crosswalks getCrosswalks()
	{
		return cw;
	}

	@Override
	public String fromOAIIdentifier(String identifier)
	{
		return identifier;
	}

	@Override
	public String quickCreate(Object nativeItem, String schemaLocation, String metadataPrefix)
	{
		return null;
	}

	@Override
	public String getOAIIdentifier(Object nativeItem)
	{
		Item item = (Item) nativeItem;
		return "tle:" //$NON-NLS-1$
			+ new ItemKey(item.getUuid(), item.getVersion(), item.getItemDefinition().getUuid()).toString();
	}

	@Override
	public String getDatestamp(Object nativeItem)
	{
		Item xml = (Item) nativeItem;
		return convertISODate(xml.getDateModified());
	}

	private String convertISODate(Date date)
	{
		return ServerVerb.createResponseDate(date);
	}

	@Override
	public Iterator<String> getSetSpecs(Object nativeItem)
	{
		Item xml = (Item) nativeItem;
		return Collections.singleton(xml.getItemDefinition().getUuid()).iterator();
	}

	@Override
	public boolean isDeleted(Object nativeItem)
	{
		Item item = (Item) nativeItem;
		return item.getStatus().equals(ItemStatus.DELETED);
	}

	@Override
	public Iterator<?> getAbouts(Object nativeItem)
	{
		// I don't think we need to support this (yet)
		return null;
	}

}