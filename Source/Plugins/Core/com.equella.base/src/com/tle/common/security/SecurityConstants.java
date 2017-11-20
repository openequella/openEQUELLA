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

package com.tle.common.security;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import com.tle.common.Check;

@SuppressWarnings("nls")
public final class SecurityConstants
{
	public static final char GRANT = 'G';
	public static final char REVOKE = 'R';

	@Deprecated
	public static enum PrivilegeType
	{
		COLLECTION,
	}

	public static final String VIRTUAL_BASE_ENTITY = "%VIRTUAL_BASE%";

	// The following are deprecated. These should be declared as constants in
	// the plug-ins that handle those "things". For example, COMMENT_*_ITEM
	// should be in the com.tle.web.viewitem plug-in.

	@Deprecated
	public static final String CREATE_ITEM = "CREATE_ITEM";
	@Deprecated
	public static final String EDIT_ITEM = "EDIT_ITEM";
	@Deprecated
	public static final String VIEW_ITEM = "VIEW_ITEM";
	@Deprecated
	public static final String CLONE_ITEM = "CLONE_ITEM";
	@Deprecated
	public static final String MOVE_ITEM = "MOVE_ITEM";
	@Deprecated
	public static final String COMMENT_CREATE_ITEM = "COMMENT_CREATE_ITEM";
	@Deprecated
	public static final String COMMENT_DELETE_ITEM = "COMMENT_DELETE_ITEM";
	@Deprecated
	public static final String COMMENT_VIEW_ITEM = "COMMENT_VIEW_ITEM";
	@Deprecated
	public static final String DISCOVER_ITEM = "DISCOVER_ITEM";
	@Deprecated
	public static final String ARCHIVE_ITEM = "ARCHIVE_ITEM";

	public static final String CREATE_VIRTUAL_BASE = "CREATE_" + SecurityConstants.VIRTUAL_BASE_ENTITY;
	public static final String EDIT_VIRTUAL_BASE = "EDIT_" + SecurityConstants.VIRTUAL_BASE_ENTITY;
	public static final String DELETE_VIRTUAL_BASE = "DELETE_" + SecurityConstants.VIRTUAL_BASE_ENTITY;
	public static final String LIST_VIRTUAL_BASE = "LIST_" + SecurityConstants.VIRTUAL_BASE_ENTITY;

	public static final String LOGGED_IN_USER_ROLE_ID = "TLE_LOGGED_IN_USER_ROLE";
	public static final String GUEST_USER_ROLE_ID = "TLE_GUEST_USER_ROLE";

	public static final String TARGET_EVERYTHING = "*";
	@Deprecated
	public static final String TARGET_BASEENTITY = "B";
	@Deprecated
	public static final String TARGET_SYSTEM_SETTING = "C";
	@Deprecated
	public static final String TARGET_ITEM_STATUS = "S";
	@Deprecated
	public static final String TARGET_ITEM_METADATA = "M";
	@Deprecated
	public static final String TARGET_DYNAMIC_ITEM_METADATA = "X";
	@Deprecated
	public static final String TARGET_ITEM = "I";
	@Deprecated
	public static final String TARGET_WORKFLOW_TASK = "T";
	@Deprecated
	public static final String TARGET_HIERARCHY_TOPIC = "H";
	@Deprecated
	public static final String TARGET_WORKFLOW_DYNAMIC_TASK = "D";

	public static final int PRIORITY_MAX = 2000;
	public static final int PRIORITY_INSTITUTION = 1900;

	public static final int PRIORITY_ALL_HTMLEDITOR_PLUGINS = 1895;
	public static final int PRIORITY_HTMLEDITOR_PLUGIN = 1890;

	public static final int PRIORITY_ALL_HARVESTER_PROFILES = 1885;
	public static final int PRIORITY_HARVESTER_PROFILE = 1880;
	public static final int PRIORITY_ALL_TAXONOMIES = 1875;
	public static final int PRIORITY_TAXONOMY = 1850;

	public static final int PRIORITY_ALL_OAUTH_CLIENTS = 1846;
	public static final int PRIORITY_OAUTH_CLIENT = 1845;

	public static final int PRIORITY_ALL_EXTERNAL_TOOLS = 1855;
	public static final int PRIORITY_EXTERNAL_TOOL = 1856;
	public static final int PRIORITY_ALL_ECHOS = 1861;
	public static final int PRIORITY_ECHO = 1860;
	public static final int PRIORITY_ALL_LTI_CONSUMERS = 1850;
	public static final int PRIORITY_LTI_CONSUMER = 1849;
	public static final int PRIORITY_ALL_USER_SCRIPTS = 1844;
	public static final int PRIORITY_USER_SCRIPTS = 1843;
	public static final int PRIORITY_ALL_KALTURAS = 1842;
	public static final int PRIORITY_KALTURA = 1841;
	public static final int PRIORITY_ALL_CONNECTORS = 1840;
	public static final int PRIORITY_CONNECTOR = 1830;
	public static final int PRIORITY_ALL_SCHEMAS = 1825;
	public static final int PRIORITY_SCHEMA = 1800;

