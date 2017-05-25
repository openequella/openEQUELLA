package com.tle.web.filemanager.applet.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.KeyStroke;

import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.appletcommon.gui.GlassProgressWorker;
import com.tle.web.filemanager.applet.FileListPanel;
import com.tle.web.filemanager.applet.backend.Backend;
import com.tle.web.filemanager.common.FileInfo;

@SuppressWarnings("nls")
public class MarkAsResourceAction extends TLEAction
{
	public static final String ICON_PATH = "star.gif";

	private static final String MARK_NAME = CurrentLocale.get("action.markasres.name");
	private static final String UNMARK_NAME = CurrentLocale.get("action.markasres.altname");

	private final Backend backend;
	private final FileListPanel fileList;
	private final boolean autoMarkAsResource;

	public MarkAsResourceAction(Backend backend, FileListPanel fileList, boolean autoMarkAsResource)
	{
		super(MARK_NAME);
		this.autoMarkAsResource = autoMarkAsResource;

		setIcon(ICON_PATH);
		setShortDescription(CurrentLocale.get("action.markasres.desc"));
		setMnemonic(KeyEvent.VK_S);

		this.backend = backend;
		this.fileList = fileList;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		final List<FileInfo> selected = fileList.getSelectedFiles();

		GlassProgressWorker<?> worker = new GlassProgressWorker<Object>(
			CurrentLocale.get("action.markasres.progress.unknown"), selected.size(), false)
		{
			@Override
			public Object construct() throws Exception
			{
				for( FileInfo info : selected )
				{
					setMessage(CurrentLocale.get("action.markasres.progress.marking", info.getName()));
					backend.toggleMarkAsResource(info);
					addProgress(1);
				}
				return null;
			}

		};
		worker.setComponent(fileList);
		worker.start();
	}

	@Override
	public void update()
	{
		setEnabled(false);
		if( autoMarkAsResource )
		{
			FileInfo cd = fileList.getCurrentDirectory();
			if( cd == null || cd.isRoot() )
			{
				// top level folder, always disable the button and set text to
				// 'unmark'
				putValue(Action.NAME, UNMARK_NAME);
				return;
			}
		}

		List<FileInfo> files = fileList.getSelectedFiles();
		if( !files.isEmpty() )
		{
			boolean foundMarked = false;
			boolean foundUnmarked = false;

			for( FileInfo file : files )
			{
				if( file.isDirectory() )
				{
					return;
				}
				else if( file.isMarkAsAttachment() )
				{
					foundMarked = true;
					if( foundUnmarked )
					{
						return;
					}
				}
				else
				{
					foundUnmarked = true;
					if( foundMarked )
					{
						return;
					}
				}
			}

			if( !foundMarked || !foundUnmarked )
			{
				putValue(Action.NAME, foundMarked ? UNMARK_NAME : MARK_NAME);
				setEnabled(true);
			}
		}
	}

	@Override
	public KeyStroke invokeForWindowKeyStroke()
	{
		return KeyStroke.getKeyStroke("VK_S");
	}
}