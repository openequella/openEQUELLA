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

package com.tle.web.selection.contribute;

import com.google.common.base.Throwables;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.item.service.ItemResolver;
import com.tle.core.quickupload.service.QuickUploadService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.ajaxupload.AjaxCallbackResponse;
import com.tle.web.sections.equella.ajaxupload.AjaxUpload;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.generic.function.PartiallyApply;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.FileUpload;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.selection.SelectAttachmentHandler;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.contribute.QuickUploadSection.QuickUploadModel;
import com.tle.web.selection.event.AttachmentSelectorEvent;
import com.tle.web.selection.event.AttachmentSelectorEventListener;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.ViewableItemResolver;
import java.io.IOException;
import javax.inject.Inject;

/** Used for quick upload on the contribution page when in a selection session */
@Bind
@SuppressWarnings("nls")
public class QuickUploadSection extends AbstractPrototypeSection<QuickUploadModel>
    implements HtmlRenderer, ViewableChildInterface, AttachmentSelectorEventListener {
  @Component private FileUpload fileUploader;
  @ViewFactory private FreemarkerFactory viewFactory;
  @EventFactory protected EventGenerator events;
  @AjaxFactory private AjaxGenerator ajax;

  @Inject private QuickUploadService quickUploadService;
  @Inject private SelectionService selectionService;
  @Inject private BundleCache bundleCache;
  @Inject private ItemResolver itemResolver;
  @Inject private ViewableItemResolver viewableItemResolver;
  private JSAssignable validateFile;

  @Override
  public SectionResult renderHtml(RenderEventContext context) {
    if (canView(context)) {
      fileUploader.setValidateFile(context, validateFile);
      fileUploader.setAjaxUploadUrl(context, ajax.getAjaxUrl(context, "upload"));
      final ItemDefinition collection = quickUploadService.getOneClickItemDef();
      if (collection != null) {
        getModel(context)
            .setCollectionName(
                new BundleLabel(collection.getName(), collection.getUuid(), bundleCache));
      }

      return viewFactory.createResult("quickupload.ftl", context);
    }

    return null;
  }

  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);
    tree.addListener(null, AttachmentSelectorEventListener.class, this);
    validateFile =
        AjaxUpload.simpleUploadValidator(
            "uploadProgress",
            PartiallyApply.partial(events.getSubmitValuesFunction("finishedUpload"), 2));
  }

  public static class UploadValidation extends AjaxCallbackResponse {
    private boolean returnFromSession;

    public boolean isReturnFromSession() {
      return returnFromSession;
    }

    public void setReturnFromSession(boolean returnFromSession) {
      this.returnFromSession = returnFromSession;
    }
  }

  @EventHandlerMethod
  public void finishedUpload(SectionInfo info, String uploadId, UploadValidation upload) {
    if (upload.isReturnFromSession()) {
      selectionService.returnFromSession(info);
    }
  }

  @AjaxMethod
  public UploadValidation upload(SectionInfo info) throws Exception {
    UploadValidation val = new UploadValidation();
    final String filename = fileUploader.getFilename(info);
    if (fileUploader.getFileSize(info) > 0 && !Check.isEmpty(filename)) {
      try {
        Pair<ItemId, Attachment> attInfo =
            quickUploadService.createOrSelectExisting(fileUploader.getInputStream(info), filename);
        ViewableItem<?> vitem =
            viewableItemResolver.createViewableItem(
                itemResolver.getItem(attInfo.getFirst(), null), null);
        SelectAttachmentHandler selectAttachmentHandler =
            selectionService.getSelectAttachmentHandler(info, vitem, null);

        if (selectAttachmentHandler != null) {
          selectAttachmentHandler.handleAttachmentSelection(
              info, attInfo.getFirst(), attInfo.getSecond(), null, false);
          val.setReturnFromSession(true);
        }
      } catch (IOException e) {
        Throwables.propagate(e);
      }
    }
    return val;
  }

  @Override
  public boolean canView(SectionInfo info) {
    final ItemDefinition itemdef = quickUploadService.getOneClickItemDef();
    final SelectionSession ss = selectionService.getCurrentSession(info);
    if (ss == null) {
      return false;
    }

    boolean quick = false;
    boolean cont = false;

    if (itemdef != null
        && (ss.isAllContributionCollections()
            || ss.getContributionCollectionIds().contains(itemdef.getUuid()))) {
      quick = true;
    }
    if (ss.isAllContributionCollections() || !ss.getContributionCollectionIds().isEmpty()) {
      cont = true;
    }

    return quick && cont;
  }

  @Override
  public void supplyFunction(SectionInfo info, AttachmentSelectorEvent event) {
    if (event.getHandler() == null) {
      event.setHandler(this);
    }
  }

  @Override
  public void handleAttachmentSelection(
      SectionInfo info,
      ItemId itemId,
      IAttachment attachment,
      String extensionType,
      boolean canForward) {
    selectionService.addSelectedResource(
        info,
        selectionService.createAttachmentSelection(info, itemId, attachment, null, null),
        canForward);
  }

  @Override
  public Object instantiateModel(SectionInfo info) {
    return new QuickUploadModel();
  }

  public FileUpload getFileUploader() {
    return fileUploader;
  }

  public static class QuickUploadModel {
    private Label collectionName;

    public Label getCollectionName() {
      return collectionName;
    }

    public void setCollectionName(Label collectionName) {
      this.collectionName = collectionName;
    }
  }
}
