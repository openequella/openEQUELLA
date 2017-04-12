package com.tle.web.filemanager.applet.actions;

import java.awt.ComponentOrientation;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import com.dytech.gui.ComponentHelper;
import com.dytech.gui.TableLayout;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.filemanager.applet.FileListPanel;
import com.tle.web.filemanager.common.FileInfo;

public abstract class AbstractNamingAction extends TLEAction
{
	private final FileListPanel fileList;

	protected final String keyPrefix;

	public AbstractNamingAction(FileListPanel fileList, String keyPrefix, String iconPath)
	{
		super(CurrentLocale.get(keyPrefix + "name")); //$NON-NLS-1$

		setShortDescription(CurrentLocale.get(keyPrefix + "desc")); //$NON-NLS-1$
		setIcon(iconPath);

		this.keyPrefix = keyPrefix;
		this.fileList = fileList;
	}

	public FileListPanel getFileList()
	{
		return fileList;
	}

	protected abstract String getOriginalFileName(FileInfo selectedFile);

	protected abstract void doAction(FileInfo selectedFile, String newName);

	@Override
	public void actionPerformed(ActionEvent e)
	{
		final FileInfo selectedFile = fileList.getSelectedFile();
		final String originalFilename = getOriginalFileName(selectedFile);
		final String lOriginalFilename = originalFilename == null ? null : originalFilename.trim().toLowerCase();

		JLabel label = new JLabel(CurrentLocale.get(keyPrefix + "dialog.instruction", //$NON-NLS-1$
			originalFilename));
		final JTextField textField = new JTextField();
		JButton okButton = new JButton(CurrentLocale.get(keyPrefix + "dialog.okbutton")); //$NON-NLS-1$
		JButton cancelButton = new JButton(CurrentLocale.get(keyPrefix + "dialog.cancelbutton")); //$NON-NLS-1$

		final int height1 = label.getPreferredSize().height;
		final int height2 = textField.getPreferredSize().height;
		final int height3 = okButton.getPreferredSize().height;
		final int width1 = 150;
		final int width2 = Math.max(okButton.getPreferredSize().width, cancelButton.getPreferredSize().width);

		final int[] rows = {height1, height2, height3,};
		final int[] cols = {width1, width2, width2,};

		JPanel all = new JPanel(new TableLayout(rows, cols));
		all.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		all.add(label, new Rectangle(0, 0, 3, 1));
		all.add(textField, new Rectangle(0, 1, 3, 1));
		all.add(okButton, new Rectangle(1, 2, 1, 1));
		all.add(cancelButton, new Rectangle(2, 2, 1, 1));

		final JDialog dialog = ComponentHelper.createJDialog(fileList);

		dialog.setTitle(CurrentLocale.get(keyPrefix + "dialog.title")); //$NON-NLS-1$
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setContentPane(all);
		dialog.setResizable(false);
		dialog.setModal(true);
		dialog.pack();

		if( CurrentLocale.isRightToLeft() )
		{
			dialog.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		}

		ComponentHelper.centre(fileList, dialog);

		textField.requestFocusInWindow();
		if( originalFilename != null )
		{
			textField.setText(originalFilename);
			textField.setSelectionStart(0);
			textField.setSelectionEnd(originalFilename.length());
		}

		final Set<String> filenames = new HashSet<String>(Lists.transform(fileList.getCurrentDirectoryFiles(),
			new Function<FileInfo, String>()
			{
				@Override
				public String apply(FileInfo info)
				{
					return info.getName().trim().toLowerCase();
				}
			}));

		if( lOriginalFilename != null )
		{
			filenames.remove(lOriginalFilename);
		}

		final ActionListener okCallback = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				final String newName = textField.getText().trim();
				if( newName.length() == 0 )
				{

					JOptionPane.showMessageDialog(dialog, CurrentLocale.get(keyPrefix + "dialog.empty"), CurrentLocale //$NON-NLS-1$
						.get(keyPrefix + "dialog.popuptitle"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
					return;
				}

				final String lNewName = newName.toLowerCase();
				if( filenames.contains(lNewName) )
				{
					JOptionPane.showMessageDialog(dialog,
						CurrentLocale.get(keyPrefix + "dialog.duplicate"), CurrentLocale //$NON-NLS-1$
							.get(keyPrefix + "dialog.popuptitle"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
					return;
				}

				doAction(selectedFile, newName);

				dialog.dispose();
			}
		};

		final ActionListener cancelCallback = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				dialog.dispose();
			}
		};

		okButton.addActionListener(okCallback);
		cancelButton.addActionListener(cancelCallback);
		textField.addActionListener(okCallback);
		textField.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if( e.getKeyCode() == KeyEvent.VK_ESCAPE )
				{
					cancelCallback.actionPerformed(null);
				}
			}
		});

		dialog.setVisible(true);
	}
}
