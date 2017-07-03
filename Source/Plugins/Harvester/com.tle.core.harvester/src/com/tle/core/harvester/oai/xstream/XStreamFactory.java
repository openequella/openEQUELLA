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

import com.thoughtworks.xstream.XStream;
import com.tle.core.harvester.oai.data.DublinCore;
import com.tle.core.harvester.oai.data.Header;
import com.tle.core.harvester.oai.data.MetadataFormat;
import com.tle.core.harvester.oai.data.OAIError;
import com.tle.core.harvester.oai.data.Record;
import com.tle.core.harvester.oai.data.Repository;
import com.tle.core.harvester.oai.data.Request;
import com.tle.core.harvester.oai.data.Response;
import com.tle.core.harvester.oai.data.ResumptionToken;
import com.tle.core.harvester.oai.data.Set;
import com.tle.core.xml.service.impl.XmlServiceImpl.ExtXStream;

/**
 *
 */
public final class XStreamFactory
{
	private static XStream xstream;

	private XStreamFactory()
	{
		throw new RuntimeException();
	}

	public static synchronized XStream getXStream()
	{
		if( xstream == null )
		{
			xstream = new ExtXStream(XStreamFactory.class.getClassLoader());
			xstream.alias("error", OAIError.class);
			xstream.alias("header", Header.class);
			xstream.alias("record", Record.class);
			xstream.alias("set", Set.class);
			xstream.alias("oai_dc:dc", DublinCore.class);
			xstream.alias("Identify", Repository.class);
			xstream.alias("metadataFormat", MetadataFormat.class);
			xstream.alias("oai:metadataFormat", MetadataFormat.class);
			xstream.alias("resumptionToken", ResumptionToken.class);
			xstream.alias("request", Request.class);
			xstream.alias("OAI-PMH", Response.class);
			xstream.registerConverter(new OAIDublinCoreConverter());
			xstream.registerConverter(new OAIDOMConverter());
			xstream.registerConverter(new OAIHeaderConverter());
			xstream.registerConverter(new OAIResumptionTokenConverter());
			xstream.registerConverter(new OAIRecordConverter());
			xstream.registerConverter(new OAISetConverter());
			xstream.registerConverter(new OAIRequestConverter());
			xstream.registerConverter(new OAIResponseConverter());
			xstream.registerConverter(new OAIErrorConverter());
			xstream.registerConverter(new OAIRepositoryConverter());
			xstream.registerConverter(new OAIMetadataFormatConverter());
		}
		return xstream;
	}

}
