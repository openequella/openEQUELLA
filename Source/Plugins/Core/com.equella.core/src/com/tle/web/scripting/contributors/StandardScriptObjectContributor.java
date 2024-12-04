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

package com.tle.web.scripting.contributors;

import com.dytech.edge.common.Constants;
import com.dytech.edge.common.PropBagWrapper;
import com.tle.beans.item.DrmSettings;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.attachments.ModifiableAttachments;
import com.tle.common.scripting.objects.*;
import com.tle.common.scripting.service.ScriptContextCreationParams;
import com.tle.common.scripting.types.ItemScriptType;
import com.tle.common.util.Logger;
import com.tle.core.guice.Bind;
import com.tle.core.scripting.service.ScriptObjectContributor;
import com.tle.core.util.script.SearchScriptWrapper;
import com.tle.web.scripting.ScriptObjectFactory;
import com.tle.web.scripting.ScriptTypeFactory;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind
@Singleton
public class StandardScriptObjectContributor implements ScriptObjectContributor {
  private static final String ITEM = "item";
  private static final String XML = "xml";
  private static final String ITEM_STATUS = "status";

  @Deprecated private static final String SEARCH_WRAPPER = "search";

  @Inject private ScriptTypeFactory scriptTypeFactory;
  @Inject private ScriptObjectFactory scriptObjectFactory;

  // Context insensitive objects (singletons)
  @Inject @Deprecated private SearchScriptWrapper search;
  @Inject private UtilsScriptObject utils;
  @Inject private SystemScriptObject system;
  @Inject private ItemScriptObject items;
  @Inject private MimeScriptObject mimeScriptObject;
  @Inject private RegionalScriptObject regionalScriptObject;
  @Inject private CollectionScriptObject collectionScriptObject;

  @Override
  public void addScriptObjects(Map<String, Object> objects, ScriptContextCreationParams params) {
    // search is legacy. Should be using utils.search instead
    objects.put(SEARCH_WRAPPER, search);

    objects.put(UtilsScriptObject.DEFAULT_VARIABLE, utils);
    objects.put(ItemScriptObject.DEFAULT_VARIABLE, items);
    if (params.isAllowSystemCalls()) {
      objects.put(SystemScriptObject.DEFAULT_VARIABLE, system);
    }

    objects.put(UserScriptObject.DEFAULT_VARIABLE, scriptObjectFactory.createUser());

    if (params.getFileHandle() != null) {
      objects.put(
          FileScriptObject.DEFAULT_VARIABLE,
          scriptObjectFactory.createFile(params.getFileHandle()));
    }

    final PropBagWrapper wrapper = new PropBagWrapper();
    final ItemPack<Item> pack = params.getItemPack();
    if (pack != null) {
      wrapper.setPropBag(pack.getXml());

      final Item item = pack.getItem();
      if (item != null) {
        // for backwards compatability ONLY. Undocumented 'feature'. Use
        // the proper item wrapping script objects such as 'attachments'
        // or 'items'
        objects.put(ITEM, item);

        final ItemStatus status = item.getStatus();
        String s = Constants.BLANK;
        if (status != null) {
          // Needs to be lower case
          s = status.toString();
        }
        objects.put(ITEM_STATUS, s);

        objects.put(
            AttachmentsScriptObject.DEFAULT_VARIABLE,
            scriptObjectFactory.createAttachments(
                new ModifiableAttachments(item), params.getFileHandle()));

        objects.put(ItemScriptType.CURRENT_ITEM, scriptTypeFactory.createItem(item));

        objects.put(
            NavigationScriptObject.DEFAULT_VARIABLE, scriptObjectFactory.createNavigation(item));

        // Script functionality expanded to provide for setting as well as
        // getting DrmSetting values, so a DrmSettings object must not
        // be null
        DrmSettings drmSettings = item.getDrmSettings();
        if (drmSettings == null) {
          drmSettings = new DrmSettings();
        }
        objects.put(
            DrmScriptObject.DEFAULT_VARIABLE, scriptObjectFactory.createDrm(item, drmSettings));
      }
    }

    objects.put(XML, wrapper);

    objects.put(
        ImagesScriptObject.DEFAULT_VARIABLE,
        scriptObjectFactory.createImages(params.getFileHandle()));

    objects.put(MimeScriptObject.DEFAULT_VARIABLE, mimeScriptObject);
    objects.put(RegionalScriptObject.DEFAULT_VARIABLE, regionalScriptObject);

    // logger is an attribute - not important enough
    final Map<String, Object> attributes = params.getAttributes();
    final Logger logger = (Logger) attributes.get(LoggingScriptObject.DEFAULT_VARIABLE);
    if (logger != null) {
      objects.put(LoggingScriptObject.DEFAULT_VARIABLE, scriptObjectFactory.createLogger(logger));
    }

    objects.put(CollectionScriptObject.DEFAULT_VARIABLE, collectionScriptObject);
  }
}
