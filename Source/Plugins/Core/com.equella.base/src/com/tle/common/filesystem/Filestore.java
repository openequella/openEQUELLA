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

package com.tle.common.filesystem;

import com.tle.annotation.NonNullByDefault;
import java.nio.file.Path;

@NonNullByDefault
public class Filestore {
  private final String id;
  private final String name;
  private final Path root;

  public Filestore(String id, String name, Path root) {
    this.id = id;
    this.name = name;
    this.root = root;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Path getRoot() {
    return root;
  }

  @SuppressWarnings("nls")
  @Override
  public String toString() {
    return "(" + id + ") " + name + ": " + root;
  }
}
