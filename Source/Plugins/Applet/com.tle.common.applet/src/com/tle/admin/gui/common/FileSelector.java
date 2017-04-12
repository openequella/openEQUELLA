package com.tle.admin.gui.common;

import javax.swing.filechooser.FileFilter;

import com.dytech.gui.file.JFileSelector;
import com.tle.common.applet.client.DialogUtils;
import com.tle.common.applet.client.DialogUtils.DialogResult;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author aholland
 */
public class FileSelector extends JFileSelector
{
	/**
	 * This is why things should NEVER be made private unless you have a very
	 * good reason to. This pisses me off greatly.
	 */
	protected FileFilter protectedFileFilter;
	protected final String dialogTitle;

	public FileSelector(String dialogTitle)
	{
		this(CurrentLocale.get("com.tle.admin.gui.common.browse"), dialogTitle); //$NON-NLS-1$
	}

	public FileSelector(String browseButtonText, String dialogTitle)
	{
		super();
		this.dialogTitle = dialogTitle;
		button.setText(browseButtonText);
	}

	@Override
	protected void buttonSelected()
	{
		DialogResult result = DialogUtils.openDialog(getParent(), dialogTitle, protectedFileFilter, null);
		if( result.isOkayed() )
		{
			setSelectedFile(result.getFile());
		}
	}

	@Override
	public void setFileFilter(FileFilter filter)
	{
		protectedFileFilter = filter;
	}
}
