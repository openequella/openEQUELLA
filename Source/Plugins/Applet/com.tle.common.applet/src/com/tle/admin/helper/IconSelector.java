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

package com.tle.admin.helper;

import com.dytech.gui.ComponentHelper;
import com.dytech.gui.TableLayout;
import com.tle.common.i18n.CurrentLocale;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Created Jan 14, 2004
 *
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class IconSelector implements ActionListener, ListSelectionListener {
  private static final String WINDOW_TITLE =
      CurrentLocale.get("com.dytech.edge.admin.helper.iconselector.title");
  private static final int WINDOW_WIDTH = 300;
  private static final int WINDOW_HEIGHT = 300;

  private String selectedIcon = null;

  private Component parent;
  private JDialog dialog;

  private JPanel content;
  private IconPane icons;
  private JButton ok;
  private JButton cancel;

  public IconSelector(Component parent, IconContainer holder) {
    this.parent = parent;
    setup(holder);
  }

  private void setup(IconContainer holder) {
    createGUI(holder);
  }

  private void createGUI(IconContainer holder) {
    ok = new JButton(CurrentLocale.get("com.dytech.edge.admin.helper.ok"));
    cancel = new JButton(CurrentLocale.get("com.dytech.edge.admin.helper.cancel"));

    ok.addActionListener(this);
    cancel.addActionListener(this);

    ok.setEnabled(false);

    icons = new IconPane(holder);
    icons.setSelectionListener(this);

    JScrollPane iconScroll = new JScrollPane(icons);
    iconScroll.getViewport().setBackground(Color.WHITE);

    final int width = cancel.getPreferredSize().width;
    final int height = cancel.getPreferredSize().height;

    final int[] rows = new int[] {TableLayout.FILL, height};
    final int[] cols = new int[] {TableLayout.FILL, width, width};

    content = new JPanel(new TableLayout(rows, cols));
    content.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    content.add(iconScroll, new Rectangle(0, 0, 3, 1));
    content.add(ok, new Rectangle(1, 1, 1, 1));
    content.add(cancel, new Rectangle(2, 1, 1, 1));
  }

  public String promptForIcon() {
    dialog = ComponentHelper.createJDialog(parent);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    dialog.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    dialog.getContentPane().add(content);
    dialog.setTitle(WINDOW_TITLE);
    dialog.setModal(true);

    ComponentHelper.centreOnScreen(dialog);
    dialog.setVisible(true);

    return selectedIcon;
  }

  /*
   * (non-Javadoc)
   * @see
   * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == ok) {
      selectedIcon = icons.getSelectedPath();
      dialog.dispose();
    } else if (e.getSource() == cancel) {
      dialog.dispose();
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event
   * .ListSelectionEvent)
   */
  @Override
  public void valueChanged(ListSelectionEvent e) {
    ok.setEnabled(true);
  }
}
