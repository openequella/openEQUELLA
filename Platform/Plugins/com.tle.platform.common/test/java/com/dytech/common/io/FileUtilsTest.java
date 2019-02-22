package com.dytech.common.io;

import com.google.common.io.Files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import junit.framework.TestCase;

public class FileUtilsTest extends TestCase {
  private final File tempFolder = new File(System.getProperty("java.io.tmpdir"));
  private File srcFolder;
  private File destFolder;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    srcFolder = new File(tempFolder, "source");
    if (!srcFolder.mkdirs()) {
      // Try creating a new file to see if it's lacking inodes or
      // something
      File f = new File(tempFolder, "test.txt");
      f.delete();
      f.createNewFile();

      throw new IOException(
          "Couldn't create directory for " + srcFolder + " for unknown reasons :(");
    }
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    FileUtils.delete(srcFolder);
    if (destFolder != null) {
      FileUtils.delete(destFolder);
    }
  }

  public void testRename() throws IOException, FileNotFoundException {
    File testContent = new File(srcFolder, "tmp1");
    PrintWriter pw = new PrintWriter(testContent);
    pw.append("This is my file");
    pw.append("It has 3 lines");
    pw.append("So there");
    pw.close();

    destFolder = new File(tempFolder, "dest");
    Files.move(srcFolder, destFolder);
  }

  /**
   * Only really makes sense on Windows.
   *
   * @throws IOException
   * @throws FileNotFoundException
   */
  public void testRenameWithFileBeingWrittenTo() throws IOException, FileNotFoundException {
    /*
     * new Thread() {
     * @Override public void run() { File testContent = new File(srcFolder,
     * "tmp1"); PrintWriter pw; try { pw = new PrintWriter(testContent); }
     * catch( FileNotFoundException e ) { throw new RuntimeException(e); }
     * pw.append("This is my file"); try { Thread.sleep(1500); } catch(
     * InterruptedException e ) { } pw.append("It has 3 lines");
     * pw.append("So there"); pw.close(); } }.start();
     * FileUtils.move(srcFolder, new File(tempFolder, "dest"), true);
     */
  }
}
