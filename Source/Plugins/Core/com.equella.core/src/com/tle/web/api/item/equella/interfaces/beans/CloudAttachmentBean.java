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

package com.tle.web.api.item.equella.interfaces.beans;

import java.util.Map;
import java.util.UUID;

public class CloudAttachmentBean extends EquellaAttachmentBean {

  private UUID providerId;
  private String vendorId;
  private String cloudType;
  private Map<String, Object> display;
  private Map<String, Object> meta;

  public UUID getProviderId() {
    return providerId;
  }

  public void setProviderId(UUID providerId) {
    this.providerId = providerId;
  }

  public String getVendorId() {
    return vendorId;
  }

  public void setVendorId(String vendorId) {
    this.vendorId = vendorId;
  }

  public String getCloudType() {
    return cloudType;
  }

  public void setCloudType(String cloudType) {
    this.cloudType = cloudType;
  }

  public Map<String, Object> getDisplay() {
    return display;
  }

  public void setDisplay(Map<String, Object> display) {
    this.display = display;
  }

  public Map<String, Object> getMeta() {
    return meta;
  }

  public void setMeta(Map<String, Object> meta) {
    this.meta = meta;
  }

  @Override
  public String getRawAttachmentType() {
    return "custom/cloud";
  }
}
