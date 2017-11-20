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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.swing.Action;
import javax.swing.JOptionPane;

import com.dytech.edge.common.Constants;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.common.applet.client.DialogUtils;
import com.tle.common.applet.client.DialogUtils.DialogResult;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public abstract class ExportAction extends TLEAction
{
	public ExportAction()
	{
		setIcon("/icons/export2.gif"); //$NON-NLS-1$
		putValue(Action.NAME, CurrentLocale.get("com.tle.admin.gui.common.actions.exportaction.name")); //$NON-NLS-1$
		putValue(Action.SHORT_DESCRIPTION, CurrentLocale.get("com.tle.admin.gui.common.actions.exportaction.desc")); //$NON-NLS-1$
	}

	public File askForDestinationFile(Component parent, String objectName, String extension)
	{
		File file = null;
		final DialogResult result = DialogUtils.saveDialog(parent,
			CurrentLocale.get("com.tle.admin.gui.common.actions.exportaction.title"), //$NON-NLS-1$
			null, DialogUtils.getSuggestedFileName(objectName, extension));
		if( result.isOkayed() )
		{
			file = result.getFile();
			if( file.exists() )
			{
				final int result2 = JOptionPane.showConfirmDialog(parent,
					CurrentLocale.get("com.tle.admin.gui.common.actions.exportaction.confirm"), CurrentLocale //$NON-NLS-1$
						.get("com.tle.admin.gui.common.actions.exportaction.overwrite"), //$NON-NLS-1$
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

				if( result2 != JOptionPane.YES_OPTION )
				{
					file = null;
				}
			}
		}
		return file;
	}

	public <T> void writeFile(Component parent, final File destination, final ExportSerialiser getter)
	{
		GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
		{
			@Override
			public Object construct() throws Exception
			{
				try( Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destination),
					Constants.UTF8)) )
				{
					out.write(getter.getSerialisedForm());
				}
				return null;
			}

			@Override
			public void finished()
			{
				Driver.displayInformation(getComponent(),
					CurrentLocale.get("com.tle.admin.gui.common.actions.exportaction.successful")); //$NON-NLS-1$
			}

			@Override
			public void exception()
			{
				Driver.displayInformation(getComponent(),
					CurrentLocale.get("com.tle.admin.gui.common.actions.exportaction.error")); //$NON-NLS-1$
				getException().printStackTrace();
			}
		};

		worker.setComponent(parent);
		worker.start();
	}

	public interface ExportSerialiser
	{
		String getSerialisedForm() throws Exception;
	}
}
