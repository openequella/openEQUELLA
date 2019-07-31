package com.tle.web.filemanager.applet.actions;

import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.filemanager.applet.FileListPanel;
import com.tle.web.filemanager.common.FileInfo;
import java.awt.event.ActionEvent;

public class PreJava6OpenAction extends TLEAction {
  private final FileListPanel fileList;

  public PreJava6OpenAction(FileListPanel fileList) {
    super(CurrentLocale.get("action.open.name")); // $NON-NLS-1$

    setShortDescription(CurrentLocale.get("action.open.desc")); // $NON-NLS-1$

    this.fileList = fileList;
  }

  public FileListPanel getFileList() {
    return fileList;
  }

  @Override
  public final void actionPerformed(ActionEvent e) {
    final FileInfo file = fileList.getSelectedFile();
    if (file != null && file.isDirectory()) {
      fileList.setCwd(file);
    }
  }

  @Override
  public void update() {
    setEnabled(fileList.getSelectionCount() == 1 && fileList.getSelectedFile().isDirectory());
  }
}
