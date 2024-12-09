package com.dytech.edge.importexport.importutil;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.importexport.SharedData;
import com.dytech.edge.importexport.SharedData.Completion;
import com.dytech.edge.importexport.SoapSession;
import com.dytech.edge.importexport.WizardPage;
import com.dytech.edge.importexport.types.Item;
import com.dytech.gui.TableLayout;
import com.tle.common.Pair;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

@SuppressWarnings("nls")
public class UploadPage extends WizardPage {
  protected int itemTotal;
  protected JLabel itemCount;
  protected JLabel itemName;

  public UploadPage(SharedData data) {
    super(data);
    setup();
  }

  private void setup() {
    JLabel heading = new JLabel("<html><h2>Uploading Content...");

    itemCount = new JLabel(" ");
    itemName = new JLabel(" ");

    itemName.setForeground(Color.GRAY);

    final int height1 = heading.getPreferredSize().height;
    final int height2 = itemCount.getPreferredSize().height;
    final int gap = 0;

    final int[] rows = {height1, height2, height2, gap, TableLayout.FILL};
    final int[] cols = {TableLayout.FILL};

    setLayout(new TableLayout(rows, cols, 5, 5));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    add(heading, new Rectangle(0, 0, 1, 1));
    add(itemCount, new Rectangle(0, 1, 1, 1));
    add(itemName, new Rectangle(0, 2, 1, 1));
  }

  @Override
  public boolean onNext() {
    return true;
  }

  @Override
  public boolean onBack() {
    return true;
  }

  @Override
  public void onShow() {
    try {
      itemTotal = data.getItems().size();
      itemCount.setText("Uploading 0 of " + itemTotal + " Items");
      Pair<Integer, Integer> res = uploadItems();
      int successful = res.getFirst();
      int failed = res.getSecond();
      data.setCompletion(
          new Completion(
              successful,
              res.getSecond(),
              successful
                  + " item(s) uploaded successfully.<br/>"
                  + failed
                  + " item(s) failed to upload"));
      /*
       * JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this
       * ), successful + " item(s) uploaded successfully.",
       * "Upload Complete", JOptionPane.INFORMATION_MESSAGE);
       */
      frame.goNext();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private Pair<Integer, Integer> uploadItems() throws Exception {
    final SoapSession soapSession = data.getSoapSession();

    int successful = 0;
    int failed = 0;
    final List<Item> items = data.getItems();
    for (int i = 0; i < items.size(); i++) {
      Item item = items.get(i);
      try {
        SwingUtilities.invokeLater(new UpdateItems(i));

        if (!soapSession.itemExists(item)) {
          final PropBagEx newItemXml = soapSession.createNewItem(item.getCollectionUuid());
          final String stagingId = newItemXml.getNode("item/staging");

          final PropBagEx xml = new PropBagEx(item.getXmlFile());
          xml.setNode("@itemdefid", item.getCollectionUuid());
          xml.setNode("/item/newitem", "true");

          final File attachments = item.getAttachmentsFolder();
          if (attachments != null) {
            xml.createNode("/item/staging", stagingId);

            final List<String> relpaths = FileUtil.getRelativeFileList(attachments.listFiles());
            for (String relpath : relpaths) {
              uploadFile(stagingId, relpath, attachments);
            }
          }
          soapSession.saveItem(xml);

          successful++;
        } else {
          throw new Exception(
              "Item " + item.getUuid() + "/" + item.getVersion() + " already exists");
        }
      } catch (Exception ex) {
        failed++;
        final int result =
            JOptionPane.showConfirmDialog(
                SwingUtilities.getWindowAncestor(this),
                "An error has occured uploading the item "
                    + item.getName()
                    + ":\n\n  "
                    + ex.getMessage()
                    + "\n\nDo you want to continue?\n",
                "Error",
                JOptionPane.YES_NO_OPTION);
        ex.printStackTrace();
        if (result != JOptionPane.YES_OPTION) {
          throw ex;
        }
      }
    }

    return new Pair<Integer, Integer>(successful, failed);
  }

  private void uploadFile(String staging, String relpath, File basepath)
      throws Exception, IOException {
    System.out.println("Uploading " + relpath);
    String filename = basepath.getAbsolutePath() + File.separatorChar + relpath;

    try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename))) {
      byte[] buffer = new byte[1048576]; // 1024 * 1024
      for (int bytes = in.read(buffer); bytes != -1; bytes = in.read(buffer)) {
        data.getSoapSession().appendFile(staging, relpath, buffer, bytes);
      }
    }
  }

  private final class UpdateItems implements Runnable {
    private final int index;

    public UpdateItems(int index) {
      this.index = index;
    }

    @Override
    public void run() {
      Item item = getData().getItems().get(index);
      itemName.setText(item.getName());
      itemCount.setText("Uploading " + (index + 1) + " of " + itemTotal + " Items");
    }
  }
}
