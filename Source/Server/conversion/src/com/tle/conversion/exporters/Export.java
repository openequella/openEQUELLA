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

package com.tle.conversion.exporters;

import java.io.IOException;
import java.rmi.server.ExportException;
import java.util.Collection;

public interface Export {
  /**
   * Exports the file specified by <code>in</code> to the file specified by <code>out</code>.
   *
   * @param in The file path for the input file.
   * @param out The file path to be output to.
   * @throws IOException If There is an error reading from or writing to either of the files.
   * @throws ExportException If there is an error exporting the file; eg. the input format is not
   *     supported.
   */
  void exportFile(String in, String out) throws IOException;

  Collection<String> getInputTypes();

  Collection<String> getOutputTypes();
}
