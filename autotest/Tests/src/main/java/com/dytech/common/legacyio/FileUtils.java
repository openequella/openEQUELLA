package com.dytech.common.legacyio;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileUtils {

  static File tempDir;

  public static synchronized File getTempDirectory() {
    if (tempDir == null) {
      try {
        tempDir = Files.createTempDirectory("eqtmp").toFile();
      } catch (IOException e) {
        throw new Error(e);
      }
    }
    return tempDir;
  }

  public static void copyFile(File file, File newFile) throws IOException {
    Files.copy(file.toPath(), newFile.toPath());
  }

  public static boolean deleteQuietly(File metsExtracted) {
    try {
      return Files.deleteIfExists(metsExtracted.toPath());
    } catch (IOException e) {
      return false;
    }
  }
}
