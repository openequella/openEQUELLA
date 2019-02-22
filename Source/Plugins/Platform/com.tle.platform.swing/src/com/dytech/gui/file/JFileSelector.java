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

package com.dytech.gui.file;

import com.dytech.gui.AbstractTextFieldButton;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

public class JFileSelector extends AbstractTextFieldButton {
  private int selectionMode;
  private File selectedFile;
  private FileFilter filter;
  private File currentDirectory;

  public JFileSelector() {
    super("Browse...");
    field.setEditable(false);
    setSelectionMode(JFileChooser.FILES_ONLY);
  }

  public void setSelectionMode(int mode) {
    selectionMode = mode;
  }

  public void setCurrentDirectory(File directory) {
    currentDirectory = directory;
  }

  public void setFileFilter(FileFilter filter) {
    this.filter = filter;
  }

  public void setSelectedFile(File file) {
    selectedFile = file;
    if (selectedFile != null) {
      setFieldText(selectedFile.getAbsolutePath());
    } else {
      setFieldText("");
    }
  }

  public File getSelectedFile() {
    return selectedFile;
  }

  @Override
  protected void buttonSelected() {
    JFileChooser chooser = new JFileChooser(currentDirectory);
    chooser.setMultiSelectionEnabled(false);
    chooser.setFileSelectionMode(selectionMode);
    chooser.setFileFilter(filter);

    int result = chooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      setSelectedFile(chooser.getSelectedFile());
    }
  }

  public static void main(String[] args) throws Exception {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

    JFileSelector tfb = new JFileSelector();
    tfb.setFileFilter(FileFilterAdapter.IMAGES());

    JFrame dialog = new JFrame();
    dialog.getContentPane().add(tfb);
    dialog.pack();
    dialog.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    dialog.setVisible(true);
  }
}
