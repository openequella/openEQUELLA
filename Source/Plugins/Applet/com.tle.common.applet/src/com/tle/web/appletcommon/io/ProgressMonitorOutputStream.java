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

package com.tle.web.appletcommon.io;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ProgressMonitorOutputStream extends BufferedOutputStream {
  private final ProgressMonitorCallback callback;

  public ProgressMonitorOutputStream(OutputStream delegate, ProgressMonitorCallback callback) {
    super(delegate);
    this.callback = callback;
  }

  /**
   * For large files, it's disastrous to call the FilterOutputStream's implementations of write(...)
   * - which results in calls the callback for every byte written to the output stream. Accordingly,
   * class inheritance altered to bypass FilterOutputStream. See Redmine #6092.
   */
  @Override
  public synchronized void write(int b) throws IOException {
    super.write(b);
  }

  @Override
  public synchronized void write(byte[] b, int off, int len) throws IOException {
    super.write(b, off, len);
    if (len > 0 && callback != null) {
      callback.addToProgress(len);
    }
  }
}
