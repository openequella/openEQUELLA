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

package com.dytech.installer;

import com.dytech.gui.ComponentHelper;
import com.dytech.installer.gui.JPanelAA;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ProgressWindow extends JFrame implements Progress {
  private JProgressBar whole;
  private JProgressBar current;
  private JScrollPane scroller;
  private JTextArea messages;

  public ProgressWindow() {
    super();
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Progress#addMessage(java.lang.String)
   */
  @Override
  public void addMessage(String msg) {
    messages.append(msg + "\n");
    messages.setCaretPosition(messages.getText().length() - 1);
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Progress#setWholeAmount(int)
   */
  @Override
  public void setWholeAmount(int i) {
    whole.setValue(i);
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Progress#setCurrentAmount(int)
   */
  @Override
  public void setCurrentAmount(int i) {
    current.setValue(i);
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Progress#getWholeAmount()
   */
  @Override
  public int getWholeAmount() {
    return whole.getValue();
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Progress#getCurrentAmount()
   */
  @Override
  public int getCurrentAmount() {
    return current.getValue();
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Progress#setCurrentMaximum(int)
   */
  @Override
  public void setCurrentMaximum(int maximum) {
    current.setMaximum(maximum);
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Progress#getCurrentMaximum()
   */
  @Override
  public int getCurrentMaximum() {
    return current.getMaximum();
  }

  @Override
  public int getWholeMaximum() {
    return whole.getMaximum();
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Progress#setup(java.lang.String, int)
   */
  @Override
  public void setup(String title, int total) {
    JPanel all = new JPanel();
    all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
    all.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    all.add(createHeader());
    all.add(Box.createRigidArea(new Dimension(0, 10)));
    all.add(createWholeProgress(total));
    all.add(Box.createRigidArea(new Dimension(0, 10)));
    all.add(createCurrentProgress());
    all.add(Box.createRigidArea(new Dimension(0, 10)));
    all.add(createMessageArea());
    all.add(Box.createRigidArea(new Dimension(0, 10)));
    all.add(createButtons());

    getContentPane().add(all);
    setTitle(title);
    setSize(500, 500);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    ComponentHelper.centreOnScreen(this);
    setVisible(true);
  }

  private JPanelAA createHeader() {
    JLabel label = new JLabel("Installing...");
    label.setFont(new Font("Arial", Font.BOLD, 24));

    JPanelAA header = new JPanelAA();
    header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
    header.add(label);
    header.add(Box.createHorizontalGlue());

    return header;
  }

  private JProgressBar createWholeProgress(int total) {
    whole = new JProgressBar(0, total);
    whole.setValue(0);
    whole.setStringPainted(true);
    whole.setForeground(new Color(153, 153, 204));
    whole.setBackground(Color.white);

    return whole;
  }

  private JProgressBar createCurrentProgress() {
    current = new JProgressBar(0, 0);
    current.setValue(0);
    current.setStringPainted(true);
    current.setForeground(new Color(153, 153, 204));
    current.setBackground(Color.white);

    return current;
  }

  private JScrollPane createMessageArea() {
    messages = new JTextArea();
    messages.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    messages.setBackground(Color.black);
    messages.setForeground(Color.green);
    messages.setRows(10);
    messages.setLineWrap(true);

    scroller = new JScrollPane(messages);

    return scroller;
  }

  private JPanelAA createButtons() {
    JButton cancel = new JButton("Cancel");
    cancel.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            int result =
                JOptionPane.showConfirmDialog(
                    ProgressWindow.this,
                    "Are you sure you want to cancel the installation?",
                    "Are you sure?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
              dispose();
              System.exit(0);
            }
          }
        });

    JPanelAA buttons = new JPanelAA();
    buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
    buttons.add(Box.createHorizontalGlue());
    buttons.add(cancel);

    return buttons;
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Progress#popupMessage(java.lang.String,
   * java.lang.String, boolean)
   */
  @Override
  public void popupMessage(String title, String message, boolean error) {
    int type = JOptionPane.INFORMATION_MESSAGE;
    if (error) {
      type = JOptionPane.ERROR_MESSAGE;
    }

    JOptionPane.showMessageDialog(this, message, title, type);
  }
}
