package com.dytech.edge.importexport.exportutil.filters;

import com.dytech.common.producers.Warehouse;
import com.dytech.edge.importexport.SharedData;
import com.dytech.edge.importexport.types.Item;
import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JOptionPane;

@SuppressWarnings("nls")
public class NativeXmlSave implements Runnable {
  private final SharedData data;
  private final Warehouse<Item> warehouse;
  private final Component parent;

  public NativeXmlSave(SharedData data, Warehouse<Item> warehouse, Component parent) {
    super();
    this.data = data;
    this.warehouse = warehouse;
    this.parent = parent;
  }

  @Override
  public void run() {
    boolean showErrors = true;

    Item item = (Item) warehouse.getProduct();
    while (item != null) {
      File xml = new File(data.getSaveFolder(), item.getUuid() + "_" + item.getVersion() + ".xml");

      try (BufferedWriter out = new BufferedWriter(new FileWriter(xml))) {
        out.write(item.getXml().toString());
      } catch (IOException ex) {
        if (showErrors) {
          if (showErrorDialog(item)) {
            showErrors = false;
          }
        }
      }

      item = (Item) warehouse.getProduct();
    }
  }

  private boolean showErrorDialog(Item item) {
    final int result =
        JOptionPane.showConfirmDialog(
            parent,
            "An error has occured saving the item "
                + item.getName()
                + " to disk.\n"
                + "Do you want to ignore further errors?",
            "Error Downloading",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.ERROR_MESSAGE);

    return result == JOptionPane.YES_OPTION;
  }
}
