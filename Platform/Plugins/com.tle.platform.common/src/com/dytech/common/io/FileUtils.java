/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dytech.common.io;

import com.google.common.base.VerifyException;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
@SuppressWarnings("nls")
public final class FileUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

  /**
   * @deprecated Use delete(Path f) instead
   */
  @Deprecated
  public static boolean delete(File f) {
    try {
      return delete(f.toPath(), null, false);
    } catch (IOException io) {
      // Can't happen due to failOnError=false
      return false;
    }
  }

  public static boolean delete(Path f) {
    try {
      return delete(f, null, false);
    } catch (IOException io) {
      // Can't happen due to failOnError=false
      return false;
    }
  }

  public static boolean delete(Path f, FileCallback callback) {
    try {
      return delete(f, callback, false);
    } catch (IOException io) {
      // Can't happen due to failOnError=false
      return false;
    }
  }

  public static boolean delete(Path f, @Nullable FileCallback callback, boolean failOnError)
      throws IOException {
    if (LOGGER.isTraceEnabled()) {
      // Exposes the code flows that delete files
      Exception tracer = new Exception("Debug stack trace - about to delete - " + f.toString());
      LOGGER.trace(tracer.getMessage(), tracer);
    }
    if (!Files.exists(f, LinkOption.NOFOLLOW_LINKS)) {
      LOGGER.debug("File does not exist.  Could not delete [" + f.toString() + "]");
      return true;
    }
    if (Files.isDirectory(f, LinkOption.NOFOLLOW_LINKS)) {
      LOGGER.debug("File [" + f.toString() + "] is a folder, deleting contents");
      try {
        final DeleteVisitor visitor = new DeleteVisitor(callback);
        Files.walkFileTree(f, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, visitor);
        return visitor.isSuccess();
      } catch (Exception e) {
        if (failOnError) {
          throwErrorOnDelete(e);
        }
        LOGGER.error("Failed to walk file tree for " + f.toString(), e);
        return false;
      }
    } else {
      try {
        LOGGER.debug("Deleting file " + f.toString());
        Files.delete(f);
        return true;
      } catch (Exception e) {
        if (failOnError) {
          throwErrorOnDelete(e);
        }
        LOGGER.error("Failed to delete file " + f.toString(), e);
        return false;
      }
    }
  }

  private static void throwErrorOnDelete(Exception e) throws IOException {
    if (e instanceof IOException) {
      throw (IOException) e;
    }
    throw new VerifyException(e);
  }

  private static int getTreeDepth(String pattern) {
    if (pattern.contains("**")) {
      return Integer.MAX_VALUE;
    }

    String[] levels = pattern.split("/");
    return levels.length;
  }

  public static List<String> grep(final Path dirs, final String pattern, final boolean filesOnly) {
    return grep(dirs, pattern, null, filesOnly);
  }

  public static List<String> grep(
      final Path dirs, final String pattern, @Nullable GrepFunctor func, final boolean filesOnly) {
    if (Files.notExists(dirs)) {
      return Collections.emptyList();
    }

    List<String> results = new ArrayList<String>();
    int depth = getTreeDepth(pattern);

    String dirName = dirs.getFileName().toString();
    if (pattern.contains("**")) {
      dirName = "**";
    }

    final PathMatcher pathMatcher =
        FileSystems.getDefault().getPathMatcher("glob:**/" + dirName + "/" + pattern);
    final DirectoryStream.Filter<Path> filter =
        new DirectoryStream.Filter<Path>() {
          boolean matched(Path entry) {
            if (pathMatcher.matches(entry)) {
              if (!filesOnly) {
                return true;
              }

              return Files.isRegularFile(entry);
            }
            return false;
          }

          @Override
          public boolean accept(Path entry) {
            return matched(entry);
          }
        };
    try {
      Files.walkFileTree(
          dirs,
          EnumSet.noneOf(FileVisitOption.class),
          depth,
          new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path file, BasicFileAttributes attrs)
                throws IOException {
              try (DirectoryStream<Path> stream = Files.newDirectoryStream(file, filter)) {
                for (Path path : stream) {
                  final String relPath = dirs.relativize(path).toString();
                  results.add(relPath);
                  if (func != null) {
                    func.matched(path, relPath);
                  }
                }
              } catch (IOException ex) {
                throw new RuntimeException("Error getting file", ex);
              }
              return FileVisitResult.CONTINUE;
            }
          });
    } catch (IOException io) {
      throw new RuntimeException("Error walking file tree for " + dirs.toString(), io);
    }
    return results;
  }

  public static long countFiles(Path file) {
    if (!Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
      return 0;
    }
    try {
      CountingVisitor visitor = new CountingVisitor();
      Files.walkFileTree(file, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, visitor);
      return visitor.getCount();
    } catch (Exception e) {
      throw new RuntimeException("Error counting files for " + file.toString(), e);
    }
  }

  public static long fileSize(Path file) throws FileNotFoundException {
    if (Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
      try {
        return Files.size(file);
      } catch (IOException io) {
        LOGGER.error("Error getting file size for " + file.toString(), io);
        throw new RuntimeException(io);
      }
    }
    throw new FileNotFoundException(file.toString());
  }

  public static void copy(Path src, Path dest) throws IOException {
    // TODO: replace this with a super fast implementation, also handle directories
    Files.copy(src, dest);
  }

  private FileUtils() {
    throw new Error();
  }

  private static class CountingVisitor extends SimpleFileVisitor<Path> {
    private long count;

    public CountingVisitor() {
      count = 0;
    }

    public long getCount() {
      return count;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      count++;
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
      count++;
      return FileVisitResult.CONTINUE;
    }
  }

  private static class DeleteVisitor extends SimpleFileVisitor<Path> {
    private boolean success;
    @Nullable private final FileCallback callback;

    public DeleteVisitor(@Nullable FileCallback callback) {
      success = true;
      this.callback = callback;
    }

    private void doDelete(Path file) {
      boolean ok = true;
      try {
        LOGGER.debug("Deleting file " + file.toString());
        Files.delete(file);
      } catch (Exception io) {
        ok = false;
        success = false;
        LOGGER.warn("Failed to delete file " + file.toString(), io);
      }
      if (ok) {
        if (callback != null) {
          try {
            callback.fileProcessed(file, null);
          } catch (Exception e) {
            LOGGER.error("File callback threw an exception on file " + file.toString(), e);
          }
        }
      }
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      doDelete(file);
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
      // Tries again, visitFile can fail trying to get file attributes
      doDelete(file);
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
      if (exc != null) {
        success = false;
        LOGGER.warn("Folder traversal failed.  Could not traverse " + dir.toString());
      }
      try {
        Files.delete(dir);
        LOGGER.warn("Deleting folder " + dir.toString());
      } catch (Exception e) {
        success = false;
        LOGGER.warn("Folder deletion failed.  Could not delete " + dir.toString());
      }
      return FileVisitResult.CONTINUE;
    }

    public boolean isSuccess() {
      return success;
    }
  }

  public interface GrepFunctor {
    void matched(Path file, String relFilepath);
  }

  public interface FileCallback {
    /**
     * @param file1 The file being processed
     * @param file2 Usually null. Only set in a dual file operation e.g. copy
     */
    void fileProcessed(Path file1, @Nullable Path file2);
  }
}
