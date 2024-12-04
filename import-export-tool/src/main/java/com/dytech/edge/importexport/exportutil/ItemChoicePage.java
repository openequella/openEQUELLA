package com.dytech.edge.importexport.exportutil;

import com.dytech.edge.importexport.SharedData;
import com.dytech.edge.importexport.SoapSession;
import com.dytech.edge.importexport.WizardPage;
import com.dytech.edge.importexport.icons.Icons;
import com.dytech.edge.importexport.types.ItemDef;
import com.dytech.gui.TableLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

@SuppressWarnings("nls")
public class ItemChoicePage extends WizardPage implements ActionListener {
  protected JLabel errorLabel;
  protected ImageIcon errorIcon;

  private JRadioButton itemDefButton;
  private JRadioButton searchButton;
  private JRadioButton ownerItemButton;
  private JRadioButton allItemsDefButton;

  private JComboBox itemDefs;
  private JTextField searchQuery;

  public ItemChoicePage(SharedData data) {
    super(data);
    setup();
  }

  private void setup() {
    errorLabel = new JLabel();
    errorIcon = Icons.getErrorIcon();

    JLabel heading = new JLabel("<html><h2>Item Selection");
    JLabel help1 = new JLabel("<html><b>Select which items to export:");

    ownerItemButton = new JRadioButton("Items that I own", true);
    allItemsDefButton = new JRadioButton("All items on the server");
    itemDefButton = new JRadioButton("Items of the following type:");
    searchButton = new JRadioButton("Items matching the search query:");

    ButtonGroup exportGroup = new ButtonGroup();
    exportGroup.add(ownerItemButton);
    exportGroup.add(allItemsDefButton);
    exportGroup.add(itemDefButton);
    exportGroup.add(searchButton);

    ownerItemButton.addActionListener(this);
    allItemsDefButton.addActionListener(this);
    itemDefButton.addActionListener(this);
    searchButton.addActionListener(this);

    searchQuery = new JTextField();
    itemDefs = new JComboBox();

    final int height1 = heading.getPreferredSize().height;
    final int height2 = help1.getPreferredSize().height;
    final int height3 = itemDefs.getPreferredSize().height;
    final int width = 10;

    final int[] rows =
        new int[] {
          height1, height2, height2, height2, height2, height3, height2, height3, TableLayout.FILL
        };
    final int[] cols = new int[] {width, width, TableLayout.FILL};

    setLayout(new TableLayout(rows, cols, 5, 5));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    add(heading, new Rectangle(0, 0, 3, 1));
    add(help1, new Rectangle(0, 1, 3, 1));

    add(ownerItemButton, new Rectangle(1, 2, 2, 1));
    add(allItemsDefButton, new Rectangle(1, 3, 2, 1));
    add(itemDefButton, new Rectangle(1, 4, 2, 1));
    add(itemDefs, new Rectangle(2, 5, 1, 1));
    add(searchButton, new Rectangle(1, 6, 2, 1));
    add(searchQuery, new Rectangle(2, 7, 1, 1));

    updateSelection();
  }

  private void displayError(String error) {
    if (error == null) {
      errorLabel.setIcon(null);
      errorLabel.setText("");
    } else {
      errorLabel.setIcon(errorIcon);
      errorLabel.setText(error);
    }
  }

  private void updateSelection() {
    itemDefs.setEnabled(itemDefButton.isSelected());
    searchQuery.setEnabled(searchButton.isSelected());
  }

  @Override
  public boolean onNext() {
    try {
      final SoapSession soapSession = data.getSoapSession();
      if (ownerItemButton.isSelected()) {
        data.setItems(soapSession.enumerateItemsForOwner());
      } else if (allItemsDefButton.isSelected()) {
        data.setItems(soapSession.enumerateAllItems());
      } else if (itemDefButton.isSelected()) {
        ItemDef idef = (ItemDef) itemDefs.getSelectedItem();
        data.setItems(soapSession.enumerateItemsForItemDef(idef));
      } else if (searchButton.isSelected()) {
        data.setItems(
            soapSession.enumerateItemsForQuery(searchQuery.getText(), data.getItemDefs()));
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      displayError("Error retrieving items.  Please try again.");
      return false;
    }
    return true;
  }

  @Override
  public boolean onBack() {
    return true;
  }

  @Override
  public void onShow() {
    displayError(null);
    itemDefs.removeAllItems();
    for (ItemDef collection : data.getItemDefs()) {
      itemDefs.addItem(collection);
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    updateSelection();
  }
}
