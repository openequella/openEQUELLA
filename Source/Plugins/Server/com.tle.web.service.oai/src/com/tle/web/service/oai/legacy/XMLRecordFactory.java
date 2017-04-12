/*
 * Created on Oct 19, 2005
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