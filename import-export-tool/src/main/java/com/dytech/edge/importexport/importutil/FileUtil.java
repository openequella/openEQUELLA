package com.dytech.edge.importexport.importutil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("nls")
public final class FileUtil {
  public static List<String> getRelativeFileList(File files[]) {
    List<String> result = new ArrayList<String>();
    getRelativeFileList(files, result, "");
    return result;
  }

  private static void getRelativeFileList(File files[], List<String> filenames, String path) {
    for (int i = 0; i < files.length; i++) {
      if (files[i].isDirectory()) {
        File children[] = files[i].listFiles();
        getRelativeFileList(children, filenames, path + "/" + files[i].getName());
      } else {
        filenames.add((path.isEmpty() ? path : path + "/") + files[i].getName());
      }
    }
  }

  private FileUtil() {
    throw new Error();
  }
}
