package com.tle.web.filemanager.applet.actions;

import com.tle.web.filemanager.applet.FileListPanel;
import com.tle.web.filemanager.applet.backend.Backend;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.KeyStroke;

public class OpenAction extends AbstractCacheSelectionAction {
  public OpenAction(Backend backend, FileListPanel fileList) {
    super(
        "action.open.name",
        "action.open.desc",
        "action.open.error.title", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        "action.open.error.message",
        backend,
        fileList); //$NON-NLS-1$

    setIcon("open.gif"); // $NON-NLS-1$
    setMnemonic(KeyEvent.VK_O);
  }

  @Override
  protected void doOperation(File file) {
    DesktopApi.open(file);
  }

  @Override
  public void update() {
    setEnabled(getFileList().getSelectionCount() == 1);
  }

  @Override
  public KeyStroke invokeForWindowKeyStroke() {
    return KeyStroke.getKeyStroke("ENTER"); // $NON-NLS-1$
  }
}
