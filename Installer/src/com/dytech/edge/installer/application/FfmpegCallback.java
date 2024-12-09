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

package com.dytech.edge.installer.application;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.Callback;
import com.dytech.installer.Wizard;
import java.awt.Component;
import java.io.File;
import javax.swing.JOptionPane;

public class FfmpegCallback implements Callback {
  private static final String[] EXE_TYPES = {"", ".exe"}; // $NON-NLS-1$

  @Override
  public void task(Wizard installer) {
    PropBagEx output = installer.getOutputNow();
    File dir = new File(output.getNode("ffmpeg/path")); // $NON-NLS-1$
    int result = JOptionPane.NO_OPTION;
    if (dir.getPath().equals("")) {
      Component parent = installer.getFrame();
      result =
          JOptionPane.showConfirmDialog(
              parent,
              "You have not entered a FFmpeg path which means no thumbnailing or previewing of"
                  + " video files can occur in EQUELLA. Are you sure?",
              "Warning", //$NON-NLS-1$ //$NON-NLS-2$
              JOptionPane.YES_NO_OPTION,
              JOptionPane.WARNING_MESSAGE);
    } else {
      if (!dir.exists() || !dir.isDirectory()) {
        JOptionPane.showMessageDialog(
            installer.getFrame(),
            "You have not specified"
                + " a valid directory.\nPlease select the correct path, and try again.",
            "Incorrect FFmpeg Directory",
            JOptionPane.ERROR_MESSAGE);
        return;
      }

      File ffmpeg = findExe(installer, dir, "ffmpeg");
      if (ffmpeg == null) {
        return;
      }

      File ffprobe = findExe(installer, dir, "ffprobe");
      if (ffprobe == null) {
        return;
      }

      installer.gotoPage(installer.getCurrentPageNumber() + 1);
    }
    if (result == JOptionPane.YES_OPTION) {
      installer.gotoPage(installer.getCurrentPageNumber() + 1);
    }
  }

  private File findExe(Wizard installer, File path, String exe) {
    for (String exeType : EXE_TYPES) {
      File exeFile = new File(path, exe + exeType);
      if (exeFile.canExecute()) {
        return exeFile;
      }
    }

    JOptionPane.showMessageDialog(
        installer.getFrame(),
        "The directory you have specified"
            + " does not contain the FFmpeg program '"
            + exe
            + "'.\nPlease select the"
            + " correct path, and try again.",
        "Incorrect FFmpeg Directory",
        JOptionPane.ERROR_MESSAGE);
    return null;
  }
}
