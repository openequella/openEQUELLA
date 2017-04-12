package com.tle.admin.hierarchy;

import java.awt.Component;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import com.dytech.gui.ComponentHelper;

public final class ProgressDialog extends JPanel
{
	private final JProgressBar progress;
	private final JDialog dialog;

	@SuppressWarnings("nls")
	private ProgressDialog(JDialog dialog)
	{
		this.dialog = dialog;

		progress = new JProgressBar();
		progress.setIndeterminate(true);

		setLayout(new MigLayout("wrap 1, fill", "[200px,grow,fill]"));
		add(progress);
	}

	public void setProgressSafely(final int done, final int total)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				progress.setIndeterminate(false);
				progress.setValue(done);
				progress.setMaximum(total);
			}
		});
	}

	public void closeDialog()
	{
		dialog.setVisible(false);
	}

	public static ProgressDialog showProgress(Component parent, String message)
	{
		JDialog d = ComponentHelper.createJDialog(parent);
		ProgressDialog p = new ProgressDialog(d);

		d.getRootPane().setWindowDecorationStyle(JRootPane.INFORMATION_DIALOG);
		d.setResizable(false);
		d.setContentPane(p);
		d.setTitle(message);
		d.pack();

		d.setLocationRelativeTo(parent);
		d.setVisible(true);

		return p;
	}
}
