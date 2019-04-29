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

package com.dytech.gui;

import java.awt.Dimension;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * A standard Swing component which displays an image.
 *
 * @author Nicholas Read
 */
public class JImage extends JLabel {
  protected int height;
  protected int width;

  public JImage(String filename) {
    ImageIcon image = new ImageIcon(filename);
    setup(image);
  }

  public JImage(ImageIcon image) {
    setup(image);
  }

  public JImage(URL url) {
    ImageIcon image = new ImageIcon(url);
    setup(image);
  }

  private void setup(ImageIcon image) {
    setBorder(null);
    setOpaque(false);

    image.setImageObserver(this);
    this.setIcon(image);

    height = image.getIconHeight();
    width = image.getIconWidth();
  }

  @Override
  public Dimension getPreferredSize() {
    int extraWidth = getInsets().left + getInsets().right;
    int extraHeight = getInsets().top + getInsets().bottom;
    return new Dimension(width + extraWidth, height + extraHeight);
  }
}
