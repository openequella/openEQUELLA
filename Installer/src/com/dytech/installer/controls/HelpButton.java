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

import com.dytech.devlib.PropBagEx;
import com.dytech.gui.ComponentHelper;
import com.dytech.gui.TableLayout;
import com.dytech.installer.InstallerException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public class HelpButton extends GuiControl implements ActionListener {
  private static final int DEFAULT_WINDOW_WIDTH = 400;
  private static final int DEFAULT_WINDOW_HEIGHT = 300;

  private JButton button;
  private Dimension windowSize;

  public HelpButton(PropBagEx controlBag) throws InstallerException {
    super(controlBag);

    button = new JButton(controlBag.getNode("button"));
    button.addActionListener(this);

    int width = controlBag.getIntNode("window/@width", DEFAULT_WINDOW_WIDTH);
    int height = controlBag.getIntNode("window/@height", DEFAULT_WINDOW_HEIGHT);
    windowSize = new Dimension(width, height);
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.controls.GuiControl#getSelection()
   */
  @Override
  public String getSelection() {
    return new String();
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.controls.GuiControl#generateControl()
   */
  @Override
  public JComponent generateControl() {
    return null;
  }

  /*
   * (non-Javadoc)
   * @see
   * com.dytech.installer.controls.GuiControl#generate(javax.swing.JPanel)
   */
  @Override
  public void generate(JPanel panel) {
    JPanel all = new JPanel(new FlowLayout(FlowLayout.LEFT));
    all.add(button);
    panel.add(all);
  }

  /*
   * (non-Javadoc)
   * @see
   * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == button) {
      JLabel titleLabel = new JLabel("<html><h3>" + title);

      JLabel descriptionLabel = new JLabel("<html>" + description);
      descriptionLabel.setBackground(Color.WHITE);
      descriptionLabel.setOpaque(true);
      descriptionLabel.setVerticalAlignment(SwingConstants.TOP);
      descriptionLabel.setBorder(
          BorderFactory.createCompoundBorder(
              BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

      JButton close = new JButton("Close");

      final int height1 = titleLabel.getPreferredSize().height;
      final int height2 = close.getPreferredSize().height;
      final int width1 = close.getPreferredSize().width;

      final int[] rows = {height1, TableLayout.FILL, height2};
      final int[] cols = {TableLayout.FILL, width1};

      JPanel all = new JPanel(new TableLayout(rows, cols));
      all.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      all.add(titleLabel, new Rectangle(0, 0, 2, 1));
      all.add(descriptionLabel, new Rectangle(0, 1, 2, 1));
      all.add(close, new Rectangle(1, 2, 1, 1));

      final JDialog dialog = ComponentHelper.createJDialog(button);
      dialog.setTitle(title);
      dialog.setModal(true);
      dialog.setSize(windowSize);
      dialog.setContentPane(all);
      dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      ComponentHelper.centreOnScreen(dialog);

      close.addActionListener(
          new ActionListener() {
            /*
             * (non-Javadoc)
             * @see
             * java.awt.event.ActionListener#actionPerformed(java.awt.event
             * .ActionEvent)
             */
            @Override
            public void actionPerformed(ActionEvent e) {
              dialog.dispose();
            }
          });

      dialog.setVisible(true);
    }
  }
}
