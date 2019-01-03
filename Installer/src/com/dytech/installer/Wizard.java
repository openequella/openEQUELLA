/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.dytech.installer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.dytech.devlib.PropBagEx;
import com.dytech.gui.ComponentHelper;
import com.dytech.gui.JImage;
import com.dytech.installer.helper.ResultHolder;

@SuppressWarnings("nls")
public class Wizard
{
	private static final String COMMANDS_FILE = "commands.xml";
	private static final String COMMANDS_DIR = ".tle";

	private PropBagEx installBag;
	private PropBagEx outputBag;
	private PropBagEx defaultBag;
	private PropBagEx commandBag;

	private List<WizardPage> pages;

	private JFrame frame;
	private JPanel pageArea;
	private JPanel navArea;

	private String imagePath;
	private String productName;
	private String productVersion;
	private String productCompany;

	private int currentPage;
	private ResultHolder result;

	/**
	 * Constructor for <code>Installer</code>. The given properties bag
	 * represents the XML installer definition.
	 */
	public Wizard(PropBagEx bag)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			System.out.println("System Look And Feel: " + UIManager.getSystemLookAndFeelClassName());
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}

		installBag = bag;
		defaultBag = loadDefaults();
		currentPage = 0;
		pages = new ArrayList<WizardPage>();

		result = new ResultHolder();

		outputBag = new PropBagEx().newSubtree("commands");
	}

	public Wizard(PropBagEx bag, PropBagEx commandBag)
	{
		this(bag);
		this.commandBag = commandBag;
	}

	/**
	 * Start the installation process. Extract all the information from the
	 * proporties bag, create the pages, create the window, and go to the first
	 * page.
	 */
	public void start() throws InstallerException
	{
		extractInformation();
		populateOutput();
		getPages();
		createWindow();
		gotoPage(0);

		frame.setSize(670, 500);
		ComponentHelper.centreOnScreen(frame);
		frame.setVisible(true);
	}

	public PropBagEx getOutput()
	{
		return (PropBagEx) result.getResult();
	}

	public PropBagEx getOutputNow()
	{
		return outputBag;
	}

	public PropBagEx getDefaults()
	{
		return defaultBag;
	}

	/**
	 * Displays the page of the given page number. All the input values are
	 * stored in the output properties bag.
	 * 
	 * @param pageNumber The page number.
	 */
	public void gotoPage(int pageNumber) throws InstallerException
	{
		if( pageNumber < 0 || pageNumber >= pages.size() )
		{
			throw new InstallerException("Page Error: Page " + pageNumber + " is not defined.");
		}

		WizardPage page = pages.get(pageNumber);

		JPanel pagePanel = new JPanel(new GridLayout(1, 1));
		page.createPage(pagePanel);
		replacePage(pageArea, pagePanel);

		JPanel navPanel = new JPanel(new GridLayout(1, 1));
		page.createNavigators(navPanel);
		replacePage(navArea, navPanel);

		currentPage = pageNumber;

		navArea.setPreferredSize(navPanel.getPreferredSize());
	}

	/**
	 * Move to page relative to the current one. For example,
	 * <code>gotoRelativePage(2)</code> is equivalent to
	 * <code>gotoPage(getCurrentPage() + 2)</code>.
	 */
	public void gotoRelativePage(int i) throws InstallerException
	{
		gotoPage(getCurrentPageNumber() + i);
	}

	public void replacePage(JPanel parent, JPanel child)
	{
		parent.removeAll();
		parent.add(child);
		parent.revalidate();
		parent.repaint();
	}

	/**
	 * Get the current page number.
	 * 
	 * @return The page number.
	 */
	public int getCurrentPageNumber()
	{
		return currentPage;
	}

	/**
	 * Declare that the first phase of the install process is finshed. This will
	 * Move the installer on to phase two; the actual installation.
	 */
	public void finished()
	{
		close();
		result.setResult(outputBag);
	}

	/**
	 * Quit the installation process.
	 */
	public void quit()
	{
		close();
		System.exit(0);
	}

	private void close()
	{
		Iterator<WizardPage> i = pages.iterator();
		while( i.hasNext() )
		{
			WizardPage p = i.next();
			p.saveControls(outputBag);
		}

		frame.dispose();
		saveDefaults(outputBag);
	}

	/**
	 * Extracts all of the "global" type information from the proporties bag.
	 * This includes information such as product names, company names, images,
	 * background colours, and more.
	 */
	private void extractInformation()
	{
		productName = installBag.getNode("product/name");
		productVersion = installBag.getNode("product/version");
		productCompany = installBag.getNode("product/company");

		if( productName.length() == 0 || productName == null )
		{
			productName = "<No Product Name>";
		}

		if( productVersion.length() == 0 || productVersion == null )
		{
			productVersion = "<No Product Version>";
		}

		if( productCompany.length() == 0 || productCompany == null )
		{
			productCompany = "<No Product Company>";
		}

		imagePath = installBag.getNode("image/@uri");
	}

	private void populateOutput()
	{
		outputBag.setNode("installer/product/name", productName);
		outputBag.setNode("installer/product/version", productVersion);
		outputBag.setNode("installer/product/company", productCompany);
		outputBag.setNode("installer/local", System.getProperty("user.dir"));
		outputBag.setNode("installer/platform", determinePlatform());
	}

	/**
	 * Create all the pages and navigation buttons.
	 */
	private void getPages() throws InstallerException
	{
		Iterator<PropBagEx> iter = installBag.iterator("pages/page");
		while( iter.hasNext() )
		{
			PropBagEx pageBag = iter.next();
			pages.add(new WizardPage(pageBag, this));
		}
	}

	public JFrame getFrame()
	{
		return frame;
	}

	/**
	 * Create the installation window which the pages appear in.
	 */
	private void createWindow()
	{
		JImage sideImage = new JImage(getClass().getResource(imagePath));
		sideImage.setVerticalAlignment(SwingConstants.TOP);
		sideImage.setAlignmentY(Component.TOP_ALIGNMENT);

		pageArea = new JPanel();
		pageArea.setLayout(new GridLayout(1, 1));

		JSeparator bar = new JSeparator();

		navArea = new JPanel();
		navArea.setLayout(new GridLayout(1, 1));

		JPanel bottom = new JPanel(new BorderLayout(5, 5));
		bottom.add(bar, BorderLayout.NORTH);
		bottom.add(navArea, BorderLayout.CENTER);

		JPanel all = new JPanel(new BorderLayout(5, 5));
		all.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		all.add(sideImage, BorderLayout.WEST);
		all.add(pageArea, BorderLayout.CENTER);
		all.add(bottom, BorderLayout.SOUTH);

		frame = new JFrame();
		frame.getContentPane().add(all);
		frame.setTitle(productName);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private String determinePlatform()
	{
		String name = System.getProperty("os.name").toLowerCase();
		boolean is64Bit = is64Bit(name);

		if( name.startsWith("windows") )
		{
			return is64Bit ? "win64" : "win32";

		}
		else if( name.startsWith("linux") )
		{
			return is64Bit ? "linux64" : "linux";
		}
		else if( name.startsWith("solaris") || name.startsWith("sunos") )
		{
			if( System.getProperty("os.arch").startsWith("sparc") )
			{
				return "solaris-sparc";
			}
			return is64Bit ? "solaris64" : "solaris-x86";
		}
		else if( name.startsWith("mac os x") )
		{
			return "mac";
		}
		else
		{
			return "unsupported";
		}
	}

	private boolean is64Bit(String name)
	{
		boolean is64bit = false;
		if( name.contains("windows") )
		{
			is64bit = (System.getenv("ProgramFiles(x86)") != null);
		}
		else
		{
			is64bit = (System.getProperty("os.arch").indexOf("64") != -1);
		}
		return is64bit;
	}

	private void saveDefaults(PropBagEx xml)
	{
		File file = getInstallFile(true);

		try( OutputStream out = new FileOutputStream(file) )
		{
			out.write(xml.toString().getBytes());
		}
		catch( Exception e )
		{
			// File not found
		}
	}

	private PropBagEx loadDefaults()
	{
		PropBagEx scriptXml = null;

		File file = getInstallFile(false);
		if( file.exists() )
		{
			int confirm = JOptionPane.showConfirmDialog(getFrame(), "Values entered during a"
				+ " previous installation have been found.\nDo you wish you use these values?", "Load Previous Values",
				JOptionPane.YES_NO_OPTION);

			if( confirm == JOptionPane.YES_OPTION )
			{
				scriptXml = new PropBagEx(file);
			}
		}
		return scriptXml;
	}

	private File getInstallFile(boolean createDir)
	{
		File file = new File(System.getProperty("user.dir"), COMMANDS_DIR);
		if( !file.exists() && createDir )
		{
			file.mkdir();
		}
		return new File(file, COMMANDS_FILE);
	}

	public PropBagEx getCommandBag()
	{
		return commandBag;
	}

	public void setCommandBag(PropBagEx commandBag)
	{
		this.commandBag = commandBag;
	}
}