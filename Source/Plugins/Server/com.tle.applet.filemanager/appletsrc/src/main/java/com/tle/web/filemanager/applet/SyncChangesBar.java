package com.tle.web.filemanager.applet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.tle.common.i18n.CurrentLocale;
import com.tle.web.filemanager.applet.actions.SynchroniseChangesAction;
import com.tle.web.filemanager.applet.backend.Backend;
import com.tle.web.filemanager.common.FileInfo;

public class SyncChangesBar extends JPanel
{
	private static final long serialVersionUID = 1L;
	private JLabel label;
	private final Backend backend;

	public SyncChangesBar(FileListPanel fileList, Backend backend)
	{
		this.backend = backend;

		label = new JLabel(" "); //$NON-NLS-1$
		label.setFont(label.getFont().deriveFont(Font.BOLD));

		JButton button = new JButton(new SynchroniseChangesAction(backend, fileList));
		button.setOpaque(false);

		setOpaque(true);
		setBackground(Color.RED);
		label.setForeground(Color.WHITE);

		setLayout(new BorderLayout());
		add(label, BorderLayout.CENTER);
		add(button, BorderLayout.EAST);

		refresh();
	}

	public void refresh()
	{
		List<FileInfo> changes = backend.getUnsynchronisedFiles();
		label.setText(CurrentLocale.get("sync.notice", changes.size())); //$NON-NLS-1$

		boolean showBar = !changes.isEmpty();
		setVisible(showBar);
		setBorder(showBar ? BorderFactory.createLineBorder(Color.RED, 3) : null);
	}

	@Override
	public Dimension getPreferredSize()
	{
		Dimension dims = super.getPreferredSize();
		if( !isVisible() )
		{
			dims.height = 0;
		}
		return dims;
	}
}
