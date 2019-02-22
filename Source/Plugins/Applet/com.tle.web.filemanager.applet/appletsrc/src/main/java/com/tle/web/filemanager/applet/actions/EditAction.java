package com.tle.web.filemanager.applet.actions;

import com.tle.web.filemanager.applet.FileListPanel;
import com.tle.web.filemanager.applet.backend.Backend;
import java.awt.event.KeyEvent;
import java.io.File;

public class EditAction extends AbstractCacheSelectionAction {
  public EditAction(Backend backend, FileListPanel fileList) {
    super(
        "action.edit.name",
        "action.edit.desc",
        "action.edit.error.title", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        "action.edit.error.message",
        backend,
        fileList); //$NON-NLS-1$

    setIcon("edit.gif"); // $NON-NLS-1$
    setMnemonic(KeyEvent.VK_E);
  }

  @Override
  protected void doOperation(File file) {
    DesktopApi.edit(file);
  }

  @Override
  public void update() {
    FileListPanel fl = getFileList();
    setEnabled(fl.getSelectionCount() == 1);
  }
}
