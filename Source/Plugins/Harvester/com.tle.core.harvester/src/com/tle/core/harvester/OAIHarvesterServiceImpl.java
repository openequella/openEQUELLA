package com.tle.core.harvester;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;

import com.tle.common.NameValue;
import com.tle.core.guice.Bind;
import com.tle.core.harvester.oai.OAIClient;
import com.tle.core.harvester.oai.data.MetadataFormat;
import com.tle.core.harvester.oai.data.Set;
import com.tle.core.harvester.oai.error.NoMetadataFormatsException;

@Bind(OAIHarvesterService.class)
@Singleton
public class OAIHarvesterServiceImpl implements OAIHarvesterService
{
	public OAIHarvesterServiceImpl()
	{
		super();
	}

	@Override
	public List<NameValue> listSets(URL url) throws Exception
	{
		OAIClient client = new OAIClient(url);
		com.tle.core.harvester.oai.data.List listSets = client.listSets();
		if( listSets == null )
		{
			return Collections.emptyList();
		}
		List<NameValue> nvs = new ArrayList<NameValue>();
		for( Object set : listSets )
		{
			String name = ((Set) set).getName();
			String spec = ((Set) set).getSpec();
			nvs.add(new NameValue(name, spec));
		}

		return nvs;
	}

	@Override
	public List<String> listMetadataFormats(URL url) throws Exception
	{
		OAIClient client = new OAIClient(url);
		com.tle.core.harvester.oai.data.List listMetas;
		try
		{
			listMetas = client.listMetadataFormats();
		}
		catch( NoMetadataFormatsException e )
		{
			return Collections.emptyList();
		}
		List<String> metas = new ArrayList<String>();
		for( Object meta : listMetas )
		{
			String prefix = ((MetadataFormat) meta).getMetadataPrefix();
			metas.add(prefix);
		}
		return metas;
	}
}
