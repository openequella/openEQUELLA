package com.dytech.edge.importexport.exportutil;

import com.dytech.edge.importexport.ErrorDialog;
import com.dytech.edge.importexport.SharedData;
import com.dytech.edge.importexport.SharedData.Completion;
import com.dytech.edge.importexport.WizardPage;
import com.dytech.edge.importexport.exportutil.filters.ItemDownloader;
import com.dytech.edge.importexport.exportutil.filters.ItemNotification;
import com.dytech.edge.importexport.exportutil.filters.NativeXmlSave;
import com.dytech.edge.importexport.exportutil.filters.XsltTransformer;
import com.dytech.edge.importexport.types.Item;
import com.dytech.gui.TableLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

@SuppressWarnings("nls")
public class DownloadPage extends WizardPage implements ItemNotification {
  protected int itemTotal;
  protected JLabel itemCount;
  protected JLabel itemName;

  public DownloadPage(SharedData data) {
    super(data);
    setup();
  }

  private void setup() {
    JLabel heading = new JLabel("<html><h2>Downloading Items...");

    itemCount = new JLabel("Downloading 0 of " + itemTotal + " Items");
    itemName = new JLabel();

    itemName.setForeground(Color.GRAY);

    final int height1 = heading.getPreferredSize().height;
    final int height2 = itemCount.getPreferredSize().height;

    final int[] rows = new int[] {height1, height2, height2, TableLayout.FILL};
    final int[] cols = new int[] {TableLayout.FILL};

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
    itemTotal = data.getItems().size();

    ItemDownloader warehouse = new ItemDownloader(data, this, this);

    List<Thread> threads = new ArrayList<Thread>();
    threads.add(new Thread(warehouse));

    if (data.getSaveAs() == SharedData.SAVE_IN_NATIVE) {
      threads.add(new Thread(new NativeXmlSave(data, warehouse, this)));
    } else if (data.getSaveAs() == SharedData.SAVE_APPLYING_XSLT) {
      threads.add(new Thread(new XsltTransformer(data, warehouse, this)));
    }
    // else if( data.getSaveAs() == SharedData.SAVE_AS_IMS )
    // {
    // // Nothing to do here
    // }

    try {
      for (Thread t : threads) {
        t.start();
      }

      for (Thread t : threads) {
        t.join();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      ErrorDialog error = new ErrorDialog((Dialog) null, "Error downloading items", ex);
    }

    // FIXME: add a count of items downloaded successfully and not
    data.setCompletion(new Completion(0, 0, "Downloads completed"));
    frame.goNext();
  }

  @Override
  public void downloadingItem(int i) {
    SwingUtilities.invokeLater(new UpdateItems(i));
  }

  private final class UpdateItems implements Runnable {
    private final int index;

    public UpdateItems(int index) {
      this.index = index;
    }

    @Override
    public void run() {
      Item item = getData().getItems().get(index - 1);
      itemName.setText(item.getName());
      itemCount.setText("Downloading " + (index) + " of " + itemTotal + " Items");
    }
  }
}
