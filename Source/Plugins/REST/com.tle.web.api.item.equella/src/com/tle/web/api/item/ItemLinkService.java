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

package com.tle.web.api.item;

import java.net.URI;

import com.tle.beans.item.ItemKey;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;
import com.tle.web.api.item.interfaces.beans.FileBean;
import com.tle.web.api.item.interfaces.beans.FolderBean;
import com.tle.web.api.item.interfaces.beans.ItemBean;
import com.tle.web.api.item.interfaces.beans.RootFolderBean;

public interface ItemLinkService
{
	ItemBean addLinks(ItemBean itemBean);

	EquellaItemBean addLinks(EquellaItemBean itemBean);

	URI getFileDirURI(StagingFile staging, String path);

	URI getFileContentURI(StagingFile staging, String path);

	URI getItemURI(ItemKey itemKey);

	RootFolderBean addLinks(RootFolderBean stagingBean);

	FileBean addLinks(StagingFile staging, FileBean fileBean, String fullPath);

	FolderBean addLinks(StagingFile staging, FolderBean fileBean, String fullPath);
}
