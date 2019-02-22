package com.tle.web.filemanager.applet.backend;

import com.tle.web.appletcommon.io.ProgressMonitorCallback;
import com.tle.web.filemanager.common.FileInfo;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public abstract class AbstractRemoteBackendImpl implements Backend {
  @Override
  public void addListener(BackendListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public final boolean isCachedLocally(FileInfo info) {
    throw new UnsupportedOperationException();
  }

  @Override
  public final File cacheFileForEditing(FileInfo info, ProgressMonitorCallback callback)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void downloadFile(FileInfo info, File destination, ProgressMonitorCallback callback)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public final List<FileInfo> getUnsynchronisedFiles() {
    throw new UnsupportedOperationException();
  }

  @Override
  public final void synchroniseFile(FileInfo info, ProgressMonitorCallback callback)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public long uploadFile(File file, FileInfo cwd, ProgressMonitorCallback progressMonitorCallback) {
    throw new UnsupportedOperationException();
  }

  public abstract InputStream readFile(String filename);

  public abstract void writeFile(String filename, long length, InputStream content);
}
