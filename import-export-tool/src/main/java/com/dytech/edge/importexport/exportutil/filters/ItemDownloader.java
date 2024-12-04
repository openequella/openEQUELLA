package com.dytech.edge.importexport.exportutil.filters;

import com.dytech.common.net.Wget;
import com.dytech.common.producers.Warehouse;
import com.dytech.edge.importexport.SharedData;
import com.dytech.edge.importexport.SoapSession;
import com.dytech.edge.importexport.types.Item;
import java.awt.Component;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import javax.swing.JOptionPane;

@SuppressWarnings("nls")
public class ItemDownloader extends Warehouse<Item> implements Runnable {
  private static final int IGNORE_THIS_ERROR = 0;
  private static final int IGNORE_FUTURE_ERRORS = 1;
  private static final int STOP_DOWNLOADING = 2;

  private final SharedData data;
  private final Component parent;
  private final ItemNotification listener;

  public ItemDownloader(SharedData data, Component parent, ItemNotification listener) {
    super();
    this.data = data;
    this.parent = parent;
    this.listener = listener;
  }

  @Override
  public void run() {
    boolean promptForErrors = true;

    final List<Item> items = data.getItems();
    final int count = items.size();
    for (int i = 0; i < count; i++) {
      Item item = items.get(i);
      if (listener != null) {
        listener.downloadingItem(i + 1);
      }

      try {
        processItem(item);
      } catch (Exception ex) {
        ex.printStackTrace();
        if (promptForErrors) {
          int result = showErrorDialog(item);
          switch (result) {
            case STOP_DOWNLOADING:
              setClosed(true); // Close the warehouse
              return;

            case IGNORE_FUTURE_ERRORS:
              promptForErrors = false;
              break;
          }
        }
      }
    }

    setClosed(true); // Close the warehouse
  }

  private void processItem(Item item) throws Exception, IOException {
    final SoapSession soapSession = data.getSoapSession();
    final URL baseUrl = getItemBaseUrl(item, "file");
    final URL fallbackBaseUrl = getItemBaseUrl(item, "items");

    // Make sure the item has all the XML
    soapSession.populateItemXml(item);

    if (data.getSaveAs() != SharedData.SAVE_AS_IMS) {
      String[] attachments = soapSession.getFilesForItem(item);
      if (attachments.length > 0) {
        File dir = new File(data.getSaveFolder(), item.getUuid() + "_" + item.getVersion());
        dir.mkdir();

        for (String filename : attachments) {
          final File f = new File(dir, filename);
          f.getParentFile().mkdirs();
          downloadAttachment(baseUrl, fallbackBaseUrl, filename, f);
        }
      }
    } else {
      String packageFile = item.getXml().getNode("item/itembody/packagefile", null);
      if (packageFile != null) {
        String filename = getUniqueFile(packageFile);

        File f = new File(data.getSaveFolder(), filename);
        downloadAttachment(baseUrl, fallbackBaseUrl, "_IMS/" + packageFile, f);

        FileWriter writer =
            new FileWriter(new File(data.getSaveFolder(), "package-mapping.txt"), true);
        writer.write(item.getUuid());
        writer.write("\t");
        writer.write(Integer.toString(item.getVersion()));
        writer.write("\t");
        writer.write(filename);
        writer.write("\n");
        writer.close();
      }
    }

    // Add the item to our "warehouse"
    addProduct(item);
  }

  /**
   * @param item
   * @param servlet 'file' or 'items'
   * @return
   * @throws Exception
   */
  private URL getItemBaseUrl(Item item, String servlet) throws Exception {
    return new URL(
        data.getInstitutionUrl(),
        servlet
            + "/"
            + item.getUuid()
            + "/" //$NON-NLS-2$
            + item.getVersion());
  }

  private String getUniqueFile(final String name) {
    int li = name.lastIndexOf('.');
    String firstPart = name.substring(0, li);
    String extension = name.substring(li);

    String newName = name;

    int i = 2;
    while (true) {
      if (!new File(data.getSaveFolder(), newName).exists()) {
        return newName;
      } else {
        newName = firstPart + " (" + i + ")" + extension;
        i++;
      }
    }
  }

  /**
   * More dodge-o-rama!
   *
   * @param baseUrl
   * @param fallbackBaseUrl
   * @param extra
   * @param file
   * @return
   * @throws UnsupportedEncodingException
   */
  private boolean downloadAttachment(URL baseUrl, URL fallbackBaseUrl, String extra, File file)
      throws UnsupportedEncodingException {
    if (!downloadAttachment(baseUrl, extra, file, false)) {
      if (!downloadAttachment(fallbackBaseUrl, extra, file, true)) {
        return false;
      }
    }
    return true;
  }

  private boolean downloadAttachment(URL baseUrl, String extra, File file, boolean logIt)
      throws UnsupportedEncodingException {
    StringBuilder newUrl = new StringBuilder(baseUrl.toString());
    String[] parts = extra.split("/");
    for (int i = 0; i < parts.length; i++) {
      newUrl.append('/');
      newUrl.append(URLEncoder.encode(parts[i], "UTF-8").replace("+", "%20"));
    }
    newUrl.append("?preview=true&drm.s=true");

    final String url = newUrl.toString();

    try {
      try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
        Wget.getURL(new URL(url), out);
        return true;
      }
    } catch (IOException ex) {
      if (file.exists()) {
        file.delete();
      }

      if (logIt) {
        System.err.println("Couldn't download from " + url);

        final StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        System.err.println(sw.toString());
      }
      return false;
    }
  }

  private int showErrorDialog(Item item) {
    Object[] options = {"Yes", "No", "Ignore All Errors"};

    final int result =
        JOptionPane.showOptionDialog(
            parent,
            "An error has occured downloading the item "
                + item.getName()
                + ".\n"
                + "Do you want to continue?",
            "Error Downloading",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.ERROR_MESSAGE,
            null,
            options,
            options[0]);

    switch (result) {
      case JOptionPane.NO_OPTION:
        return STOP_DOWNLOADING;

      case JOptionPane.CANCEL_OPTION:
        return IGNORE_FUTURE_ERRORS;

      default:
        return IGNORE_THIS_ERROR;
    }
  }
}
