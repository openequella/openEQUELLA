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
