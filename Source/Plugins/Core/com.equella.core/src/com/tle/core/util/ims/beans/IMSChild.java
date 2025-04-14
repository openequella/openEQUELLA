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

package com.tle.core.util.ims.beans;

import com.tle.core.xstream.XMLDataChild;

public abstract class IMSChild extends IMSWrapper implements XMLDataChild {
  private static final long serialVersionUID = 1L;

  protected IMSChild parent;

  @Override
  public void setParentObject(Object parent) {
    this.parent = (IMSChild) parent;
  }

  @Override
  protected String getFullBase() {
    if (parent == null) {
      return super.getFullBase();
    } else {
      return parent.getFullBase() + super.getFullBase();
    }
  }

  public IMSChild getParent() {
    return parent;
  }

  public IMSManifest getRootManifest() {
    if (parent == null) {
      return (IMSManifest) this;
    } else {
      return parent.getRootManifest();
    }
  }
}
