package org.example.soap;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("nls")
public class Main
{
	private final String endpoint;
	private final String username;
	private final String password;

	private final boolean useTokens;
	private final String tokenUser;
	private final String sharedSecretId;
	private final String sharedSecretValue;
	
	private final String searchQuery;
	private final String contributeCollection;

	public static void main(String[] args)
	{
		Main m = new Main();
		m.execute();
	}

	public Main()
	{
		Properties settings = new Properties();
		InputStream propStream = null;
		try
		{
			propStream = Main.class.getResourceAsStream("settings.properties");
			settings.load(propStream);

			endpoint = settings.getProperty("endpoint");
			username = settings.getProperty("username");
			password = settings.getProperty("password");

			useTokens = Boolean.valueOf(settings.getProperty("useTokens"));
			tokenUser = settings.getProperty("tokenUser");
			sharedSecretId = settings.getProperty("sharedSecretId");
			sharedSecretValue = settings.getProperty("sharedSecretValue");

			setupProxy(settings.getProperty("proxyHost"), settings.getProperty("proxyPort"),
				settings.getProperty("proxyUsername"), settings.getProperty("proxyPassword"));
			
			searchQuery = settings.getProperty("searchQuery");
			contributeCollection = settings.getProperty("contributeCollection");
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
		finally
		{
			if( propStream != null )
			{
				try
				{
					propStream.close();
				}
				catch( Exception e )
				{
					throw new RuntimeException(e);
				}
			}
		}
	}

	private void setupProxy(String proxyHost, String proxyPort, final String proxyUser, final String proxyPassword)
	{
		if( proxyHost != null && proxyHost.trim().length() > 0 )
		{
			Properties sysprops = System.getProperties();

			// Older JVMs
			sysprops.put("proxySet", "true");
			sysprops.put("proxyHost", proxyHost);
			sysprops.put("proxyPort", proxyPort);

			// Java 1.4 and up
			sysprops.put("http.proxyHost", proxyHost);
			sysprops.put("http.proxyPort", proxyPort);
			sysprops.put("https.proxyHost", proxyHost);
			sysprops.put("https.proxyPort", proxyPort);
			sysprops.put("ftp.proxyHost", proxyHost);
			sysprops.put("ftp.proxyPort", proxyPort);

			// Setup any authentication
			if( proxyUser != null && proxyUser.length() > 0 )
			{
				// JDK 1.4
				Authenticator.setDefault(new Authenticator()
				{
					@Override
					protected PasswordAuthentication getPasswordAuthentication()
					{
						return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
					}
				});

				// Older JVMs
				sysprops.put("http.proxyUser", proxyUser);
				sysprops.put("http.proxyPassword", proxyPassword);
			}

			System.setProperties(sysprops);
		}
	}

	protected void execute()
	{
		// search for items
		searchItems(searchQuery, null, true, 2, false, 0, 10);

		// contribute
		final String attachmentFilename = "testAttachment.jpg";
		contribute(findACollectionUuid(contributeCollection), "Test Item Name", "Test Item Description",
			"Test Attachment Name", attachmentFilename, getAttachmentData(attachmentFilename));
	}

	protected void searchItems(String query, String where, boolean onlyLive, int sortType, boolean reverseSort,
		int offset, int maxResults)
	{
		// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// EQUELLA SOAP Searching Code
		// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		final EQUELLASOAP equella = new EQUELLASOAP(endpoint, username, password);

		String tokenPostfix = "";
		if( useTokens )
		{
			try
			{
				tokenPostfix = "?token="
					+ URLEncoder.encode(EQUELLASOAP.generateToken(tokenUser, sharedSecretId, sharedSecretValue),
						"UTF-8");
			}
			catch( UnsupportedEncodingException e )
			{
				throw new RuntimeException(e);
			}
		}

		final XMLWrapper results = equella.searchItems(query, where, onlyLive, sortType, reverseSort, offset,
			maxResults);
		final NodeList nodes = results.nodeList("/results/result");
		for( int i = 0; i < nodes.getLength(); i++ )
		{
			final Node result = nodes.item(i);
			final String url = results.nodeValue("xml/item/url", result) + tokenPostfix;
			final String name = results.nodeValue("xml/item/name", result);

			System.out.println(name + ": " + url);
		}

		equella.logout();
	}

	protected void contribute(String collectionUuid, String itemName, String itemDescription,
		String attachmentDescription, String attachmentFilename, byte[] attachmentData)
	{
		// ///////////////////////////////////////////////////////////////////////////////////////////////////////
		// EQUELLA SOAP Contribution Code
		// ///////////////////////////////////////////////////////////////////////////////////////////////////////

		// Create the item on the server. The general process is:
		// 1. newItem (creates a new item in the staging folder, doesn't get
		// committed until saveItem is called)
		// 2. set metadata on the item XML (some are system fields e.g.
		// /xml/item/attachments, others could be custom schema fields)
		// 3. saveItem (commits the changes to a previous newItem or editItem
		// call)

		int filesize = (attachmentData != null ? attachmentData.length : 0);

		EQUELLASOAP equella = new EQUELLASOAP(endpoint, username, password);
		XMLWrapper item = equella.newItem(collectionUuid);

		// set item name and description
		String stagingUuid = item.nodeValue("/xml/item/staging");

		// name and description xpaths are collection dependent! You need to
		// find the correct xpath to use for the name and description nodes
		XMLWrapper collection = equella.getCollection(collectionUuid);
		XMLWrapper schema = equella.getSchema(collection.nodeValue("/itemdef/schemaUuid"));

		String itemNameXPath = "/xml" + schema.nodeValue("/schema/itemNamePath");
		String itemDescriptionXPath = "/xml" + schema.nodeValue("/schema/itemDescriptionPath");
		item.createNodeFromXPath(itemNameXPath, itemName);
		item.createNodeFromXPath(itemDescriptionXPath, itemDescription);

		// check attachments
		if( filesize > 0 )
		{
			equella.uploadFile(stagingUuid, attachmentFilename, attachmentData);

			// create the attachment object on the item
			Node attachmentsNode = item.node("/xml/item/attachments");
			if( attachmentsNode == null )
			{
				attachmentsNode = item.createNode(item.node("/xml/item"), "attachments");
			}

			Node attachmentNode = item.createNode(attachmentsNode, "attachment");

			Attr type = item.createAttribute(attachmentNode, "type");
			type.setTextContent("local");
			Node file = item.createNode(attachmentNode, "file");
			file.setTextContent(attachmentFilename);
			Node description = item.createNode(attachmentNode, "description");
			description.setTextContent(attachmentDescription);
			Node size = item.createNode(attachmentNode, "size");
			size.setTextContent(Integer.toString(filesize));
		}

		// save and submit
		equella.saveItem(item, true);

		System.out.println("Item \"" + itemName + "\" contributed");

		equella.logout();
	}

	private String findACollectionUuid(String findCollectionName)
	{
		List<String> collectionUuids = new ArrayList<String>();
		EQUELLASOAP equella = new EQUELLASOAP(endpoint, username, password);
		try
		{
			XMLWrapper collectionsXml = equella.getContributableCollections();
			NodeList collectionNodes = collectionsXml.nodeList("/xml/itemdef");
			for( int i = 0; i < collectionNodes.getLength(); i++ )
			{
				Node collectionNode = collectionNodes.item(i);
				String collectionName = collectionsXml.nodeValue("name", collectionNode);
				String collectionUuid = collectionsXml.nodeValue("uuid", collectionNode);
				if( findCollectionName != null && findCollectionName.equals(collectionName.trim()) )
				{
					return collectionUuid;
				}
				collectionUuids.add(collectionUuid);
			}

			if( collectionUuids.size() == 0 )
			{
				throw new RuntimeException("No collections are available to contribute to");
			}
			return collectionUuids.get(0);
		}
		finally
		{
			equella.logout();
		}
	}

	private byte[] getAttachmentData(String filename)
	{
		InputStream attachmentStream = null;
		ByteArrayOutputStream data = null;

		try
		{
			attachmentStream = Main.class.getResourceAsStream(filename);
			data = new ByteArrayOutputStream();
			byte buffer[] = new byte[4096];
			for( int bytes = attachmentStream.read(buffer, 0, buffer.length); bytes != -1; bytes = attachmentStream
				.read(buffer, 0, buffer.length) )
			{
				data.write(buffer, 0, bytes);
			}
			return data.toByteArray();
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
		finally
		{
			try
			{
				if( attachmentStream != null )
				{
					attachmentStream.close();
				}
				if( data != null )
				{
					data.close();
				}
			}
			catch( Exception e )
			{
				throw new RuntimeException(e);
			}
		}
	}
}
