package com.tle.web.filemanager.applet.actions;

import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.appletcommon.gui.GlassProgressWorker;
import com.tle.web.filemanager.applet.FileListPanel;
import com.tle.web.filemanager.applet.backend.Backend;
import com.tle.web.filemanager.common.FileInfo;
import java.awt.event.ActionEvent;

public class ExtractArchiveAction extends TLEAction {
  private final Backend backend;
  private final FileListPanel fileList;

  public ExtractArchiveAction(Backend backend, FileListPanel fileList) {
    super(CurrentLocale.get("action.extract.name")); // $NON-NLS-1$

    setIcon("extract.gif"); // $NON-NLS-1$
    setShortDescription(CurrentLocale.get("action.extract.desc")); // $NON-NLS-1$

    this.backend = backend;
    this.fileList = fileList;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final FileInfo selected = fileList.getSelectedFile();

    GlassProgressWorker<?> worker =
        new GlassProgressWorker<Object>(
            CurrentLocale.get("action.extract.progress", selected.getName()),
            -1,
            false) //$NON-NLS-1$
        {
          @Override
          public Object construct() throws Exception {
            backend.extractArchive(selected);
            return null;
          }
        };
    worker.setComponent(fileList);
    worker.start();
  }

  @Override
  @SuppressWarnings("nls")
  public void update() {
    boolean e = fileList.getSelectionCount() == 1;
    if (e) {
      String n = fileList.getSelectedFile().getName().toLowerCase();
      e =
          n.endsWith(".zip")
              || n.endsWith(".jar")
              || n.endsWith(".war")
              || n.endsWith(".tar.bz2")
              || n.endsWith(".tar.gz");
    }
    setEnabled(e);
  }
}
