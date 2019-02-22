package com.tle.web.filemanager.applet.actions;

import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.appletcommon.gui.GlassProgressWorker;
import com.tle.web.appletcommon.io.ProgressMonitorCallback;
import com.tle.web.filemanager.applet.FileListPanel;
import com.tle.web.filemanager.applet.backend.Backend;
import com.tle.web.filemanager.common.FileInfo;
import java.awt.ComponentOrientation;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;

public abstract class AbstractCacheSelectionAction extends TLEAction {
  private static final long serialVersionUID = -1054339700016767559L;

  private final FileListPanel fileList;
  private final Backend backend;
  private final String errorTitleKey;
  private final String errorMsgKey;

  public AbstractCacheSelectionAction(
      String titleKey,
      String descriptionKey,
      String errorTitleKey,
      String errorMsgKey,
      Backend backend,
      FileListPanel fileList) {
    super(CurrentLocale.get(titleKey));

    setShortDescription(CurrentLocale.get(descriptionKey));

    this.errorTitleKey = errorTitleKey;
    this.errorMsgKey = errorMsgKey;
    this.backend = backend;
    this.fileList = fileList;
  }

  public FileListPanel getFileList() {
    return fileList;
  }

  @Override
  public final void actionPerformed(ActionEvent e) {
    final FileInfo file = fileList.getSelectedFile();

    if (file.isDirectory()) {
      fileList.setCwd(file);
    } else if (backend.isCachedLocally(file)) {
      try {
        doOperation(backend.cacheFileForEditing(file, null));
      } catch (IOException ex) {
        handleException(ex);
      }
    } else {
      GlassProgressWorker<?> worker =
          new GlassProgressWorker<File>(
              CurrentLocale.get("action.abstractcache.progress", file.getName()),
              (int) file.getSize(),
              false) //$NON-NLS-1$
          {
            @Override
            public File construct() throws Exception {
              return backend.cacheFileForEditing(
                  file,
                  new ProgressMonitorCallback() {
                    @Override
                    public void addToProgress(int value) {
                      addProgress(value);
                    }
                  });
            }

            @Override
            public void finished() {
              doOperation(get());
            }

            @Override
            public void exception() {
              handleException(getException());
            }
          };
      worker.setComponent(fileList);
      worker.start();
    }
  }

  protected abstract void doOperation(File file);

  protected void handleException(Exception ex) {
    ex.printStackTrace();
    if (CurrentLocale.isRightToLeft()) {
      fileList.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    }
    JOptionPane.showMessageDialog(
        fileList,
        CurrentLocale.get(errorMsgKey),
        CurrentLocale.get(errorTitleKey),
        JOptionPane.ERROR_MESSAGE);
  }
}
