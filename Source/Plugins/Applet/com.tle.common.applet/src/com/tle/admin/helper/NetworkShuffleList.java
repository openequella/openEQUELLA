/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.admin.helper;

import com.dytech.gui.TableLayout;
import com.tle.common.i18n.CurrentLocale;
import java.awt.Dialog;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("nls")
public class NetworkShuffleList extends JComponent
    implements ActionListener, ListSelectionListener {
  protected JButton add;
  protected JButton remove;
  protected JList list;
  protected DefaultListModel model;
  protected JScrollPane listScroll;

  public NetworkShuffleList() {
    setup();
  }

  @Override
  public void setEnabled(boolean b) {
    super.setEnabled(b);
    refreshButtons();
  }

  public Network getNetworkAt(int index) {
    return (Network) model.getElementAt(index);
  }

  public int getNetworkCount() {
    return model.getSize();
  }

  public void removeAllNetworks() {
    model.clear();
  }

  public void addNetwork(Network network) {
    model.addElement(network);
  }

  private void setup() {
    add = new JButton(CurrentLocale.get("com.dytech.edge.admin.helper.networkshufflelist.add"));
    remove =
        new JButton(CurrentLocale.get("com.dytech.edge.admin.helper.networkshufflelist.remove"));

    add.addActionListener(this);
    remove.addActionListener(this);

    model = new DefaultListModel();
    list = new JList(model);
    list.addListSelectionListener(this);

    listScroll = new JScrollPane(list);

    final int height1 = add.getPreferredSize().height;
    final int width1 = add.getPreferredSize().width;

    final int[] rows = new int[] {height1, height1, TableLayout.FILL};
    final int[] columns = new int[] {width1, TableLayout.FILL};

    TableLayout layout = new TableLayout(rows, columns, 5, 5);
    setLayout(layout);

    add(add, new Rectangle(0, 0, 1, 1));
    add(remove, new Rectangle(0, 1, 1, 1));
    add(listScroll, new Rectangle(1, 0, 1, 3));

    refreshButtons();
  }

  private void refreshButtons() {
    boolean enabled = super.isEnabled();
    boolean selection = !list.isSelectionEmpty();

    list.setEnabled(enabled);
    listScroll.setEnabled(enabled);
    add.setEnabled(enabled);
    remove.setEnabled(enabled && selection);
  }

  // // EVENT HANDLERS //////////////////////////////////////////////////////

  @Override
  public void valueChanged(ListSelectionEvent e) {
    refreshButtons();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == add) {
      Window w = SwingUtilities.getWindowAncestor(this);
      NetworkDetails nd = new NetworkDetails((Dialog) w);

      nd.setVisible(true);

      if (nd.getResult() == NetworkDetails.RESULT_OK) {
        model.addElement(nd.getNetwork());
        list.setSelectedIndex(model.getSize() - 1);
      }
    } else if (e.getSource() == remove) {
      int[] is = list.getSelectedIndices();
      for (int i = is.length - 1; i >= 0; i--) {
        model.removeElementAt(is[i]);
      }
    }
  }
}
