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

import com.dytech.gui.TableLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class JCalendar extends JComponent implements ActionListener, MouseListener {
  @SuppressWarnings("nls")
  private static final String[] DAYS = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

  @SuppressWarnings("nls")
  private static final String[] MONTHS = {
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December"
  };

  private static final int YEAR_END = 2100;

  private static final int YEAR_START = 1900;

  private final int[] dayCounts = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
  private JLabel dateDisplay;
  private DayLabel[][] days;
  private JComboBox months;
  private JComboBox years;
  private JPanel dayGrid;

  private DayLabel currentSelection;

  public JCalendar() {
    // Start with todays date
    this(getCalendar().getTime());
  }

  public JCalendar(Date d) {
    setup();
    setDate(d);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == months || e.getSource() == years) {
      update();
    }
  }

  private JComponent createCentre() {
    dayGrid = new JPanel(new GridLayout(7, 7, 5, 5));
    dayGrid.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    dayGrid.setBackground(Color.WHITE);
    dayGrid.addMouseListener(this);

    Map<TextAttribute, Float> dayAttributes = new HashMap<TextAttribute, Float>();
    dayAttributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
    Font dayFont = new Font(dayAttributes);

    for (int i = 0; i < DAYS.length; i++) {
      JLabel day = new JLabel(DAYS[i]);
      day.setHorizontalAlignment(SwingConstants.CENTER);
      day.setVerticalAlignment(SwingConstants.CENTER);
      day.setFont(dayFont);

      dayGrid.add(day);
    }

    days = new DayLabel[6][7];
    for (int i = 0; i < days.length; i++) {
      for (int j = 0; j < days[i].length; j++) {
        days[i][j] = new DayLabel();
        days[i][j].setDay(0);
        dayGrid.add(days[i][j]);
      }
    }

    return dayGrid;
  }

  private void createGUI() {
    JComponent north = createNorth();
    JComponent centre = createCentre();

    setLayout(new BorderLayout(5, 5));
    add(north, BorderLayout.NORTH);
    add(centre, BorderLayout.CENTER);
  }

  private JComponent createNorth() {
    Map<TextAttribute, Float> fontAttributes = new HashMap<TextAttribute, Float>();
    fontAttributes.put(TextAttribute.SIZE, 30f);
    fontAttributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);

    dateDisplay = new JLabel();
    dateDisplay.setVerticalAlignment(SwingConstants.BOTTOM);
    dateDisplay.setFont(new Font(fontAttributes));
    dateDisplay.setForeground(Color.BLUE);

    months = new JComboBox(MONTHS);
    years = new JComboBox();

    for (int i = YEAR_START; i <= YEAR_END; i++) {
      years.addItem(Integer.valueOf(i));
    }

    months.addActionListener(this);
    years.addActionListener(this);

    final int height1 = months.getPreferredSize().height;
    final int width1 = months.getPreferredSize().width;

    final int[] rows = {height1, height1};
    final int[] cols = {TableLayout.FILL, width1};

    JPanel all = new JPanel(new TableLayout(rows, cols, 5, 5));
    all.add(dateDisplay, new Rectangle(0, 0, 1, 2));
    all.add(months, new Rectangle(1, 0, 1, 1));
    all.add(years, new Rectangle(1, 1, 1, 1));

    return all;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    Point impact = e.getPoint();
    Component c = dayGrid.getComponentAt(impact);

    if (c instanceof DayLabel) {
      DayLabel newSelection = (DayLabel) c;
      if (newSelection.isEnabled()) {
        if (currentSelection != null) {
          currentSelection.setSelected(false);
        }
        currentSelection = newSelection;
        newSelection.setSelected(true);
      }
    }
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    // We don't care about this event
  }

  @Override
  public void mouseExited(MouseEvent e) {
    // We don't care about this event
  }

  @Override
  public void mousePressed(MouseEvent e) {
    // We don't care about this event
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    // We don't care about this event
  }

  public Date getDate() {
    Date result = null;
    if (currentSelection != null) {
      Calendar c = getCalendar();
      c.set(Calendar.YEAR, years.getSelectedIndex() + YEAR_START);
      c.set(Calendar.MONTH, months.getSelectedIndex());
      c.set(Calendar.DAY_OF_MONTH, currentSelection.getDay());
      c.set(Calendar.HOUR, 0);
      c.set(Calendar.MINUTE, 0);
      c.set(Calendar.SECOND, 0);
      c.set(Calendar.MILLISECOND, 0);

      result = c.getTime();
    }
    return result;
  }

  public void setDate(Date date) {
    Calendar c = getCalendar();
    c.setTime(date);
    setDate(c.get(Calendar.MONTH), c.get(Calendar.YEAR));
  }

  public void setDate(int month, int year) {
    if (month < 0 || month > MONTHS.length) {
      throw new IllegalArgumentException(
          "Month must be between 0 and " + MONTHS.length); // $NON-NLS-1$
    }

    if (year < YEAR_START || year > YEAR_END) {
      throw new IllegalArgumentException(
          "Year must be between " + YEAR_START + " and " + YEAR_END); // $NON-NLS-1$ //$NON-NLS-2$
    }

    months.removeActionListener(this);
    years.removeActionListener(this);

    months.setSelectedIndex(month);
    years.setSelectedIndex(year - YEAR_START);

    months.addActionListener(this);
    years.addActionListener(this);

    update();
  }

  public void setSelectionColor(Color c) {
    dateDisplay.setForeground(c);
  }

  private void setup() {
    createGUI();
  }

  private void update() {
    currentSelection = null;

    StringBuilder buffer = new StringBuilder(20);
    buffer.append(months.getSelectedItem());
    buffer.append(' ');
    buffer.append(years.getSelectedItem());
    dateDisplay.setText(buffer.toString());

    int year = years.getSelectedIndex() + YEAR_START;
    int month = months.getSelectedIndex();

    Calendar firstDay = getCalendar();
    firstDay.set(Calendar.YEAR, year);
    firstDay.set(Calendar.MONTH, month);
    firstDay.set(Calendar.DAY_OF_MONTH, 1);

    int startDay = firstDay.get(Calendar.DAY_OF_WEEK) - 1;

    if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
      dayCounts[1] = 29;
    } else {
      dayCounts[1] = 28;
    }

    int row = 0;
    int column = 0;
    int lastMonth = month - 1;
    if (lastMonth == -1) {
      lastMonth = 11;
    }

    while (column < startDay) {
      int day = dayCounts[lastMonth] - startDay + column + 1;
      days[row][column].setDay(day);
      days[row][column].setEnabled(false);
      days[row][column].setSelected(false);
      column++;
    }

    for (int day = 1; day <= dayCounts[month]; day++, column++) {
      days[row][column].setDay(day);
      days[row][column].setEnabled(true);
      days[row][column].setSelected(false);

      if (column == days[row].length - 1) {
        row++;
        column = -1;
      }
    }

    int remainingDays = 1;
    while (column > 0 || row < days.length) {
      while (column < days[row].length) {
        days[row][column].setDay(remainingDays);
        days[row][column].setEnabled(false);
        days[row][column].setSelected(false);

        remainingDays++;
        column++;
      }
      column = 0;
      row++;
    }
  }

  private static Calendar getCalendar() {
    return Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
  }
}

class DayLabel extends JLabel {
  private boolean selected;
  private int day;
  private final JLabel label;

  private final Border selectedBorder = BorderFactory.createLineBorder(Color.BLACK);
  private final Color selectedBackground = new Color(245, 245, 190);

  private final Border normalBorder = null;
  private final Color normalBackground = Color.WHITE;
  private final Color enabledForeground = Color.BLACK;
  private final Color disabledForeground = Color.LIGHT_GRAY;

  public DayLabel() {
    label = new JLabel();
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setVerticalAlignment(SwingConstants.CENTER);
    label.setOpaque(true);

    setLayout(new GridLayout(1, 1, 0, 0));
    add(label);

    update();
  }

  public int getDay() {
    return day;
  }

  public void setDay(int day) {
    this.day = day;
    label.setText(Integer.toString(day));
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    update();
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
    update();
  }

  private void update() {
    if (selected) {
      label.setBorder(selectedBorder);
      label.setBackground(selectedBackground);
    } else {
      label.setBorder(normalBorder);
      label.setBackground(normalBackground);
      if (isEnabled()) {
        label.setForeground(enabledForeground);
      } else {
        label.setForeground(disabledForeground);
      }
    }
  }
}
