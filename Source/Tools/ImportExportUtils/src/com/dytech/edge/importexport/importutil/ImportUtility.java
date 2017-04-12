/*
 * Created on Nov 13, 2003
 */
package com.dytech.edge.importexport.importutil;

import java.util.ArrayList;
import java.util.List;

import com.dytech.edge.importexport.BaseUtility;
import com.dytech.edge.importexport.FinishedPage;
import com.dytech.edge.importexport.ProxyPage;
import com.dytech.edge.importexport.ServerPage;
import com.dytech.edge.importexport.WizardFrame;
import com.dytech.edge.importexport.WizardPage;

/**
 * @author Nicholas Read
 * @author Charles O'Farrell
 * @author George Francis
 */
@SuppressWarnings("nls")
public class ImportUtility extends BaseUtility
{
	private WizardFrame frame;

	/**
	 * Displays the wizard with the beginning panel.
	 */
	public void startWizard()
	{
		frame.setVisible(true);
	}

	/**
	 * Creates the GUI for the wizard. This should only be called once, and that
	 * should be during construction.
	 */
	@Override
	protected void createGUI()
	{
		List<WizardPage> pages = new ArrayList<WizardPage>();
		pages.add(new ProxyPage(data));
		pages.add(new ServerPage(data));
		pages.add(new ImportPage(data));
		pages.add(new ReviewPage(data));
		pages.add(new UploadPage(data));
		pages.add(new FinishedPage(data));

		frame = new WizardFrame(pages, false);
	}

	public static void main(String[] args)
	{
		try
		{
			ImportUtility i = new ImportUtility();
			i.startWizard();
		}
		catch( Exception ex )
		{
			System.out.println("unhandled exception");
			ex.printStackTrace();
		}
	}
}
