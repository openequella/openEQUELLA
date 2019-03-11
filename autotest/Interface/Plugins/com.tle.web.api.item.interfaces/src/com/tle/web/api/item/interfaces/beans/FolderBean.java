package com.tle.web.api.item.interfaces.beans;

import java.util.List;

@SuppressWarnings("nls")
public class FolderBean extends GenericFileBean {
  @SuppressWarnings("hiding")
  public static final String TYPE = "folder";

  private List<FileBean> files;
  private List<FolderBean> folders;

  public List<FileBean> getFiles() {
    return files;
  }

  public void setFiles(List<FileBean> files) {
    this.files = files;
  }

  public List<FolderBean> getFolders() {
    return folders;
  }

  public void setFolders(List<FolderBean> folders) {
    this.folders = folders;
  }
}
