package com.dytech.edge.importexport.importutil;

import com.dytech.common.text.NumberStringComparator;
import com.dytech.devlib.PropBagEx;
import com.dytech.edge.importexport.SharedData;
import com.dytech.edge.importexport.WizardPage;
import com.dytech.edge.importexport.types.Item;
import com.dytech.edge.importexport.types.ItemDef;
import com.dytech.gui.TableLayout;
import com.tle.common.Check;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

@SuppressWarnings("nls")
public class ImportPage extends WizardPage implements ActionListener {
  private File dirFile;
  private File xsltFile;
  private List<FilePair> files;

  // GUI elements
  private JButton xsltButton;
  private JButton dirButton;
  private JComboBox typeComboBox;
  private JTextField dirTextField;
  private JTextField xsltTextField;
  private JFileChooser dirChooser;
  private JFileChooser xsltChooser;

  public ImportPage(SharedData data) {
    super(data);
    setup();
  }

  private List<ItemDef> getCollections() {
    try {
      final List<ItemDef> collections = data.getSoapSession().getContributableCollections();
      final List<ItemDef> filteredCollections = new ArrayList<ItemDef>();

      for (ItemDef collection : collections) {
        if (!collection.isSystem()) {
          filteredCollections.add(collection);
        }
      }
      Collections.sort(filteredCollections, new NumberStringComparator<ItemDef>());
      return filteredCollections;
    } catch (Exception e) {
      JOptionPane.showMessageDialog(
          SwingUtilities.getWindowAncestor(this),
          "Error retrieving Item Definitions from the server.",
          "Error",
          JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
    return null;
  }

  private void setup() {
    JLabel heading = new JLabel("<html><h2>Item Selection");
    JLabel typeLabel = new JLabel("Type");
    JLabel dirLabel = new JLabel("Source");
    JLabel xsltLabel = new JLabel("XSLT");
    JLabel requiredLabel = new JLabel("<html><b>Required:");
    JLabel optionalLabel = new JLabel("<html><b>Optional:");

    xsltTextField = new JTextField();
    dirTextField = new JTextField();

    xsltButton = new JButton("...");
    dirButton = new JButton("...");

    xsltTextField.setEditable(false);
    dirTextField.setEditable(true);

    xsltButton.addActionListener(this);
    dirButton.addActionListener(this);

    typeComboBox = new JComboBox();
    typeComboBox.setEnabled(false);

    dirChooser = new JFileChooser();
    dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    xsltChooser = new JFileChooser();
    xsltChooser.setFileFilter(new XSLTFileFilter());

    final int height1 = typeComboBox.getPreferredSize().height;
    final int height2 = xsltTextField.getPreferredSize().height;
    final int height3 = optionalLabel.getPreferredSize().height;
    final int height4 = heading.getPreferredSize().height;

    final int width2 = xsltButton.getPreferredSize().width;
    final int width3 = dirLabel.getPreferredSize().width + 5;

    final int[] rows = new int[] {height4, height3, height1, height2, height3, height2};
    final int[] cols = new int[] {width3, TableLayout.FILL, width2};

    setLayout(new TableLayout(rows, cols, 5, 0));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    add(heading, new Rectangle(0, 0, 2, 1));

    add(requiredLabel, new Rectangle(0, 1, 3, 1));

    add(typeLabel, new Rectangle(0, 2, 1, 1));
    add(typeComboBox, new Rectangle(1, 2, 2, 1));

    add(dirLabel, new Rectangle(0, 3, 1, 1));
    add(dirTextField, new Rectangle(1, 3, 1, 1));
    add(dirButton, new Rectangle(2, 3, 1, 1));

    add(optionalLabel, new Rectangle(0, 4, 3, 1));

    add(xsltLabel, new Rectangle(0, 5, 1, 1));
    add(xsltTextField, new Rectangle(1, 5, 1, 1));
    add(xsltButton, new Rectangle(2, 5, 1, 1));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    if (source == dirButton) {
      final int reply = dirChooser.showDialog(this, "Select");
      File file = getFile(reply, dirChooser, dirTextField, dirFile);
      if (file != null) {
        dirTextField.setText(file.getAbsolutePath());
      }
    } else if (source == xsltButton) {
      int reply = xsltChooser.showDialog(this, "Select");
      xsltFile = getFile(reply, xsltChooser, xsltTextField, xsltFile);
    }
  }

  private File getFile(int reply, JFileChooser chooser, JTextField field, File oldFile) {
    switch (reply) {
      case JFileChooser.ERROR_OPTION:
        {
          JOptionPane.showMessageDialog(
              SwingUtilities.getWindowAncestor(this),
              "Error retrieving file",
              "Error",
              JOptionPane.ERROR_MESSAGE);
          field.setText("");
          return null;
        }
      case JFileChooser.APPROVE_OPTION:
        {
          File file = chooser.getSelectedFile();
          field.setText(file.getPath());
          return file;
        }
      case JFileChooser.CANCEL_OPTION:
        {
          return oldFile;
        }
    }
    field.setText("");
    return null;
  }

  class XSLTFileFilter extends javax.swing.filechooser.FileFilter {
    @Override
    public boolean accept(File pathname) {
      if (pathname.isDirectory()) {
        return true;
      }

      String name = pathname.getName();
      return name.endsWith(".xsl") || name.endsWith(".xslt");
    }

    @Override
    public String getDescription() {
      return null;
    }
  }

  @Override
  public boolean onNext() {
    boolean flag = false;
    try {
      String dirText = dirTextField.getText();
      dirFile = new File(dirText);

      if (Check.isEmpty(dirText)) {
        throw new Exception("No Source selected");
      } else if (!typeComboBox.isEnabled()) {
        throw new Exception("No Type selected");
      }

      if (!dirFile.exists()) {
        throw new Exception("Source folder doesn't exist");
      }

      getFiles();
      data.setItems(new ArrayList<Item>());

      for (FilePair pair : files) {
        PropBagEx xml = null;
        if (xsltFile != null) {
          String transformed = transform(pair.xmlFile);
          xml = new PropBagEx(transformed);
        } else {
          xml = new PropBagEx(pair.xmlFile);
        }

        String uuid = xml.getNode("item/@id");
        if (uuid.length() == 0) {
          uuid = UUID.randomUUID().toString();
          xml.setNode("item/@id", uuid);
        }

        getItem(pair.directoryFile, xml);
      }
      flag = true;
    } catch (Exception e) {
      String message = e.getMessage();
      if (Check.isEmpty(message)) {
        message = "Unexpected error, see console for trace";
      }
      JOptionPane.showMessageDialog(
          SwingUtilities.getWindowAncestor(this), message, "Error", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }

    return flag;
  }

  protected void getAttachments(File directory, PropBagEx item_xml) {
    File[] attachments = directory.listFiles();
    if (item_xml.getSubtree("item/attachments") != null || attachments.length == 0) {
      return;
    }

    List<String> relpaths = FileUtil.getRelativeFileList(attachments);

    PropBagEx attachmentsXml = item_xml.newSubtree("item/attachments");
    for (int i = 0; i < attachments.length; i++) {
      PropBagEx attachment = attachmentsXml.newSubtree("attachment");
      String filepath = relpaths.get(i);

      attachment.setNode("@type", "local");
      attachment.setNode("size", attachments[i].length());
      attachment.setNode("file", filepath);
      attachment.setNode("description", filepath);
    }
  }

  protected void getItem(File directory, PropBagEx xml) throws Exception {
    final ItemDef itemDef = (ItemDef) typeComboBox.getSelectedItem();

    xml.setNode("item/@itemdefid", itemDef.getUuid());

    String itemName = xml.getNode("item/name");
    if (itemName.length() == 0) {
      if (directory != null) {
        itemName = directory.getName();
      } else {
        itemName = "Unknown name";
      }
      xml.setNode("item/name", itemName);
    }
    final Item item =
        new Item(
            itemName,
            xml.getNode("item/@id"),
            xml.getIntNode("item/@version", 1),
            itemDef.getUuid(),
            data.getSoapSession().getUserId());
    item.setName(itemName);

    final File xmlFile = File.createTempFile("importutil", "xml");
    xmlFile.deleteOnExit();

    try {
      if (directory != null) {
        item.setAttachmentsFolder(directory);
        getAttachments(directory, xml);
      }

      data.getItems().add(item);

      try (FileWriter fileout = new FileWriter(xmlFile, false)) {
        fileout.write(xml.toString());
      }
      item.setXmlFile(xmlFile);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(
          SwingUtilities.getWindowAncestor(this),
          "Item is not valid XML: " + xmlFile.toString(),
          "Invalid Configuration",
          JOptionPane.ERROR_MESSAGE);
      ex.printStackTrace();
      throw ex;
    }
  }

  @Override
  public boolean onBack() {
    return true;
  }

  @Override
  public void onShow() {
    typeComboBox.removeAllItems();
    for (ItemDef collection : getCollections()) {
      typeComboBox.addItem(collection);
    }
    typeComboBox.setEnabled(true);
  }

  public void getFiles() {
    File[] dirFiles = dirFile.listFiles();

    Map<String, File> xmlHash = new HashMap<String, File>();
    Map<String, File> dirHash = new HashMap<String, File>();
    for (int i = 0; i < dirFiles.length; i++) {
      final File tempFile = dirFiles[i];
      if (tempFile.isDirectory()) {
        dirHash.put(tempFile.getName(), tempFile);
      } else {
        final String name = tempFile.getName();
        final int pos = name.lastIndexOf('.');
        if (pos >= 0) {
          final String ext = name.substring(pos + 1);
          if (ext.equals("xml")) {
            final String shortName = name.substring(0, pos);
            xmlHash.put(shortName, tempFile);
          }
        }
      }
    }

    files = new ArrayList<FilePair>();
    for (String name : xmlHash.keySet()) {
      final File xml = xmlHash.get(name);
      if (xml != null) {
        final File dir = dirHash.get(name);
        files.add(new FilePair(xml, dir));
      }
    }
  }

  private class FilePair {
    public File xmlFile;
    public File directoryFile;

    public FilePair(File xmlFile, File directoryFile) {
      this.xmlFile = xmlFile;
      this.directoryFile = directoryFile;
    }
  }

  private String transform(File file) throws Exception {
    // Instantiate a TransformerFactory.
    TransformerFactory tFactory = TransformerFactory.newInstance();

    // Use the TransformerFactory to process the stylesheet Source and
    // generate a Transformer.
    Transformer transformer = tFactory.newTransformer(new StreamSource(xsltFile));

    // Perform the transformation.
    StringWriter transformedResults = new StringWriter();
    transformer.transform(new StreamSource(file), new StreamResult(transformedResults));

    return transformedResults.toString();
  }
}
