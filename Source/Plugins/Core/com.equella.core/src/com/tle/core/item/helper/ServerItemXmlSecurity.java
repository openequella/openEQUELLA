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

package com.tle.core.item.helper;

import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.common.security.Privilege;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemResolver;
import com.tle.core.security.TLEAclManager;
import com.tle.core.url.URLCheckerService;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind(ItemXmlSecurity.class)
@Singleton
public class ServerItemXmlSecurity implements ItemXmlSecurity {

  private final TLEAclManager aclService;
  private final URLCheckerService urlCheckerService;
  private final ItemResolver itemResolver;

  @Inject
  public ServerItemXmlSecurity(
      TLEAclManager aclService, URLCheckerService urlCheckerService, ItemResolver itemResolver) {
    this.aclService = aclService;
    this.urlCheckerService = urlCheckerService;
    this.itemResolver = itemResolver;
  }

  @Override
  public boolean hasPrivilege(Item bean, Privilege privilege) {
    return aclService.hasPrivilege(bean, privilege);
  }

  @Override
  public boolean isUrlDisabled(String url) {
    return urlCheckerService.isUrlDisabled(url);
  }

  @Override
  public boolean checkRestrictedAttachment(Item bean, Attachment attachment) {
    return itemResolver.checkRestrictedAttachment(bean, attachment, null);
  }
}
