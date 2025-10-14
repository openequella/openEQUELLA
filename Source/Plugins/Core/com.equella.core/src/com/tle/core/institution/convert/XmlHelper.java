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

package com.tle.core.institution.convert;

import com.dytech.common.io.UnicodeReader;
import com.dytech.common.text.NumberStringComparator;
import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractConverter.FormatFile;
import com.tle.core.services.FileSystemService;
import com.tle.core.xml.service.XmlService;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("nls")
@Bind
@Singleton
public class XmlHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(XmlHelper.class);
  public static final String EXPORT_BUCKET_FILE_PATTERN = "*/*.xml";

  @Inject private FileSystemService fileSystemService;
  @Inject private XmlService xmlService;
  private final XppDriver xppDriver = new XppDriver();

  public XStream createXStream(ClassLoader classLoader) {
    return xmlService.createDefault(classLoader);
  }

  public void writeFile(TemporaryFileHandle staging, String filename, String content) {
    try (Writer out =
        new OutputStreamWriter(
            fileSystemService.getOutputStream(staging, filename, false), StandardCharsets.UTF_8)) {
      out.write(content);
    } catch (Exception e) {
      throw new RuntimeException(
          "Error writing xml for file:" + staging.getAbsolutePath() + "/" + filename, e);
    }
  }

  public void writeFromPropBagEx(TemporaryFileHandle staging, String filename, PropBagEx xml) {
    writeFile(staging, filename, xml.toString());
  }

  public PropBagEx readToPropBagEx(TemporaryFileHandle staging, String filename) {
    try (InputStream stream = fileSystemService.read(staging, filename)) {
      return new PropBagEx(stream);
    } catch (Exception e) {
      throw new RuntimeException(
          "Error reading xml from file:" + staging.getAbsolutePath() + filename, e);
    }
  }

  public void writeXmlFile(TemporaryFileHandle file, String path, Object obj, XStream xstream) {
    try (OutputStream outStream = fileSystemService.getOutputStream(file, path, false)) {
      xstream.toXML(obj, new OutputStreamWriter(outStream, StandardCharsets.UTF_8));
    } catch (IOException ioe) {
      throw new RuntimeException("Error writing file " + file.getAbsolutePath(), ioe);
    }
  }

  public void writeXmlFile(TemporaryFileHandle file, String path, Object obj) {
    try (OutputStream outStream = fileSystemService.getOutputStream(file, path, false)) {
      xmlService.serialiseToWriter(obj, new OutputStreamWriter(outStream, StandardCharsets.UTF_8));
    } catch (IOException ioe) {
      throw new RuntimeException("Error writing file " + file.getAbsolutePath(), ioe);
    }
  }

  public <O> O readXmlFile(final TemporaryFileHandle file, String path, final XStream xstream) {
    return readXmlFile(file, path, xstream, null, null);
  }

  @SuppressWarnings("unchecked")
  public <O> O readXmlFile(
      final TemporaryFileHandle file,
      String path,
      final XStream xstream,
      O rootObject,
      DataHolder dataHolder) {
    try (Reader reader = new UnicodeReader(fileSystemService.read(file, path), Constants.UTF8)) {
      return (O) xstream.unmarshal(xppDriver.createReader(reader), rootObject, dataHolder);
    } catch (IOException re) {
      LOGGER.error("Error reading: " + file.getAbsolutePath());
      throw new RuntimeException(re);
    }
  }

  @SuppressWarnings("unchecked")
  public <O> O readXmlFile(final TemporaryFileHandle file, String path) {
    try (Reader reader = new UnicodeReader(fileSystemService.read(file, path), Constants.UTF8)) {
      return xmlService.deserialiseFromXml(getClass().getClassLoader(), reader);
    } catch (IOException re) {
      LOGGER.error("Error reading: " + file.getAbsolutePath());
      throw new RuntimeException(re);
    }
  }

  public List<String> getXmlFileList(final TemporaryFileHandle folder) {
    if (isBucketed(folder)) {
      return fileSystemService.grep(folder, "", EXPORT_BUCKET_FILE_PATTERN);
    }
    return fileSystemService.grep(folder, "", "*.xml");
  }

  /**
   * Use the provided pattern to find XML files first. If none are found then find by standard
   * pattern.
   *
   * @param folder Folder in which to search for XML files
   * @param pattern Pattern used to find files.
   * @return List of file names.
   */
  public List<String> getXmlFileListByPattern(final TemporaryFileHandle folder, String pattern) {
    List<String> files = fileSystemService.grep(folder, "", pattern);
    if (files.size() == 0) {
      return fileSystemService.grep(folder, "", "*.xml");
    }
    return files;
  }

  /**
   * Returns the files in order based on their filenames (i.e. the bucket folder, if any, is
   * ignored)
   *
   * @param folder
   * @return
   */
  public List<String> getXmlFileListOrdered(final TemporaryFileHandle folder) {
    final List<String> files = getXmlFileList(folder);
    Collections.sort(
        files,
        new NumberStringComparator<String>() {
          private static final long serialVersionUID = 1L;

          @Override
          public int compare(String term1, String term2) {
            File f1 = new File(term1);
            File f2 = new File(term2);
            return super.compare(f1.getName(), f2.getName());
          }
        });
    return files;
  }

  /**
   * Writes out a format file to the specified folder which indicates whether the exported entity
   * are stored in a bucket folder or not.
   *
   * <p>For example, the exported favourite item XML file is stored in a bucket folder
   * `favourites/items/<bucket-id>/item-id.xml`, thus the format XML file should be placed in
   * `favourites/items`, and the bucketed parameter should be set to true.
   *
   * @param folder The folder in which to write the format file.
   * @param bucketed True if the exported entity XML file are stored in a bucket folder.
   */
  public void writeExportFormatXmlFile(final TemporaryFileHandle folder, boolean bucketed) {
    FormatFile ff = new FormatFile();
    ff.setBucketed(bucketed);
    writeXmlFile(
        folder,
        InstitutionImportExportConstants.EXPORT_FORMAT_FILE,
        ff,
        xmlService.createDefault(getClass().getClassLoader()));
  }

  /**
   * @param folder
   * @return true if the format file exists and the format is set to bucketed
   */
  public boolean isBucketed(final TemporaryFileHandle folder) {
    if (fileSystemService.fileExists(folder, InstitutionImportExportConstants.EXPORT_FORMAT_FILE)) {
      FormatFile ff = readXmlFile(folder, InstitutionImportExportConstants.EXPORT_FORMAT_FILE);
      return ff.isBucketed();
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public <O> O readXmlString(String xml) {
    return xmlService.deserialiseFromXml(getClass().getClassLoader(), xml);
  }

  public <O> String writeToXmlString(O obj) {
    return xmlService.serialiseToXml(obj);
  }
}
