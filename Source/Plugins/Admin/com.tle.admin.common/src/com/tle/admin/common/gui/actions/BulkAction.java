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
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.common.io.FileExtensionFilter;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.beans.exception.InvalidDataException;
import com.dytech.gui.file.FileFilterAdapter;
import com.dytech.gui.workers.GlassSwingWorker;
import com.google.common.io.Files;
import com.tle.admin.Driver;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.common.applet.client.DialogUtils;
import com.tle.common.applet.client.DialogUtils.DialogResult;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public abstract class BulkAction<T> extends TLEAction
{
	static final Log LOGGER = LogFactory.getLog(BulkAction.class);

	protected String successMessage;
	protected String confirmOverrideMessage;
	protected String errorMessage;
	protected String importDialogTitle;

	@SuppressWarnings("nls")
	public BulkAction()
	{
		setIcon("/icons/import2.gif");
		putValue(Action.NAME, CurrentLocale.get("com.tle.admin.gui.common.actions.bulkaction.name"));
		putValue(Action.SHORT_DESCRIPTION, CurrentLocale.get("com.tle.admin.gui.common.actions.bulkaction.description"));

		successMessage = CurrentLocale.get("com.tle.admin.gui.common.actions.bulkaction.success");
		confirmOverrideMessage = CurrentLocale.get("com.tle.admin.gui.common.actions.bulkaction.override");
		errorMessage = CurrentLocale.get("com.tle.admin.gui.common.actions.bulkaction.error");
		importDialogTitle = CurrentLocale.get("com.tle.admin.gui.common.actions.bulkaction.title");
	}

	@SuppressWarnings("nls")
	@Override
	public void actionPerformed(ActionEvent e)
	{
		final Component parentFrame = getParent();
		int result2 = JOptionPane.showConfirmDialog(parentFrame, confirmOverrideMessage);
		if( result2 == JOptionPane.CANCEL_OPTION )
		{
			return;
		}
		final boolean override = result2 == JOptionPane.YES_OPTION;

		FileFilterAdapter csvFilter = new FileFilterAdapter(new FileExtensionFilter("csv"),
			CurrentLocale.get("com.tle.admin.gui.common.actions.bulkaction.csvfile"));

		final DialogResult result = DialogUtils.openDialog(parentFrame, importDialogTitle, csvFilter, null);
		if( result.isOkayed() )
		{
			final File selectedFile = result.getFile();

			GlassSwingWorker<Void> worker = new GlassSwingWorker<Void>()
			{
				@Override
				public Void construct() throws Exception
				{
					bulkImport(Files.toByteArray(selectedFile), override);
					return null;
				}

				@Override
				protected void afterFinished()
				{
					refresh();
					JOptionPane.showMessageDialog(parentFrame, successMessage);
				}

				@Override
				public void exception()
				{
					Throwable t = getException();
					if( t instanceof InvalidDataException )
					{
						StringBuilder validationMessages = new StringBuilder();
						List<ValidationError> errors = ((InvalidDataException) t).getErrors();
						if( errors.size() > 0 )
						{
							for( ValidationError error : errors )
							{
								validationMessages.append(error.getField());
								validationMessages.append(": "); //$NON-NLS-1$
								validationMessages.append(error.getMessage());
								validationMessages.append("\n"); //$NON-NLS-1$
							}
						}
						else
						{
							validationMessages.append(t.getMessage());
						}
						Driver.displayInformation(parentFrame, validationMessages.toString());
					}
					else
					{
						LOGGER.error(t);
						Driver.displayError(parentFrame, errorMessage, t);
					}
				}
			};
			worker.setComponent(parentFrame);
			worker.start();
		}
	}

	protected abstract Component getParent();

	protected abstract void bulkImport(byte[] array, boolean override) throws Exception;

	protected abstract void refresh();

	public String getSuccessMessage()
	{
		return successMessage;
	}

	public void setSuccessMessage(String successMessage)
	{
		this.successMessage = successMessage;
	}

	public String getConfirmOverrideMessage()
	{
		return confirmOverrideMessage;
	}

	public void setConfirmOverrideMessage(String confirmOverrideMessage)
	{
		this.confirmOverrideMessage = confirmOverrideMessage;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	public String getImportDialogTitle()
	{
		return importDialogTitle;
	}

	public void setImportDialogTitle(String importDialogTitle)
	{
		this.importDialogTitle = importDialogTitle;
	}
}
