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

package com.dytech.edge.queries;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tle.common.Check;

/**
 * @author jmaginnis
 */
@SuppressWarnings("nls")
public abstract class FreeTextQuery implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FIELD_UUID = "uuid";
	public static final String FIELD_VERSION = "version";
	public static final String FIELD_UNIQUE = "unique";
	public static final String FIELD_ID = "id";
	public static final String FIELD_ID_RANGEABLE = "id_range";
	public static final String FIELD_ITEMDEFID = "itemdefid";
	public static final String FIELD_SCHEMAID = "schemaid";
	public static final String FIELD_LIVE = "live";
	public static final String FIELD_ITEMSTATUS = "itemstatus";
	public static final String FIELD_OWNER = "owner";
	public static final String FIELD_BODY = "body";
	public static final String FIELD_BODY_NOSTEM = "body_nostem";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_NAME_VECTORED = "name_vectored";
	public static final String FIELD_NAME_VECTORED_NOSTEM = "name_vectored_nostem";
	public static final String FIELD_NAME_AUTOCOMPLETE = "name_autocomplete";
	public static final String FIELD_ALL = "all";
	public static final String FIELD_COLLABORATOR = "collaborator";
	public static final String FIELD_PACKAGEFILE = "/item/itembody/packagefile";
	public static final String FIELD_CAL = "/item/copyright/uniqueid";
	public static final String FIELD_DOWNLOAD_URL = "/item/originalurl";
	public static final String FIELD_MODERATING = "moderating";
	public static final String FIELD_ATTACHMENT_VECTORED = "attachment_vectored";
	public static final String FIELD_ATTACHMENT_VECTORED_NOSTEM = "attachment_vectored_nostem";
	public static final String FIELD_ATTACHMENT_MIME_TYPES = "attachment.mimetypes";
	public static final String FIELD_ATTACHMENT_UUID = "attachment.uuid";
	public static final String FIELD_ATTACHMENT_UUID_VERSION = "attachment.uuid.version";
	public static final String FIELD_LIVE_APPROVAL_DATE = "live.approval.date";
	public static final String FIELD_REAL_THUMB = "realthumb";
	public static final String FIELD_VIDEO_THUMB = "videothumb";
	public static final String FIELD_IS_BAD_URL = "badurl";

	public static final String FIELD_INDEXEDTIME = "indexedtime";
	public static final String FIELD_RATING = "rating";
	public static final String FIELD_WORKFLOW_UNANIMOUS = "unanimous";
	public static final String FIELD_WORKFLOW_ASSIGNEDTO = "assignedto";
	public static final String FIELD_WORKFLOW_ACCEPTED = "accepted";
	public static final String FIELD_WORKFLOW_TASKID = "taskid";
	public static final String FIELD_INSTITUTION = "institution";
	public static final String FIELD_REALLASTMODIFIED = "lastmodified";
	public static final String FIELD_REALCREATED = "created";

	public static final String FIELD_COURSE_ID = "courseid";
	public static final String FIELD_ACTIVATION_ID = "activation_id";
	public static final String FIELD_ACTIVATION_ATTACHMENT = "activation_attachment";
	public static final String FIELD_ACTIVATION_FROM = "activation_from";
	public static final String FIELD_ACTIVATION_UNTIL = "activation_until";
	public static final String FIELD_ACTIVATION_STATUS = "activation_status";

	public static final String FIELD_BOOKMARK_DATE = "bookmark_date";
	public static final String FIELD_BOOKMARK_OWNER = "bookmark_owner";
	public static final String FIELD_BOOKMARK_TAGS = "bookmark_tags";

	// The things one learns from Sonar's quibbles - the mutability of the
	// contents of a final array, so ...
	public static final List<String> BASIC_NAME_BODY_ATTACHMENT_FIELDS = Collections.unmodifiableList(Arrays.asList(
		FIELD_NAME_VECTORED, FreeTextQuery.FIELD_BODY, FreeTextQuery.FIELD_ATTACHMENT_VECTORED));

	private static final Map<String, String> FIELD_MAPPING = new HashMap<String, String>();
	static
	{
		FIELD_MAPPING.put("/item/datemodified", FIELD_REALLASTMODIFIED);
		FIELD_MAPPING.put("/item/datecreated", FIELD_REALLASTMODIFIED);
		FIELD_MAPPING.put("/item/@id", FIELD_UUID);
		FIELD_MAPPING.put("/item/@version", FIELD_VERSION);
		FIELD_MAPPING.put("/item/@itemdefid", FIELD_ITEMDEFID);
		FIELD_MAPPING.put("/item/@itemstatus", FIELD_ITEMSTATUS);
		FIELD_MAPPING.put("/item/owner", FIELD_OWNER);
		FIELD_MAPPING.put("/item/attachments/attachment/mimetype", FIELD_ATTACHMENT_MIME_TYPES);
		FIELD_MAPPING.put("/item/moderation/liveapprovaldate", FIELD_LIVE_APPROVAL_DATE);
	}

	public static String getRealField(String field)
	{
		String t = FIELD_MAPPING.get(field.toLowerCase());
		return t != null ? t : field;
	}

	public static String combineQuery(String q1, String q2)
	{
		if( Check.isEmpty(q2) )
		{
			return q1;
		}
		if( Check.isEmpty(q1) )
		{
			return q2;
		}
		else
		{
			return '(' + q1 + ") AND (" + q2 + ')';
		}

	}

	protected FreeTextQuery()
	{
		// nothing
	}
}
