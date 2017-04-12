package com.dytech.edge.importexport.exportutil;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.importexport.SharedData;
import com.dytech.edge.importexport.WizardPage;
import com.dytech.edge.importexport.icons.Icons;
import com.dytech.gui.TableLayout;

/**
 * @author Nicholas Read
 * @dytech.jira see Jira Issue TLE-797 :
 *              http://apps.dytech.com.au/jira/browse/TLE-797
 */

@SuppressWarnings("nls")
public class SaveAndXsltPage extends WizardPage implements ActionListener
{
	private JLabel errorLabel;
	private ImageIcon errorIcon;

	private JRadioButton asNativeXML;
	private JRadioButton asIMSPackage;
	private JRadioButton usingXSLT;
	private JTextField folderField;
	private JTextField xsltField;
	private JButton folderBrowse;
	private JButton xsltBrowse;

	public SaveAndXsltPage(SharedData data)
	{
		super(data);
		setup();
	}

	private void setup()
	{
		JLabel heading = new JLabel("<html><h2>Save Settings");
		JLabel help1 = new JLabel("<html><b>Save items to folder:");
		JLabel help2 = new JLabel("<html><b>Save items:");

		errorLabel = new JLabel();
		errorIcon = Icons.getErrorIcon();

		folderField = new JTextField();
		xsltField = new JTextField();

		folderField.setEditable(false);
		xsltField.setEditable(false);

		folderBrowse = new JButton("Browse");
		xsltBrowse = new JButton("Upload");

		xsltBrowse.setEnabled(false);

		asNativeXML = new JRadioButton("XML and attachments", true);
		asIMSPackage = new JRadioButton("IMS packages only");
		usingXSLT = new JRadioButton("Other format using an XSLT");

		ButtonGroup saveAsGroup = new ButtonGroup();
		saveAsGroup.add(asNativeXML);
		saveAsGroup.add(asIMSPackage);
		saveAsGroup.add(usingXSLT);

		folderBrowse.addActionListener(this);
		xsltBrowse.addActionListener(this);
		asNativeXML.addActionListener(this);
		asIMSPackage.addActionListener(this);
		usingXSLT.addActionListener(this);

		final int height1 = heading.getPreferredSize().height;
		final int height2 = help1.getPreferredSize().height;
		final int height3 = folderField.getPreferredSize().height;
		final int height4 = 5;
		final int width1 = 10;
		final int width2 = folderBrowse.getPreferredSize().width;

		final int[] rows = {height1, height2, height3, height4, height2, height2, height2, height2, height3, height3,
				TableLayout.FILL};
		final int[] cols = {width1, width1, TableLayout.FILL, width2};

		setLayout(new TableLayout(rows, cols, 5, 5));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		add(heading, new Rectangle(0, 0, 4, 1));
		add(help1, new Rectangle(0, 1, 4, 1));
		add(folderField, new Rectangle(2, 2, 1, 1));
		add(folderBrowse, new Rectangle(3, 2, 1, 1));

		add(help2, new Rectangle(0, 4, 4, 1));
		add(asNativeXML, new Rectangle(1, 5, 3, 1));
		add(asIMSPackage, new Rectangle(1, 6, 3, 1));
		add(usingXSLT, new Rectangle(1, 7, 2, 1));

		add(xsltField, new Rectangle(2, 8, 1, 1));
		add(xsltBrowse, new Rectangle(3, 8, 1, 1));

		add(errorLabel, new Rectangle(0, 9, 4, 1));
	}

	protected void displayError(String error)
	{
		if( error == null )
		{
			errorLabel.setIcon(null);
			errorLabel.setText("");
		}
		else
		{
			errorLabel.setIcon(errorIcon);
			errorLabel.setText(error);
		}
	}

	@Override
	public boolean onNext()
	{
		String error = null;
		if( data.getSaveFolder() == null )
		{
			error = "You must select a folder to save the items to.";
		}
		else if( !data.getSaveFolder().exists() )
		{
			error = "The destination folder does not exist.";
		}
		else if( usingXSLT.isSelected() && data.getXslt() == null )
		{
			error = "You must select a valid XSLT, or save as a different type.";
		}

		if( error != null )
		{
			displayError(error);
			return false;
		}
		else
		{
			if( asIMSPackage.isSelected() )
			{
				data.setSaveAs(SharedData.SAVE_AS_IMS);
			}
			else if( usingXSLT.isSelected() )
			{
				data.setSaveAs(SharedData.SAVE_APPLYING_XSLT);
			}
			else
			{
				data.setSaveAs(SharedData.SAVE_IN_NATIVE);
			}

			return true;
		}
	}

	@Override
	public boolean onBack()
	{
		return true;
	}

	@Override
	public void onShow()
	{
		displayError(null);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == asNativeXML || e.getSource() == asIMSPackage )
		{
			xsltBrowse.setEnabled(false);
			xsltField.setEnabled(false);
		}
		else if( e.getSource() == usingXSLT )
		{
			xsltBrowse.setEnabled(true);
			xsltField.setEnabled(true);
		}
		else if( e.getSource() == folderBrowse )
		{
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			String existingDir = folderField.getText().trim();
			if( existingDir.length() > 0 )
			{
				chooser.setCurrentDirectory(new File(existingDir));
			}

			final int result = chooser.showSaveDialog(this);
			if( result == JFileChooser.APPROVE_OPTION )
			{
				final File saveFolder = chooser.getSelectedFile();
				data.setSaveFolder(saveFolder);
				folderField.setText(saveFolder.toString());
			}
		}
		else if( e.getSource() == xsltBrowse )
		{
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new XsltFileFilter());

			final int result = chooser.showOpenDialog(this);
			if( result == JFileChooser.APPROVE_OPTION )
			{
				File f = chooser.getSelectedFile();
				try
				{
					PropBagEx xml = new PropBagEx(f);
					data.setXslt(xml.toString());
					xsltField.setText(f.toString());
				}
				catch( Exception ex )
				{
					JOptionPane.showMessageDialog(this, "The select XSLT is not valid XML.", "Invalid XSLT",
						JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	protected class XsltFileFilter extends FileFilter
	{
		private static final String DESCRIPTION = "XSL Transformation Files";

		@Override
		public boolean accept(File f)
		{
			if( f.isDirectory() )
			{
				return true;
			}
			else
			{
				String name = f.getName().toLowerCase();
				return name.endsWith("xsl") || name.endsWith("xslt");
			}
		}

		@Override
		public String getDescription()
		{
			return DESCRIPTION;
		}
	}
}
