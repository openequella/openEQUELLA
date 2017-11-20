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

package com.tle.admin.common.gui;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.gui.JStatusBar;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.EditorInterface;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public final class EditorHelper
{
	private static final Log LOGGER = LogFactory.getLog(EditorHelper.class);

	/**
	 * The default text for an editors "save" button.
	 */
	public static final String DEFAULT_SAVE_TEXT = CurrentLocale.get("com.dytech.edge.admin.gui.save"); //$NON-NLS-1$

	/**
	 * The default text for an editors "close" button.
	 */
	public static final String DEFAULT_CLOSE_TEXT = CurrentLocale.get("com.dytech.edge.admin.gui.close"); //$NON-NLS-1$

	/**
	 * The default status bar spinner.
	 */
	private static ImageIcon statusBarSpinner;

	/**
	 * A private constructor that should never be called.
	 */
	private EditorHelper()
	{
		throw new Error();
	}

	/**
	 * @return the default statusbar spinner icon for editors.
	 */
	public static ImageIcon getStatusBarSpinner()
	{
		if( statusBarSpinner == null )
		{
			URL url = EditorHelper.class.getResource("/icons/network_spinner.gif"); //$NON-NLS-1$
			statusBarSpinner = new ImageIcon(url);
		}
		return statusBarSpinner;
	}

	public static void listenForClosing(final Window window, final EditorInterface editor)
	{
		if( window instanceof JFrame )
		{
			((JFrame) window).setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		}

		if( window instanceof JDialog )
		{
			((JDialog) window).setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		}

		window.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				// If the spinner is showing, then something is happening and we
				// do not
				// want the window to be closed down.
				if( !editor.getStatusBar().isSpinnerVisible() )
				{
					if( !EditorHelper.onClose(editor, null) )
					{
						return;
					}
				}
				window.setVisible(false);
				window.dispose();
			}
		});
	}

	/**
	 * This should be called by an editor when the save button is selected.
	 */
	public static void onSave(final EditorInterface editor)
	{
		saveHelper(editor, false);
	}

	/**
	 * This should be called by an editor when the close button is selected.
	 * Returns true if closed
	 */
	public static boolean onClose(EditorInterface editor, Integer confirmOption)
	{
		if( editor.isReadOnly() )
		{
			editor.close(confirmOption);
		}
		else if( !editor.hasDetectedChanges() )
		{
			unlockHelper(editor);
			editor.close(confirmOption);
		}
		else
		{
			int confirm;
			if( confirmOption == null )
			{
				String message = CurrentLocale.get("com.dytech.edge.admin.gui.editorhelper.confirm", //$NON-NLS-1$
					editor.getDocumentName());
				String[] buttons = {CurrentLocale.get("com.dytech.edge.admin.gui.save"), //$NON-NLS-1$
						CurrentLocale.get("com.dytech.edge.admin.gui.dontsave"), //$NON-NLS-1$
						CurrentLocale.get("com.dytech.edge.admin.gui.cancel")}; //$NON-NLS-1$

				confirm = JOptionPane.showOptionDialog(editor.getParentWindow(), message,
					CurrentLocale.get("com.dytech.edge.admin.gui.editorhelper.savebefore"), //$NON-NLS-1$
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, buttons, buttons[0]);
			}
			else
			{
				confirm = confirmOption;
			}

			if( confirm == JOptionPane.YES_OPTION )
			{
				saveHelper(editor, true);
			}
			if( confirm == JOptionPane.NO_OPTION )
			{
				unlockHelper(editor);
				editor.close(confirm);
			}
			else
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Provides a uniform method for saving.
	 * 
	 * @param editor The editor that we should work with.
	 * @param closeAfterSave true if the editor should close after saving.
	 */
	private static void saveHelper(final EditorInterface editor, final boolean closeAfterSave)
	{
		if( !editor.beforeSaving() )
		{
			return;
		}

		final JStatusBar statusbar = editor.getStatusBar();
		statusbar.setSpinnerVisible(true);

		final GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
		{
			@Override
			public Object construct() throws EditorException
			{
				statusbar.setMessage(CurrentLocale.get("com.dytech.edge.admin.gui.editorhelper.validating")); //$NON-NLS-1$
				editor.validation();

				statusbar.setMessage(CurrentLocale.get("com.dytech.edge.admin.gui.editorhelper.saving")); //$NON-NLS-1$
				editor.save();

				return null;
			}

			@Override
			protected void afterConstruct()
			{
				super.afterConstruct();
				statusbar.setSpinnerVisible(false);
				statusbar.clearMessage();
			}

			@Override
			public void finished()
			{
				if( closeAfterSave )
				{
					unlockHelper(editor);
					editor.close(null);
				}
				else
				{
					Driver.displayInformation(editor.getParentWindow(),
						CurrentLocale.get("com.dytech.edge.admin.gui.editorhelper.success", editor //$NON-NLS-1$
							.getDocumentName()));
				}
			}

			@Override
			public void exception()
			{
				Exception ex = getException();
				if( !(ex instanceof EditorException) )
				{
					LOGGER.fatal("Exception should have been an EditorException", ex); //$NON-NLS-1$
					Driver.displayError(editor.getParentWindow(), "unknown", ex); //$NON-NLS-1$
				}
				else
				{
					EditorException eex = (EditorException) ex;
					if( eex.isFatal() )
					{
						Driver.displayError(editor.getParentWindow(), eex.getCategory(), eex.getCause());
					}
					else
					{
						Driver.displayInformation(editor.getParentWindow(), eex.getMessage());
					}
				}
			}
		};

		worker.setComponent(editor.getParentWindow());
		worker.start();
	}

	/**
	 * Asks the editor to unlock it's resource in a new thread.
	 */
	private static void unlockHelper(final EditorInterface editor)
	{
		// Cancel the locking in the background
		final Thread t = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				editor.unlock();
			}
		});
		t.start();
	}
}
