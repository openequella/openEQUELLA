package com.tle.configmanager;

import com.dytech.gui.ComponentHelper;
import com.thoughtworks.xstream.XStream;
import com.tle.common.Check;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;

// Author: Andrew Gibb

@SuppressWarnings({"serial", "nls"})
public class ConfigLauncherGUI extends JFrame implements ActionListener {
  public static final String MANDATORY_CONFIG = "mandatory-config.properties";
  public static final String OPTIONAL_CONFIG = "optional-config.properties";
  public static final String HIBERNATE_CONFIG = "hibernate.properties";
  public static final String LOGGING_CONFIG = "learningedge-log4j.properties";
  public static final String IMAGEMAGICK_CONFIG =
      "plugins/com.tle.core.imagemagick/config.properties.unresolved";
  public static final String HIKARI_CONFIG = "hikari.properties";

  private final String TITLE = "TLE Configuration Manager";
  private final String PATH = "./configs/";
  private final String ORACLE = "oracle";
  private final String POSTGRESQL = "postgresql";
  private final String MSSQL = "ms sql";
  private final String source;
  private final String destination;

  private JLabel lblConfig;
  private JComboBox<ConfigProfile> cmbConfigs;
  private JButton btnNew, btnEdit, btnApply, btnDelete;
  private JSeparator sep;
  private List<ConfigProfile> configs;

  public ConfigLauncherGUI(String source, String destination) {
    setTitle(TITLE);

    setupGUI();
    setResizable(false);
    setDefaultCloseOperation(EXIT_ON_CLOSE);

    this.source = source;
    this.destination = destination;

    // Set a minimum width...leave the height to the pack...
    setMinimumSize(new Dimension(300, 0));
    pack();
    ComponentHelper.centreOnScreen(this);

    // Updated combo box containing profiles
    updateConfigs();
  }

  // Sets up the GUI for managing/loading the configuration profiles
  private void setupGUI() {
    Container contents = getContentPane();
    contents.setLayout(new MigLayout("wrap 3", "[grow][grow][grow]"));

    configs = new ArrayList<ConfigProfile>();
    lblConfig = new JLabel("Configurations: ");
    cmbConfigs = new JComboBox<ConfigProfile>();

    btnNew = new JButton("New");
    btnNew.addActionListener(this);

    btnApply = new JButton("Apply Configuration");
    btnApply.addActionListener(this);

    btnEdit = new JButton("Edit");
    btnEdit.addActionListener(this);

    sep = new JSeparator();
    btnDelete = new JButton("Delete");
    btnDelete.addActionListener(this);

    contents.add(lblConfig, "growx, spanx 3");
    contents.add(cmbConfigs, "growx, spanx 3");
    contents.add(btnNew, "growx, center");
    contents.add(btnEdit, "growx, center");
    contents.add(btnDelete, "growx, center");
    contents.add(sep, "growx, spanx 3");
    contents.add(btnApply, "center, growx, spanx 3");
  }

