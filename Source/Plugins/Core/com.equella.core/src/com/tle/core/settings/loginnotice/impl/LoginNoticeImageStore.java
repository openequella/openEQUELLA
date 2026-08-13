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

package com.tle.core.settings.loginnotice.impl;

import com.tle.common.URLUtils;
import com.tle.common.filesystem.FileEntry;
import com.tle.core.filesystem.CustomisationFile;
import com.tle.core.guice.Bind;
import com.tle.core.services.FileSystemService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

/**
 * Where login notice images live: naming, de-duplication, retrieval, and clean up of the ones a
 * notice no longer references.
 *
 * <p>Storage only. Whether the content is fit to be stored, and whether the caller is allowed to
 * store it, are both settled before anything reaches this class - see {@link
 * LoginNoticeImageValidator} and {@link LoginNoticeServiceImpl}.
 */
@Bind
@Singleton
public class LoginNoticeImageStore {

  private static final String LOGIN_NOTICE_IMAGE_FOLDER_NAME = "loginnoticeimages/";

  private final FileSystemService fileSystemService;

  @Inject
  public LoginNoticeImageStore(FileSystemService fileSystemService) {
    this.fileSystemService = fileSystemService;
  }

  /**
   * Stores the supplied image under the supplied name, or a variation of it when that name is
   * taken.
   *
   * @param imageBytes Content of the image, already validated by the caller.
   * @param name File name the image was uploaded under, which may already be in use.
   * @return The name the image was stored under, which is {@code name} unless it had to be made
   *     unique.
   */
  public String save(byte[] imageBytes, String name) throws IOException {
    String nameToUse = iterateImageNameIfDuplicateExists(name);
    fileSystemService.write(
        new CustomisationFile(),
        getLoginNoticeImageFileName(nameToUse),
        new ByteArrayInputStream(imageBytes),
        false);
    return nameToUse;
  }

  /**
   * Reads the stored image with the provided name.
   *
   * @param name File name of the image, as returned by {@link #save}.
   * @return A stream of the image content, or empty if no image is stored under that name. The
   *     caller is responsible for closing the stream.
   */
  public Optional<InputStream> read(String name) throws IOException {
    CustomisationFile customisationFile = new CustomisationFile();
    String fileName = getLoginNoticeImageFileName(name);
    return fileSystemService.fileExists(customisationFile, fileName)
        ? Optional.of(fileSystemService.read(customisationFile, fileName))
        : Optional.empty();
  }

  /** Removes every stored image, for when the notice they belonged to is gone. */
  public void deleteAll() {
    fileSystemService.removeFile(new CustomisationFile(), LOGIN_NOTICE_IMAGE_FOLDER_NAME);
  }

  /**
   * Removes the stored images the supplied notice no longer references, so images dropped while
   * editing do not accumulate.
   *
   * @param notice HTML of the notice being saved.
   */
  public void removeUnusedImages(String notice) throws IOException {
    CustomisationFile customisationFile = new CustomisationFile();
    FileEntry[] fileNameList =
        fileSystemService.enumerate(customisationFile, LOGIN_NOTICE_IMAGE_FOLDER_NAME, null);
    Elements imgList = Jsoup.parse(notice).getElementsByTag("img");
    List<String> srcList = imgList.eachAttr("src");
    for (FileEntry imageFile : fileNameList) {
      boolean imageFileUsed = false;
      for (String src : srcList) {
        String srcDecoded = URLUtils.basicUrlDecode(src);
        if (srcDecoded.contains(imageFile.getName())) {
          imageFileUsed = true;
        }
      }
      if (!imageFileUsed) {
        fileSystemService.removeFile(
            customisationFile, getLoginNoticeImageFileName(imageFile.getName()));
      }
    }
  }

  private String iterateImageNameIfDuplicateExists(String name) {
    CustomisationFile customisationFile = new CustomisationFile();
    String nameWithoutExtension = FilenameUtils.removeExtension(name);
    String extension = '.' + FilenameUtils.getExtension(name);
    if (fileSystemService.fileExists(customisationFile, getLoginNoticeImageFileName(name))) {
      int i = 0;
      String uniqueFileName;
      do {
        uniqueFileName = (nameWithoutExtension + '_' + (++i) + extension);
      } while (fileSystemService.fileExists(
          customisationFile, getLoginNoticeImageFileName(uniqueFileName)));
      return uniqueFileName;
    }
    return name;
  }

  private String getLoginNoticeImageFileName(String fileName) {
    return LOGIN_NOTICE_IMAGE_FOLDER_NAME + fileName;
  }
}
