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

package com.tle.core.harvester.oai.xstream;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.core.harvester.oai.data.Repository;

/**
 * 
 */
public class OAIRepositoryConverter extends OAIAbstractConverter
{
	private static final String REPOSITORYNAME = "repositoryName";
	private static final String ADMINEMAIL = "adminEmail";
	private static final String GRANULARITY = "granularity";
	private static final String DELETEDRECORD = "deletedRecord";
	private static final String EARLIESTDATESTAMP = "earliestDatestamp";
	private static final String PROTOCOLVERSION = "protocolVersion";
	private static final String BASEURL = "baseURL";

	@Override
	public boolean canConvert(Class kclass)
	{
		return kclass.equals(Repository.class);
	}

	@Override
	public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext arg2)
	{
		Repository rep = (Repository) object;
		startNode(writer, BASEURL, rep.getBaseURL());
		startNode(writer, PROTOCOLVERSION, rep.getProtocolVersion());
		startNode(writer, EARLIESTDATESTAMP, rep.getEarliestDatestamp());
		startNode(writer, DELETEDRECORD, rep.getDeletedRecord());
		startNode(writer, GRANULARITY, rep.getGranularity());
		startNode(writer, ADMINEMAIL, rep.getAdminEmails());
		startNode(writer, REPOSITORYNAME, rep.getName());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		Repository rep = new Repository();
		for( ; reader.hasMoreChildren(); reader.moveUp() )
		{
			reader.moveDown();
			String name = reader.getNodeName();
			String value = reader.getValue();
			if( name.equals(BASEURL) )
			{
				rep.setBaseURL(value);
			}
			else if( name.equals(PROTOCOLVERSION) )
			{
				rep.setProtocolVersion(value);
			}
			else if( name.equals(EARLIESTDATESTAMP) )
			{
				rep.setEarliestDatestamp(value);
			}
			else if( name.equals(DELETEDRECORD) )
			{
				rep.setDeletedRecord(value);
			}
			else if( name.equals(ADMINEMAIL) )
			{
				rep.addAdminEmail(value);
			}
			else if( name.equals(GRANULARITY) )
			{
				rep.setGranularity(value);
			}
			else if( name.equals(REPOSITORYNAME) )
			{
				rep.setName(value);
			}
		}

		return rep;
	}

}
