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

package com.tle.admin.common.gui.actions;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.swing.Action;
import javax.swing.filechooser.FileFilter;

import com.dytech.devlib.PropBagEx;
import com.dytech.gui.file.FileFilterAdapter;
import com.tle.admin.Driver;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.common.applet.client.DialogUtils;
import com.tle.common.applet.client.DialogUtils.DialogResult;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public abstract class ImportAction extends TLEAction
{
	public ImportAction()
	{
		setIcon("/icons/import2.gif");
		putValue(Action.NAME, CurrentLocale.get("com.tle.admin.gui.common.actions.importaction.name"));
		putValue(Action.SHORT_DESCRIPTION, CurrentLocale.get("com.tle.admin.gui.common.actions.importaction.desc"));
	}

	public InputStream askForImportStream(Component parent, FileFilter filter)
	{
		final DialogResult result = DialogUtils.openDialog(parent,
			CurrentLocale.get("com.tle.admin.gui.common.actions.importaction.title"), filter, null);
		if( result.isOkayed() )
		{
			try
			{
				return new BufferedInputStream(new FileInputStream(result.getFile()));
			}
			catch( FileNotFoundException e )
			{
				return null;
			}
		}
		return null;
	}

	public PropBagEx askForXmlImport(Component parent)
	{
		try( InputStream in = askForImportStream(parent, FileFilterAdapter.XML()) )
		{
			return in == null ? null : new PropBagEx(in);
		}
		catch( Exception ex )
		{
			Driver.displayInformation(parent,
				CurrentLocale.get("com.tle.admin.gui.common.actions.importaction.selected"));
			return null;
		}
	}
}
