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

package com.dytech.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

public class ExceptionDialog extends JDialog implements ActionListener {
  private final DateFormat dateFormat = new SimpleDateFormat("H:mm 'on' EEEE d MMMM yyyy");

  protected String title;
  protected String message;
  protected String version;
  protected Throwable throwable;

  protected JButton okButton;

  public ExceptionDialog(String title, String message, String version, Throwable throwable) {
    setup(title, message, version, throwable);
  }

  public ExceptionDialog(
      Dialog dialog, String title, String message, String version, Throwable throwable) {
    super(dialog);
    setup(title, message, version, throwable);
  }

  public ExceptionDialog(
      Frame frame, String title, String message, String version, Throwable throwable) {
    super(frame);
    setup(title, message, version, throwable);
  }

  private void setup(String title, String message, String version, Throwable throwable) {
    this.title = title;
    this.message = message;
    this.version = version;
    this.throwable = throwable;

    createGUI();
  }

  private void createGUI() {
    JPanel all = new JPanel(new BorderLayout(5, 5));

    JTabbedPane tabs = new JTabbedPane();

    tabs.addTab("Error", createNiceMessages());
    tabs.addTab("Details", createExpertArea());

    all.add(tabs, BorderLayout.CENTER);
    all.add(createButtons(), BorderLayout.SOUTH);

    setModal(true);
    setSize(400, 300);
    getContentPane().add(all);
    ComponentHelper.centreOnScreen(this);
  }

  private JComponent createNiceMessages() {
    JLabel heading = new JLabel("<html><font size=+1>" + title + "</font></html>");

    JTextArea body = new JTextArea(message);
    body.setHighlighter(null);
    body.setEditable(false);
    body.setFocusable(false);
    body.setOpaque(false);
    body.setBorder(null);
    body.setLineWrap(true);
    body.setWrapStyleWord(true);
    body.setFont(heading.getFont());

    final int[] rows = new int[] {heading.getPreferredSize().height, TableLayout.FILL};
    final int[] columns = new int[] {TableLayout.FILL};

    TableLayout layout = new TableLayout(rows, columns, 5, 5);
    JPanel all = new JPanel(layout);
    all.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    all.add(heading, new Rectangle(0, 0, 1, 1));
    all.add(body, new Rectangle(0, 1, 1, 1));

    return all;
  }

  private JComponent createButtons() {
    okButton = new JButton("OK");
    okButton.addActionListener(this);
    getRootPane().setDefaultButton(okButton);

    JPanel all = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    all.add(okButton);

    all.setPreferredSize(all.getMinimumSize());

    return all;
  }

  private JComponent createExpertArea() {
    JLabel nameLabel = new JLabel("Exception:");
    JLabel causeLabel = new JLabel("Cause:");
    JLabel dateLabel = new JLabel("Date:");
    JLabel versionLabel = new JLabel("Version:");

    String cause = throwable.getCause() == null ? "<none>" : throwable.getCause().toString();

    JLabel nameValue = new JLabel(throwable.toString());
    JLabel causeValue = new JLabel(cause);
    JLabel dateValue = new JLabel(dateFormat.format(new Date()));
    JLabel versionValue = new JLabel(version);

    JTextArea trace = new JTextArea();
    populateStackTrace(trace, throwable);

    JScrollPane scroll = new JScrollPane(trace);

    final int width = nameLabel.getPreferredSize().width;
    final int height = nameLabel.getPreferredSize().height;

    final int[] rows = new int[] {height, height, height, height, TableLayout.FILL};
    final int[] columns = new int[] {width, TableLayout.FILL};

    TableLayout layout = new TableLayout(rows, columns, 5, 5);
    JPanel all = new JPanel(layout);
    all.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    all.add(nameLabel, new Rectangle(0, 0, 1, 1));
    all.add(nameValue, new Rectangle(1, 0, 1, 1));

    all.add(causeLabel, new Rectangle(0, 1, 1, 1));
    all.add(causeValue, new Rectangle(1, 1, 1, 1));

    all.add(dateLabel, new Rectangle(0, 2, 1, 1));
    all.add(dateValue, new Rectangle(1, 2, 1, 1));

    all.add(versionLabel, new Rectangle(0, 3, 1, 1));
    all.add(versionValue, new Rectangle(1, 3, 1, 1));

    all.add(scroll, new Rectangle(0, 4, 2, 1));

    return all;
  }

  public void populateStackTrace(JTextArea view, Throwable t) {
    view.append(t.toString() + '\n');

    StackTraceElement[] trace = t.getStackTrace();
    for (int i = 0; i < trace.length; i++) {
      view.append("\tat " + trace[i] + '\n');
    }

    Throwable ourCause = t.getCause();
    if (ourCause != null) {
      populateStackTraceAsCause(view, ourCause, trace);
    }
  }

  private void populateStackTraceAsCause(
      JTextArea view, Throwable t, StackTraceElement[] causedTrace) {
    StackTraceElement[] trace = t.getStackTrace();
    int m = trace.length - 1;
    int n = causedTrace.length - 1;

    while (m >= 0 && n >= 0 && trace[m].equals(causedTrace[n])) {
      m--;
      n--;
    }
    int framesInCommon = trace.length - 1 - m;

    view.append("Caused by: " + t.toString() + '\n');
    for (int i = 0; i <= m; i++) {
      view.append("\tat " + trace[i] + '\n');
    }

    if (framesInCommon != 0) {
      view.append("\t... " + framesInCommon + " more\n");
    }

    // Recurse if we have a cause
    Throwable ourCause = t.getCause();
    if (ourCause != null) {
      populateStackTraceAsCause(view, ourCause, trace);
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == okButton) {
      dispose();
    }
  }
}
