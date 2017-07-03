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

package com.dytech.edge.ejb.helpers.metadata.mappers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.ejb.helpers.metadata.mapping.Mapping;
import com.google.common.base.Throwables;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.MetadataMapping;
import com.tle.beans.entity.itemdef.mapping.IMSMapping;
import com.tle.common.filesystem.handle.FileHandle;

public abstract class AbstractXPathPackageMapper implements PackageMapper
{
	@Override
	public void mapMetadata(ItemDefinition itemdef, PropBagEx item, FileHandle handle, String packageName)
	{
		MetadataMapping mappingData = itemdef.getMetadataMapping();
		Schema schema = itemdef.getSchema();
		XPathMapper mapper = new XPathMapper(schema);
		Collection<IMSMapping> imsMapping = mappingData.getImsMapping();
		mapper.processMapping(imsMapping);
		for( IMSMapping ims : imsMapping )
		{
			if( ims.isReplace() )
			{
				String path = ims.getItemdef();
				Iterator<PropBagEx> j = item.iterateAll(path);
				while( j.hasNext() )
				{
					j.next();
					j.remove();
				}
			}
		}
		try( InputStream imsStream = getXmlStream(handle, packageName) )
		{
			Collection<Mapping> mappings = mapper.map(imsStream);
			for( Mapping mapping : mappings )
			{
				mapping.update(item);
			}
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}
	}

	protected abstract InputStream getXmlStream(FileHandle handle, String packageName) throws IOException;
}
