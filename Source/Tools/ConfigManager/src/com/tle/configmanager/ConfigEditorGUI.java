package com.tle.configmanager;

import com.dytech.gui.ComponentHelper;
import com.thoughtworks.xstream.XStream;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

// Author: Andrew Gibb

@SuppressWarnings({"serial", "nls"})
public class ConfigEditorGUI extends JDialog implements ActionListener {
  public static final int RESULT_CANCEL = 0;
  public static final int RESULT_SAVE = 1;
  private int result = RESULT_CANCEL;

  // GUI Components
  JTabbedPane jtp;
  JComboBox<String> cmbDbTypes;
  JTextField txtPort, txtHost, txtUser, txtDb, txtAdminURL, txtProfName, txtHttp, txtHttps, txtAjp;
  JPasswordField txtPass;
  JButton save, cancel;
  JPathBrowser pbrFiles,
      pbrJava,
      pbrFree,
      pbrStop,
      pbrReport,
      pbrPlugins,
      pbrConPath,
      pbrImageMagick;
  JCheckBox chkDevInst;

  // Configuration Profile
  ConfigProfile prof;

  public ConfigEditorGUI() {
    // Build GUI
    setupGUI();
  }

  public ConfigEditorGUI(ConfigProfile prof) {
    // Build GUI then load Profile
    setupGUI();
    loadConfig(prof);
    txtProfName.setEnabled(false); // Cannot edit name of profile
  }

  public ConfigEditorGUI(ConfigProfile prof, String s) {
    // Build GUI then load Profile
    setupGUI();

    loadConfig(prof);
    txtProfName.setText(prof.getProfName() + "_copy");
  }

  private void setupGUI() {
    // Setup Window
    setTitle("TLE Internal Config Editor");

    this.setResizable(false);

    // Setup Layout
    Container contents = getContentPane();
    contents.setLayout(new MigLayout("wrap 2", "[][]"));

    // Build Tabs
    jtp = new JTabbedPane();
    jtp.addTab("Hibernate", buildDBTab());
    jtp.addTab("Mandatory", buildManTab());
    jtp.addTab("Miscellaneous", buildMiscTab());

    JLabel lblProfName = new JLabel("Profile Name: ");
    txtProfName = new JTextField(25);

    contents.add(lblProfName);
    contents.add(txtProfName);
    contents.add(jtp, "span");

    save = new JButton("Save");
    save.addActionListener(this);
    cancel = new JButton("Cancel");
    cancel.addActionListener(this);

    contents.add(save, "span, split, alignx right, tag ok");
    contents.add(cancel, "tag cancel");

    // Set a minimum width...leave the height to the pack...
    setMinimumSize(new Dimension(500, 0));
    pack();
    ComponentHelper.centreOnScreen(this);
  }

  // Hibernate Tab
  private JComponent buildDBTab() {
    // Labels
    String[] dbs = new String[] {"MS SQL", "PostgreSQL", "Oracle"};
    JLabel lblSeldb = new JLabel("Select Database Type: ");
    JLabel lblPort = new JLabel("Port: ");
    JLabel lblHost = new JLabel("Enter Host: ");
    JLabel lblDb = new JLabel("Enter Database: ");
    JLabel lblUser = new JLabel("Enter Username: ");
    JLabel lblPass = new JLabel("Enter Password: ");

    // Controls
    cmbDbTypes = new JComboBox<String>(dbs);
    cmbDbTypes.setSelectedIndex(-1);
    txtPort = new JTextField(25);
    txtHost = new JTextField(25);
    txtUser = new JTextField(25);
    txtDb = new JTextField(25);
    txtPass = new JPasswordField(25);

    // Lay out the panel. MIG FTMFW!!
    JPanel all = new JPanel(new MigLayout("wrap 2, fillx", "[align label][grow]"));

    // Add Components
    all.add(lblSeldb);
    all.add(cmbDbTypes);
    all.add(lblPort);
    all.add(txtPort);
    all.add(lblHost);
    all.add(txtHost);
    all.add(lblDb);
    all.add(txtDb);
    all.add(lblUser);
    all.add(txtUser);
    all.add(lblPass);
    all.add(txtPass);

    // Add ActionListeners
    cmbDbTypes.addActionListener(this);

    return all;
  }

