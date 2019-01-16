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

package com.tle.admin.boot;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.dytech.gui.ComponentHelper;
import com.dytech.gui.JImage;

@SuppressWarnings("nls")
public class LoadingDialog extends JFrame {
  private static final long serialVersionUID = 1L;
  private static final int WINDOW_WIDTH = 349;
  private static final int WINDOW_HEIGHT = 105;

  public LoadingDialog(String windowTitle) {
    setupDialog(windowTitle);
  }

  private void setupDialog(String windowTitle) {
    JImage image = new JImage(LoadingDialog.class.getResource("/icons/splash.gif"));
    JImage anim =
        new JImage(LoadingDialog.class.getResource("/icons/loading_animation.gif")); // $NON-NLS-1$

    JPanel all = new JPanel(null);
    all.add(anim);
    all.add(image);

    image.setBounds(0, 0, 349, 105);
    anim.setBounds(258, 41, 24, 24);

    setIconImage(
        new ImageIcon(LoadingDialog.class.getResource("/icons/windowicon.gif"))
            .getImage()); //$NON-NLS-1$

    setTitle(windowTitle);
    setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    setUndecorated(true);
    getContentPane().add(all);

    ComponentHelper.centreOnScreen(this);
  }
}
