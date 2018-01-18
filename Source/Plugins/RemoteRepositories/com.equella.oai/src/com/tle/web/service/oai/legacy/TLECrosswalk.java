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

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.dytech.devlib.PropBagEx;
import com.google.common.base.Throwables;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.SchemaTransform;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.core.guice.Bind;
import com.tle.core.item.helper.ItemHelper;
import com.tle.core.item.service.ItemService;
import com.tle.core.schema.service.SchemaService;

import ORG.oclc.oai.server.crosswalk.Crosswalk;
import ORG.oclc.oai.server.crosswalk.CrosswalkItem;
import ORG.oclc.oai.server.crosswalk.Crosswalks;
import ORG.oclc.oai.server.verb.CannotDisseminateFormatException;

@Deprecated
@Bind
@Singleton
public class TLECrosswalk extends Crosswalks
{
	private static final Logger LOGGER = Logger.getLogger(TLECrosswalk.class);

	public static final String DC_FORMAT = "oai_dc"; //$NON-NLS-1$

	@Inject
	private SchemaService schemaService;
	@Inject
	private ItemService itemService;
	@Inject
	private Crosswalk crosswalk;
	@Inject
	private ItemHelper itemHelper;

	private Map<String, CrosswalkItem> crosswalksMap;

	@Inject
	public TLECrosswalk(OAIProperties properties)
	{
		super(properties.getProperties());

		setupMap();
		init();
	}

	@SuppressWarnings("unchecked")
	private void setupMap()
	{
		Field field;
		try
		{
			field = Crosswalks.class.getDeclaredField("crosswalksMap"); //$NON-NLS-1$
			field.setAccessible(true);
			crosswalksMap = (Map<String, CrosswalkItem>) field.get(this);
		}
		catch( Exception e )
		{
			Throwables.propagate(e);
		}
	}

	private class TLECross extends Crosswalk
	{
		private final String prefix;

		public TLECross(String prefix, String schemaLocation, String contentType, String docType, String encoding)
		{
			super(schemaLocation, contentType, docType, encoding);
			this.prefix = prefix;
		}

		@Override
		public boolean isAvailableFor(Object nativeItem)
		{
			Schema schema = getSchema((Item) nativeItem);
			Iterator<SchemaTransform> i = schema.getExportTransforms().iterator();
			boolean isAvailable = false;
			while( !isAvailable && i.hasNext() )
			{
				isAvailable = i.next().getType().equalsIgnoreCase(getPrefix());
			}
			return isAvailable;
		}

		@Override
		public String createMetadata(Object nativeItem) throws CannotDisseminateFormatException
		{
			Item item = (Item) nativeItem;
			try
			{
				ItemPack pack = new ItemPack();
				pack.setItem(item);
				pack.setXml(itemService.getItemXmlPropBag(item));
				PropBagEx xml = itemHelper.convertToXml(pack);

				String s = schemaService.transformForExport(getSchema(item).getId(), getPrefix(), xml, true);
				if( s != null )
				{
					return s;
				}
			}
			catch( Exception e )
			{
				LOGGER.error("Error transforming", e);
			}

			if( prefix.equals(DC_FORMAT) )
			{
				return crosswalk.createMetadata(nativeItem);
			}

			throw new CannotDisseminateFormatException(getPrefix());
		}

		private String getPrefix()
		{
			return prefix;
		}

		private Schema getSchema(Item xml)
		{
			return xml.getItemDefinition().getSchema();
		}

	}

	private void init()
	{
		// addCrossWalk(
		// "oai_marc",
		// "http://www.openarchives.org/OAI/1.1/oai_marc.xsd",
		// "http://www.openarchives.org/OAI/1.1/oai_marc"
		// );
		// crosswalksMap.put("oai_dc", new DCCrosswalk(null));
	}

	private void addCrossWalk(String metadataPrefix, String schema, String metadataNamespace)
	{
		if( crosswalksMap.containsKey(metadataNamespace) )
		{
			return;
		}

		Crosswalk crosswalk1 = new TLECross(metadataPrefix, metadataNamespace + ' ' + schema, null, null, null);
		crosswalksMap.put(metadataPrefix, new CrosswalkItem(metadataPrefix, schema, metadataNamespace, crosswalk1));
	}

	@Override
	public boolean containsValue(String metadataPrefix)
	{
		return getSchemaTypes().containsKey(metadataPrefix);
	}

	@Override
	public Iterator<?> iterator()
	{
		return getSchemaTypes().entrySet().iterator();
	}

	private Map<String, CrosswalkItem> getSchemaTypes()
	{
		List<String> set = schemaService.getExportSchemaTypes();

		crosswalksMap.clear();
		addCrossWalk("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc.xsd", //$NON-NLS-1$ //$NON-NLS-2$
			"http://www.openarchives.org/OAI/2.0/oai_dc/"); //$NON-NLS-1$

		for( String type : set )
		{
			type = type.toLowerCase();
			if( !crosswalksMap.containsKey(type) )
			{
				addCrossWalk(type, type, type);
			}
		}

		return crosswalksMap;
	}
}