	public static final int PRIORITY_ALL_WORKFLOWS = 1725;
	public static final int PRIORITY_WORKFLOW = 1700;
	public static final int PRIORITY_WORKFLOW_TASK = 1600;
	public static final int PRIORITY_ALL_CUSTOM_LINKS = 1550;
	public static final int PRIORITY_CUSTOM_LINK = 1540;
	public static final int PRIORITY_ALL_PORTLETS = 1510;
	public static final int PRIORITY_PORTLET = 1505;
	public static final int PRIORITY_GLOBAL_ITEM_STATUS = 1500;
	public static final int PRIORITY_ALL_COLLECTIONS = 1425;
	public static final int PRIORITY_COLLECTION = 1400;
	public static final int PRIORITY_ITEM_STATUS = 1300;
	public static final int PRIORITY_ITEM_METADATA = 1200;
	public static final int PRIORITY_DYNAMIC_METADATA = 1190;
	public static final int PRIORITY_ALL_COURSE_INFO = 1175;
	public static final int PRIORITY_COURSE_INFO = 1150;
	public static final int PRIORITY_ALL_POWER_SEARCHES = 1125;
	public static final int PRIORITY_POWER_SEARCH = 1100;
	public static final int PRIORITY_ALL_FEDERATED_SEARCHES = 925;
	public static final int PRIORITY_FEDERATED_SEARCH = 900;
	public static final int PRIORITY_ALL_DYNA_COLLECTIONS = 875;
	public static final int PRIORITY_DYNA_COLLECTION = 850;
	public static final int PRIORITY_ALL_FILTER_GROUPS = 825;
	public static final int PRIORITY_FILTER_GROUP = 800;
	public static final int PRIORITY_ALL_REPORTS = 525;
	public static final int PRIORITY_REPORT = 500;
	public static final int PRIORITY_ALL_EMAIL_TEMPLATES = 425;
	public static final int PRIORITY_EMAIL_TEMPLATE = 400;
	public static final int PRIORITY_ALL_MANAGING = 375;
	public static final int PRIORITY_MANAGING = 350;
	public static final int PRIORITY_ALL_SYSTEM_SETTINGS = 325;
	public static final int PRIORITY_SYSTEM_SETTING = 300;
	public static final int PRIORITY_HIERARCHY_TOPIC = 200;
	public static final int PRIORITY_ITEM = 100;
	public static final int PRIORITY_OBJECT_INSTANCE = 0;

	public static final String CREATE_PFX = "CREATE_";

	public static enum Recipient
	{
		EVERYONE("*", true), USER("U"), GROUP("G"), ROLE("R"), IP_ADDRESS("I"), HTTP_REFERRER("F"), OWNER("$OWNER",
			true), SHARE_PASS("E"), TOKEN_SECRET_ID("T");

		private final String prefix;
		private final boolean standalone;

		private Recipient(String prefix)
		{
			this(prefix, false);
		}

		private Recipient(String prefix, boolean standalone)
		{
			this.prefix = prefix;
			this.standalone = standalone;
		}

		public String getPrefix()
		{
			return prefix;
		}

		public boolean isStandalone()
		{
			return standalone;
		}
	}

	public static String getRecipient(Recipient r)
	{
		if( !r.isStandalone() )
		{
			throw new IllegalArgumentException("Only recipients that are 'standalone' may use this method");
		}
		return r.getPrefix();
	}

	public static String getRecipient(Recipient r, String id)
	{
		String result = r.getPrefix();
		if( !r.isStandalone() )
		{
			Check.checkNotNull(id);

			try
			{
				id = URLEncoder.encode(id, "UTF-8");
			}
			catch( UnsupportedEncodingException ex )
			{
				throw new RuntimeException(ex);
			}
			result += ':' + id;
		}
		return result;
	}

	public static Recipient getRecipientType(String token)
	{
		for( Recipient r : Recipient.values() )
		{
			if( token.startsWith(r.getPrefix()) )
			{
				return r;
			}
		}

		throw new IllegalArgumentException("Unparseable token: " + token);
	}

	public static String getRecipientValue(String token)
	{
		String result = null;
		int index = token.indexOf(':');
		if( index >= 0 )
		{
			result = token.substring(index + 1);
			try
			{
				result = URLDecoder.decode(result, "UTF-8");
			}
			catch( UnsupportedEncodingException ex )
			{
				throw new RuntimeException(ex);
			}
		}
		return result;
	}

	private SecurityConstants()
	{
		throw new Error();
	}
}
