/*
 * Created on Apr 12, 2005
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
import com.tle.core.xstream.impl.XmlServiceImpl.ExtXStream;

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
