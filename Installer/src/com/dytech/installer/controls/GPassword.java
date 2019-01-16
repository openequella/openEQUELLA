/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dytech.installer.controls;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.WindowConstants;

import com.dytech.devlib.PropBagEx;
import com.dytech.gui.ComponentHelper;
import com.dytech.gui.TableLayout;
import com.dytech.installer.InstallerException;
import com.dytech.installer.Item;

public class GPassword extends GuiControl {
  protected JPasswordField field;

  public GPassword(PropBagEx controlBag) throws InstallerException {
    super(controlBag);
  }

  @Override
  public String getSelection() {
    return new String(field.getPassword());
  }

  @Override
  public JComponent generateControl() {
    field = new JPasswordField();
    field.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
    field.addFocusListener(
        new FocusListener() {
          private String lastPassword;

          @Override
          public void focusGained(FocusEvent e) {
            lastPassword = getSelection();
          }

          @Override
          public void focusLost(FocusEvent e) {
            String newPassword = getSelection();
            if (!newPassword.isEmpty() && !newPassword.equals(lastPassword)) {
              if (!ConfirmPassword.confirm(field)) {
                field.setText(""); // $NON-NLS-1$
              }
            }
          }
        });

    if (items.size() >= 1) {
      field.setText(((Item) items.get(0)).getValue());
    }

    return field;
  }

  @Override
  public void saveToTargets(PropBagEx outputBag) {
    String value = getSelection();
    Iterator i = targets.iterator();
    while (i.hasNext()) {
      String target = (String) i.next();
      outputBag.setNode(target, value);
    }
  }

  private static class ConfirmPassword {
    private boolean confirmed = false;

    private ConfirmPassword(final JPasswordField fieldToConfirm) {
      final String newPassword = new String(fieldToConfirm.getPassword());

      final JDialog d = ComponentHelper.createJDialog(fieldToConfirm);
      final JPasswordField confirm = new JPasswordField();

      final JButton cancel = new JButton("Cancel");
      cancel.addActionListener(
          new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              d.dispose();
            }
          });

      final JButton ok = new JButton("Confirmed");
      ok.addActionListener(
          new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              confirmed = true;
              d.dispose();
            }
          });

      ok.setEnabled(false);

      confirm.addKeyListener(
          new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
              ok.setEnabled(new String(confirm.getPassword()).equals(newPassword));
            }
          });

      final int width1 = ok.getPreferredSize().width;
      final int[] rows = {
        TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED,
      };
      final int[] cols = {
        100, width1, width1,
      };

      final JPanel content = new JPanel(new TableLayout(rows, cols));
      content.add(new JLabel("Please confirm this password:"), new Rectangle(0, 0, 3, 1));
      content.add(confirm, new Rectangle(0, 1, 3, 1));
      content.add(ok, new Rectangle(1, 2, 1, 1));
      content.add(cancel, new Rectangle(2, 2, 1, 1));

      d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      d.setTitle("Confirm Password");
      d.setContentPane(content);
      d.setModal(true);
      d.pack();

      ComponentHelper.centreOnScreen(d);

      d.setVisible(true);
    }

    public boolean isConfirmed() {
      return confirmed;
    }

    public static boolean confirm(JPasswordField fieldToConfirm) {
      return new ConfirmPassword(fieldToConfirm).isConfirmed();
    }
  }
}
