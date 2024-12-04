package com.dytech.edge.importexport;

import com.dytech.common.text.NumberStringComparator;
import com.dytech.edge.importexport.icons.Icons;
import com.dytech.edge.importexport.types.ItemDef;
import com.dytech.gui.TableLayout;
import com.tle.common.Check;
import java.awt.Rectangle;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

@SuppressWarnings("nls")
public class ServerPage extends WizardPage {
  protected JTextField server;
  protected JTextField username;
  protected JPasswordField password;

  protected JLabel errorLabel;
  protected ImageIcon errorIcon;

  public ServerPage(SharedData data) {
    super(data);

    errorLabel = new JLabel();
    errorIcon = Icons.getErrorIcon();

    JLabel heading = new JLabel("<html><h2>Server Connection");
    JLabel help = new JLabel("<html><b>Please enter the server details:");
    JLabel serverLabel = new JLabel("Server URL");
    JLabel usernameLabel = new JLabel("Username");
    JLabel passwordLabel = new JLabel("Password");

    server = new JTextField("http://");
    username = new JTextField();
    password = new JPasswordField();

    final int width = serverLabel.getPreferredSize().width;
    final int height1 = heading.getPreferredSize().height;
    final int height2 = help.getPreferredSize().height;
    final int height3 = server.getPreferredSize().height;

    final int[] rows =
        new int[] {height1, height2, height3, height3, height3, height3, height3, TableLayout.FILL};
    final int[] cols = new int[] {width, TableLayout.FILL};

    setLayout(new TableLayout(rows, cols, 5, 5));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    add(heading, new Rectangle(0, 0, 2, 1));
    add(help, new Rectangle(0, 1, 2, 1));

    add(serverLabel, new Rectangle(0, 2, 1, 1));
    add(server, new Rectangle(1, 2, 1, 1));

    add(usernameLabel, new Rectangle(0, 3, 1, 1));
    add(username, new Rectangle(1, 3, 1, 1));

    add(passwordLabel, new Rectangle(0, 4, 1, 1));
    add(password, new Rectangle(1, 4, 1, 1));

    add(errorLabel, new Rectangle(0, 6, 2, 2));
  }

  protected void displayError(String error) {
    if (error == null) {
      errorLabel.setIcon(null);
      errorLabel.setText("");
    } else {
      errorLabel.setIcon(errorIcon);
      errorLabel.setText("<html>" + error);
    }
  }

  @Override
  public boolean onNext() {
    String error = null;
    if (server.getText().length() == 0) {
      error = "You must enter the host or IP address of the server.";
    } else if (username.getText().length() == 0) {
      error = "You must enter your username to connect to the server.";
    } else if (password.getPassword().length == 0) {
      error = "You must enter the password for the given username.";
    }

    if (error != null) {
      displayError(error);
      return false;
    }

    boolean success = setupSoapSession(server.getText().trim());
    if (success) {
      try {
        data.setItemDefs(data.getSoapSession().getContributableCollections());
        Collections.sort(data.getItemDefs(), new NumberStringComparator<ItemDef>());
        return true;
      } catch (Exception ex) {
        displayError("Error retrieving item definitions.  Try again.");
      }
    }
    return false;
  }

  private boolean setupSoapSession(String url) {
    URL institutionUrl;
    try {
      institutionUrl = new URL((url.endsWith("/") ? url : url + '/'));
    } catch (MalformedURLException e) {
      displayError("Server URL must be like 'http://hostname[:port][/context]'");
      return false;
    }

    // Now try and setup a soap session
    try {
      data.setSoapSession(
          new SoapSession(institutionUrl, username.getText(), new String(password.getPassword())));

      data.setInstitutionUrl(institutionUrl);

      return true;
    } catch (Exception ex) {
      ex.printStackTrace();
      if (ex.getCause() != null
          && ex.getCause().getMessage().equals("Username or password incorrect")) {
        displayError("Username or Password is incorrect. Check your settings.");
      } else {
        String msg = ex.getMessage();
        if (Check.isEmpty(msg)) {
          displayError("Could not connect to server.  Possibly an incorrect username/password?");
        } else {
          displayError("Could not connect to server: " + ex.getMessage());
        }

        // ErrorDialog e = new ErrorDialog((Dialog) null,
        // "Error connecting to server", ex);
        // e.setVisible(true);
      }
    }

    return false;
  }

  @Override
  public boolean onBack() {
    return true;
  }

  @Override
  public void onShow() {
    displayError(null);
  }
}
