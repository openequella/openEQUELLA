package com.tle.core.harvester;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.dytech.devlib.PropBagEx;
import com.tle.common.NameValue;
import com.tle.common.harvester.HarvesterException;
import com.tle.core.guice.Bind;
import com.tle.core.harvester.soap.SoapHarvesterService;
import com.tle.web.remoting.soap.SoapClientFactory;

@Bind(EQUELLAHarvesterService.class)
@Singleton
public class EQUELLAHarvesterServiceImpl implements EQUELLAHarvesterService
{
	private static final Logger LOGGER = Logger.getLogger(EQUELLAHarvesterService.class);

	@Inject
	private SoapClientFactory clientFactory;

	public static final Comparator<NameValue> NAMEVALUE_COMPARATOR = new Comparator<NameValue>()
	{
		@Override
		public int compare(NameValue o1, NameValue o2)
		{
			return o1.toString().compareToIgnoreCase(o2.toString());
		}
	};

	public EQUELLAHarvesterServiceImpl()
	{
		super();
	}

	@SuppressWarnings("nls")
	@Override
	public List<NameValue> listCollections(URL url, String user, String pass, String endPoint) throws Exception
	{
		final URL endpointUrl = new URL(url, endPoint);

		SoapHarvesterService soapClient = clientFactory.createSoapClient(SoapHarvesterService.class, endpointUrl,
			"http://soap.harvester.core.tle.com");

		List<NameValue> nvs = new ArrayList<NameValue>();
		try
		{
			if( !user.isEmpty() )
			{
				soapClient.login(user, pass);
			}

			final PropBagEx collectionBag = new PropBagEx(soapClient.getSearchableCollections());
			for( PropBagEx collection : collectionBag.iterateAll("itemdef") )
			{
				nvs.add(new NameValue(collection.getNode("name"), collection.getNode("uuid")));
			}

			Collections.sort(nvs, NAMEVALUE_COMPARATOR);

			final PropBagEx dynaCollectionBag = new PropBagEx(soapClient.getDynamicCollections());
			for( PropBagEx collection : dynaCollectionBag.iterateAll("dyncol") )
			{
				String col = "dyna:" + collection.getNode("uuid") + ":" + collection.getNode("virtval");
				nvs.add(new NameValue(collection.getNode("name"), col));
			}
		}
		catch( Exception ex )
		{
			// Log the cause, but don't pass it back to the admin console
			// (e.g. Admin console doesn't know SoapFault)
			LOGGER.error("Error getting collections", ex);
			String message = ex.getMessage();
			throw new HarvesterException(message);
		}

		return nvs;
	}
}
