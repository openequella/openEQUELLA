package com.tle.web.filemanager.applet.actions;

import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.appletcommon.gui.GlassProgressWorker;
import com.tle.web.appletcommon.gui.InterruptAwareGlassPaneProgressMonitorCallback;
import com.tle.web.appletcommon.gui.UserCancelledException;
import com.tle.web.filemanager.applet.FileListPanel;
import com.tle.web.filemanager.applet.backend.Backend;
import com.tle.web.filemanager.common.FileInfo;
import java.awt.ComponentOrientation;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

@SuppressWarnings("nls")
public class DownloadAction extends TLEAction {
  private final Backend backend;
  private final FileListPanel fileList;
  private final boolean isPostJava5;

  public DownloadAction(Backend backend, FileListPanel fileList, boolean isPostJava5) {
    super(CurrentLocale.get("action.download.name"));

    setShortDescription(CurrentLocale.get("action.download.desc"));
    setIcon("download.gif");

    this.backend = backend;
    this.fileList = fileList;
    this.isPostJava5 = isPostJava5;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final List<FileInfo> files = fileList.getSelectedFiles();

    JFileChooser fc = new JFileChooser();
    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    if (fc.showSaveDialog(fileList) == JFileChooser.APPROVE_OPTION) {
      final File destDir = fc.getSelectedFile();

      if (CurrentLocale.isRightToLeft()) {
        fileList.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
      }
      if (!destDir.isDirectory()) {
        JOptionPane.showMessageDialog(
            fileList,
            CurrentLocale.get("action.download.notdir.message"),
            CurrentLocale.get("action.download.notdir.title"),
            JOptionPane.WARNING_MESSAGE);
        return;
      }

      if (Utils.filenamesClash(destDir.list(), fileList.getCurrentDirectoryFiles())) {
        if (!Utils.confirmOverwrite(fileList, "action.download.overwrite.msg")) {
          return;
        }
      }

      GlassProgressWorker<?> worker =
          new GlassProgressWorker<Object>(
              CurrentLocale.get("action.download.progress.unknown"), -1, true) {
            @Override
            public Object construct() throws Exception {
              setTotal((int) getFileSize(files));
              download(destDir, files);
              return null;
            }

            private void download(File destDir, List<FileInfo> files) throws IOException {
              for (FileInfo fileInfo : files) {
                final File destFile = new File(destDir, fileInfo.getName());
                if (fileInfo.isDirectory()) {
                  download(destFile, backend.listFiles(fileInfo.getFullPath()));
                } else {
                  setMessage(
                      CurrentLocale.get(
                          "action.download.progress.downloading", destFile.getName()));
                  backend.downloadFile(
                      fileInfo, destFile, new InterruptAwareGlassPaneProgressMonitorCallback(this));
                }
              }
            }

            private long getFileSize(List<FileInfo> files) {
              long total = 0;
              for (FileInfo file : files) {
                total +=
                    file.isDirectory()
                        ? getFileSize(backend.listFiles(file.getFullPath()))
                        : file.getSize();
              }
              return total;
            }

            @Override
            public void finished() {
              if (isPostJava5) {
                DesktopApi.open(destDir);
              }
            }

            @Override
            public void exception() {
              Exception ex = getException();
              if (!(ex instanceof UserCancelledException)) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                    fileList,
                    CurrentLocale.get("action.download.error.message"),
                    CurrentLocale.get("action.download.error.title"),
                    JOptionPane.ERROR_MESSAGE);
              }
            }
          };
      worker.setComponent(fileList);
      worker.start();
    }
  }

  @Override
  public void update() {
    setEnabled(!fileList.getSelectedFiles().isEmpty());
  }
}
