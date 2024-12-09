package com.dytech.edge.importexport;

import com.dytech.gui.flatter.FlatterLookAndFeel;
import com.tle.common.util.BlindSSLSocketFactory;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.axis.AxisProperties;

public abstract class BaseUtility {
  protected final SharedData data;

  @SuppressWarnings("nls")
  protected BaseUtility() {
    try {
      UIManager.setLookAndFeel(new FlatterLookAndFeel());
      System.setProperty(
          "org.apache.commons.logging.LogFactory",
          "org.apache.commons.logging.impl.SLF4JLogFactory");
      BlindSSLSocketFactory.register();
      AxisProperties.setProperty(
          "axis.socketSecureFactory", "org.apache.axis.components.net.SunFakeTrustSocketFactory");
    } catch (UnsupportedLookAndFeelException e) {
      throw new RuntimeException(e);
    }
    data = new SharedData();
    createGUI();
  }

  protected abstract void createGUI();
}
