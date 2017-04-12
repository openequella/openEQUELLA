package com.tle.admin.boot;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.dytech.gui.ComponentHelper;
import com.dytech.gui.JImage;

@SuppressWarnings("nls")
public class LoadingDialog extends JFrame
{
	private static final long serialVersionUID = 1L;
	private static final int WINDOW_WIDTH = 349;
	private static final int WINDOW_HEIGHT = 105;

	public LoadingDialog(String windowTitle)
	{
		setupDialog(windowTitle);
	}

	private void setupDialog(String windowTitle)
	{
		JImage image = new JImage(LoadingDialog.class.getResource("/icons/splash.gif"));
		JImage anim = new JImage(LoadingDialog.class.getResource("/icons/loading_animation.gif")); //$NON-NLS-1$

		JPanel all = new JPanel(null);
		all.add(anim);
		all.add(image);

		image.setBounds(0, 0, 349, 105);
		anim.setBounds(258, 41, 24, 24);

		setIconImage(new ImageIcon(LoadingDialog.class.getResource("/icons/windowicon.gif")).getImage()); //$NON-NLS-1$

		setTitle(windowTitle);
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setUndecorated(true);
		getContentPane().add(all);

		ComponentHelper.centreOnScreen(this);
	}
}
