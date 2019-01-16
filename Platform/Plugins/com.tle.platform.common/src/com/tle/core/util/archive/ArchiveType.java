/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.util.archive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import com.tle.annotation.Nullable;

public enum ArchiveType {
  ZIP(".zip", ".jar", ".war") // $NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
  {
    @Override
    public ArchiveExtractor createExtractor(InputStream in) {
      final ZipInputStream zin = new ZipInputStream(in);
      return new ArchiveExtractor() {
        @Override
        public ArchiveEntry getNextEntry() throws IOException {
          final ZipEntry entry = zin.getNextEntry();
          if (entry == null) {
            return null;
          }
          // Some poorly created ZIP files incorrectly use
          // backslashes as a
          // directory separator instead of forward slash. This
          // causes us to
          // write out filenames containing backslashes rather
          // than creating
          // directories. Replace all backslashes with forward
          // slashes.
          return new ArchiveEntry(
              entry.getName().replace('\\', '/'), entry.isDirectory(), entry.getSize());
        }

        @Override
        public InputStream getStream() {
          return zin;
        }
      };
    }

    @Override
    public ArchiveCreator createArchiver(OutputStream archive) throws IOException {
      final ZipOutputStream zout = new ZipOutputStream(archive);
      return new ArchiveCreator() {
        @Override
        public OutputStream newEntry(String name, long size) throws IOException {
          ZipEntry entry = new ZipEntry(name);
          entry.setSize(size);
          zout.putNextEntry(entry);
          return zout;
        }

        @Override
        public void closeEntry() throws IOException {
          zout.closeEntry();
        }

        @Override
        public void close() throws IOException {
          zout.close();
        }
      };
    }
  },

  /** BZ2 implementation is just WAAAY to slow. Do not use. */
  @Deprecated
  TAR_BZ2(".tar.bz2") // $NON-NLS-1$
  {
    @Override
    public ArchiveExtractor createExtractor(InputStream in) throws IOException {
      return createTarXZipExtractor(new BZip2CompressorInputStream(in));
    }

    @Override
    public ArchiveCreator createArchiver(OutputStream archive) throws IOException {
      return createTarXZipArchiver(new BZip2CompressorOutputStream(archive));
    }
  },

  TAR_GZ(".tar.gz", ".tgz") // $NON-NLS-1$ //$NON-NLS-2$
  {
    @Override
    public ArchiveExtractor createExtractor(InputStream in) throws IOException {
      return createTarXZipExtractor(new GZIPInputStream(in));
    }

    @Override
    public ArchiveCreator createArchiver(OutputStream archive) throws IOException {
      return createTarXZipArchiver(new GZIPOutputStream(archive));
    }
  };

  private final String[] fileExtensions;

  private ArchiveType(String... fileExtensions) {
    assert fileExtensions != null;
    this.fileExtensions = fileExtensions;
  }

  public abstract ArchiveExtractor createExtractor(InputStream archive) throws IOException;

  public abstract ArchiveCreator createArchiver(OutputStream archive) throws IOException;

  public static List<String> getAllExtensions() {

    List<String> rv = new ArrayList<String>();
    for (ArchiveType at : EnumSet.allOf(ArchiveType.class)) {
      rv.addAll(Arrays.asList(at.fileExtensions));
    }
    return rv;
  }

  protected static ArchiveExtractor createTarXZipExtractor(InputStream xzip) {
    final TarArchiveInputStream tin = new TarArchiveInputStream(xzip);
    return new ArchiveExtractor() {
      @Override
      public ArchiveEntry getNextEntry() throws IOException {
        TarArchiveEntry entry = tin.getNextTarEntry();
        if (entry == null) {
          return null;
        }
        return new ArchiveEntry(entry.getName(), entry.isDirectory(), entry.getSize());
      }

      @Override
      public InputStream getStream() {
        return tin;
      }
    };
  }

  protected static ArchiveCreator createTarXZipArchiver(OutputStream xzip) {
    final TarArchiveOutputStream tout = new TarArchiveOutputStream(xzip);
    tout.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
    return new ArchiveCreator() {
      @Override
      public OutputStream newEntry(String name, long size) throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(size);
        tout.putArchiveEntry(entry);
        return tout;
      }

      @Override
      public void closeEntry() throws IOException {
        tout.closeArchiveEntry();
      }

      @Override
      public void close() throws IOException {
        tout.close();
      }
    };
  }

  @Nullable
  private static ArchiveType getForFilenamePrivate(String filename) {
    final String testFilename = filename.toLowerCase();
    for (ArchiveType type : ArchiveType.values()) {
      for (String fileExtension : type.fileExtensions) {
        if (testFilename.endsWith(fileExtension)) {
          return type;
        }
      }
    }
    return null;
  }

  @SuppressWarnings("nls")
  public static ArchiveType getForFilename(String filename) {
    final ArchiveType type = getForFilenamePrivate(filename);
    if (type == null) {
      throw new RuntimeException("No archive handler found for file " + filename);
    }
    return type;
  }

  public static boolean isArchiveType(String filename) {
    return getForFilenamePrivate(filename) != null;
  }
}
