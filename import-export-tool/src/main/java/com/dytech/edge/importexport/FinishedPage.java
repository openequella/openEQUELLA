package com.dytech.edge.importexport;

import com.dytech.edge.importexport.SharedData.Completion;
import com.dytech.gui.TableLayout;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JLabel;

public class FinishedPage extends WizardPage {
  private final JLabel title;
  private final JLabel text;

  public FinishedPage(SharedData data) {
    super(data);
    title = new JLabel("<html><h2>Finished");
    text = new JLabel("<html><b>Finished");

    final int height1 = title.getPreferredSize().height;
    // final int height2 = text.getPreferredSize().height;
    final int[] rows = new int[] {height1, TableLayout.FILL};
    final int[] cols = new int[] {TableLayout.FILL};

    setLayout(new TableLayout(rows, cols, 5, 5));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    add(title, new Rectangle(0, 0, 1, 1));
    add(text, new Rectangle(0, 1, 1, 1));
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
    Completion completion = data.getCompletion();
    if (completion != null) {
      text.setText("<html><b>" + completion.getMessage());
    } else {
      text.setText("<html><b>Finished");
    }
  }
}
