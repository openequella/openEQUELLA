package com.dytech.edge.importexport.exportutil;

import com.dytech.edge.importexport.BaseUtility;
import com.dytech.edge.importexport.FinishedPage;
import com.dytech.edge.importexport.ProxyPage;
import com.dytech.edge.importexport.ServerPage;
import com.dytech.edge.importexport.WizardFrame;
import com.dytech.edge.importexport.WizardPage;
import java.util.ArrayList;
import java.util.List;

public class ExportUtility extends BaseUtility {
  protected WizardFrame frame;

  /** Displays the wizard with the beginning panel. */
  public void startWizard() {
    frame.setVisible(true);
  }

  /**
   * Creates the GUI for the wizard. This should only be called once, and that should be during
   * construction.
   */
  @Override
  protected void createGUI() {
    List<WizardPage> pages = new ArrayList<WizardPage>();
    pages.add(new ProxyPage(data));
    pages.add(new ServerPage(data));
    pages.add(new ItemChoicePage(data));
    pages.add(new SaveAndXsltPage(data));
    pages.add(new DownloadPage(data));
    pages.add(new FinishedPage(data));

    frame = new WizardFrame(pages, true);
  }

  public static void main(String[] args) throws Exception {
    ExportUtility i = new ExportUtility();
    i.startWizard();
  }
}
