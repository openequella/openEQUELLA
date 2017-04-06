package org.example.soap;

import java.net.URL;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.Base64;

import com.tle.web.remoting.soap.SoapService51;
import com.tle.web.remoting.soap.SoapService51ServiceLocator;

/**
 * @author aholland
 */
public class EQUELLASOAP
{
	public final SoapService51 client;

	/**
	 * Create an EQUELLASOAP and login to the EQUELLA server
	 * 
	 * @param endpoint Endpoint is of the form:
	 *            http://myserver/mysint/services/SoapService51
	 * @param username User's login name
	 * @param password User's password
	 */
	public EQUELLASOAP(String endpoint, String username, String password)
	{
		try
		{
			SoapService51ServiceLocator locator = new SoapService51ServiceLocator();

			// This is important! it is required to maintain user state between
			// SOAP method invocations
			locator.setMaintainSession(true);

			client = locator.getSoapService51Endpoint(new URL(endpoint));

			// The user must be logged in before any SOAP methods are invoked,
			// otherwise the methods will assume you are an anonymous user
			// and your privileges will be limited.
			client.login(username, password);
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	/********************************************************************************************************
	 * You may want to add more wrapper methods like the ones below and make the
	 * client variable private
	 ********************************************************************************************************/

	/**
	 * Search for items on the EQUELLA server. Consult the SOAP API
	 * documentation for more information on the values of the parameters and
	 * return result.
	 */
	public XMLWrapper searchItems(String query, String where, boolean onlyLive, int sortType, boolean reverseSort,
		int offset, int maxResults)
	{
		try
		{
			return new XMLWrapper(client.searchItems(query, null, where, onlyLive, sortType, reverseSort, offset,
				maxResults));
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets a list of collections that the user you logged in as can contribute
	 * to.
	 */
	public XMLWrapper getContributableCollections()
	{
		try
		{
			return new XMLWrapper(client.getContributableCollections());
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates an item in the collection with the id of collectionId for you to
	 * begin editing with. Note that this will not create an item on the server
	 * until you call the saveItem method. The item XML will be initialised with
	 * a new UUID and a new staging ID where attachments can be uploaded to.
	 */
	public XMLWrapper newItem(String collectionUuid)
	{
		try
		{
			return new XMLWrapper(client.newItem(collectionUuid));
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Save changes made to an item which also unlocks the item. Before calling
	 * this, you must either use {@link #editItem(String, int, boolean)
	 * editItem} or {@link #newItem(String) newItem} and use the XML returned by
	 * these methods to pass in as the item parameter.
	 */
	public void saveItem(XMLWrapper item, boolean submit)
	{
		try
		{
			client.saveItem(item.toString(), submit);
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Upload a file into the staging area. Note that this does not attach the
	 * file to your item! To link the file to the item you need to add an
	 * attachment node to the item XML. Consult the SOAP API documentation for
	 * the format of the attachment nodes.
	 */
	public void uploadFile(String stagingUuid, String serverFilename, byte[] data)
	{
		try
		{
			Base64 b64 = new Base64(-1);
			String base64Data = b64.encodeToString(data);
			client.uploadFile(stagingUuid, serverFilename, base64Data, true);
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	public XMLWrapper getCollection(String collectionUuid)
	{
		try
		{
			return new XMLWrapper(client.getCollection(collectionUuid));
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	public XMLWrapper getSchema(String schemaUuid)
	{
		try
		{
			return new XMLWrapper(client.getSchema(schemaUuid));
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * You should call this when you are done with the EQUELLASOAP object
	 */
	public void logout()
	{
		try
		{
			client.logout();
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	private static final String UTF8 = "UTF-8"; //$NON-NLS-1$ 

	/**
	 * Generates a token that is valid for 30 minutes. This should be appended
	 * to URLs so that users are not forced to log in to view content. E.g.
	 * <code>string itemURL = "http://MYSERVER/myinst/items/619722b1-22f8-391a-2bcf-46cfaab36265/1/?token=" + URLEncoder.encode(EQUELLASOAP.generateToken("fred.smith", "IntegSecret", "squirrel"), "UTF-8");</code>
	 * In the example above, if fred.smith is a valid username on the EQUELLA
	 * server he will be automatically logged into the system so that he can
	 * view item 619722b1-22f8-391a-2bcf-46cfaab36265/1 (provided he has the
	 * permissions to do so). Note that to use this functionality, the Shared
	 * Secrets user management plugin must be enabled (see User Management in
	 * the EQUELLA Administration Console) and a shared secret must be
	 * configured.
	 * 
	 * @param username The username of the user to log in as
	 * @param sharedSecretId The ID of the shared secret
	 * @param sharedSecretValue The value of the shared secret
	 * @return A token that can be directly appended to a URL (i.e. it is
	 *         already URL encoded) E.g. URL = URL + "?token=" +
	 *         URLEncoder.encode(EQUELLASOAP.generateToken(x,y,z), "UTF-8")
	 */
	public static String generateToken(String username, String sharedSecretId, String sharedSecretValue)
	{
		final String secretId = (sharedSecretId == null ? "" : sharedSecretId); //$NON-NLS-1$

		String time = Long.toString(System.currentTimeMillis());
		String toMd5 = username + secretId + time + sharedSecretValue;

		StringBuilder b = new StringBuilder();
		b.append(username);
		b.append(':');
		b.append(secretId);
		b.append(':');
		b.append(time);
		b.append(':');
		b.append(new String(Base64.encodeBase64(getMd5Bytes(toMd5))));

		return b.toString();
	}

	private static byte[] getMd5Bytes(String str)
	{
		MessageDigest digest;
		try
		{
			digest = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
			digest.update(str.getBytes(UTF8));
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
		return digest.digest();
	}
}
