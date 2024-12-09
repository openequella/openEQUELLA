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

package com.dytech.gui.calendar;

import com.dytech.gui.ComponentHelper;
import com.dytech.gui.TableLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

@SuppressWarnings("nls")
public class CalendarDialog extends JDialog implements ActionListener {
  private Date result;

  private JButton select;
  private JButton close;
  private JCalendar calendar;

  public CalendarDialog() throws HeadlessException {
    super();
    setup();
  }

  public CalendarDialog(Dialog owner) throws HeadlessException {
    super(owner);
    setup();
  }

  public CalendarDialog(Dialog owner, boolean modal) throws HeadlessException {
    super(owner, modal);
    setup();
  }

  public CalendarDialog(Frame owner) throws HeadlessException {
    super(owner);
    setup();
  }

  public CalendarDialog(Frame owner, boolean modal) throws HeadlessException {
    super(owner, modal);
    setup();
  }

  public CalendarDialog(Dialog owner, String title) throws HeadlessException {
    super(owner, title);
    setup();
  }

  public CalendarDialog(Dialog owner, String title, boolean modal) throws HeadlessException {
    super(owner, title, modal);
    setup();
  }

  public CalendarDialog(Frame owner, String title) throws HeadlessException {
    super(owner, title);
    setup();
  }

  public CalendarDialog(Frame owner, String title, boolean modal) throws HeadlessException {
    super(owner, title, modal);
    setup();
  }

  public CalendarDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc)
      throws HeadlessException {
    super(owner, title, modal, gc);
    setup();
  }

  public CalendarDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
    super(owner, title, modal, gc);
    setup();
  }

  private void setup() {
    calendar = new JCalendar();

    select = new JButton("Select Date");
    close = new JButton("Close");

    select.addActionListener(this);
    close.addActionListener(this);

    final int width1 = select.getPreferredSize().width;
    final int width2 = close.getPreferredSize().width;
    final int height = select.getPreferredSize().height;

    final int[] rows = {TableLayout.FILL, height};
    final int[] cols = {TableLayout.FILL, width1, width2};

    JPanel all = new JPanel(new TableLayout(rows, cols, 5, 5));
    all.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    all.add(calendar, new Rectangle(0, 0, 3, 1));
    all.add(select, new Rectangle(1, 1, 1, 1));
    all.add(close, new Rectangle(2, 1, 1, 1));

    getContentPane().add(all);
    setSize(450, 300);

    ComponentHelper.centreOnScreen(this);
  }

  public static Date showCalendarDialog(Component parentComponent, String title, Date initial) {
    if (parentComponent == null) {
      throw new IllegalArgumentException("parentComponent must not be null");
    }

    Window window = null;
    if (parentComponent instanceof Window) {
      window = (Window) parentComponent;
    } else {
      window = SwingUtilities.getWindowAncestor(parentComponent);
    }

    CalendarDialog dialog = null;
    if (window instanceof Frame) {
      dialog = new CalendarDialog((Frame) window, title, true);
    } else {
      dialog = new CalendarDialog((Dialog) window, title, true);
    }

    if (initial != null) {
      dialog.setDate(initial);
    }

    dialog.setVisible(true);

    return dialog.getDate();
  }

  public void setDate(Date d) {
    calendar.setDate(d);
  }

  public void setDate(int month, int year) {
    calendar.setDate(month, year);
  }

  public Date getDate() {
    return result;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == select) {
      Date selection = calendar.getDate();
      if (selection == null) {
        String message = "You have not selected a day. Do you really want continue?";
        String[] buttons = {"Close Window", "Do Not Close"};

        final int choice =
            JOptionPane.showOptionDialog(
                this,
                message,
                "No Day Selected",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                buttons,
                buttons[1]);

        if (choice == JOptionPane.YES_OPTION) {
          result = null;
          dispose();
        }
      } else {
        result = selection;
        dispose();
      }
    } else if (e.getSource() == close) {
      result = null;
      dispose();
    }
  }
}
