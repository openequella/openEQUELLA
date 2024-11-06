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

package com.tle.web.wizard.impl;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.dytech.edge.common.FileNode;
import com.dytech.edge.common.ScriptContext;
import com.dytech.edge.exceptions.ItemNotFoundException;
import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.dytech.edge.exceptions.WorkflowException;
import com.dytech.edge.wizard.WizardException;
import com.dytech.edge.wizard.WizardTimeoutException;
import com.dytech.edge.wizard.beans.DRMPage;
import com.dytech.edge.wizard.beans.DefaultWizardPage;
import com.dytech.edge.wizard.beans.FixedMetadata;
import com.dytech.edge.wizard.beans.Metadata;
import com.dytech.edge.wizard.beans.NavPage;
import com.dytech.edge.wizard.beans.WizardPage;
import com.google.common.base.Strings;
import com.google.inject.Provider;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.Wizard;
import com.tle.beans.item.DrmSettings;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.ModifiableAttachments;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.beans.workflow.WorkflowStep;
import com.tle.common.Check;
import com.tle.common.filesystem.FileEntry;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.quota.exception.QuotaExceededException;
import com.tle.common.scripting.ScriptException;
import com.tle.common.scripting.service.ScriptingService;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.guice.BindFactory;
import com.tle.core.institution.InstitutionService;
import com.tle.core.item.dao.AttachmentDao;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.item.standard.operations.AbstractCloneOperation;
import com.tle.core.item.standard.operations.CloneFactory;
import com.tle.core.item.standard.operations.CloneOperation;
import com.tle.core.item.standard.operations.DuringSaveOperation;
import com.tle.core.item.standard.operations.MoveDirectOperation;
import com.tle.core.item.standard.operations.NewVersionOperation;
import com.tle.core.item.standard.operations.SaveOperation;
import com.tle.core.item.standard.operations.workflow.StatusOperation;
import com.tle.core.item.standard.service.MetadataMappingService;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.plugins.ClassBeanLocator;
import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.quota.service.QuotaService;
import com.tle.core.scripting.service.StandardScriptContextParams;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.wizard.LERepository;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.controls.universal.handlers.fileupload.FileUploadState;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.viewable.PreviewableItem;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.impl.ViewableItemFactory;
import com.tle.web.viewurl.FilestoreBookmark;
import com.tle.web.wizard.SimpleLERepository;
import com.tle.web.wizard.WebWizardPage;
import com.tle.web.wizard.WizardInfo;
import com.tle.web.wizard.WizardService;
import com.tle.web.wizard.WizardState;
import com.tle.web.wizard.WizardState.Operation;
import com.tle.web.wizard.WizardStateInterface;
import com.tle.web.wizard.page.WebWizardPageState;
import com.tle.web.wizard.page.WizardPageFactory;
import com.tle.web.wizard.scripting.WizardScriptConstants;
import com.tle.web.wizard.scripting.WizardScriptContextCreationParams;
import com.tle.web.wizard.scripting.WizardScriptObjectContributor;
import com.tle.web.wizard.scripting.objects.impl.ControlScriptWrapper;
import com.tle.web.wizard.scripting.objects.impl.PageScriptWrapper;
import com.tle.web.wizard.section.SelectThumbnailSection.ThumbnailOption;
import com.tle.web.wizard.section.model.DuplicateData;
import com.tle.web.workflow.tasks.ModerationService;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jmaginnis
 * @dytech.jira see Jira Defect TLE-596 : http://apps.dytech.com.au/jira/browse/TLE-596
 * @dytech.jira see Jira System Change Request (SCR) TLE-683 :
 *     http://apps.dytech.com.au/jira/browse/TLE-683
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind(WizardService.class)
@Singleton
public class WizardServiceImpl
    implements WizardService, WizardScriptObjectContributor, WizardPageFactory {
  private static final String WIZARD_INFO_KEY = "$WIZARDS$";
  private static String KEY_PFX =
      AbstractPluginService.getMyPluginId(WizardServiceImpl.class) + ".";

  @Inject private FileSystemService fileSystemService;
  @Inject private ItemService itemService;
  @Inject private ItemDefinitionService itemDefinitionService;
  @Inject private InstitutionService institutionService;
  @Inject private QuotaService quotaService;
  @Inject private FreeTextService freeTextService;
  @Inject private WizardDrmHelper wizardDrmHelper;
  @Inject private ScriptingService scriptingService;
  @Inject private ViewableItemFactory viewableItemFactory;
  @Inject private UserSessionService userSessionService;
  @Inject private ModerationService moderationService;
  @Inject private MetadataMappingService mappingService;
  @Inject private Provider<WebRepository> repositoryProvider;
  @Inject private Provider<WebWizardPage> pageProvider;
  @Inject private Provider<SimpleLERepository> simpleRepoProvider;
  @Inject private Provider<WizardInitOperation> initProvider;
  @Inject private Provider<StatusOperation> statusOpFactory;
  @Inject private ItemOperationFactory workflowFactory;
  @Inject private CloneFactory cloneFactory;
  @Inject private WizardOperationFactory wizardOpFactory;
  @Inject private AttachmentDao attachmentDao;

  private PluginTracker<WizardScriptObjectContributor> scriptObjectTracker;

  private static final Logger LOGGER = LoggerFactory.getLogger(WizardServiceImpl.class);

  public WizardServiceImpl() {
    super();
  }

  @Override
  public void doOperations(WizardState state, WorkflowOperation... operations) {
    itemService.operation(state.getItemId(), operations);
    refreshStatus(state);
  }

  /**
   * Do the actual save
   *
   * @dytech.jira see Jira Defect TLE-823 : http://apps.dytech.com.au/jira/browse/TLE-823
   */
  @Override
  public void doSave(WizardState state, boolean unlock, WorkflowOperation... operations) {
    String stagingId = state.getStagingId();
    if (!Check.isEmpty(stagingId)) {
      StagingFile stagingFile = new StagingFile(stagingId);
      fileSystemService.removeFile(stagingFile, FileUploadState.UPLOADS_FOLDER());
      try {
        quotaService.checkQuotaAndReturnNewItemSize(state.getItem(), stagingFile);
      } catch (QuotaExceededException e) {
        throw new WizardException(e.getMessage(), e);
      }
    }
    PropBagEx erroredXml = (PropBagEx) state.getItemxml().clone();
    PropBagEx docxml = state.getItemxml();

    transferMetaData(state, docxml);

    ScriptContext scriptContext = createScriptContext(state, null, null, null);
    ItemDefinition itemdefinition = state.getItemDefinition();
    mappingService.mapLiterals(itemdefinition, docxml, scriptContext);
    List<WorkflowOperation> oplist = new ArrayList<WorkflowOperation>();

    addUnsavedOperations(state, oplist, true);
    if (state.isLockedForEditing() || state.isNewItem()) {
      processDRM(state, docxml, scriptContext);
      oplist.add(workflowFactory.editMetadata(state.getItemPack()));
    }

    oplist.addAll(Arrays.asList(operations));
    oplist.add(workflowFactory.setItemThumbnail(ensureValidThumbnail(state)));
    oplist.add(createSaveOperation(state, unlock));
    StatusOperation statop = statusOpFactory.get();
    oplist.add(statop);
    oplist.add(initProvider.get());
    operations = oplist.toArray(new WorkflowOperation[oplist.size()]);
    ItemPack pack;
    try {
      pack = itemService.operation(state.getItemId(), operations);
      state.setItemPack(pack);
      state.setRedraftAfterSave(false);
      state.setWorkflowStatus(statop.getStatus());
      state.setLockedForEditing(false);
      state.setNewItem(false);
      state.setItemId(pack.getItemId());
    } catch (WorkflowException e) {
      state.getItemPack().setXml(erroredXml);
      Throwable cause = e.getCause();
      if (cause == null) {
        cause = e;
      }
      throw new WizardException(cause);
    }
  }

  private String ensureValidThumbnail(WizardState state) {
    final Item item = state.getItem();
    String thumb = state.getThumbnail();
    if (Strings.isNullOrEmpty(thumb)) {
      thumb = item.getThumb();
      if (thumb.contains("initial")) {
        return ThumbnailOption.DEFAULT.toString();
      }
    }

    if (thumb.contains(ThumbnailOption.CUSTOM.toString())) {
      for (Attachment attachment : item.getAttachments()) {
        String uuid = thumb.split(":")[1];
        if (uuid.equals(attachment.getUuid()) && !"suppress".equals(attachment.getThumbnail())) {
          return thumb;
        }
      }
      return ThumbnailOption.DEFAULT.toString();
    }
    return thumb;
  }

  private void processDRM(WizardState state, PropBagEx docxml, ScriptContext scriptContext) {
    PropBagEx oldRights = docxml.getSubtree("item/rights");
    if (oldRights != null) {
      DrmSettings drmSettings = null;
      DRMPage selectedDrm = null;
      List<DRMPage> drm = state.getDrm();
      for (DRMPage page : drm) {
        if (scriptingService.evaluateScript(page.getScript(), "drm", scriptContext)) {
          selectedDrm = page;
          break;
        }
      }
      if (selectedDrm != null) {
        String owner = state.getItem().getOwner();
        if (owner == null) {
          owner = CurrentUser.getUserID();
        }
        drmSettings = wizardDrmHelper.readItemxmlIntoDrmSettings(docxml, selectedDrm, owner);
      }

      if (drmSettings != null) {
        // need to set both ways!
        state.getItem().setDrmSettings(drmSettings);
        drmSettings.setItem(state.getItem());
      }

      docxml.deleteNode("item/rights");
    }
  }

  private SaveOperation createSaveOperation(WizardState state, boolean unlock) {
    Map<String, DuringSaveOperation> saveOperations = state.getSaveOperations();
    List<WorkflowOperation> preSaves = new ArrayList<WorkflowOperation>();
    List<WorkflowOperation> postSaves = new ArrayList<WorkflowOperation>();
    for (DuringSaveOperation saveOp : saveOperations.values()) {
      WorkflowOperation postOp = saveOp.createPostSaveWorkflowOperation();
      WorkflowOperation preOp = saveOp.createPreSaveWorkflowOperation();
      if (postOp != null) {
        postSaves.add(postOp);
      }
      if (preOp != null) {
        preSaves.add(preOp);
      }
    }
    return workflowFactory.saveWithOperations(unlock, preSaves, postSaves);
  }

  private void refreshStatus(WizardState state) {
    try {
      StatusOperation statop = statusOpFactory.get();
      List<WorkflowOperation> ops = new ArrayList<WorkflowOperation>();
      if (state.isNewItem()) {
        ops.add(wizardOpFactory.state(state));
      }
      ops.add(initProvider.get());
      ops.add(statop);
      itemService.operation(state.getItemId(), ops.toArray(new WorkflowOperation[ops.size()]));
      state.setWorkflowStatus(statop.getStatus());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Merges the values from each target/value pair in to the document bag.
   *
   * @dytech.jira see Jira Issue TLE-689 : http://apps.dytech.com.au/jira/browse/TLE-689
   */
  @Override
  public void transferMetaData(WizardState state, PropBagEx dest) {
    FixedMetadata metadata = state.getWizard().getMetadata();
    if (metadata != null) {
      for (Metadata data : metadata.getData()) {
        dest.setNode(data.getTarget(), data.getValue());
      }
    }
  }

  /**
   * @param state
   * @throws Exception
   */
  private void setupForNewItem(WizardState state) {
    state.setEditable(true);
    state.setNewItem(true);
    state.setLockedForEditing(false);
    state.setEntryThroughEdit(false);

    refreshStatus(state);
    injectDynamicXml(state.getItemPack());
  }

  /** This method adds addition XML to the metadata from the item, like DRM and CAL. */
  private void injectDynamicXml(ItemPack<Item> pack) {
    PropBagEx newitem = pack.getXml();
    Item item = pack.getItem();

    DrmSettings settings = item.getDrmSettings();
    if (settings != null) {
      wizardDrmHelper.writeItemxmlFromDrmSettings(newitem, settings);
    }
  }

  @Override
  public WizardState loadItem(ItemKey itemkey, boolean bEdit, boolean redraft) {
    WizardState state = new WizardState(Operation.EDITING);
    state.setNewItem(false);
    state.setMergeDRMDefaults(false);
    state.setItemId(itemkey);
    state.setRedraftAfterSave(redraft);
    state.setEntryThroughEdit(bEdit);
    loadAndLock(state, bEdit, false);
    return state;
  }

  @Override
  public void reload(WizardState state, boolean bEdit) {
    loadAndLock(state, bEdit, false);
  }

  @Override
  public void reloadSaveAndContinue(WizardState state) {
    loadAndLock(state, true, true);
  }

  protected void loadAndLock(WizardState state, boolean bEdit, boolean saveAndContinue) {
    // Simply for the ItemNotFoundException
    itemService.getUnsecure(state.getItemId());
    List<WorkflowOperation> ops = new ArrayList<WorkflowOperation>();
    if (bEdit) {
      if (state.isMovedItem()) {
        ops.add(workflowFactory.editForMove());
      } else if (state.isRedraftAfterSave()) {
        ops.add(workflowFactory.editForRedraft());
      } else if (saveAndContinue) {
        ops.add(workflowFactory.startEditForSaveAndContinue(state.getStagingId()));
      } else {
        ops.add(workflowFactory.startEdit(true));
      }
    }

    addUnsavedOperations(state, ops, false);
    StatusOperation statop = statusOpFactory.get();
    ops.add(statop);
    ItemPack<Item> itemPack =
        itemService.operation(state.getItemId(), ops.toArray(new WorkflowOperation[ops.size()]));
    state.setWorkflowStatus(statop.getStatus());
    state.setLockedForEditing(saveAndContinue || bEdit);
    state.setEditable(saveAndContinue || bEdit);
    state.setItemPack(itemPack);
    injectDynamicXml(itemPack);

    getPagesInternal(state, !state.isInDraft(), true);
  }

  private void addUnsavedOperations(
      WizardState state, List<WorkflowOperation> ops, boolean forSave) {
    if (!forSave) {
      ops.add(initProvider.get());
    }
    if (state.isRedraftAfterSave()) {
      ops.add(forSave ? workflowFactory.redraft() : workflowFactory.offlineRedraft());
    }
    List<UnsavedEditOperation> unsavedEdits = state.getUnsavedEdits();
    for (UnsavedEditOperation editCreator : unsavedEdits) {
      ops.add(editCreator.getOperation(forSave));
    }
    if (!forSave) {
      ops.add(initProvider.get());
    }
  }

  @Override
  public void newVersion(WizardState state) {
    try {
      ItemKey itemKey = state.getItemId();
      NewVersionOperation newVersionOp = workflowFactory.newVersion();
      ItemPack<Item> itemPack = itemService.operation(itemKey, newVersionOp, initProvider.get());

      Collection<DuringSaveOperation> saveOps = newVersionOp.getDuringSaveOperation();
      int i = 0;
      for (DuringSaveOperation saveOp : saveOps) {
        String name = saveOp.getName();
        state.setWizardSaveOperation(Check.isEmpty(name) ? "_newVersionOp" + (i++) : name, saveOp);
      }

      Item item = itemPack.getItem();
      state.setOriginalItemVer(itemKey.getVersion());
      state.setMergeDRMDefaults(false);
      state.setItemPack(itemPack);
      state.setItemId(new ItemId(item.getUuid(), item.getVersion()));
      getPagesInternal(state, true, true);
      setupForNewItem(state);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @param state
   * @param cloneOp
   * @return
   */
  protected WizardState cloneItemInternal(WizardState state, AbstractCloneOperation cloneOp) {
    try {
      ItemKey itemKey = state.getItemId();
      ItemPack<Item> itemPack = itemService.operation(itemKey, cloneOp, initProvider.get());

      WizardState newState = new WizardState(Operation.CLONING);

      Collection<DuringSaveOperation> saveOps = cloneOp.getDuringSaveOperation();
      int i = 0;
      for (DuringSaveOperation saveOp : saveOps) {
        String name = saveOp.getName();
        newState.setWizardSaveOperation(
            Check.isEmpty(name) ? "_newVersionOp" + (i++) : name, saveOp);
      }

      Item item = itemPack.getItem();
      newState.setItemPack(itemPack);
      newState.setItemId(new ItemId(item.getUuid(), item.getVersion()));
      newState.setMergeDRMDefaults(true);

      setupForNewItem(newState);

      return newState;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public WizardState cloneItem(
      WizardState state, String newItemdefUuid, String transform, boolean copyAttachments) {
    CloneOperation clone = cloneFactory.clone(newItemdefUuid, copyAttachments, false);
    clone.setTransform(transform);
    return cloneItemInternal(state, clone);
  }

  @Override
  public WizardState moveItem(ItemId itemkey, final String newItemdefUuid, String transform) {
    WizardState state = new WizardState(Operation.MOVING);
    state.setNewItem(false);
    state.setMergeDRMDefaults(false);
    state.setItemId(itemkey);
    state.setRedraftAfterSave(false);
    state.setEntryThroughEdit(true);
    state.setMovedItem(true);

    state.addUnsavedEdit(new MoveUnsaved(newItemdefUuid, transform));
    loadAndLock(state, true, false);
    return state;
  }

  @Override
  public List<WizardInfo> listWizardsInSession() {
    Set<WizardInfo> wizards = userSessionService.getAttribute(WIZARD_INFO_KEY);
    if (wizards == null) {
      return new ArrayList<WizardInfo>();
    }
    List<WizardInfo> wiz = new ArrayList<WizardInfo>();
    wiz.addAll(wizards);
    return wiz;
  }

  public static class MoveUnsaved extends FactoryMethodLocator<MoveDirectOperation>
      implements UnsavedEditOperation {
    private static final long serialVersionUID = 1L;

    private final String transform;

    public MoveUnsaved(String newItemdefUuid, String transform) {
      super(CloneFactory.class, "moveDirect", newItemdefUuid, false);
      this.transform = transform;
    }

    @Override
    public WorkflowOperation getOperation(boolean forSave) {
      MoveDirectOperation op = get();
      if (!forSave) {
        op.setTransform(transform);
      }
      op.setForWizard(!forSave);
      return op;
    }
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.edge.wizard.LERepository#getUserUUID()
   */
  @Override
  public String getUserUUID() {
    return CurrentUser.getUserID();
  }

  @Override
  public void previewHtml(String html) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void selectTopLevelFilesAsAttachments(WizardState state) {
    if (!state.isLockedForEditing() && !state.isNewItem()) {
      return;
    }
    ModifiableAttachments attachments = state.getAttachments();

    FileNode fileTree = getFileTree(state, "");
    List<FileNode> currentFiles = linearizeFileTree(fileTree.getFiles(), null);
    Set<String> realSet = new HashSet<String>();
    Iterator<FileNode> iter = currentFiles.iterator();
    while (iter.hasNext()) {
      FileNode fnode = iter.next();
      if (!fnode.isFolder() && fnode.isActualfile()) {
        realSet.add(fnode.getFullpath());
      }
    }
    Iterator<FileAttachment> fileiter = attachments.getIterator(AttachmentType.FILE);
    while (fileiter.hasNext()) {
      FileAttachment fitem = fileiter.next();
      if (!realSet.contains(fitem.getFilename())) {
        fileiter.remove();
      }
    }

    List<FileAttachment> files = attachments.getList(AttachmentType.FILE);
    Map<String, FileAttachment> curMap = UnmodifiableAttachments.convertToUrlMap(files);
    for (FileNode fnode : currentFiles) {
      if (fnode.getFiles() == null
          && !fnode.isFolder()
          && fnode.isActualfile()
          && fnode.getFullpath().indexOf('/') == -1) {
        String filename = fnode.getFullpath();
        String name = fnode.getName();
        if (!curMap.containsKey(filename)) {
          FileAttachment file = new FileAttachment();
          file.setFilename(filename);
          file.setDescription(name);
          file.setSize(fnode.getLength());
          attachments.addAttachment(file);
        }
      }
    }
  }

  private List<FileNode> linearizeFileTree(
      List<FileNode> dst, List<FileNode> src, int indent, String path) {
    if (src == null) {
      return dst;
    }

    for (FileNode fnode : src) {
      fnode.setIndent(indent);
      if (path != null) {
        fnode.setFullpath(path + '/' + fnode.getName());
      } else {
        fnode.setFullpath(fnode.getName());
      }
      dst.add(fnode);

      if (fnode.isFolder()) {
        if (fnode.getFiles() != null) {
          linearizeFileTree(dst, fnode.getFiles(), indent + 1, fnode.getFullpath());
        }
        dst.add(new FileNode(fnode));
      }
    }
    return dst;
  }

  @Override
  public List<FileNode> linearizeFileTree(List<FileNode> vec, String prefix) {
    if (prefix != null && prefix.length() == 0) {
      prefix = null;
    }
    return linearizeFileTree(new ArrayList<FileNode>(), vec, 0, prefix);
  }

  @Override
  public String getOriginalUrl() {
    return null;
  }

  @Override
  public FileNode getFileTree(WizardState state, String path) {
    StagingFile stagingFile = new StagingFile(state.getStagingId());
    FileFilter filter =
        new FileFilter() {
          @Override
          public boolean accept(File pathname) {
            String name = pathname.getName();
            return !pathname.isDirectory() || name.equals("_zips") || name.charAt(0) != '_';
          }
        };

    try {
      FileEntry fileTree = fileSystemService.enumerateTree(stagingFile, path, filter);
      FileNode root = recurseTree(fileTree);
      root.setFullpath(path);
      return root;
    } catch (IOException ex) {
      throw new RuntimeApplicationException("Error enumerating file tree", ex);
    }
  }

  private FileNode recurseTree(FileEntry file) {
    List<FileNode> kids = null;
    if (file.isFolder()) {
      Iterator<FileEntry> i = file.getFiles().iterator();
      if (i.hasNext()) {
        kids = new ArrayList<FileNode>();
        while (i.hasNext()) {
          kids.add(recurseTree(i.next()));
        }
      }
    }
    int len = (int) file.getLength();

    return new FileNode(file.getName(), len, kids, file.isFolder());
  }

  private int checkPages(List<WebWizardPage> pages) {
    int errorPage = -1;
    int pagenum = 0;
    for (WebWizardPage page : pages) {
      page.checkScript();
      if (errorPage == -1 && page.isViewable() && !(page.isSubmitted() && page.isValid())) {
        errorPage = pagenum;
      }
      pagenum++;
    }
    return errorPage;
  }

  @Override
  public boolean checkDuplicateXpathValue(
      WizardState state, String xpath, Collection<String> values, boolean canAccept) {
    return checkEditboxDuplicate(state, xpath, values, canAccept, null);
  }

  @Override
  public boolean checkEditboxDuplicate(
      WizardState state,
      String xpath,
      Collection<String> values,
      boolean canAccept,
      String fieldName) {
    final String prefix = xpath + '$';
    clearDuplicatesWithPrefix(state, prefix);
    final String uuid = state.getItem().getUuid();
    boolean isUnique = true;
    for (String value : values) {
      if (!Check.isEmpty(value)) {
        String identifier = prefix + value;
        isUnique &=
            setDuplicates(
                state,
                identifier,
                value,
                freeTextService.getKeysForNodeValue(uuid, state.getItemDefinition(), xpath, value),
                canAccept,
                false,
                fieldName);
      }
    }
    return isUnique;
  }

  @Override
  public void checkDuplicateUrls(WizardState state, String[] urls) {
    if (urls == null) {
      return;
    }

    final String prefix = "url$";
    clearDuplicatesWithPrefix(state, prefix);

    final String ignoreItem = state.getItemId().getUuid();
    final ItemDefinition itemdef = state.getItemDefinition();

    for (String url : urls) {
      String identifier = prefix + url;
      setDuplicates(
          state,
          identifier,
          url,
          itemService.getItemsWithUrl(url, itemdef, ignoreItem),
          true,
          false,
          "");
    }
  }

  @Override
  public void checkLinkAttachmentDuplicate(WizardState state, String url, String linkUuid) {
    final String ignoreItem = state.getItemId().getUuid();
    final ItemDefinition itemdef = state.getItemDefinition();
    List<ItemId> duplicateLinkAttachments = itemService.getItemsWithUrl(url, itemdef, ignoreItem);
    if (duplicateLinkAttachments.size() > 0) {
      setDuplicates(state, linkUuid, url, duplicateLinkAttachments, true, true, "");
    }
  }

  @Override
  public void checkFileAttachmentDuplicate(WizardState state, String fileName, String fileUuid) {
    try {
      final String md5 = fileSystemService.getMD5Checksum(state.getFileHandle(), fileName);
      final String excludeItemUuid = state.getItemId().getUuid();
      List<Attachment> duplicateFileAttachments =
          attachmentDao.findByMd5Sum(md5, state.getItemDefinition(), true, excludeItemUuid);
      if (duplicateFileAttachments.size() > 0) {
        List<ItemId> list =
            duplicateFileAttachments.stream()
                .map(attachment -> attachment.getItem().getItemId())
                .collect(Collectors.toList());
        setDuplicates(state, fileUuid, fileName, list, true, true, "");
      }
    } catch (IOException e) {
      LOGGER.error("Failed to check duplicates of " + fileName, e);
    }
  }

  private void clearDuplicatesWithPrefix(WizardState state, String prefix) {
    Iterator<String> iter = state.getDuplicateData().keySet().iterator();
    while (iter.hasNext()) {
      if (iter.next().startsWith(prefix)) {
        iter.remove();
      }
    }
  }

  private boolean setDuplicates(
      WizardState state,
      String identifier,
      String value,
      List<? extends ItemKey> list,
      boolean canAccept,
      boolean isAttachmentDuplicate,
      String wizardControlLabel) {
    Map<String, DuplicateData> duplicatesMap = state.getDuplicateData();

    boolean isUnique = Check.isEmpty(list);
    if (isUnique) {
      duplicatesMap.remove(identifier);
    } else {
      duplicatesMap.put(
          identifier,
          new DuplicateData(
              identifier, value, list, canAccept, isAttachmentDuplicate, wizardControlLabel));
    }
    return isUnique;
  }

  private List<WebWizardPage> initPages(WizardState state) throws ScriptException {
    Wizard wizard = state.getWizard();

    List<DRMPage> drm = state.getDrm();
    drm.clear();

    List<DefaultWizardPage> newPages = new ArrayList<DefaultWizardPage>();
    for (WizardPage page : wizard.getPages()) {
      newPages.addAll(createDefaultWizardPageFromWizardPage(state, page));
    }

    // see Jira Defect TLE-596 :
    // http://apps.dytech.com.au/jira/browse/TLE-596
    // Was getting the wrong count of pages.

    WebRepository repos = repositoryProvider.get();
    repos.setState(state);

    List<WebWizardPageState> pageStates = state.getPageStates();
    boolean createState = false;
    if (pageStates == null) {
      pageStates = new ArrayList<WebWizardPageState>();
      createState = true;
    }

    List<WebWizardPage> pages = new ArrayList<WebWizardPage>();
    int pageNum = 0;
    for (DefaultWizardPage pageXML : newPages) {
      WebWizardPageState pageState = getPageState(pageStates, pageNum, createState);

      pages.add(createWebWizardPageFromDefaultWizardPage(repos, pageXML, pageState, pageNum));
      pageNum++;
    }

    state.setPageStates(pageStates);
    return pages;
  }

  protected WebWizardPageState getPageState(
      List<WebWizardPageState> pageStates, int pageNum, boolean createState) {
    WebWizardPageState pageState;
    if (createState) {
      pageState = new WebWizardPageState();
      pageStates.add(pageState);
    } else {
      pageState = pageStates.get(pageNum);
    }

    return pageState;
  }

  protected List<DefaultWizardPage> createDefaultWizardPageFromWizardPage(
      WizardState state, WizardPage page) {
    List<DefaultWizardPage> newPages = new ArrayList<DefaultWizardPage>();

    String type = page.getType();
    if (type.equals(DRMPage.TYPE)) {
      DRMPage drmPage = (DRMPage) page;
      state.getDrm().add(drmPage);
      newPages.addAll(wizardDrmHelper.generatePages(drmPage));
    } else if (type.equals(NavPage.TYPE)) {
      newPages.add(((NavPage) page).createPage());
    } else {
      newPages.add((DefaultWizardPage) page);
    }

    return newPages;
  }

  protected WebWizardPage createWebWizardPageFromDefaultWizardPage(
      WebRepository repos, DefaultWizardPage pageXML, WebWizardPageState pageState, int pageNum) {
    WebWizardPage page = pageProvider.get();
    page.setState(pageState);
    page.setWizardPage(pageXML);
    page.setPageNumber(pageNum);
    page.setWebRepository(repos);
    page.init();

    return page;
  }

  @Override
  public WizardState newItem(String itemdefUuid) {
    return newItem(itemdefUuid, null, null);
  }

  @Override
  public WizardState newItem(String itemdefUuid, PropBagEx initialXml, StagingFile staging) {
    ItemDefinition definition =
        itemDefinitionService.getWithNoSecurity(itemDefinitionService.identifyByUuid(itemdefUuid));
    ItemPack<Item> pack =
        itemService.operation(
            null, workflowFactory.create(initialXml, definition, staging), initProvider.get());
    WizardState state = new WizardState(Operation.CREATING);
    Item item = pack.getItem();
    item.setItemDefinition(definition);
    state.setItemPack(pack);
    state.setItemId(pack.getItemId());
    state.setMergeDRMDefaults(true);
    setupForNewItem(state);
    return state;
  }

  public void setItemDefinitionService(ItemDefinitionService itemDefinitionService) {
    this.itemDefinitionService = itemDefinitionService;
  }

  @Override
  public void cancelEdit(WizardState state) {
    boolean unlock = false;
    if (state.isLockedForEditing()) {
      state.setLockedForEditing(false);
      unlock = true;
    }
    itemService.operation(
        state.getItemId(), workflowFactory.cancelEdit(state.getStagingId(), unlock));
  }

  @Override
  public void unlock(WizardState state) {
    itemService.forceUnlock(state.getItem());
    reload(state, false);
  }

  private List<WebWizardPage> getPagesInternal(
      WizardState state, boolean submitted, boolean forcePageRefresh) {
    List<WebWizardPage> pages = forcePageRefresh ? null : state.getPages();

    if (pages != null) {
      return pages;
    }

    pages = initPages(state);
    if (submitted) {
      for (WebWizardPage page : pages) {
        page.setSubmitted(true);
      }
    }
    checkPages(pages);
    state.setPages(pages);
    return pages;
  }

  @Override
  public List<WebWizardPage> getWizardPages(WizardState state) {
    return getPagesInternal(state, false, false);
  }

  @Override
  public int checkPages(WizardState state) {
    List<WebWizardPage> pages = getPagesInternal(state, false, false);
    return checkPages(pages);
  }

  public WorkflowStep getCurrentStep(WizardState state) {
    WorkflowStatus workflowStatus = getWorkflowStatus(state);
    return workflowStatus.getStepForId(state.getTaskId());
  }

  @Override
  public WorkflowStatus getWorkflowStatus(WizardState state) {
    WorkflowStatus status = state.getWorkflowStatus();
    if (status != null) {
      return status;
    }
    refreshStatus(state);
    return state.getWorkflowStatus();
  }

  @Override
  public ScriptContext createScriptContext(
      WizardState state,
      com.tle.core.wizard.controls.WizardPage page,
      HTMLControl control,
      Map<String, Object> attributes) {
    WizardScriptContextParams params =
        new WizardScriptContextParams(state, page, control, getWorkflowStatus(state), attributes);
    return createScriptContext(params);
  }

  @Override
  public ScriptContext createScriptContext(
      ItemPack itemPack,
      com.tle.core.wizard.controls.WizardPage page,
      HTMLControl control,
      Map<String, Object> attributes) {
    WizardScriptContextParams params =
        new WizardScriptContextParams(itemPack, page, control, attributes);
    return createScriptContext(params);
  }

  private ScriptContext createScriptContext(WizardScriptContextParams params) {
    ScriptContext scriptContext = scriptingService.createScriptContext(params);

    // now add our own bits...
    Map<String, Object> objects = new HashMap<String, Object>();
    for (WizardScriptObjectContributor contrib : scriptObjectTracker.getBeanList()) {
      contrib.addWizardScriptObjects(objects, params);
    }
    for (Map.Entry<String, Object> entry : objects.entrySet()) {
      scriptContext.addScriptObject(entry.getKey(), entry.getValue());
    }

    return scriptContext;
  }

  @Override
  public void addWizardScriptObjects(
      Map<String, Object> objects, WizardScriptContextCreationParams params) {
    WizardState wizardState = params.getWizardState();

    String workflowStep = wizardState != null ? wizardState.getTaskId() : null;

    if (workflowStep == null) {
      workflowStep = Constants.BLANK;
    }
    objects.put(WizardScriptConstants.WORKFLOW_STEP, workflowStep);

    if (wizardState != null) {
      objects.put(WizardScriptConstants.WIZARD_UUID, wizardState.getWizid());
    }

    if (params.getPage() != null) {
      objects.put(WizardScriptConstants.PAGE, new PageScriptWrapper(params.getPage()));
      if (params.getControl() != null) {
        objects.put(
            WizardScriptConstants.CONTROL,
            new ControlScriptWrapper(params.getControl(), params.getPage()));
      }
    }
  }

  @Override
  public boolean evaluateScript(String script, String scriptName, ScriptContext context)
      throws ScriptException {
    return scriptingService.evaluateScript(script, scriptName, context);
  }

  @Override
  public Object executeScript(
      String script, String scriptName, ScriptContext context, boolean function)
      throws ScriptException {
    return scriptingService.executeScript(script, scriptName, context, function);
  }

  @Override
  public Object executeScript(
      String script,
      String scriptName,
      ScriptContext context,
      boolean function,
      Class<?> expecedReturnType)
      throws ScriptException {
    return scriptingService.executeScript(script, scriptName, context, function, expecedReturnType);
  }

  public static class WizardScriptContextParams extends StandardScriptContextParams
      implements WizardScriptContextCreationParams {
    private final WizardState state;
    private final WorkflowStatus workflowStatus;
    private final com.tle.core.wizard.controls.WizardPage page;
    private final HTMLControl control;

    protected WizardScriptContextParams(
        WizardState state,
        com.tle.core.wizard.controls.WizardPage page,
        HTMLControl control,
        WorkflowStatus workflowStatus,
        Map<String, Object> attributes) {
      super(
          state.getItemPack(),
          (state.getStagingId() == null ? null : new StagingFile(state.getStagingId())),
          true,
          attributes);
      this.state = state;
      this.page = page;
      this.control = control;
      this.workflowStatus = workflowStatus;
    }

    protected WizardScriptContextParams(
        ItemPack itemPack,
        com.tle.core.wizard.controls.WizardPage page,
        HTMLControl control,
        Map<String, Object> attributes) {
      super(itemPack, null, true, attributes);
      this.state = null;
      this.page = page;
      this.control = control;
      this.workflowStatus = null;
    }

    @Override
    public HTMLControl getControl() {
      return control;
    }

    @Override
    public com.tle.core.wizard.controls.WizardPage getPage() {
      return page;
    }

    @Override
    public WorkflowStatus getWorkflowStatus() {
      return workflowStatus;
    }

    @Override
    public boolean isModerationAllowed() {
      if (workflowStatus != null) {
        return workflowStatus.isModerationAllowed();
      }
      return false;
    }

    @Override
    public boolean isAnOwner() {
      if (workflowStatus != null) {
        return workflowStatus.isOwner();
      }
      return false;
    }

    @Override
    public WizardState getWizardState() {
      return state;
    }
  }

  @Inject
  public void setPluginService(PluginService pluginService) {
    scriptObjectTracker =
        new PluginTracker<WizardScriptObjectContributor>(
            pluginService, "com.tle.web.wizard", "scriptObjects", "id");
    scriptObjectTracker.setBeanKey("class");
  }

  @Override
  public ViewableItem createViewableItem(final WizardStateInterface state) {
    return new ViewableItem() {
      private boolean fromRequest;

      @Override
      public Bookmark createStableResourceUrl(final String path) {
        String stagingid = state.getStagingId();
        if (stagingid != null) {
          return new FilestoreBookmark(institutionService, stagingid, path);
        }
        return new FilestoreBookmark(institutionService, getItemId(), path);
      }

      @Override
      public Attachment getAttachmentByFilepath(String filepath) {
        return UnmodifiableAttachments.convertToUrlMap(getItem().getAttachmentsUnmodifiable())
            .get(filepath);
      }

      @Override
      public Attachment getAttachmentByUuid(String uuid) {
        return UnmodifiableAttachments.convertToMapUuid(getItem().getAttachmentsUnmodifiable())
            .get(uuid);
      }

      @Override
      public FileHandle getFileHandle() {
        return state.getFileHandle();
      }

      @Override
      public Item getItem() throws ItemNotFoundException {
        return state.getItem();
      }

      @Override
      public ItemKey getItemId() {
        return state.getItemId();
      }

      @Override
      public String getItemdir() {
        return viewableItemFactory.getItemdirForPreview(state.getWizid());
      }

      @Override
      public URI getServletPath() {
        return viewableItemFactory.getServletPathForPreview(state.getWizid());
      }

      @Override
      public PropBagEx getItemxml() {
        return state.getItemxml();
      }

      @Override
      public Set<String> getPrivileges() {
        return state.getWorkflowStatus().getSecurityStatus().getAllowedPrivileges();
      }

      @Override
      public WorkflowStatus getWorkflowStatus() throws ItemNotFoundException {
        return state.getWorkflowStatus();
      }

      @Override
      public boolean isDRMApplicable() {
        return Check.isEmpty(state.getStagingId());
      }

      @Override
      public boolean isItemForReal() {
        return false;
      }

      @Override
      public void refresh() {
        throw new UnsupportedOperationException();
      }

      @Override
      public void update(ItemPack pack, WorkflowStatus status) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean isFromRequest() {
        return fromRequest;
      }

      @Override
      public void setFromRequest(boolean fromRequest) {
        this.fromRequest = fromRequest;
      }

      @Override
      public String getItemExtensionType() {
        return null;
      }
    };
  }

  @Override
  public void addToSession(SectionInfo info, WizardStateInterface state, boolean resumable) {
    // Clears out transients
    synchronized (state.getItem()) {
      state.onSessionSave();
      userSessionService.setAttribute(state.getWizid(), new WizardSessionState(state));

      if (resumable) {
        // Not resumable if in moderation
        if (moderationService.isModerating(info)) {
          return;
        }

        // save some info about the session (for resumption)
        Set<WizardInfo> wizards = userSessionService.getAttribute(WIZARD_INFO_KEY);
        if (wizards == null) {
          wizards = new HashSet<WizardInfo>();
        }
        final WizardInfo winfo = new WizardInfo();
        winfo.setUuid(state.getWizid());

        final Item item = state.getItem();
        // The item in the wizard state could be a deserialised one.
        // Can't just read complex values (e.g. collection name) out of it.
        final ItemDefinition collection =
            itemDefinitionService.get(item.getItemDefinition().getId());
        winfo.setCollectionName(CurrentLocale.get(collection.getName()));
        winfo.setItemUuid(item.getUuid());
        winfo.setItemVersion(item.getVersion());
        winfo.setNewItem(item.isNewItem());
        wizards.add(winfo);
        userSessionService.setAttribute(WIZARD_INFO_KEY, wizards);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends WizardStateInterface> T getFromSession(SectionInfo info, String id) {
    WizardStateInterface wizState = info.getAttributeForClass(WizardStateInterface.class);
    if (wizState != null) {
      return (T) wizState;
    }

    final WizardSessionState wizSessionState = userSessionService.getAttribute(id);
    if (wizSessionState != null) {
      wizState = wizSessionState.getWizardState();
      info.setAttribute(WizardStateInterface.class, wizState);
      return (T) wizState;
    }
    throw new WizardException(
        new WizardTimeoutException(CurrentLocale.get(KEY_PFX + "error.type.timedout")));
  }

  @Override
  public void removeFromSession(SectionInfo info, String id, boolean cancelEdit) {
    Set<WizardInfo> wizards = userSessionService.getAttribute(WIZARD_INFO_KEY);
    if (wizards != null) {
      WizardInfo winfo = new WizardInfo();
      winfo.setUuid(id);
      wizards.remove(winfo);
      userSessionService.setAttribute(WIZARD_INFO_KEY, wizards);
    }

    WizardStateInterface state = getFromSession(info, id);
    if (state instanceof WizardState) {
      WizardState wizState = (WizardState) state;
      if (cancelEdit && wizState.isEntryThroughEdit()) {

        cancelEdit(wizState);
      }
    }

    info.setAttribute(WizardStateInterface.class, null);
    userSessionService.removeAttribute(id);
  }

  @Override
  public void updateSession(SectionInfo info, WizardStateInterface state) {
    addToSession(info, state, true);
  }

  public static class WizardSessionState extends ClassBeanLocator<WizardService>
      implements PreviewableItem {
    private static final long serialVersionUID = 1L;

    private final WizardStateInterface wizardState;

    public WizardSessionState(WizardStateInterface state) {
      super(WizardService.class);
      this.wizardState = state;
    }

    @Override
    public ViewableItem getViewableItem() {
      return get().createViewableItem(wizardState);
    }

    public WizardStateInterface getWizardState() {
      return wizardState;
    }
  }

  @Override
  public com.tle.web.wizard.page.WizardPage createWizardPage() {
    return pageProvider.get();
  }

  @Override
  public LERepository createRepository(PropBagEx docxml, boolean expert) {
    SimpleLERepository repos = simpleRepoProvider.get();
    repos.setExpert(expert);
    repos.setDocumentXml(docxml);
    return repos;
  }

  @Override
  public Object getThreadLock() {
    return userSessionService.getSessionLock();
  }

  @BindFactory
  public interface WizardOperationFactory {
    WizardStateOperation state(WizardState state);
  }

  @Override
  public void ensureInitialisedPage(
      SectionInfo info, WebWizardPage page, JSCallable reloadFunction, boolean load) {
    boolean doLoad = load;
    try {
      if (!page.isLoaded()) {
        page.setReloadFunction(reloadFunction);
        page.createPage();
        doLoad = true;
      }
      if (doLoad) {
        page.loadFromDocument(info);
        page.saveDefaults();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
