package com.tle.web.filemanager.applet.backend;

import java.util.LinkedList;
import java.util.List;

import org.jvnet.flamingo.bcb.BreadcrumbBarCallBack;
import org.jvnet.flamingo.bcb.BreadcrumbItem;

import com.tle.web.filemanager.applet.gui.SystemIconCache;
import com.tle.web.filemanager.common.FileInfo;

/**
 * @author Nicholas Read
 */
public class BreadCrumbAdapter extends BreadcrumbBarCallBack<FileInfo>
{
	private final Backend backend;

	public BreadCrumbAdapter(Backend backend)
	{
		this.backend = backend;
	}

	@Override
	public List<KeyValuePair<FileInfo>> getPathChoices(BreadcrumbItem<FileInfo>[] paths)
	{
		String folderPath = ""; //$NON-NLS-1$
		if( paths != null )
		{
			folderPath = paths[paths.length - 1].getValue().getFullPath();
		}

		List<KeyValuePair<FileInfo>> results = new LinkedList<KeyValuePair<FileInfo>>();
		for( FileInfo child : backend.listFiles(folderPath) )
		{
			if( child.isDirectory() )
			{
				KeyValuePair<FileInfo> kvp = new KeyValuePair<FileInfo>(child.getName(), child);
				kvp.setIcon(SystemIconCache.getIcon(child, false));
				results.add(kvp);
			}
		}
		return results;
	}
}
