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

package com.tle.web.lti;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Aaron
 */
public class LtiData implements Serializable
{
	private String contextId;
	private String contextLabel;
	private String contextTitle;

	private String userId;

	private String resourceLinkId;
	private String resourceLinkTitle;
	private String toolConsumerInfoProductFamilyCode;
	private String toolConsumerInfoVersion;
	private String toolConsumerInstanceGuid;

	private String returnUrl;
	private String launchPresentationDocumentTarget;

	private final Map<String, String> custom = new HashMap<>();

	private LisData lisData;
	private OAuthData oAuthData;

	public String getContextId()
	{
		return contextId;
	}

	public void setContextId(String contextId)
	{
		this.contextId = contextId;
	}

	public String getContextLabel()
	{
		return contextLabel;
	}

	public void setContextLabel(String contextLabel)
	{
		this.contextLabel = contextLabel;
	}

	public String getContextTitle()
	{
		return contextTitle;
	}

	public void setContextTitle(String contextTitle)
	{
		this.contextTitle = contextTitle;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public String getResourceLinkId()
	{
		return resourceLinkId;
	}

	public void setResourceLinkId(String resourceLinkId)
	{
		this.resourceLinkId = resourceLinkId;
	}

	public String getResourceLinkTitle()
	{
		return resourceLinkTitle;
	}

	public void setResourceLinkTitle(String resourceLinkTitle)
	{
		this.resourceLinkTitle = resourceLinkTitle;
	}

	public String getToolConsumerInfoProductFamilyCode()
	{
		return toolConsumerInfoProductFamilyCode;
	}

	public void setToolConsumerInfoProductFamilyCode(String toolConsumerInfoProductFamilyCode)
	{
		this.toolConsumerInfoProductFamilyCode = toolConsumerInfoProductFamilyCode;
	}

	public String getToolConsumerInfoVersion()
	{
		return toolConsumerInfoVersion;
	}

	public void setToolConsumerInfoVersion(String toolConsumerInfoVersion)
	{
		this.toolConsumerInfoVersion = toolConsumerInfoVersion;
	}

	public String getToolConsumerInstanceGuid()
	{
		return toolConsumerInstanceGuid;
	}

	public void setToolConsumerInstanceGuid(String toolConsumerInstanceGuid)
	{
		this.toolConsumerInstanceGuid = toolConsumerInstanceGuid;
	}

	public LisData getLisData()
	{
		return lisData;
	}

	public void setLisData(LisData lisData)
	{
		this.lisData = lisData;
	}

	public OAuthData getOAuthData()
	{
		return oAuthData;
	}

	public void setOAuthData(OAuthData oAuthData)
	{
		this.oAuthData = oAuthData;
	}

	public String getReturnUrl()
	{
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl)
	{
		this.returnUrl = returnUrl;
	}

	public void addCustom(String name, String value)
	{
		custom.put(name, value);
	}

	public String getCustom(String name)
	{
		return custom.get(name);
	}

	public String getLaunchPresentationDocumentTarget()
	{
		return launchPresentationDocumentTarget;
	}

	public void setLaunchPresentationDocumentTarget(String launchPresentationDocumentTarget)
	{
		this.launchPresentationDocumentTarget = launchPresentationDocumentTarget;
	}

	public Map<String, String> getCustomParameters()
	{
		return custom;
	}

	public static class OAuthData implements Serializable
	{
		private String nonce;
		private String consumerKey;
		private String consumerSecret;
		private String signatureMethod;

		public String getNonce()
		{
			return nonce;
		}

		public void setNonce(String nonce)
		{
			this.nonce = nonce;
		}

		public String getConsumerKey()
		{
			return consumerKey;
		}

		public void setConsumerKey(String consumerKey)
		{
			this.consumerKey = consumerKey;
		}

		public String getConsumerSecret()
		{
			return consumerSecret;
		}

		public void setConsumerSecret(String consumerSecret)
		{
			this.consumerSecret = consumerSecret;
		}

		public String getSignatureMethod()
		{
			return signatureMethod;
		}

		public void setSignatureMethod(String signatureMethod)
		{
			this.signatureMethod = signatureMethod;
		}

	}

	public static class LisData implements Serializable
	{
		private String resultSourcedid;
		private String outcomeServiceUrl;
		private String personNameGiven;
		private String personNameFamily;
		private String personNameFull;
		private String contactEmailPrimary;

		public String getResultSourcedid()
		{
			return resultSourcedid;
		}

		public void setResultSourcedid(String resultSourcedid)
		{
			this.resultSourcedid = resultSourcedid;
		}

		public String getOutcomeServiceUrl()
		{
			return outcomeServiceUrl;
		}

		public void setOutcomeServiceUrl(String outcomeServiceUrl)
		{
			this.outcomeServiceUrl = outcomeServiceUrl;
		}

		public String getPersonNameGiven()
		{
			return personNameGiven;
		}

		public void setPersonNameGiven(String personNameGiven)
		{
			this.personNameGiven = personNameGiven;
		}

		public String getPersonNameFamily()
		{
			return personNameFamily;
		}

		public void setPersonNameFamily(String personNameFamily)
		{
			this.personNameFamily = personNameFamily;
		}

		public String getPersonNameFull()
		{
			return personNameFull;
		}

		public void setPersonNameFull(String personNameFull)
		{
			this.personNameFull = personNameFull;
		}

		public String getContactEmailPrimary()
		{
			return contactEmailPrimary;
		}

		public void setContactEmailPrimary(String contactEmailPrimary)
		{
			this.contactEmailPrimary = contactEmailPrimary;
		}
	}
}