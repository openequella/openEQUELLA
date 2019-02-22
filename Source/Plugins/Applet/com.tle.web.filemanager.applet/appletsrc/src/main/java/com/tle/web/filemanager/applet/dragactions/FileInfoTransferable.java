package com.tle.web.filemanager.applet.dragactions;

import com.tle.web.filemanager.common.FileInfo;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

@SuppressWarnings("nls")
public class FileInfoTransferable implements Transferable {
  public static DataFlavor fileInfoFlavor = null;
  public static DataFlavor localFileInfoFlavor = null;

  static {
    try {
      fileInfoFlavor = new DataFlavor(FileInfo.class, "Non local FileInfo");
      localFileInfoFlavor =
          new DataFlavor(
              DataFlavor.javaJVMLocalObjectMimeType
                  + "; class=com.tle.web.filemanager.common.FileInfo",
              "Local FileInfo");
    } catch (Exception e) {
      System.err.println(e);
    }
  }

  private FileInfo fileInfo;

  public FileInfoTransferable(FileInfo fileInfo) {
    this.fileInfo = fileInfo;
  }

  @Override
  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    return this.fileInfo;
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {fileInfoFlavor, localFileInfoFlavor};
  }

  @Override
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return flavor.equals(fileInfoFlavor) || flavor.equals(localFileInfoFlavor);
  }
}
