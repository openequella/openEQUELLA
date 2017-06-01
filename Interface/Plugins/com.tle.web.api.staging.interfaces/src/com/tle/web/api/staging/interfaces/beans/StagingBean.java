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

package com.tle.web.api.staging.interfaces.beans;

import java.util.List;

import com.tle.web.api.interfaces.beans.AbstractExtendableBean;
import com.tle.web.api.interfaces.beans.BlobBean;

public class StagingBean extends AbstractExtendableBean
{
	private String uuid;
	private String directUrl;
	private List<BlobBean> files;

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getDirectUrl()
	{
		return directUrl;
	}

	public void setDirectUrl(String directUrl)
	{
		this.directUrl = directUrl;
	}

	public List<BlobBean> getFiles()
	{
		return files;
	}

	public void setFiles(List<BlobBean> files)
	{
		this.files = files;
	}
}