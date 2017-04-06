/*
 * Created on Oct 26, 2004
 */
package com.dytech.edge.licence.client;

import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 * @author Nicholas Read
 */
public class Main
{
	public static void main(String[] args) throws Exception
	{
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		OptionsWindow panel = new OptionsWindow();

		JFrame frame = new JFrame();
		frame.getContentPane().add(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 500);
		frame.setTitle("TLE License Generator"); //$NON-NLS-1$
		frame.setVisible(true);
	}
}
