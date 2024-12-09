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

package com.dytech.gui.filter;

import com.dytech.gui.ComponentHelper;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

public class FilterDialog<T> implements ActionListener {
  private String title;
  private FilterList<T> list;
  private JButton okButton;
  private JButton cancelButton;

  private JPanel content;
  private JDialog dialog;
  private boolean okClicked;

  public static void main(String[] args) {
    FilterDialog<String> name =
        new FilterDialog<String>(
            "test",
            new FilterModel<String>() {
              @Override
              public List<String> search(String pattern) {
                return Collections.singletonList("sdf");
              }
            });
    name.showDialog(new JFrame());
  }

  public FilterDialog(String title, FilterModel<T> model) {
    this.title = title;

    setup(model);
  }

  private void setup(FilterModel<T> model) {
    list = new FilterList<T>(model);

    okButton = new JButton("OK");
    cancelButton = new JButton("Cancel");

    okButton.addActionListener(this);
    cancelButton.addActionListener(this);

    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttons.add(okButton);
    buttons.add(cancelButton);

    content = new JPanel(new BorderLayout(5, 5));
    content.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    content.add(list, BorderLayout.CENTER);
    content.add(buttons, BorderLayout.SOUTH);
  }

  public void setListCellRenderer(ListCellRenderer lcr) {
    list.setListCellRenderer(lcr);
  }

  public List<T> showDialog(Component parent) {
    dialog = ComponentHelper.createJDialog(parent);
    dialog.getContentPane().add(content);
    dialog.setSize(250, 400);
    dialog.setTitle(title);
    dialog.setModal(true);

    ComponentHelper.centreOnScreen(dialog);

    dialog.setVisible(true);

    if (okClicked) {
      return list.getSelectedValues();
    } else {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == okButton) {
      closeDialog(!list.isSelectionEmpty());
    } else if (e.getSource() == cancelButton) {
      closeDialog(false);
    }
  }

  private void closeDialog(boolean okClicked) {
    this.okClicked = okClicked;
    dialog.dispose();
    dialog = null;
  }
}
