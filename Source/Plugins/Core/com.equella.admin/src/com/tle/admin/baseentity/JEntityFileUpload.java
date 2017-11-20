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

package com.tle.admin.baseentity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.tle.admin.gui.common.FileSelector;
import com.tle.common.EntityPack;
import com.tle.common.adminconsole.FileUploader;
import com.tle.common.adminconsole.RemoteAdminService;
import com.tle.common.i18n.CurrentLocale;

public class JEntityFileUpload extends FileSelector
{
	private static final long serialVersionUID = 1L;

	private final RemoteAdminService adminService;

	private EntityPack<?> pack;

	public JEntityFileUpload(RemoteAdminService adminService, String browseTitle)
	{
		super(browseTitle);
		this.adminService = adminService;
	}

	public void load(EntityPack<?> pack1, String name)
	{
		this.pack = pack1;
		setFieldText(name);
		setup();
	}

	private void setup()
	{
		if( getFieldText().length() == 0 )
		{
			button.setText(CurrentLocale.get("com.tle.admin.baseentity.jentityfileupload.browse")); //$NON-NLS-1$
		}
		else
		{
			button.setText(CurrentLocale.get("com.tle.admin.baseentity.jentityfileupload.remove")); //$NON-NLS-1$
		}
	}

	@Override
	protected void buttonSelected()
	{
		if( getFieldText().length() == 0 )
		{
			super.buttonSelected();
		}
		else
		{
			adminService.removeFile(pack.getStagingID(), getFieldText());
			setFieldText(""); //$NON-NLS-1$
			setSelectedFile(null);
		}
		setup();
	}

	public String save()
	{
		File file = getSelectedFile();
		if( file != null )
		{
			try
			{
				uploadXSLT(file);
			}
			catch( IOException e )
			{
				throw new RuntimeException(e);
			}
			return file.getName();
		}
		return getFieldText();
	}

	private void uploadXSLT(File file) throws IOException
	{
		try( InputStream in = new BufferedInputStream(new FileInputStream(file)) )
		{
			FileUploader.upload(adminService, pack.getStagingID(), file.getName(), in);
		}
	}
}
