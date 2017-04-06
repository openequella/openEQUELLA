package com.tle.web.filemanager.applet.actions;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.dytech.gui.ComponentHelper;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.appletcommon.gui.GlassProgressWorker;
import com.tle.web.appletcommon.gui.InterruptAwareGlassPaneProgressMonitorCallback;
import com.tle.web.appletcommon.gui.UserCancelledException;
import com.tle.web.filemanager.applet.FileListPanel;
import com.tle.web.filemanager.applet.backend.Backend;
import com.tle.web.filemanager.applet.gui.CheckboxList;
import com.tle.web.filemanager.common.FileInfo;

public class SynchroniseChangesAction extends TLEAction
{
	private final FileListPanel fileList;
	private final Backend backend;

	public SynchroniseChangesAction(Backend backend, FileListPanel fileList)
	{
		super(CurrentLocale.get("action.sync.name")); //$NON-NLS-1$

		this.backend = backend;
		this.fileList = fileList;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		final List<FileInfo> files = SelectDialog.show(fileList, backend.getUnsynchronisedFiles());
		if( !Check.isEmpty(files) )
		{
			GlassProgressWorker<?> worker = new GlassProgressWorker<File>(
				CurrentLocale.get("action.sync.progress.unknown"), files.size(), true) //$NON-NLS-1$
			{
				@Override
				public File construct() throws Exception
				{
					for( FileInfo info : files )
					{
						setMessage(CurrentLocale.get("action.sync.progress.syncing", info.getName())); //$NON-NLS-1$
						backend.synchroniseFile(info, new InterruptAwareGlassPaneProgressMonitorCallback(this));
					}
					return null;
				}

				@Override
				public void exception()
				{
					Exception ex = getException();
					if( !(ex instanceof UserCancelledException) )
					{
						ex.printStackTrace();
						if( CurrentLocale.isRightToLeft() )
						{
							fileList.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
						}

						JOptionPane.showMessageDialog(fileList,
							CurrentLocale.get("action.sync.error.message"), CurrentLocale //$NON-NLS-1$
								.get("action.sync.error.title"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
					}
				}
			};
			worker.setComponent(fileList);
			worker.start();
		}
	}

	private static final class SelectDialog
	{
		private JDialog dialog;
		private List<FileInfo> results;

		private SelectDialog(Component parent, List<FileInfo> changes)
		{
			JLabel message = new JLabel("<html>" + CurrentLocale.get("action.sync.select.message", changes //$NON-NLS-1$ //$NON-NLS-2$
				.size()));

			final CheckboxList<FileInfo> list = new CheckboxList<FileInfo>()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public String elementToString(FileInfo element)
				{
					return element.getFullPath();
				}
			};
			list.setElements(changes, true);
			list.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));

			JButton syncNow = new JButton(CurrentLocale.get("action.sync.select.dosync")); //$NON-NLS-1$
			JButton cancel = new JButton(CurrentLocale.get("action.sync.select.docancel")); //$NON-NLS-1$

			JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
			buttons.add(syncNow);
			buttons.add(cancel);

			JPanel panel = new JPanel(new BorderLayout());
			panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
			panel.add(message, BorderLayout.NORTH);
			panel.add(new JScrollPane(list), BorderLayout.CENTER);
			panel.add(buttons, BorderLayout.SOUTH);

			dialog = ComponentHelper.createJDialog(parent);

			if( CurrentLocale.isRightToLeft() )
			{
				dialog.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			}

			dialog.setTitle(CurrentLocale.get("action.sync.select.title")); //$NON-NLS-1$
			dialog.setContentPane(panel);
			dialog.setSize(350, 350);
			dialog.setModal(true);

			ComponentHelper.centreOnScreen(dialog);

			syncNow.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					results = list.getSelectedElements();
					dialog.dispose();
				}
			});

			cancel.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					dialog.dispose();
				}
			});
		}

		public static List<FileInfo> show(Component parent, List<FileInfo> changes)
		{
			SelectDialog instance = new SelectDialog(parent, changes);

			instance.dialog.setVisible(true);
			return instance.results;
		}
	}
}