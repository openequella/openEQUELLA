package com.dytech.edge.importexport.icons;

import com.dytech.gui.JImage;
import javax.swing.ImageIcon;

@SuppressWarnings("nls")
public class Icons {
  public static ImageIcon getErrorIcon() {
    return new ImageIcon(Icons.class.getResource("error.gif"));
  }

  public static JImage getHeader(boolean export) {
    return new JImage(Icons.class.getResource((export ? "exportheader.gif" : "importheader.gif")));
  }

  public static JImage getFooter() {
    return new JImage(Icons.class.getResource("footer.gif"));
  }

  public static JImage getWorking() {
    return new JImage(Icons.class.getResource("working.gif"));
  }
}
