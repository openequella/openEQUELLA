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

package com.tle.core.connectors.brightspace.beans;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Aaron
 *
 */
@XmlRootElement
public class BrightspaceLtiLink
{
	@JsonProperty("LtiLinkId")
	private String ltiLinkId;
	@JsonProperty("OrgUnitId")
	private String orgUnitId;
	@JsonProperty("Title")
	private String title;
	@JsonProperty("Url")
	private String url;
	@JsonProperty("Description")
	private String description;
	@JsonProperty("Key")
	private String key;
	@JsonProperty("PlainSecret")
	private String plainSecret;
	@JsonProperty("IsVisible")
	private boolean visible;
	@JsonProperty("SignMessage")
	private boolean signMessage;
	@JsonProperty("SignWithTc")
	private boolean signWithTc;
	@JsonProperty("SendTcInfo")
	private boolean sendTcInfo;
	@JsonProperty("SendContextInfo")
	private boolean sendContextInfo;
	@JsonProperty("SendUserId")
	private boolean sendUserId;
	@JsonProperty("SendUserName")
	private boolean sendUserName;
	@JsonProperty("SendUserEmail")
	private boolean sendUserEmail;
	@JsonProperty("SendLinkTitle")
	private boolean sendLinkTitle;
	@JsonProperty("SendLinkDescription")
	private boolean sendLinkDescription;
	@JsonProperty("SendD2LUserName")
	private boolean sendD2LUserName;
	@JsonProperty("SendD2LOrgDefinedId")
	private boolean sendD2LOrgDefinedId;
	@JsonProperty("SendD2LOrgRoleId")
	private boolean sendD2LOrgRoleId;
	//Note: this is MANDATORY, or it won't work.
	@JsonInclude(Include.ALWAYS)
	@JsonProperty("CustomParameters")
	private Object[] customParameters;

	public String getLtiLinkId()
	{
		return ltiLinkId;
	}

	public void setLtiLinkId(String ltiLinkId)
	{
		this.ltiLinkId = ltiLinkId;
	}

	public String getOrgUnitId()
	{
		return orgUnitId;
	}

	public void setOrgUnitId(String orgUnitId)
	{
		this.orgUnitId = orgUnitId;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public String getPlainSecret()
	{
		return plainSecret;
	}

	public void setPlainSecret(String plainSecret)
	{
		this.plainSecret = plainSecret;
	}

	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	public boolean isSignMessage()
	{
		return signMessage;
	}

	public void setSignMessage(boolean signMessage)
	{
		this.signMessage = signMessage;
	}

	public boolean isSignWithTc()
	{
		return signWithTc;
	}

	public void setSignWithTc(boolean signWithTc)
	{
		this.signWithTc = signWithTc;
	}

	public boolean isSendTcInfo()
	{
		return sendTcInfo;
	}

	public void setSendTcInfo(boolean sendTcInfo)
	{
		this.sendTcInfo = sendTcInfo;
	}

	public boolean isSendContextInfo()
	{
		return sendContextInfo;
	}

	public void setSendContextInfo(boolean sendContextInfo)
	{
		this.sendContextInfo = sendContextInfo;
	}

	public boolean isSendUserId()
	{
		return sendUserId;
	}

	public void setSendUserId(boolean sendUserId)
	{
		this.sendUserId = sendUserId;
	}

	public boolean isSendUserName()
	{
		return sendUserName;
	}

	public void setSendUserName(boolean sendUserName)
	{
		this.sendUserName = sendUserName;
	}

	public boolean isSendUserEmail()
	{
		return sendUserEmail;
	}

	public void setSendUserEmail(boolean sendUserEmail)
	{
		this.sendUserEmail = sendUserEmail;
	}

	public boolean isSendLinkTitle()
	{
		return sendLinkTitle;
	}

	public void setSendLinkTitle(boolean sendLinkTitle)
	{
		this.sendLinkTitle = sendLinkTitle;
	}

	public boolean isSendLinkDescription()
	{
		return sendLinkDescription;
	}

	public void setSendLinkDescription(boolean sendLinkDescription)
	{
		this.sendLinkDescription = sendLinkDescription;
	}

	public boolean isSendD2LUserName()
	{
		return sendD2LUserName;
	}

	public void setSendD2LUserName(boolean sendD2LUserName)
	{
		this.sendD2LUserName = sendD2LUserName;
	}

	public boolean isSendD2LOrgDefinedId()
	{
		return sendD2LOrgDefinedId;
	}

	public void setSendD2LOrgDefinedId(boolean sendD2LOrgDefinedId)
	{
		this.sendD2LOrgDefinedId = sendD2LOrgDefinedId;
	}

	public boolean isSendD2LOrgRoleId()
	{
		return sendD2LOrgRoleId;
	}

	public void setSendD2LOrgRoleId(boolean sendD2LOrgRoleId)
	{
		this.sendD2LOrgRoleId = sendD2LOrgRoleId;
	}
}
