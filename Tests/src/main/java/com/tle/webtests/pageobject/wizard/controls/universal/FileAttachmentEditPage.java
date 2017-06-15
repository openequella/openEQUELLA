package com.tle.webtests.pageobject.wizard.controls.universal;

import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

public class FileAttachmentEditPage extends AbstractFileAttachmentEditPage<FileAttachmentEditPage>
{
	public FileAttachmentEditPage(UniversalControl universalControl)
	{
		super(universalControl);
	}

	@Override
	public String getTypeId()
	{
		return "fd";
	}

}
