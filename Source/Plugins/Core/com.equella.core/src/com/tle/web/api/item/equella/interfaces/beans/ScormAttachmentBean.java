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

package com.tle.web.api.item.equella.interfaces.beans;

@SuppressWarnings("nls")
public class ScormAttachmentBean extends EquellaAttachmentBean
{
	private String packageFile;
	private String scormVersion;
	private long size;
	private String md5;

	public String getScormVersion()
	{
		return scormVersion;
	}

	public void setScormVersion(String scormVersion)
	{
		this.scormVersion = scormVersion;
	}

	@Override
	public String getRawAttachmentType()
	{
		return "custom/scorm";
	}

	public String getPackageFile()
	{
		return packageFile;
	}

	public void setPackageFile(String packageFile)
	{
		this.packageFile = packageFile;
	}

	public long getSize()
	{
		return size;
	}

	public void setSize(long size)
	{
		this.size = size;
	}

	public String getMd5()
	{
		return md5;
	}

	public void setMd5(String md5)
	{
		this.md5 = md5;
	}
}
