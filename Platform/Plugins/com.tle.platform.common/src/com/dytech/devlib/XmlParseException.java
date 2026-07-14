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

package com.dytech.devlib;

import java.io.Serial;

/**
 * Thrown when {@link PropBagEx} fails to parse XML input - for example malformed XML, or XML that
 * contains a DOCTYPE declaration (which is rejected to prevent XXE injection).
 *
 * <p>This almost always indicates bad caller-supplied input rather than an internal fault, so the
 * REST layer maps it to a {@code 400 Bad Request}. It extends {@link RuntimeException} to preserve
 * the previous throwing behaviour for existing callers.
 */
public class XmlParseException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public XmlParseException(String message, Throwable cause) {
    super(message, cause);
  }
}
