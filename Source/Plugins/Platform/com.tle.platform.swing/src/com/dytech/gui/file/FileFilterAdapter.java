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

package com.dytech.gui.file;

import com.dytech.common.io.FileExtensionFilter;
import java.io.File;

/**
 * Adapts java.io.FileFilter or javax.swing.filechooser.FileFilter to implement both interfaces.
 *
 * @author Nicholas Read
 */
public class FileFilterAdapter extends javax.swing.filechooser.FileFilter {
  private java.io.FileFilter filter;
  private String description;

  public FileFilterAdapter(java.io.FileFilter filter, String description) {
    this.filter = filter;
    this.description = description;
  }

  @Override
  public boolean accept(File f) {
    return filter.accept(f);
  }

  @Override
  public String getDescription() {
    return description;
  }

  public static FileFilterAdapter IMAGES() {
    return new FileFilterAdapter(FileExtensionFilter.IMAGES(), "Image Files");
  }

  public static FileFilterAdapter HTML() {
    return new FileFilterAdapter(FileExtensionFilter.HTML(), "HTML Files");
  }

  public static FileFilterAdapter ZIP() {
    return new FileFilterAdapter(FileExtensionFilter.ZIP(), "ZIP Archives");
  }

  public static FileFilterAdapter XML() {
    return new FileFilterAdapter(FileExtensionFilter.XML(), "XML Files");
  }

  public static FileFilterAdapter XSLT() {
    return new FileFilterAdapter(FileExtensionFilter.XSLT(), "XSL Transformations");
  }
}
