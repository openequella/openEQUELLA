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

package com.dytech.edge.installer.application;

import java.awt.Component;
import java.io.File;

import javax.swing.JOptionPane;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.Callback;
import com.dytech.installer.Wizard;

public class LibAvCallback implements Callback
{
	private static final String[] EXE_TYPES = {"", ".exe"}; //$NON-NLS-1$

	@Override
	public void task(Wizard installer)
	{
		PropBagEx output = installer.getOutputNow();
		File dir = new File(output.getNode("libav/path")); //$NON-NLS-1$
		int result = JOptionPane.NO_OPTION;
		if( dir.getPath().equals("") )
		{
			Component parent = installer.getFrame();
			result = JOptionPane
				.showConfirmDialog(
					parent,
					"You have not entered a Libav path which means no thumbnailing or previewing of video files can occur in EQUELLA. Are you sure?", "Warning", //$NON-NLS-1$ //$NON-NLS-2$
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		}
		else
		{
			if( !dir.exists() || !dir.isDirectory() )
			{
				JOptionPane.showMessageDialog(installer.getFrame(), "You have not specified"
					+ " a valid directory.\nPlease select the correct path, and try again.",
					"Incorrect Libav Directory", JOptionPane.ERROR_MESSAGE);
				return;
			}

			File avconv = findExe(installer, dir, "avconv");
			if( avconv == null )
			{
				return;
			}

			File avprobe = findExe(installer, dir, "avprobe");
			if( avprobe == null )
			{
				return;
			}

			installer.gotoPage(installer.getCurrentPageNumber() + 1);
		}
		if( result == JOptionPane.YES_OPTION )
		{
			installer.gotoPage(installer.getCurrentPageNumber() + 1);
		}
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

		JOptionPane.showMessageDialog(installer.getFrame(),
			"The directory you have specified" + " does not contain the Libav program '" + exe
				+ "'.\nPlease select the" + " correct path, and try again.", "Incorrect LibAv Directory",
			JOptionPane.ERROR_MESSAGE);
		return null;
	}
}
