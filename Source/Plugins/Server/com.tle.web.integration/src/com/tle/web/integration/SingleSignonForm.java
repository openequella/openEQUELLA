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

package com.tle.web.integration;

import com.tle.web.sections.annotations.Bookmarked;

/**
 * @author jmaginnis
 */
@Bookmarked(supported = true)
public class SingleSignonForm
{
	@Bookmarked(stateful = false)
	private String method;
	@Bookmarked(stateful = false)
	private String template;
	@Bookmarked(stateful = false)
	private String returnurl;
	@Bookmarked(stateful = false)
	private String returnprefix;
	@Bookmarked(stateful = false)
	private String action;
	@Bookmarked(stateful = false)
	private String courseId;
	@Bookmarked(stateful = false)
	private String courseCode;
	@Bookmarked(stateful = false)
	private String cancelurl;
	@Bookmarked(stateful = false)
	private String options;
	@Bookmarked(stateful = false)
	private String query;
	@Bookmarked(stateful = false)
	private boolean itemonly = false;
	@Bookmarked(stateful = false)
	private boolean packageonly = false;
	@Bookmarked(stateful = false)
	private boolean attachmentonly = false;
	@Bookmarked(stateful = false)
	private boolean selectMultiple = false;
	@Bookmarked(stateful = false)
	private boolean useDownloadPrivilege = false;
	@Bookmarked(stateful = false)
	private String itemXml;
	@Bookmarked(stateful = false)
	private String powerXml;
	@Bookmarked(stateful = false, parameter = "structure")
	private String structure;
	@Bookmarked(stateful = false)
	private boolean forcePost;
	@Bookmarked(stateful = false)
	private boolean cancelDisabled;
	@Bookmarked(stateful = false)
	private boolean attachmentUuidUrls;

	public String getAction()
	{
		return action;
	}

	public void setAction(String action)
	{
		this.action = action;
	}

	public String getReturnprefix()
	{
		return returnprefix;
	}

	public void setReturnprefix(String returnprefix)
	{
		this.returnprefix = returnprefix;
	}

	public String getReturnurl()
	{
		return returnurl;
	}

	public void setReturnurl(String returnurl)
	{
		this.returnurl = returnurl;
	}

	public String getCourseId()
	{
		return courseId;
	}

	public void setCourseId(String courseId)
	{
		this.courseId = courseId;
	}

	public String getCourseCode()
	{
		return courseCode;
	}

	public void setCourseCode(String courseCode)
	{
		this.courseCode = courseCode;
	}

	public boolean isItemonly()
	{
		return itemonly;
	}

	public boolean isPackageonly()
	{
		return packageonly;
	}

	public void setPackageonly(boolean packageonly)
	{
		this.packageonly = packageonly;
	}

	public void setItemonly(boolean itemonly)
	{
		this.itemonly = itemonly;
	}

	public boolean isAttachmentonly()
	{
		return attachmentonly;
	}

	public void setAttachmentonly(boolean attachmentonly)
	{
		this.attachmentonly = attachmentonly;
	}

	public String getCancelurl()
	{
		return cancelurl;
	}

	public void setCancelurl(String cancelurl)
	{
		this.cancelurl = cancelurl;
	}

	public String getTemplate()
	{
		return template;
	}

	public void setTemplate(String template)
	{
		this.template = template;
	}

	public String getOptions()
	{
		return options;
	}

	public void setOptions(String options)
	{
		this.options = options;
	}

	public String getQuery()
	{
		return query;
	}

	public void setQuery(String query)
	{
		this.query = query;
	}

	public String getMethod()
	{
		return method;
	}

	public void setMethod(String method)
	{
		this.method = method;
	}

	public void setSelectMultiple(boolean selectMultiple)
	{
		this.selectMultiple = selectMultiple;
	}

	public boolean isSelectMultiple()
	{
		return selectMultiple;
	}

	public void setUseDownloadPrivilege(boolean useDownloadPrivilege)
	{
		this.useDownloadPrivilege = useDownloadPrivilege;
	}

	public boolean isUseDownloadPrivilege()
	{
		return useDownloadPrivilege;
	}

	public String getItemXml()
	{
		return itemXml;
	}

	public void setItemXml(String itemXml)
	{
		this.itemXml = itemXml;
	}

	public String getPowerXml()
	{
		return powerXml;
	}

	public void setPowerXml(String powerXml)
	{
		this.powerXml = powerXml;
	}

	public String getStructure()
	{
		return structure;
	}

	public void setStructure(String structure)
	{
		this.structure = structure;
	}

	public boolean isForcePost()
	{
		return forcePost;
	}

	public void setForcePost(boolean forcePost)
	{
		this.forcePost = forcePost;
	}

	public boolean isCancelDisabled()
	{
		return cancelDisabled;
	}

	public void setCancelDisabled(boolean cancelDisabled)
	{
		this.cancelDisabled = cancelDisabled;
	}

	public boolean isAttachmentUuidUrls()
	{
		return attachmentUuidUrls;
	}

	public void setAttachmentUuidUrls(boolean attachmentUuidUrls)
	{
		this.attachmentUuidUrls = attachmentUuidUrls;
	}
}