  // Mandatory Tab
  private JComponent buildManTab() {
    // Labels
    JLabel lblTomcat = new JLabel("Tomcat Ports: ");
    JLabel lblFiles = new JLabel("Path to Filestore: ");
    JLabel lblJava = new JLabel("Path to Java Home: ");
    JLabel lblAdminURL = new JLabel("Admin URL: ");
    JLabel lblFreeText = new JLabel("Path to Freetext: ");
    JLabel lblStopWords = new JLabel("Path to StopWords: ");
    JLabel lblReporting = new JLabel("Path to Reporting: ");
    JLabel lblPlugins = new JLabel("Path to Plugins: ");

    // Controls
    txtHttp = new JTextField();
    txtHttps = new JTextField();
    txtAjp = new JTextField();
    pbrFiles = new JPathBrowser(JFileChooser.DIRECTORIES_ONLY);
    pbrJava = new JPathBrowser(JFileChooser.DIRECTORIES_ONLY);
    txtAdminURL = new JTextField("http://");
    pbrFree = new JPathBrowser(JFileChooser.DIRECTORIES_ONLY);
    pbrStop = new JPathBrowser(JFileChooser.FILES_AND_DIRECTORIES);
    pbrReport = new JPathBrowser(JFileChooser.DIRECTORIES_ONLY);
    pbrPlugins = new JPathBrowser(JFileChooser.FILES_AND_DIRECTORIES);

    // Layout
    JPanel all = new JPanel(new MigLayout("wrap 2, fillx", "[align label][grow, fill]"));

    all.add(lblTomcat);
    all.add(txtHttp);
    all.add(txtHttps, "skip 1");
    all.add(txtAjp, "skip 1");
    all.add(lblFiles);
    all.add(pbrFiles);
    all.add(lblJava);
    all.add(pbrJava);
    all.add(lblAdminURL);
    all.add(txtAdminURL);
    all.add(lblFreeText);
    all.add(pbrFree);
    all.add(lblStopWords);
    all.add(pbrStop);
    all.add(lblReporting);
    all.add(pbrReport);
    all.add(lblPlugins);
    all.add(pbrPlugins);

    return all;
  }

  // Miscellaneous Tab
  private JComponent buildMiscTab() {
    // Labels and Controls
    JLabel lblOpt = new JLabel("-- Optional --");
    JLabel lblConPath = new JLabel("Conversion Path: ");
    JLabel lblDevInst = new JLabel("Development Install: ");
    JLabel lblPlug = new JLabel("-- Plugins --");
    JLabel lblImageMagick = new JLabel("Image Magik Path: ");

    pbrImageMagick = new JPathBrowser(JFileChooser.DIRECTORIES_ONLY);
    pbrConPath = new JPathBrowser(JFileChooser.FILES_AND_DIRECTORIES);
    chkDevInst = new JCheckBox();
    chkDevInst.addActionListener(this);

    JSeparator sep1 = new JSeparator();

    // Layout
    JPanel all = new JPanel(new MigLayout("wrap 2, fillx", "[align label][grow, fill]"));
    all.add(lblOpt, "span");

    all.add(lblDevInst);
    all.add(chkDevInst);
    all.add(lblConPath);
    all.add(pbrConPath);

    all.add(sep1, "grow, span");

    all.add(lblPlug, "span");
    all.add(lblImageMagick);
    all.add(pbrImageMagick);

    return all;
  }

