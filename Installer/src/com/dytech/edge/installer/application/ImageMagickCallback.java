package com.dytech.edge.installer.application;

import java.io.File;

import javax.swing.JOptionPane;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.Callback;
import com.dytech.installer.Wizard;

/**
 * @author Nicholas Read
 */
public class ImageMagickCallback implements Callback
{
	private static final String[] EXE_TYPES = {"", ".exe"}; //$NON-NLS-1$

	@Override
	public void task(Wizard installer)
	{
		PropBagEx output = installer.getOutputNow();
		File dir = new File(output.getNode("imagemagick/path")); //$NON-NLS-1$

		if( !dir.exists() || !dir.isDirectory() )
		{
			JOptionPane.showMessageDialog(installer.getFrame(), "You have not specified"
				+ " a valid directory.\nPlease select the correct path, and try again.",
				"Incorrect ImageMagick Directory", JOptionPane.ERROR_MESSAGE);
			return;
		}

		File convert = findExe(installer, dir, "convert");
		if( convert == null )
		{
			return;
		}

		File identify = findExe(installer, dir, "identify");
		if( identify == null )
		{
			return;
		}

		installer.gotoPage(installer.getCurrentPageNumber() + 1);
	}

	private File findExe(Wizard installer, File path, String exe)
	{
		for( String exeType : EXE_TYPES )
		{
			File exeFile = new File(path, exe + exeType);
			if( exeFile.canExecute() )
			{
				return exeFile;
			}
		}

		JOptionPane.showMessageDialog(installer.getFrame(), "The directory you have specified"
			+ " does not contain the ImageMagick program '" + exe + "'.\nPlease select the"
			+ " correct path, and try again.", "Incorrect ImageMagick Directory", JOptionPane.ERROR_MESSAGE);
		return null;
	}
}
