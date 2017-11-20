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

package com.tle.web.wizard.section.model;

import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.equella.layout.TwoColumnLayout.TwoColumnModel;

/**
 * @author jmaginnis
 */

public class WizardForm extends TwoColumnModel
{
	// Wizard stuff
	@Bookmarked
	protected String wizid;
	@Bookmarked(onlyForContext = "wizardEntry")
	private String method;
	@Bookmarked(onlyForContext = "wizardEntry")
	private String itemdefUuid;
	@Bookmarked(onlyForContext = "wizardEntry")
	private String uuid;
	@Bookmarked(onlyForContext = "wizardEntry")
	private int version;
	@Bookmarked(onlyForContext = "wizardEntry")
	private boolean redraft;
	@Bookmarked(onlyForContext = "wizardEntry")
	private boolean edit;
	@Bookmarked(onlyForContext = "wizardEntry")
	private boolean newversion;
	@Bookmarked(onlyForContext = "wizardEntry")
	private String transform;
	@Bookmarked(onlyForContext = "wizardEntry")
	private boolean cloneAttachments;

	private String reason;
	private boolean showErrorHelp;

	private boolean noCancel;
	private Throwable exception;

	public String getReason()
	{
		return reason;
	}

	public void setReason(final String reason)
	{
		this.reason = reason;
	}

	public boolean isShowErrorHelp()
	{
		return showErrorHelp;
	}

	public void setShowErrorHelp(boolean showErrorHelp)
	{
		this.showErrorHelp = showErrorHelp;
	}

	public String getWizid()
	{
		return wizid;
	}

	public void setWizid(final String wizid)
	{
		this.wizid = wizid;
	}

	public boolean isEdit()
	{
		return edit;
	}

	public void setEdit(final boolean edit)
	{
		this.edit = edit;
	}

	public boolean isRedraft()
	{
		return redraft;
	}

	public void setRedraft(final boolean redraft)
	{
		this.redraft = redraft;
	}

	public boolean isNewversion()
	{
		return newversion;
	}

	public void setNewversion(final boolean newversion)
	{
		this.newversion = newversion;
	}

	public String getItemdefUuid()
	{
		return itemdefUuid;
	}

	public void setItemdefUuid(final String itemdefUuid)
	{
		this.itemdefUuid = itemdefUuid;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(final String uuid)
	{
		this.uuid = uuid;
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(final int version)
	{
		this.version = version;
	}

	public String getMethod()
	{
		return method;
	}

	public void setMethod(final String method)
	{
		this.method = method;
	}

	// public LanguageBundle getTitle()
	// {
	// return title;
	// }
	//
	// public void setTitle(final LanguageBundle title)
	// {
	// this.title = title;
	// }

	public String getTransform()
	{
		return transform;
	}

	public void setTransform(final String transform)
	{
		this.transform = transform;
	}

	public boolean isCloneAttachments()
	{
		return cloneAttachments;
	}

	public void setCloneAttachments(final boolean cloneAttachments)
	{
		this.cloneAttachments = cloneAttachments;
	}

	public boolean isNoCancel()
	{
		return noCancel;
	}

	public void setNoCancel(final boolean noCancel)
	{
		this.noCancel = noCancel;
	}

	public Throwable getException()
	{
		return exception;
	}

	public void setException(Throwable exception)
	{
		this.exception = exception;
	}
}
