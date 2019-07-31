package com.tle.web.filemanager.applet.backend;

import com.tle.web.appletcommon.io.ProgressMonitorCallback;
import com.tle.web.filemanager.common.FileInfo;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Implementations must provide a constructor that take a single java.net.URL parameter,
 * representing the server end-point.
 */
public interface Backend {
  void addListener(BackendListener listener);

  List<FileInfo> listFiles(String directory);

  void toggleMarkAsResource(FileInfo info);

  void newFolder(FileInfo fileInfo);

  void delete(FileInfo info);

  void copy(FileInfo sourceFile, FileInfo destFile);

  /** @return true for a successful rename. */
  boolean move(FileInfo sourceFile, FileInfo destinationFile);

  void extractArchive(FileInfo info);

  boolean isCachedLocally(FileInfo info);

  File cacheFileForEditing(FileInfo info, ProgressMonitorCallback callback) throws IOException;

  void downloadFile(FileInfo info, File destination, ProgressMonitorCallback callback)
      throws IOException;

  long uploadFile(File file, FileInfo cwd, ProgressMonitorCallback progressMonitorCallback)
      throws IOException;

  void synchroniseFile(FileInfo info, ProgressMonitorCallback callback) throws IOException;

  List<FileInfo> getUnsynchronisedFiles();
}
