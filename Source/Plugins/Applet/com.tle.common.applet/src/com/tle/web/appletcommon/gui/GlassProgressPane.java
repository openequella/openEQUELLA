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

package com.tle.web.appletcommon.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.AdvancedSwingWorker;
import com.dytech.gui.workers.BusyGlassPane;

public class GlassProgressPane extends BusyGlassPane {
  private JLabel message;
  private JProgressBar progress;
  private AdvancedSwingWorker<?> worker;

  private JButton cancel;

  protected GlassProgressPane(
      final String messageText,
      final int total,
      final Component activeComponent,
      final boolean stopClosing,
      final AdvancedSwingWorker<?> worker,
      final boolean cancellable) {
    super(activeComponent, stopClosing);

    this.worker = worker;

    message = new JLabel(messageText);
    message.setForeground(Color.WHITE);
    message.setHorizontalAlignment(SwingConstants.CENTER);
    message.setHorizontalTextPosition(SwingConstants.CENTER);

    Font font = message.getFont();
    message.setFont(new Font(font.getFamily(), Font.BOLD, (int) (font.getSize() * 1.2)));

    progress = new JProgressBar();
    setTotal(total);

    // We cannot I18N the button text, as this class is initialised before
    // CurrentLocale.
    cancel = new JButton("Cancel");
    cancel.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            cancel.setEnabled(false);
            GlassProgressPane.this.worker.interrupt();
          }
        });

    final int height1 = message.getPreferredSize().height;
    final int height2 = progress.getPreferredSize().height;
    final int height3 = cancel.getPreferredSize().height;
    final int width1 = cancel.getPreferredSize().width;

    final int[] rows = {
      height3, TableLayout.FILL, height1, height2, TableLayout.FILL, height3,
    };
    final int[] cols = {
      width1, TableLayout.FILL, 100, TableLayout.FILL, width1,
    };

    setLayout(new TableLayout(rows, cols));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(message, new Rectangle(0, 2, 5, 1));
    add(progress, new Rectangle(2, 3, 1, 1));
    add(cancel, new Rectangle(4, 5, 1, 1));

    setCancellable(cancellable);
  }

  public void setWorker(AdvancedSwingWorker<?> worker) {
    this.worker = worker;
  }

  public void setCancellable(boolean cancellable) {
    cancel.setVisible(cancellable);
    if (cancellable) {
      cancel.setEnabled(true);
    }
  }

  public void addProgress(int value) {
    progress.setValue(progress.getValue() + value);
  }

  public void setTotal(int total) {
    if (total > 0) {
      progress.setMaximum(total);
      progress.setIndeterminate(false);
    } else {
      progress.setIndeterminate(true);
    }
  }

  public void setMessage(String messageText) {
    message.setText(messageText);
  }

  public void resetProgress() {
    progress.setValue(0);
  }

  @Override
  public void paintComponent(Graphics g) {
    g.setColor(new Color(0, 0, 0, 200));
    g.fillRect(0, 0, this.getWidth(), this.getHeight());
  }
}
