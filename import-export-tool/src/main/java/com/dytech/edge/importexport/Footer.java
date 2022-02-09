package com.dytech.edge.importexport;

import com.dytech.edge.importexport.icons.Icons;
import com.dytech.gui.JImage;
import java.awt.Dimension;
import javax.swing.JPanel;

public class Footer extends JPanel {
  protected JImage footer;
  protected JImage working;

  public Footer() {
    setLayout(null);

    footer = Icons.getFooter();
    working = Icons.getWorking();

    final int footerWidth = 350;
    final int workingWidth = 43;
    footer.setBounds(0, 0, footerWidth, 21);
    // Offset working icon from right-hand side of footer - then add a bit of padding
    working.setBounds(footerWidth - workingWidth - 8, 9, 43, 8);

    setWorking(false);
  }

  public void setWorking(boolean b) {
    removeAll();
    if (b) {
      add(working);
    }
    add(footer);
    updateUI();
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(450, 21);
  }
}
