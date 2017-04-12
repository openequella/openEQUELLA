package com.dytech.edge.importexport;

import java.net.CookieHandler;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.importexport.types.Item;
import com.dytech.edge.importexport.types.ItemDef;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.web.remoting.soap.SoapService51;

/**
 * Note: to compile this class you need to run the generate-source target in the
 * build script
 * 
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public final class SoapSession
{
	protected static final Logger LOGGER = Logger.getLogger(SoapSession.class.getName());

	protected final SoapService51 client;
	protected final URL institutionUrl;
	protected final String userId;
	protected final Base64 encoder;
	protected boolean loggedIn;

	public SoapSession(URL institutionUrl, String username, String password) throws Exception
	{
		this.institutionUrl = institutionUrl;
		encoder = new Base64(-1);

		CookieHandler.setDefault(new BasicCookieHandler());
		final URL endpointUrl = new URL(institutionUrl, "services/SoapService51");

		System.setProperty("axis.socketFactory", "com.dytech.edge.importexport.TleDefaultSocketFactory");
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SLF4JLog");
		System.setProperty("org.apache.commons.logging.LogFactory", "org.apache.commons.logging.impl.SLF4JLogFactory");

		final CustomSoapService51Locator locator = new CustomSoapService51Locator();

		client = locator.getSoapService51Endpoint(endpointUrl);

		final PropBagEx userXml = new PropBagEx(client.login(username, password));
		userId = userXml.getNode("uuid");
		loggedIn = true;

		final Map<String, List<String>> fakeHeaders = new HashMap<String, List<String>>();
		fakeHeaders.put("Set-Cookie", locator.getCookiesForUrl(endpointUrl));
		CookieHandler.getDefault().put(endpointUrl.toURI(), fakeHeaders);

		new KeepAlive().start();
	}

	public PropBagEx createNewItem(String collectionUuid) throws Exception
	{
		return new PropBagEx(client.newItem(collectionUuid));
	}

	public void saveItem(PropBagEx xml) throws Exception
	{
		client.saveItem(xml.toString(), true);
	}

	public void appendFile(String staging, String filename, byte[] data, int length) throws Exception
	{
		if( data != null && length > 0 )
		{
			final byte[] chunk = new byte[length];
			System.arraycopy(data, 0, chunk, 0, chunk.length);
			final String encodedData = encoder.encodeToString(chunk);

			client.uploadFile(staging, filename, encodedData, false);
		}
	}

	public List<ItemDef> getContributableCollections() throws Exception
	{
		final List<ItemDef> results = new ArrayList<ItemDef>();
		final PropBagEx collectionBag = new PropBagEx(client.getContributableCollections());
		for( PropBagEx collection : collectionBag.iterateAll("itemdef") )
		{
			results.add(new ItemDef(collection));
		}
		return results;
	}

	public String[] getFilesForItem(Item item) throws Exception
	{
		return client.getItemFilenames(item.getUuid(), item.getVersion(), null, true);
	}

	public void populateItemXml(Item item) throws Exception
	{
		item.setXml(new PropBagEx(client.getItem(item.getUuid(), item.getVersion(), null)));
	}

	public boolean itemExists(Item item) throws Exception
	{
		return client.itemExists(item.getUuid(), item.getVersion());
	}

	public List<Item> enumerateItemsForQuery(String query, List<ItemDef> itemDefs) throws Exception
	{
		return search(query, itemDefs, null);
	}

	public List<Item> enumerateItemsForOwner() throws Exception
	{
		final List<Item> results = search(null, null, null);
		final Iterator<Item> it = results.iterator();
		while( it.hasNext() )
		{
			Item item = it.next();
			if( !item.getOwnerId().equals(userId) )
			{
				it.remove();
			}
		}
		return results;
	}

	public List<Item> enumerateItemsForItemDef(ItemDef itemdef) throws Exception
	{
		return search(null, Collections.singletonList(itemdef), null);
	}

	public List<Item> enumerateAllItems() throws Exception
	{
		return search(null, null, null);
	}

	private List<Item> search(String query, List<ItemDef> collections, String where) throws Exception
	{
		String[] colls = null;
		if( collections != null )
		{
			final List<String> colUuids = Lists.transform(collections, new Function<ItemDef, String>()
			{
				@Override
				public String apply(ItemDef collection)
				{
					return collection.getUuid();
				}
			});
			colls = colUuids.toArray(new String[]{});
		}

		boolean moreResults = true;
		ArrayList<Item> itemResults = new ArrayList<Item>();
		int available = -1;
		int processed = 0;
		while( moreResults )
		{
			final PropBagEx results = new PropBagEx(client.searchItems(query, colls, where, false, 2, false, processed,
				-1));
			if( available == -1 )
			{
				available = results.getIntNode("available");
			}
			int count = results.getIntNode("@count");

			itemResults.addAll(mergeResults(results));
			processed += count;

			// Should never be greater, but... you never know :)
			if( processed >= available )
			{
				moreResults = false;
			}
		}
		return itemResults;
	}

	private List<Item> mergeResults(PropBagEx xml)
	{
		final int count = xml.getIntNode("@count"); //$NON-NLS-1$
		List<Item> results = new ArrayList<Item>(count);
		for( Iterator<PropBagEx> i = xml.iterateAll("result/xml/item"); i.hasNext(); ) //$NON-NLS-1$
		{
			PropBagEx item = i.next();

			String uuid = item.getNode("@id"); //$NON-NLS-1$
			String idef = item.getNode("@itemdefid"); //$NON-NLS-1$
			String name = item.getNode("name"); //$NON-NLS-1$
			int version = item.getIntNode("@version", 1); //$NON-NLS-1$
			String owner = item.getNode("owner"); //$NON-NLS-1$

			results.add(new Item(name, uuid, version, idef, owner));
		}
		return results;
	}

	public String getUserId()
	{
		return userId;
	}

	public class KeepAlive extends Thread
	{
		@Override
		public void run()
		{
			super.run();
			try
			{
				while( true )
				{
					Thread.sleep(120000);
					if( loggedIn )
					{
						LOGGER.info("Keep alive");
						client.keepAlive();
					}
				}
			}
			catch( InterruptedException i )
			{
				// meh
			}
			catch( Exception e )
			{
				throw new RuntimeException(e);
			}
		}
	}
}
