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

import com.dytech.gui.ComponentHelper;
import com.dytech.gui.TableLayout;
import com.tle.common.i18n.CurrentLocale;
import java.awt.Dialog;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("nls")
public class NetworkDetails extends JDialog implements ActionListener {
  public static final int RESULT_OK = 0;
  public static final int RESULT_CANCEL = 1;

  private int result = RESULT_CANCEL;

  private JTextField name;
  private JTextField min;
  private JTextField max;
  private JButton ok;
  private JButton cancel;

  public NetworkDetails(Dialog owner) {
    super(owner);
    setup();
  }

  public int getResult() {
    return result;
  }

  public Network getNetwork() {
    Network n = new Network();
    n.setName(name.getText());
    n.setMin(min.getText());
    n.setMax(max.getText());
    return n;
  }

  private void setup() {
    JLabel nameLabel =
        new JLabel(CurrentLocale.get("com.dytech.edge.admin.helper.networkdetails.rangename"));
    JLabel minLabel =
        new JLabel(CurrentLocale.get("com.dytech.edge.admin.helper.networkdetails.startaddress"));
    JLabel maxLabel =
        new JLabel(CurrentLocale.get("com.dytech.edge.admin.helper.networkdetails.endaddress"));

    name = new JTextField();
    min = new JTextField();
    max = new JTextField();

    ok = new JButton(CurrentLocale.get("com.dytech.edge.admin.helper.ok"));
    cancel = new JButton(CurrentLocale.get("com.dytech.edge.admin.helper.cancel"));

    ok.addActionListener(this);
    cancel.addActionListener(this);

    final int height1 = name.getPreferredSize().height;
    final int height2 = ok.getPreferredSize().height;
    final int width1 = minLabel.getPreferredSize().width;
    final int width2 = cancel.getPreferredSize().width;

    final int[] rows = {height1, height1, height1, TableLayout.FILL, height2};
    final int[] cols = {width1, TableLayout.FILL, width2, width2};

    JPanel all = new JPanel(new TableLayout(rows, cols, 5, 5));
    all.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    all.add(nameLabel, new Rectangle(0, 0, 1, 1));
    all.add(name, new Rectangle(1, 0, 3, 1));

    all.add(minLabel, new Rectangle(0, 1, 1, 1));
    all.add(min, new Rectangle(1, 1, 3, 1));

    all.add(maxLabel, new Rectangle(0, 2, 1, 1));
    all.add(max, new Rectangle(1, 2, 3, 1));

    all.add(ok, new Rectangle(2, 4, 1, 1));
    all.add(cancel, new Rectangle(3, 4, 1, 1));

    setModal(true);
    setSize(350, 170);
    setTitle(CurrentLocale.get("com.dytech.edge.admin.helper.networkdetails.add"));
    getContentPane().add(all);

    ComponentHelper.centreOnScreen(this);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == ok) {
      // if the JTextField is not empty
      if (!name.getText().trim().isEmpty()) {
        result = RESULT_OK;
        dispose();
      } else
      // the JTextField is empty, then pop-up message and the JTextfield
      // gets focus.
      {
        JOptionPane.showMessageDialog(
            ok, CurrentLocale.get("com.dytech.edge.admin.helper.networkdetails.entername"));
        name.requestFocus();
      }
    } else if (e.getSource() == cancel) {
      result = RESULT_CANCEL;
      dispose();
    }
  }
}
