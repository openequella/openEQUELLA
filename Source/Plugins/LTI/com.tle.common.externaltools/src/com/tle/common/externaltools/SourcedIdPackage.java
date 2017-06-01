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

package com.tle.common.externaltools;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author larry
 */
public class SourcedIdPackage
{
	protected static final String LMS_SOURCED_ID = "lmsSourcedId";
	protected static final String LMS_OUTCOME_SERVICE_URL = "lmsOutcomeServiceUrl";
	protected static final String LMS_OAUTH_CONSUMER_KEY = "lmsOauthConsumerKey";
	private String lmsSourcedId;
	private String lmsOutcomeServiceUrl;
	private String lmsOauthConsumerKey;

	// default constructor in case Gson/Json needs it
	public SourcedIdPackage()
	{
		super();
	}

	public SourcedIdPackage(String sourcedId, String outcomeServiceUrl, String oauthConsumerKey)
	{
		this.lmsSourcedId = sourcedId;
		this.lmsOutcomeServiceUrl = outcomeServiceUrl;
		this.lmsOauthConsumerKey = oauthConsumerKey;
	}

	public String getLmsSourcedId()
	{
		return lmsSourcedId;
	}

	public String getLmsOutcomeServiceUrl()
	{
		return lmsOutcomeServiceUrl;
	}

	public String getLmsOauthConsumerKey()
	{
		return lmsOauthConsumerKey;
	}

	public static class SrcdIdPkgSerializer implements JsonSerializer<SourcedIdPackage>
	{
		/*
		 * Calling the JsonPrimitive Constructor on null just throws a
		 * nullPointer exception, so we're better off forestalling that
		 * possibility here.
		 */
		@Override
		public JsonElement serialize(SourcedIdPackage src, Type typeOfSrc, JsonSerializationContext context)
		{
			JsonObject result = new JsonObject();
			String srcElem = src.getLmsSourcedId();
			if( srcElem != null )
			{
				result.add(LMS_SOURCED_ID, new JsonPrimitive(srcElem));
			}
			srcElem = src.getLmsOutcomeServiceUrl();
			if( srcElem != null )
			{
				result.add(LMS_OUTCOME_SERVICE_URL, new JsonPrimitive(srcElem));
			}
			srcElem = src.getLmsOauthConsumerKey();
			if( srcElem != null )
			{
				result.add(LMS_OAUTH_CONSUMER_KEY, new JsonPrimitive(srcElem));
			}
			return result;
		}

		public SourcedIdPackage deserialize(JsonElement j)
		{
			JsonObject jsonObject = j.getAsJsonObject();
			String lmsSourcedId = jsonObject.get(LMS_SOURCED_ID) != null ? jsonObject.get(LMS_SOURCED_ID).getAsString()
				: null;
			String lmsOutcomeServiceUrl = jsonObject.get(LMS_OUTCOME_SERVICE_URL) != null ? jsonObject.get(
				LMS_OUTCOME_SERVICE_URL).getAsString() : null;
			String lmsOauthConsumerKey = jsonObject.get(LMS_OAUTH_CONSUMER_KEY) != null ? jsonObject.get(
				LMS_OAUTH_CONSUMER_KEY).getAsString() : null;

			return new SourcedIdPackage(lmsSourcedId, lmsOutcomeServiceUrl, lmsOauthConsumerKey);
		}
	}

}
