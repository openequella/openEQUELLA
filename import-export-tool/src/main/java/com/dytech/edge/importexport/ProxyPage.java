package com.dytech.edge.importexport;

import com.dytech.common.net.Proxy;
import com.dytech.gui.TableLayout;
import com.tle.common.Check;
import java.awt.Dialog;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class ProxyPage extends WizardPage {
  protected JTextField host;
  protected JTextField port;
  protected JTextField username;
  protected JPasswordField password;

  public ProxyPage(SharedData data) {
    super(data);

    JLabel heading = new JLabel("<html><h2>Proxy Settings");
    JLabel help = new JLabel("<html><b>Please enter the proxy server details (if any):");
    JLabel hostLabel = new JLabel("Host");
    JLabel portLabel = new JLabel("Port");
    JLabel usernameLabel = new JLabel("Username");
    JLabel passwordLabel = new JLabel("Password");

    host = new JTextField();
    port = new JTextField();
    username = new JTextField();
    password = new JPasswordField();

    final int width =
        Math.max(usernameLabel.getPreferredSize().width, passwordLabel.getPreferredSize().width);
    final int height1 = heading.getPreferredSize().height;
    final int height2 = help.getPreferredSize().height;
    final int height3 = host.getPreferredSize().height;

    final int[] rows =
        new int[] {height1, height2, height3, height3, height3, height3, TableLayout.FILL};
    final int[] cols = new int[] {width, TableLayout.FILL};

    setLayout(new TableLayout(rows, cols, 5, 5));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    add(heading, new Rectangle(0, 0, 2, 1));
    add(help, new Rectangle(0, 1, 2, 1));

    add(hostLabel, new Rectangle(0, 2, 1, 1));
    add(host, new Rectangle(1, 2, 1, 1));

    add(portLabel, new Rectangle(0, 3, 1, 1));
    add(port, new Rectangle(1, 3, 1, 1));

    add(usernameLabel, new Rectangle(0, 4, 1, 1));
    add(username, new Rectangle(1, 4, 1, 1));

    add(passwordLabel, new Rectangle(0, 5, 1, 1));
    add(password, new Rectangle(1, 5, 1, 1));
  }

  @Override
  public void onShow() {
    // Nothing to do
  }

  @Override
  public boolean onBack() {
    return true;
  }

  @Override
  public boolean onNext() {
    String hostText = host.getText();
    if (Check.isEmpty(hostText)) {
      Proxy.disableProxy();
      return true;
    }

    try {
      int portInt = 80;
      String portText = port.getText();
      if (!Check.isEmpty(portText)) {
        portInt = Integer.parseInt(portText);
      }

      Proxy.setProxy(hostText, portInt, username.getText(), new String(password.getPassword()));
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      ErrorDialog d = new ErrorDialog((Dialog) null, "Error setting proxy settings", e);
      d.setVisible(true);
      return false;
    }
  }
}
