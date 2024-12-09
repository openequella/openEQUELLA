package com.dytech.edge.importexport.exportutil.filters;

import com.dytech.common.producers.Warehouse;
import com.dytech.devlib.PropBagEx;
import com.dytech.edge.importexport.SharedData;
import com.dytech.edge.importexport.types.Item;
import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.StringWriter;
import javax.swing.JOptionPane;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

@SuppressWarnings("nls")
public class XsltTransformer implements Runnable {
  private final SharedData data;
  private final Warehouse<Item> warehouse;
  private final Component parent;

  public XsltTransformer(SharedData data, Warehouse<Item> warehouse, Component parent) {
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
      File xml = new File(data.getSaveFolder(), item.getUuid() + "_" + item.getVersion() + ".txt");

      try (BufferedWriter out = new BufferedWriter(new FileWriter(xml))) {
        String result = transform(item.getXml());
        out.write(result);
      } catch (Exception ex) {
        if (showErrors) {
          if (showErrorDialog(item)) {
            showErrors = false;
          }
        }
      }

      item = (Item) warehouse.getProduct();
    }
  }

  private String transform(PropBagEx xml) throws Exception {
    // Transform XML to get the body of the email
    TransformerFactory tFactory = TransformerFactory.newInstance();
    StringReader strXSLT = new StringReader(data.getXslt());
    Transformer oTransformer = tFactory.newTransformer(new StreamSource(strXSLT));

    StringReader strReader = new StringReader(xml.toString());
    StreamSource oInput = new StreamSource(strReader);

    StringWriter strWriter = new StringWriter();
    StreamResult oOutput = new StreamResult(strWriter);

    // Transform!!
    oTransformer.transform(oInput, oOutput);
    strReader.close();

    // We don't actuallly have to close this. See javadoc!
    // strWriter.close ();

    return strWriter.toString();
  }

  private boolean showErrorDialog(Item item) {
    final int result =
        JOptionPane.showConfirmDialog(
            parent,
            "An error has occured transforming the item "
                + item.getName()
                + " with.\n"
                + "the given XSLT.  Do you want to ignore further errors?",
            "Error Transforming",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.ERROR_MESSAGE);

    return result == JOptionPane.YES_OPTION;
  }
}
