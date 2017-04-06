package com.tle.web.scorm;

import com.tle.web.scorm.treeviewer.ScormTreeNavigationSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewurl.ItemUrlExtender;

public class DownloadScormPackageLink implements ItemUrlExtender
{
	private static final long serialVersionUID = 1L;

	@Override
	public void execute(SectionInfo info)
	{
		ScormTreeNavigationSection navSection = info.lookupSection(ScormTreeNavigationSection.class);
		if( navSection != null )
		{
			navSection.getModel(info).setDownload(true);
		}
	}
}
