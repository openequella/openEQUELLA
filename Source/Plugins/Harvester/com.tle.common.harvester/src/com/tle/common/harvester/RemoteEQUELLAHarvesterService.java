package com.tle.common.harvester;

import java.net.URL;
import java.util.List;

import com.tle.common.NameValue;

public interface RemoteEQUELLAHarvesterService
{
	/**
	 * Lists the searchable collections (including dynamic) that the supplied
	 * user can harvest.
	 * 
	 * @param url The server url
	 * @param user The username
	 * @param pass The password
	 * @param endPoint The soap endpoint
	 * @return A NameValue list of collections, name and uuid. <br>
	 *         Dynamic collections are returned in the format of
	 *         "dyna:UUID:virtulisation"
	 */
	List<NameValue> listCollections(URL url, String user, String pass, String endPoint) throws Exception;
}
