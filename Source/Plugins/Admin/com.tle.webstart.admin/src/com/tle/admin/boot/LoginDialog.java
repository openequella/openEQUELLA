package com.tle.admin.boot;

import com.dytech.gui.ComponentHelper;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class LoginDialog extends JDialog implements ActionListener, WindowListener, KeyListener {

  public static final int RESULT_CANCEL = 0;
  public static final int RESULT_OK = 1;

  private int result = RESULT_CANCEL;

  private final JLabel message;
  private final JTextField usernameTextField;
  private final JPasswordField passwordTextField;
  private final JButton okButton;
  private final JButton cancelButton;

  public LoginDialog() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

      message = new JLabel("Error message");
      message.setVisible(false);
      okButton = new JButton("OK");
      okButton.addActionListener(this);
      okButton.setEnabled(false);
      cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(this);
      usernameTextField = new JTextField();
      usernameTextField.addKeyListener(this);
      passwordTextField = new JPasswordField();
      passwordTextField.addKeyListener(this);

      JPanel all = new JPanel();

      all.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      all.setLayout(new GridLayout(6, 1, 0, 5));
      all.add(message);
      all.add(new JLabel("Username:"));
      all.add(usernameTextField);
      all.add(new JLabel("Password:"));
      all.add(passwordTextField);

      JPanel buttonPanel = new JPanel();
      // buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      buttonPanel.setLayout(new GridLayout(1, 2, 10, 10));
      buttonPanel.add(okButton);
      buttonPanel.add(cancelButton);
      all.add(buttonPanel);

      setModal(true);
      setTitle("Enter login credentials");
      getContentPane().add(all);
      getRootPane().setDefaultButton(okButton);

      setResizable(false);
      pack();
      ComponentHelper.ensureMinimumSize(this, 400, 0);
      ComponentHelper.centreOnScreen(this);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int getResult() {
    return result;
  }

  public String getUsername() {
    return usernameTextField.getText();
  }

  public void setUsername(String username) {
    usernameTextField.setText(username);
  }

  public String getPassword() {
    return new String(passwordTextField.getPassword());
  }

  public void setErrorMessage(String errorMessage) {
    message.setText(errorMessage);
    if (errorMessage != null) {
      message.setVisible(true);
    }
  }

  protected void updateOkButton() {
    boolean usernameEmpty = (usernameTextField.getText().trim().length() == 0);
    boolean passwordEmpty = (passwordTextField.getPassword().length == 0);

    okButton.setEnabled(!usernameEmpty && !passwordEmpty);
  }

  @Override
  public void keyReleased(KeyEvent e) {
    updateOkButton();
  }

  @Override
  public void keyPressed(KeyEvent e) {
    // We do not want to listen to this event
  }

  @Override
  public void keyTyped(KeyEvent e) {
    // We do not want to listen to this event
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == okButton) {
      result = RESULT_OK;
      setVisible(false);
    } else if (e.getSource() == cancelButton) {
      result = RESULT_CANCEL;
      setVisible(false);
    }
  }

  @Override
  public void windowOpened(WindowEvent e) {
    // We do not want to listen to this event
  }

  @Override
  public void windowClosing(WindowEvent e) {
    // We do not want to listen to this event
  }

  @Override
  public void windowClosed(WindowEvent e) {
    dispose();
  }

  @Override
  public void windowIconified(WindowEvent e) {
    // We do not want to listen to this event
  }

  @Override
  public void windowDeiconified(WindowEvent e) {
    // We do not want to listen to this event
  }

  @Override
  public void windowActivated(WindowEvent e) {
    // We do not want to listen to this event
  }

  @Override
  public void windowDeactivated(WindowEvent e) {
    // We do not want to listen to this event
  }
}