  private void loadConfig(ConfigProfile p) {
    prof = p;

    txtProfName.setText(prof.getProfName());

    cmbDbTypes.setSelectedItem(prof.getDbtype().toString());

    txtPort.setText(prof.getPort());
    txtHost.setText(prof.getHost());
    txtDb.setText(prof.getDatabase());
    txtUser.setText(prof.getUsername());
    txtPass.setText(prof.getPassword()); // mmmmmm Deprecated

    // Mandatory Tab
    txtHttp.setText(prof.getHttp());
    txtHttps.setText(prof.getHttps());
    txtAjp.setText(prof.getAjp());
    pbrFiles.setPath(prof.getFilestore());
    pbrJava.setPath(prof.getJavahome());
    txtAdminURL.setText(prof.getAdminurl());
    pbrFree.setPath(prof.getFreetext());
    pbrStop.setPath(prof.getStopwords());
    pbrReport.setPath(prof.getReporting());
    pbrPlugins.setPath(prof.getPlugins());

    // Misc Tab
    chkDevInst.setSelected(prof.isDevinst());
    if (prof.isDevinst()) {
      pbrConPath.setEnabled(false);
    }
    pbrConPath.setPath(prof.getConversion());
    pbrImageMagick.setPath(prof.getImagemagick());
  }

  @SuppressWarnings("deprecation")
  private void saveConfig() throws IOException {
    Boolean saved = false;

    prof = new ConfigProfile(txtProfName.getText());

    // Hibernate Tab
    prof.setDbtype(cmbDbTypes.getSelectedItem().toString());
    prof.setPort(txtPort.getText());
    prof.setHost(txtHost.getText());
    prof.setDatabase(txtDb.getText());
    prof.setUsername(txtUser.getText());
    prof.setPassword(txtPass.getText()); // mmmmmm Deprecated

    // Mandatory Tab
    prof.setHttp(txtHttp.getText());
    prof.setHttps(txtHttps.getText());
    prof.setAjp(txtAjp.getText());
    prof.setFilestore(pbrFiles.getPath());
    prof.setJavahome(pbrJava.getPath());
    prof.setAdminurl(txtAdminURL.getText());
    prof.setFreetext(pbrFree.getPath());
    prof.setStopwords(pbrStop.getPath());
    prof.setReporting(pbrReport.getPath());
    prof.setPlugins(pbrPlugins.getPath());

    // Misc Tab
    prof.setDevinst(chkDevInst.isSelected());
    prof.setConversion(pbrConPath.getPath());
    prof.setImagemagick(pbrImageMagick.getPath());

    // Create XML
    XStream stream = new XStream();
    String xml = stream.toXML(prof);

    // Write to file
    BufferedWriter output;

    String filename = "./configs/" + txtProfName.getText() + ".xml";
    File check = new File(filename);

    if (check.isFile() && check.exists()) {
      int answer =
          JOptionPane.showConfirmDialog(
              null,
              "A profile with this name already exists!\n\nAre you sure you want to overwrite it?",
              "Overwrite?",
              JOptionPane.YES_NO_OPTION);
      if (answer == JOptionPane.YES_OPTION) {
        output = new BufferedWriter(new FileWriter(filename));
        output.write(xml);
        output.close();
        saved = true;
      }
    } else {
      output = new BufferedWriter(new FileWriter(filename));
      output.write(xml);
      output.close();
      saved = true;
    }

    if (saved) {
      result = RESULT_SAVE;
      this.dispose();
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == cancel) {
      this.dispose();
    } else if (e.getSource() == save) {
      try {
        saveConfig();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    } else if (e.getSource() == cmbDbTypes) {
      String choice = cmbDbTypes.getSelectedItem().toString();

      if (choice.equalsIgnoreCase("oracle")) {
        txtPort.setText("1521");
      } else if (choice.equalsIgnoreCase("postgresql")) {
        txtPort.setText("5432");
      } else if (choice.equalsIgnoreCase("ms sql")) {
        txtPort.setText("1433");
      }
    } else if (e.getSource() == chkDevInst) {
      if (pbrConPath.isEnabled()) {
        pbrConPath.setEnabled(false);
      } else {
        pbrConPath.setEnabled(true);
      }
    }
  }

  public int getResult() {
    return result;
  }
}
