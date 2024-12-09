package com.dytech.edge.importexport.importutil;

import com.dytech.edge.importexport.SharedData;
import com.dytech.edge.importexport.WizardPage;
import com.dytech.gui.TableLayout;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JLabel;

@SuppressWarnings("nls")
public class ReviewPage extends WizardPage {
  private JLabel items;

  public ReviewPage(SharedData data) {
    super(data);
    setup();
  }

  private void setup() {
    JLabel heading = new JLabel("<html><h2>Content Review");
    JLabel help1 = new JLabel("<html><b>The following content was found for upload:");

    JLabel help2 = new JLabel("<html><b>Select 'Next' to upload the content.");
    items = new JLabel();

    final int height1 = heading.getPreferredSize().height;
    final int height2 = help1.getPreferredSize().height;
    final int width = 10;

    final int[] rows = new int[] {height1, height2, height2, height2, height2, TableLayout.FILL};
    final int[] cols = new int[] {width, TableLayout.FILL};

    setLayout(new TableLayout(rows, cols, 5, 5));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    add(heading, new Rectangle(0, 0, 2, 1));
    add(help1, new Rectangle(0, 1, 2, 1));

    add(items, new Rectangle(1, 2, 1, 1));

    add(help2, new Rectangle(0, 4, 2, 1));
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
    items.setText("- " + data.getItems().size() + " Item(s)");
    // We don't care if we are shown.
  }
}
