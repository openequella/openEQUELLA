package com.tle.web.api.item;

import java.net.URI;

import com.tle.beans.item.ItemKey;
import com.tle.core.filesystem.StagingFile;
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
