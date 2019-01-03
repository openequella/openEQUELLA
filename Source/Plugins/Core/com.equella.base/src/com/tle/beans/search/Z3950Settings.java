/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.beans.search;

@SuppressWarnings("nls")
public class Z3950Settings extends XmlBasedSearchSettings
{
	private static final String SEARCH_TYPE = "Z3950SearchEngine";

	private static final int DEFAULT_PORT = 210;
	private static final String DEFAULT_HOST = "library.anu.edu.au";
	private static final String DEFAULT_DATABASE = "innopac";
	private static final String DEFAULT_USERNAME = "";

	private String host = DEFAULT_HOST;
	private int port = DEFAULT_PORT;
	private String database = DEFAULT_DATABASE;
	private String username = DEFAULT_USERNAME;
	private String password;
	private String group;

	private String importRecordSchema;
	private boolean advanced;

	private String standardAttributes;

	public enum RecordFormat
	{
		MODS("MODS", "http://www.loc.gov/mods/"), OAI_MARC("MARC", "http://www.openarchives.org/OAI/oai_marc");

		private final String name;
		private final String uri;

		private RecordFormat(String name, String uri)
		{
			this.name = name;
			this.uri = uri;
		}

		public String getName()
		{
			return name;
		}

		public String getUri()
		{
			return uri;
		}
	}

	public enum AttributeProfile
	{
		EQUELLA("Equella Default"), BATH_0("Bath Level 0"), BATH_1("Bath Level 1");

		private final String name;

		private AttributeProfile(String name)
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}
	}

	public String getImportRecordSchema()
	{
		return importRecordSchema;
	}

	public void setImportRecordSchema(String importRecordSchema)
	{
		this.importRecordSchema = importRecordSchema;
	}

	@Override
	protected String getType()
	{
		return SEARCH_TYPE;
	}

	@Override
	protected void _load()
	{
		super._load();
		host = get("host", host);
		port = get("port", port);
		database = get("database", database);
		username = get("username", username);
		password = getNonEmpty("password", password);
		group = getNonEmpty("group", group);
		importRecordSchema = get("importRecordSchema", RecordFormat.MODS.getUri());
		advanced = get("advanced", false);
		standardAttributes = get("standardAttributes", standardAttributes);

		if( host.length() == 0 )
		{
			host = DEFAULT_HOST;
			port = DEFAULT_PORT;
			database = DEFAULT_DATABASE;
		}
	}

	@Override
	protected void _save()
	{
		super._save();
		put("host", host);
		put("port", port);
		put("database", database);
		put("username", username);
		put("password", password);
		put("group", group);
		put("importRecordSchema", importRecordSchema);
		put("advanced", advanced);
		put("standardAttributes", standardAttributes);
	}

	public String getDatabase()
	{
		return database;
	}

	public void setDatabase(String database)
	{
		this.database = database;
	}

	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public String getGroup()
	{
		return group;
	}

	public void setGroup(String group)
	{
		this.group = group;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public boolean isAdvanced()
	{
		return advanced;
	}

	public void setAdvanced(boolean advanced)
	{
		this.advanced = advanced;
	}

	public String getStandardAttributes()
	{
		return standardAttributes;
	}

	public void setStandardAttributes(String standardAttributes)
	{
		this.standardAttributes = standardAttributes;
	}
}
