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

package com.tle.web.api.item.interfaces.beans;

import java.util.List;

import com.tle.web.api.item.interfaces.beans.GenericFileBean;

@SuppressWarnings("nls")
public class FolderBean extends GenericFileBean
{
	@SuppressWarnings("hiding")
	public static final String TYPE = "folder";

	private List<FileBean> files;
	private List<FolderBean> folders;

	public List<FileBean> getFiles()
	{
		return files;
	}

	public void setFiles(List<FileBean> files)
	{
		this.files = files;
	}

	public List<FolderBean> getFolders()
	{
		return folders;
	}

	public void setFolders(List<FolderBean> folders)
	{
		this.folders = folders;
	}
}
