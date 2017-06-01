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
