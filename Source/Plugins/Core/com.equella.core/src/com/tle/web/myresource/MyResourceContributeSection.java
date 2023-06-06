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

package com.tle.web.myresource;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.Attachments;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.Utils;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.services.FileSystemService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.core.util.archive.ArchiveProgress;
import com.tle.mycontent.ContentHandlerSection;
import com.tle.mycontent.service.MyContentFields;
import com.tle.mycontent.service.MyContentService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.ajaxupload.AjaxCallbackResponse;
import com.tle.web.sections.equella.ajaxupload.AjaxUpload;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.PartiallyApply;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Div;
import com.tle.web.sections.standard.FileDrop;
import com.tle.web.sections.standard.FileUpload;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;
import com.tle.web.template.RenderNewTemplate;
import com.tle.web.template.section.event.BlueBarEvent;
import com.tle.web.template.section.event.BlueBarEventListener;
import com.tle.web.template.section.event.BlueBarRenderable;
import com.tle.web.viewable.impl.ViewableItemFactory;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;

@SuppressWarnings("nls")
@Bind
public class MyResourceContributeSection
    extends AbstractPrototypeSection<MyResourceContributeSection.MyResourceModel>
    implements HtmlRenderer, ContentHandlerSection, BlueBarEventListener {
  public static final String TOKEN_PARAMETER = "SESSION"; // $NON-NLS-1$

  private static final String UPLOAD_ONLY = "uploadonly";
  private static final String UNARCHIVE_ONLY = "unarchiveonly";
  private static final String UNARCHIVE_DELETE = "unarchivedelete";

  private static final PluginResourceHelper URL_HELPER =
      ResourcesService.getResourceHelper(MyResourceContributeSection.class);

  private static String DOWNLOAD_OFFICE_INTEG_URL = "https://equella.github.io/";

  @PlugKey("dnd.upload")
  private static String STRING_UPLOAD;

  @PlugKey("dnd.extract")
  private static String STRING_EXTRACT;

  @PlugKey("dnd.extractdelete")
  private static String STRING_EXTRACT_DELETE;

  @PlugKey("button.upload")
  private static Label LABEL_UPLOAD;

  @PlugKey("button.saveedit")
  private static Label LABEL_SAVE_EDIT;

  @PlugKey("breadcrumb.link.scrapbook")
  private static Label LABEL_SCRAPBOOK;

  @PlugKey("label.edit.single")
  private static Label LABEL_EDIT_SINGLE;

  @PlugKey("label.download.officeintegration")
  private static Label LABEL_DOWNLOAD_OFFICE_INTEGRATION;

  @PlugKey("label.replacefile")
  private static Label LABEL_REPLACE_FILE;

  @PlugKey("label.downloads")
  private static Label LABEL_DOWNLOADS;

  @Inject private ConfigurationService configService;
  @Inject private ItemService itemService;
  @Inject private MyContentService myContentService;
  @Inject private FileSystemService fileSystemService;
  @Inject private ReceiptService receiptService;
  @Inject private ViewableItemFactory viewableItemService;
  @Inject private AttachmentResourceService attachmentResourceService;
  @Inject private ItemOperationFactory workflowFactory;
  @Inject private StagingService stagingService;

  @ViewFactory private FreemarkerFactory viewFactory;
  @EventFactory private EventGenerator events;
  @AjaxFactory private AjaxGenerator ajax;

  @Component(name = "dndt")
  private TextField dndTagsField;

  @Component(name = "t")
  private TextField tagsField;

  @Component(name = "d")
  private TextField descriptionField;

  @Component(name = "f")
  private FileUpload fileUploader;

  @Component(name = "s")
  private Button saveButton;

  @PlugKey("button.returntoscrapbook")
  @Component(name = "c")
  private Button cancelButton;

  @Component(name = "rt")
  private Div returnToScrapbookButtonDiv;

  @Component(name = "fm")
  private FileDrop fileDrop;

  @Component
  @PlugKey("dnd.archiveoptinos")
  private SingleSelectionList<FileDropAction> archiveOptionsDropDown;

  private static final IncludeFile INCLUDE =
      new IncludeFile(URL_HELPER.url("scripts/scrapbook.js"));
  private static final JSCallAndReference SCRAPBOOK_CLASS =
      new ExternallyDefinedFunction("Scrapbook", INCLUDE);

  private static final ExternallyDefinedFunction VALIDATE_FILE =
      new ExternallyDefinedFunction(SCRAPBOOK_CLASS, "validateFile", 0);
  private JSAssignable validateFile;

  public Label getEditTitle() {
    return LABEL_EDIT_SINGLE;
  }

  public Label getEditFileLabel() {
    return LABEL_REPLACE_FILE;
  }

  @Override
  public SectionResult renderHtml(RenderEventContext context) {
    final MyResourceModel model = getModel(context);

    fileDrop.setAjaxUploadUrl(context, ajax.getAjaxUrl(context, "dndUpload"));
    fileDrop.setValidateFile(
        context,
        Js.functionValue(
            Js.call(
                VALIDATE_FILE, PartiallyApply.partial(ajax.getAjaxFunction("contributeDND"), 3))));

    saveButton.setLabel(context, model.isEditing() ? LABEL_SAVE_EDIT : LABEL_UPLOAD);
    archiveOptionsDropDown.setRendererType(context, "bootstrapsplitdropdown");
    Map<String, String> toggleAttrs = new HashMap<>();
    toggleAttrs.put(
        TagRenderer.ARIA_LABEL, archiveOptionsDropDown.getState(context).getLabelText());
    archiveOptionsDropDown.getState(context).setAttribute("toggleAttrs", toggleAttrs);
    if (model.isEditing()) {
      final ItemId itemId = new ItemId(model.getEditItem());
      final FileAttachment attachment = getAttachment(context, itemId);
      final ViewableResource attachmentResource =
          attachmentResourceService.getViewableResource(
              context, viewableItemService.createNewViewableItem(itemId), attachment);
      final Label description = new TextLabel(attachmentResource.getDescription());
      final ImageRenderer thumbImage =
          attachmentResource.createStandardThumbnailRenderer(description);
      model.setThumbnail(thumbImage);
      model.setFilenameLabel(new TextLabel(attachment.getFilename(), false));
      fileUploader.setAjaxUploadUrl(context, ajax.getAjaxUrl(context, "replaceFile"));
      fileUploader.setValidateFile(context, validateFile);
      return viewFactory.createResult("editscrapbook.ftl", context);
    } else {
      return viewFactory.createResult("contributescrapbook.ftl", context);
    }
  }

  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);

    archiveOptionsDropDown.setListModel(
        new SimpleHtmlListModel<FileDropAction>(
            new FileDropAction(STRING_UPLOAD, UPLOAD_ONLY, false, false),
            new FileDropAction(STRING_EXTRACT, UNARCHIVE_ONLY, true, false),
            new FileDropAction(STRING_EXTRACT_DELETE, UNARCHIVE_DELETE, true, true)) {
          @Override
          protected Option<FileDropAction> convertToOption(FileDropAction obj) {
            return new NameValueOption<MyResourceContributeSection.FileDropAction>(
                new BundleNameValue(obj.getName(), obj.getValue()), obj);
          }
        });

    archiveOptionsDropDown.setAlwaysSelect(true);
    archiveOptionsDropDown.setEventHandler(
        JSHandler.EVENT_CHANGE,
        events.getNamedHandler("changeType", archiveOptionsDropDown.createGetExpression()));

    saveButton.setClickHandler(events.getSubmitValuesFunction("contribute"));
    cancelButton.setClickHandler(events.getSubmitValuesFunction("cancel"));
    cancelButton.setCancel(true);

    JSCallable editFileAjaxFunction =
        ajax.getAjaxUpdateDomFunction(
            tree, null, events.getEventHandler("editFile"), "editFileAjaxDiv");

    validateFile =
        AjaxUpload.simpleUploadValidator(
            "uploadProgress",
            PartiallyApply.partial(events.getSubmitValuesFunction("finishedFile"), 2));
  }

  private FileAttachment getAttachment(SectionInfo info, ItemId itemId) {
    final Item item = getItem(info, itemId);
    final Attachments attachments = new UnmodifiableAttachments(item);
    final List<FileAttachment> files = attachments.getList(AttachmentType.FILE);
    return files.get(0);
  }

  private Item getItem(SectionInfo info, ItemId itemId) {
    Item item = info.getAttribute("MyResourceContributionSection.item");
    if (item == null) {
      item = itemService.get(itemId);
      info.setAttribute("MyResourceContributionSection.item", item);
    }
    return item;
  }

  @EventHandlerMethod
  public void changeType(SectionInfo info, String type) {
    archiveOptionsDropDown.setSelectedStringValue(info, type);
  }

  public SingleSelectionList<FileDropAction> getArchiveOptionsDropDown() {
    return archiveOptionsDropDown;
  }

  public void addCrumbs(SectionInfo info, Decorations decorations, Breadcrumbs crumbs) {
    final SectionInfo fwd = info.createForward("/access/myresources.do");
    final HtmlLinkState state = new HtmlLinkState();
    state.setBookmark(new InfoBookmark(fwd));
    state.setLabel(LABEL_SCRAPBOOK);
    crumbs.add(state);
  }

  @Override
  public List<HtmlComponentState> getMajorActions(RenderContext context) {
    return null;
  }

  @Override
  public List<HtmlComponentState> getMinorActions(RenderContext context) {
    return null;
  }

  @Override
  public Class<MyResourceModel> getModelClass() {
    return MyResourceModel.class;
  }

  @Override
  public String getDefaultPropertyName() {
    return "myr";
  }

  private void processStream(SectionInfo info, InputStream stream, String filename) {
    final ItemId itemId = getItemId(info);
    final boolean newItem = (itemId == null);

    final List<WorkflowOperation> ops = new ArrayList<WorkflowOperation>();
    if (newItem) {
      ops.add(workflowFactory.create(myContentService.getMyContentItemDef(), ItemStatus.PERSONAL));
    } else {
      // handle editing
      ops.add(workflowFactory.startEdit(false));
    }

    final String tags = dndTagsField.getValue(info);

    final MyContentFields fields = new MyContentFields();
    fields.setResourceId(MyResourceConstants.MYRESOURCE_CONTENT_TYPE);
    fields.setTags(tags);
    fields.setTitle(filename);
    StagingFile staging = stagingService.createStagingArea();
    try {
      fileSystemService.write(staging, filename, stream, false);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    ops.add(
        myContentService.getEditOperation(fields, filename, staging.getUuid(), !newItem, !newItem));
    ops.add(workflowFactory.save());
    itemService.operation(itemId, ops.toArray(new WorkflowOperation[ops.size()]));
  }

  private boolean isArchiveFile(String filename) {
    if (filename.endsWith(".zip")
        || filename.endsWith(".jar")
        || filename.endsWith(".war")
        || filename.endsWith(".tar.bz2")
        || filename.endsWith(".tar.gz")) {
      return true;
    }
    return false;
  }

  /** @throws IOException */
  @AjaxMethod
  public boolean contributeDND(SectionInfo info, String stagingId, String filename)
      throws IOException {
    FileDropAction action = archiveOptionsDropDown.getSelectedValue(info);
    final SectionInfo theInfo = info;
    String fn = "";
    if (!Check.isEmpty(filename)) {
      fn = filename.toLowerCase();
    }
    StagingFile stagingSrc = new StagingFile(stagingId);
    InputStream stream = fileSystemService.read(stagingSrc, "file");

    if (isArchiveFile(fn)) {
      if (action.isExtract()) {
        final StagingFile staging = stagingService.createStagingArea();
        fileSystemService.write(staging, filename, stream, false);
        try {
          final int[] count = {0};

          fileSystemService.unzipFile(
              staging,
              filename,
              "",
              new ArchiveProgress(0) {
                @Override
                public void nextEntry(String entryPath) {
                  final File file = fileSystemService.getExternalFile(staging, entryPath);
                  if (!file.isDirectory() && file.canRead()) {
                    count[0]++;
                  }
                  try {
                    processStream(theInfo, new FileInputStream(file), file.getName());
                  } catch (FileNotFoundException e) {
                    e.printStackTrace();
                  }
                }
              });
          fileSystemService.removeFile(staging);
        } catch (IOException ex) {
          throw new RuntimeException("Error extracting archive", ex);
        }
      }
    }
    if ((!action.isCommitArchive() && isArchiveFile(fn)) || !isArchiveFile(fn)) {
      this.processStream(info, stream, filename);
    }
    fileSystemService.removeFile(stagingSrc);
    return true;
  }

  /** @throws IOException */
  @AjaxMethod
  public String dndUpload(SectionInfo info) throws IOException {
    final StagingFile staging = stagingService.createStagingArea();
    fileSystemService.write(staging, "file", fileDrop.getInputStream(info), false);
    return staging.getUuid();
  }

  public static class UploadValidation extends AjaxCallbackResponse {
    private String filename;
    private String uuid;

    public String getFilename() {
      return filename;
    }

    public void setFilename(String filename) {
      this.filename = filename;
    }

    public String getUuid() {
      return uuid;
    }

    public void setUuid(String uuid) {
      this.uuid = uuid;
    }
  }

  @EventHandlerMethod
  public void finishedFile(SectionInfo info, String uploadId, UploadValidation upload) {
    MyResourceModel model = getModel(info);
    model.setStagingId(upload.uuid);
    model.setFilename(upload.filename);
  }

  @EventHandlerMethod
  public void contribute(SectionInfo info) {
    final List<WorkflowOperation> ops = new ArrayList<WorkflowOperation>();
    MyResourceModel model = getModel(info);
    ItemId itemId = getItemId(info);
    final String tags = tagsField.getValue(info);
    String title = descriptionField.getValue(info).trim();
    String filename = model.getFilename();
    if (Check.isEmpty(title)) {
      title = filename;
    }
    boolean fileAttached = filename != null;
    String stagingId = fileAttached ? model.getStagingId() : null;
    ops.add(workflowFactory.startEdit(fileAttached));

    final MyContentFields fields = new MyContentFields();
    fields.setResourceId(MyResourceConstants.MYRESOURCE_CONTENT_TYPE);
    fields.setTags(tags);
    fields.setTitle(title);

    ops.add(
        myContentService.getEditOperation(fields, filename, stagingId, fileAttached, fileAttached));
    ops.add(workflowFactory.save());

    itemService.operation(itemId, ops.toArray(new WorkflowOperation[ops.size()]));

    // In New UI we will return to New UI after the editing is successful so there is no need
    // to show the receipt.
    if (!RenderNewTemplate.isNewUIEnabled()) {
      String key = URL_HELPER.key("edit.success");
      receiptService.setReceipt(new KeyLabel(key, Utils.ent(title)));
    }

    myContentService.returnFromContribute(info);
  }

  @AjaxMethod
  public UploadValidation replaceFile(SectionInfo info) throws IOException {
    UploadValidation val = new UploadValidation();
    String filename = fileUploader.getFilename(info);

    InputStream stream = fileUploader.getInputStream(info);
    StagingFile staging = stagingService.createStagingArea();
    fileSystemService.write(staging, filename, stream, false);
    val.setUuid(staging.getUuid());
    val.setFilename(filename);
    return val;
  }

  private InputStream fileStreamFromStaging(SectionInfo info, ItemId itemId) throws IOException {
    final MyResourceModel model = getModel(info);
    final StagingFile staging = new StagingFile(model.getStagingId());
    final String filename = getAttachment(info, itemId).getFilename();
    if (fileSystemService.fileExists(staging, filename)) {
      return fileSystemService.read(staging, filename);
    }
    return null;
  }

  @EventHandlerMethod
  public void cancel(SectionInfo info) {
    myContentService.returnFromContribute(info);
  }

  @EventHandlerMethod
  public void editFile(SectionInfo info, boolean openWith) {
    MyResourceModel model = getModel(info);
    model.setLoadApplet(true);
    model.setOpenWith(openWith);
  }

  private ItemId getItemId(SectionInfo info) {
    MyResourceModel model = getModel(info);
    ItemId itemId = null;
    String editItem = model.getEditItem();
    if (editItem != null) {
      itemId = new ItemId(editItem);
    }
    return itemId;
  }

  public TextField getTagsField() {
    return tagsField;
  }

  public TextField getDndTagsField() {
    return this.dndTagsField;
  }

  public TextField getDescriptionField() {
    return descriptionField;
  }

  public void edit(SectionInfo info, ItemId itemId) {
    final MyResourceModel model = getModel(info);
    model.setEditItem(itemId.toString());
    model.setStagingId(UUID.randomUUID().toString());

    final MyContentFields fields = myContentService.getFieldsForItem(itemId);
    tagsField.setValue(info, fields.getTags());
    descriptionField.setValue(info, fields.getTitle());
  }

  public FileUpload getFileUploader() {
    return fileUploader;
  }

  public FileDrop getFileDrop() {
    return this.fileDrop;
  }

  public Button getSaveButton() {
    return saveButton;
  }

  public Button getCancelButton() {
    return cancelButton;
  }

  public Div getReturnToScrapbookButtonDiv() {
    return returnToScrapbookButtonDiv;
  }

  @Override
  public void addBlueBarResults(RenderContext context, BlueBarEvent event) {
    final HtmlLinkState downloadOfficeIntegrationLink = new HtmlLinkState();
    downloadOfficeIntegrationLink.setBookmark(new SimpleBookmark(DOWNLOAD_OFFICE_INTEG_URL));
    downloadOfficeIntegrationLink.setLabel(LABEL_DOWNLOAD_OFFICE_INTEGRATION);
    event.addTab(
        new BlueBarRenderable(
            "downloads",
            LABEL_DOWNLOADS,
            viewFactory.createResultWithModel("downloads.ftl", downloadOfficeIntegrationLink),
            50));
  }

  public static class FileDropAction extends NameValue {
    private static final long serialVersionUID = 1L;
    private boolean commitArchive;
    private boolean extract;

    public FileDropAction(String name, String value) {
      this(name, value, false, false);
    }

    public FileDropAction(String name, String value, boolean extract, boolean commitArchive) {
      super(name, value);

      this.extract = extract;
      this.commitArchive = commitArchive;
    }

    public void setExtract(boolean extract) {
      this.extract = extract;
    }

    public void setCommitArchive(boolean commitArchive) {
      this.commitArchive = commitArchive;
    }

    public boolean isExtract() {
      return extract;
    }

    public boolean isCommitArchive() {
      return commitArchive;
    }
  }

  public static class MyResourceModel {
    @Bookmarked(name = "i")
    private String editItem;

    @Bookmarked(name = "e")
    private String errorKey;

    private ImageRenderer thumbnail;
    private Label filenameLabel;

    private boolean loadApplet;
    private boolean openWith;

    @Bookmarked(name = "s")
    private String stagingId;

    @Bookmarked(name = "fn")
    private String filename;

    @Bookmarked(name = "w")
    private boolean editWith;

    public String getErrorKey() {
      return errorKey;
    }

    public void setErrorKey(String errorKey) {
      this.errorKey = errorKey;
    }

    public boolean isEditing() {
      return editItem != null;
    }

    public String getEditItem() {
      return editItem;
    }

    public void setEditItem(String editItem) {
      this.editItem = editItem;
    }

    public ImageRenderer getThumbnail() {
      return thumbnail;
    }

    public void setThumbnail(ImageRenderer thumbnail) {
      this.thumbnail = thumbnail;
    }

    public Label getFilenameLabel() {
      return filenameLabel;
    }

    public void setFilenameLabel(Label filenameLabel) {
      this.filenameLabel = filenameLabel;
    }

    public boolean isLoadApplet() {
      return loadApplet;
    }

    public void setLoadApplet(boolean loadApplet) {
      this.loadApplet = loadApplet;
    }

    public boolean isOpenWith() {
      return openWith;
    }

    public void setOpenWith(boolean openWith) {
      this.openWith = openWith;
    }

    public String getStagingId() {
      return stagingId;
    }

    public void setStagingId(String stagingId) {
      this.stagingId = stagingId;
    }

    public boolean isEditWith() {
      return editWith;
    }

    public void setEditWith(boolean editWith) {
      this.editWith = editWith;
    }

    public String getFilename() {
      return filename;
    }

    public void setFilename(String filename) {
      this.filename = filename;
    }
  }
}