  // Updates the available configuration profiles
  public void updateConfigs() {
    File srcDir = new File(PATH);
    File[] configFiles = srcDir.listFiles();
    Reader rdr;

    cmbConfigs.removeAllItems();
    configs.clear();

    if (configFiles != null) {
      for (File f : configFiles) {
        if (f.isFile()) {
          XStream xstream = new XStream();
          try {
            rdr = new BufferedReader(new FileReader(f));
            ConfigProfile prof = (ConfigProfile) xstream.fromXML(rdr);
            configs.add(prof);
            rdr.close();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }

      Collections.sort(
          configs,
          new Comparator<ConfigProfile>() {
            @Override
            public int compare(ConfigProfile o1, ConfigProfile o2) {
              return o1.getProfName().compareToIgnoreCase(o2.getProfName());
            }
          });

      for (ConfigProfile prof : configs) {
        cmbConfigs.addItem(prof);
      }
    }
    if (configs.isEmpty()) {
      btnEdit.setEnabled(false);
      btnApply.setEnabled(false);
      btnDelete.setEnabled(false);
    } else {
      btnEdit.setEnabled(true);
      btnApply.setEnabled(true);
      btnDelete.setEnabled(true);
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == btnNew) {
      addProfile();
    } else if (e.getSource() == btnEdit) {
      editProfile();
    } else if (e.getSource() == btnDelete) {
      deleteProfile();
    } else if (e.getSource() == btnApply) {
      try {
        loadProfile();
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(
            null,
            "Error loading configuration: \n" + ex.getMessage(),
            "Load Failed",
            JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  // Adds a new profile which is either a clone of an existing profile or is
  // blank
  private void addProfile() {
    ConfigEditorGUI confEd = null;

    if (configs != null && cmbConfigs.getSelectedItem() != null) {
      ConfigProfile selectedProf = (ConfigProfile) cmbConfigs.getSelectedItem();
      int result =
          JOptionPane.showConfirmDialog(
              null,
              "Do you want to clone the currently selected configuration?: "
                  + selectedProf.getProfName(),
              "Clone Confirmation",
              JOptionPane.YES_NO_OPTION);

      if (result == JOptionPane.YES_OPTION) {
        confEd = new ConfigEditorGUI(selectedProf, "copy");
      } else {
        confEd = new ConfigEditorGUI();
      }
    } else {
      confEd = new ConfigEditorGUI();
    }
    confEd.setModal(true);
    confEd.setVisible(true);

    if (confEd.getResult() == ConfigEditorGUI.RESULT_SAVE) {
      updateConfigs();
    }
  }

  // Edits and existing profile
  private void editProfile() {
    int index = cmbConfigs.getSelectedIndex();
    ConfigProfile selectedProf = (ConfigProfile) cmbConfigs.getSelectedItem();
    ConfigEditorGUI confEd = new ConfigEditorGUI(selectedProf);

    confEd.setModal(true);
    confEd.setVisible(true);

    if (confEd.getResult() == ConfigEditorGUI.RESULT_SAVE) {
      updateConfigs();
      cmbConfigs.setSelectedIndex(index);
    }
  }

  // Deletes a configuration profile
  private void deleteProfile() {
    ConfigProfile selectedProf = (ConfigProfile) cmbConfigs.getSelectedItem();
    File toDel = new File(PATH + selectedProf.getProfName() + ".xml");

    int result =
        JOptionPane.showConfirmDialog(
            null,
            "Are you sure you want to delete this configuration?: " + selectedProf.getProfName(),
            "Delete Confirmation",
            JOptionPane.YES_NO_OPTION);

    if (result == JOptionPane.YES_OPTION) {
      boolean success = toDel.delete();
      if (!success) {
        JOptionPane.showMessageDialog(
            null,
            "Unable to delete configuration: " + selectedProf.getProfName(),
            "Delete Failed",
            JOptionPane.ERROR_MESSAGE);
      }
    }

    updateConfigs();
  }

  // Loads a profile (SUPER HACKISH)
  private void loadProfile() throws FileNotFoundException, IOException, ConfigurationException {
    ConfigProfile selectedProf = (ConfigProfile) cmbConfigs.getSelectedItem();
    File srcDir = new File(source);

    // Remove Current Configuration Files
    File destDir = new File(destination);
    FileUtils.deleteDirectory(destDir);

    // Create Destination
    destDir.mkdir();

    // Copy Required Files (Database Specific)
    boolean oracleSelected = selectedProf.getDbtype().equalsIgnoreCase(ORACLE);
    if (oracleSelected) {
      org.apache.commons.io.FileUtils.copyFile(
          new File(srcDir + "/hibernate.properties.oracle"),
          new File(destDir + "/hibernate.properties"));
    } else if (selectedProf.getDbtype().equalsIgnoreCase(POSTGRESQL)) {
      FileUtils.copyFile(
          new File(srcDir + "/hibernate.properties.postgresql"),
          new File(destDir + "/hibernate.properties"));
    } else if (selectedProf.getDbtype().equalsIgnoreCase(MSSQL)) {
      FileUtils.copyFile(
          new File(srcDir + "/hibernate.properties.sqlserver"),
          new File(destDir + "/hibernate.properties"));
    }

    // Mandatory / Optional / Logging
    FileUtils.copyFile(
        new File(srcDir + "/mandatory-config.properties"),
        new File(destDir + "/mandatory-config.properties"));
    FileUtils.copyFile(
        new File(srcDir + "/optional-config.properties"),
        new File(destDir + "/optional-config.properties"));

    // Copy custom development logging file
    FileUtils.copyFile(
        new File("./learningedge-log4j.properties"),
        new File(destDir + "/learningedge-log4j.properties"));

    // Other Miscellaneous Files
    FileUtils.copyFile(
        new File(srcDir + "/en-stopWords.txt"), new File(destDir + "/en-stopWords.txt"));
    FileUtils.copyFile(
        new File(srcDir + "/" + HIKARI_CONFIG), new File(destDir + "/" + HIKARI_CONFIG));

    // Plugins Folder
    FileUtils.copyDirectoryToDirectory(new File(srcDir + "/plugins"), destDir);

    // Edit Hibernate Properties
    String hibProp = readFile(destination + "/" + HIBERNATE_CONFIG);

    hibProp = hibProp.replace("${datasource/host}", selectedProf.getHost());
    hibProp = hibProp.replace("${datasource/port}", selectedProf.getPort());
    hibProp = hibProp.replace("${datasource/database}", selectedProf.getDatabase());
    hibProp = hibProp.replace("${datasource/username}", selectedProf.getUsername());
    hibProp = hibProp.replace("${datasource/password}", selectedProf.getPassword());
    hibProp =
        hibProp.replace(
            "${datasource/schema}",
            oracleSelected ? "hibernate.default_schema = " + selectedProf.getUsername() : "");

    writeFile(destination + "/hibernate.properties", hibProp);

    // Edit Mandatory Properties
    PropertyEditor mandProps = new PropertyEditor();
    mandProps.load(new File(destination + "/" + MANDATORY_CONFIG));

    String http = selectedProf.getHttp();
    String portFromUrl = selectedProf.getAdminurl().split(":")[1];
    mandProps.setProperty(
        "http.port",
        !Check.isEmpty(http) ? http : !Check.isEmpty(portFromUrl) ? portFromUrl : "80");

    String https = selectedProf.getHttps();
    if (!Check.isEmpty(https)) {
      mandProps.setProperty("https.port", https);
    }

    String ajp = selectedProf.getAjp();
    if (!Check.isEmpty(https)) {
      mandProps.setProperty("ajp.port", ajp);
    }
    mandProps.setProperty("filestore.root", selectedProf.getFilestore());
    mandProps.setProperty("java.home", selectedProf.getJavahome());
    mandProps.setProperty("admin.url", selectedProf.getAdminurl());
    mandProps.setProperty("freetext.index.location", selectedProf.getFreetext());
    mandProps.setProperty("freetext.stopwords.file", selectedProf.getStopwords());
    String reporting = selectedProf.getReporting();
    if (!Check.isEmpty(reporting)) {
      mandProps.setProperty("reporting.workspace.location", reporting);
    }
    mandProps.setProperty("plugins.location", selectedProf.getPlugins());

    mandProps.save(new File(destination + "/" + MANDATORY_CONFIG));

    // Edit Optional Properties
    String optProp = readFile(destination + "/" + OPTIONAL_CONFIG);
    if (selectedProf.isDevinst()) {
      optProp =
          optProp.replace(
              "#conversionService.disableConversion = false",
              "conversionService.disableConversion = true");
      optProp =
          optProp.replace(
              "conversionService.conversionServicePath = ${install.path#t\\/}/conversion/conversion-service.jar",
              "#conversionService.conversionServicePath =");
      optProp =
          optProp.replace("#pluginPathResolver.wrappedClass", "pluginPathResolver.wrappedClass");
    } else {
      optProp =
          optProp.replace(
              "${install.path#t\\/}/conversion/conversion-service.jar",
              selectedProf.getConversion());
    }

    writeFile(destination + "/optional-config.properties", optProp);

    // Edit ImageMagik Properties (MORE HAX...)
    File imgmgk = new File(destination + "/" + IMAGEMAGICK_CONFIG);
    PropertyEditor magickProps = new PropertyEditor();
    magickProps.load(imgmgk);
    magickProps.setProperty("imageMagick.path", selectedProf.getImagemagick());
    magickProps.save(new File((destination + "/" + IMAGEMAGICK_CONFIG).replace(".unresolved", "")));
    imgmgk.delete();

    JOptionPane.showMessageDialog(
        null,
        "The configuration: " + selectedProf.getProfName() + " has been successfully loaded.",
        "Load Success",
        JOptionPane.INFORMATION_MESSAGE);
  }

  // Reads a file into a string
  private String readFile(String path) throws IOException {
    StringBuilder contents = new StringBuilder();

    BufferedReader br = new BufferedReader(new FileReader(path));
    String line = null;

    while ((line = br.readLine()) != null) {
      contents.append(line);
      contents.append(System.getProperty("line.separator"));
    }
    br.close();

    return contents.toString();
  }

  // Writes a file from String
  private void writeFile(String path, String contents) throws IOException {
    BufferedWriter output = null;

    output = new BufferedWriter(new FileWriter(new File(path)));
    output.write(contents);
    output.close();
  }
}
