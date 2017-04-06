package com.tle.configmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

// Author: Andrew Gibb

@SuppressWarnings("nls")
public class ConfigLauncher
{
	private static ConfigLauncherGUI gui;
	private String source;
	private String destination;

	public ConfigLauncher()
	{
		// Set System Look and Feel
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch( Exception e )
		{
			JOptionPane.showMessageDialog(new JFrame(), "Could not set System UI Look & Feel:\n" + e.getMessage(),
				"Setup Error", JOptionPane.INFORMATION_MESSAGE);
		}

		// Load Default Location Properties
		Properties defaultProps = new Properties();

		try
		{
			defaultProps.load(new FileInputStream("./conf_loc.properties"));
			source = defaultProps.getProperty("tle.config.file_src");
			destination = defaultProps.getProperty("tle.config.file_dest");

			// Check source exists
			if( !exists(source) )
			{
				throw new IOException("Cannot load source path.\nPlease check conf_loc.properties file");
			}
		}
		catch( IOException e )
		{
			JOptionPane.showMessageDialog(new JFrame(), "Error loading default properties:\n" + e.getMessage(),
				"Configuration Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		// Create and display GUI
		gui = new ConfigLauncherGUI(source, destination);
		gui.setVisible(true);
	}

	private boolean exists(String path)
	{
		File f = new File(path);
		return f.exists();
	}
}
