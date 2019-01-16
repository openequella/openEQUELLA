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

package com.tle.core.connectors.equella.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.queries.FreeTextQuery;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.common.Format;
import com.tle.common.connectors.ConnectorContent;
import com.tle.common.connectors.ConnectorCourse;
import com.tle.common.connectors.ConnectorFolder;
import com.tle.common.connectors.ConnectorTerminology;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.search.DefaultSearch;
import com.tle.common.searching.SearchResults;
import com.tle.common.searching.SimpleSearchResults;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.core.connectors.service.ConnectorRepositoryImplementation;
import com.tle.core.connectors.service.ConnectorRepositoryService.ExternalContentSortType;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.item.dao.AttachmentDao;
import com.tle.core.item.service.ItemService;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.core.services.user.UserService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsController;
import com.tle.web.selection.SelectedResource;
import com.tle.web.viewable.impl.ViewableItemFactory;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

@NonNullByDefault
@Bind
@Singleton
public class EquellaConnectorService implements ConnectorRepositoryImplementation {
  private static final String KEY_PFX =
      AbstractPluginService.getMyPluginId(EquellaConnectorService.class) + ".";
  private static final String VIEW_ITEM = "VIEW_ITEM"; // $NON-NLS-1$

  @Inject private FreeTextService freeTextService;
  @Inject private UserService userService;
  @Inject private TLEAclManager aclManager;
  @Inject private ItemService itemService;
  @Inject private ViewItemUrlFactory itemUrls;
  @Inject private ViewableItemFactory viewableItemFactory;
  @Inject private AttachmentResourceService attachmentResourceService;

  @Inject private AttachmentDao attachmentDao;

  // no!! shouldn't need this
  @Inject private SectionsController sectionsController;

  @Override
  public boolean isRequiresAuthentication(Connector connector) {
    return false;
  }

  @Override
  public String getAuthorisationUrl(
      Connector connector, String forwardUrl, @Nullable String authData) {
    return null;
  }

  @Override
  public List<ConnectorContent> findUsages(
      Connector connector,
      String username,
      final String uuid,
      int version,
      boolean versionIsLatest,
      boolean archived,
      boolean allVersion)
      throws LmsUserNotFoundException {
    final DefaultSearch search = new DefaultSearch();
    search.setPrivilege(VIEW_ITEM);
    if (!archived) {
      search.setItemStatuses(ItemStatus.LIVE);
    }
    if (allVersion) {
      search.addMust(FreeTextQuery.FIELD_ATTACHMENT_UUID, uuid);
    } else {
      final List<String> values = new ArrayList<String>();
      values.add(uuid + "." + version);
      if (versionIsLatest) {
        values.add(uuid + ".0");
      }
      search.addMust(FreeTextQuery.FIELD_ATTACHMENT_UUID_VERSION, values);
    }
    final FreetextSearchResults<FreetextResult> results = freeTextService.search(search, 0, -1);

    final List<ConnectorContent> contentList = new ArrayList<ConnectorContent>();
    Collection<Item> foundItems = results.getResults();
    foundItems = aclManager.filterNonGrantedObjects(Collections.singleton(VIEW_ITEM), foundItems);

    for (Item item : foundItems) {
      final String name = CurrentLocale.get(item.getName(), item.getUuid());

      final String owner = getUser(userService, item.getOwner());

      final List<CustomAttachment> attachments =
          new UnmodifiableAttachments(item).getCustomList("resource");
      for (CustomAttachment attachment : attachments) {
        final String remoteItemUuid = (String) attachment.getData("uuid");
        final Integer remoteItemVersion = (Integer) attachment.getData("version");
        if (remoteItemUuid != null && remoteItemUuid.equals(uuid)) {
          if (allVersion
              || remoteItemVersion == version
              || (versionIsLatest && remoteItemVersion == 0)) {
            contentList.add(convertAttachment(item, attachment, name, owner));
          }
        }
      }
    }
    return contentList;
  }

  @SuppressWarnings("deprecation")
  private String getUser(UserService userService, String uuid) {
    if (uuid == null || uuid.length() == 0 || uuid.equals("0")) {
      return CurrentLocale.get("user.nouser");
    }

    String user = null;

    UserBean informationForUser = userService.getInformationForUser(uuid);
    if (informationForUser != null) {
      user = Format.format(informationForUser);
    }

    if (user == null) {
      user = CurrentLocale.get("user.unknownuser");
    }

    return user;
  }

  @Override
  public SearchResults<ConnectorContent> findAllUsages(
      Connector connector,
      String username,
      String query,
      String courseId,
      String folderId,
      boolean archived,
      int offset,
      int count,
      ExternalContentSortType sortType,
      boolean reverseSort)
      throws LmsUserNotFoundException {
    // YUK, should be no sort HQL in the service!
    final String sortHql;
    if (sortType != null) {
      switch (sortType) {
        case DATE_ADDED:
          sortHql = "ORDER BY a.item.dateCreated " + (reverseSort ? "ASC" : "DESC");
          break;
        case NAME:
        default:
          sortHql = "ORDER BY a.description " + (reverseSort ? "DESC" : "ASC");
          break;
      }
    } else {
      sortHql = "ORDER BY a.description " + (reverseSort ? "DESC" : "ASC");
    }
    final List<CustomAttachment> resources =
        attachmentDao.findResourceAttachmentsByQuery(query, !archived, sortHql);

    // filter unviewable items
    final List<ConnectorContent> content = Lists.newArrayList();
    final Iterator<CustomAttachment> resIt = resources.iterator();
    int index = 0;
    while (resIt.hasNext()) {
      final CustomAttachment custom = resIt.next();
      final Item item = custom.getItem();
      final Set<String> privs =
          aclManager.filterNonGrantedPrivileges(item, Collections.singleton(VIEW_ITEM));
      if (privs.isEmpty()) {
        resIt.remove();
      } else {
        // count < 0 == ALL
        if (index >= offset && (count < 0 || index < offset + count)) {
          final String name = CurrentLocale.get(item.getName(), item.getUuid());
          // Use user link service
          final String owner = getUser(userService, item.getOwner());

          content.add(convertAttachment(item, custom, name, owner));
        }
        index++;
        if (count >= 0 && index >= offset + count) {
          break;
        }
      }
    }

    return new SimpleSearchResults<ConnectorContent>(
        content, content.size(), offset, resources.size());
  }

  @Override
  public int getUnfilteredAllUsagesCount(
      Connector connector, String username, String query, boolean archived) {
    try {
      return findAllUsages(
              connector,
              username,
              query,
              null,
              null,
              archived,
              0,
              0,
              ExternalContentSortType.NAME,
              false)
          .getAvailable();
    } catch (LmsUserNotFoundException lms) {
      throw Throwables.propagate(lms);
    }
  }

  /**
   * @param item
   * @param attachment
   * @param name
   * @param owner
   * @param fakeCourseNameAndFolder This is not an ideal way to do this.
   * @return
   */
  private ConnectorContent convertAttachment(
      Item item, CustomAttachment attachment, String name, String owner) {
    final String targetItemUuid = (String) attachment.getData("uuid");
    final Integer targetItemVersion = (Integer) attachment.getData("version");

    // FIXME: shouldn't NEED an info. this is just DODGE-O-RAMA
    final SectionInfo info = sectionsController.createForward("/viewitem/viewitem.do");

    final ConnectorContent content = new ConnectorContent(attachment.getUuid());

    // linking attachment
    content.setFolder(attachment.getDescription());
    final ViewItemUrl attachmentUrl =
        attachmentResourceService
            .getViewableResource(
                info, viewableItemFactory.createNewViewableItem(item.getItemId() /*
																									* new
																									* ItemId (
																									* targetItemUuid
																									* ,
																									* targetItemVersion
																									* )
																									*/), attachment)
            .createDefaultViewerUrl();
    content.setFolderUrl(attachmentUrl.getHref());

    // linking item
    content.setCourse(name);
    content.setCourseUrl(
        itemUrls.createItemUrl(info, item.getItemId(), ViewItemUrl.FLAG_FULL_URL).getHref());

    content.setUuid(targetItemUuid);
    content.setVersion(targetItemVersion);

    final String targetAttachmentUuid = attachment.getUrl();
    if (!Check.isEmpty(targetAttachmentUuid)) {
      if (targetAttachmentUuid.equalsIgnoreCase("viewims.jsp")) {
        // load the IMS attachment and also set the attachmentUuid
        List<ImsAttachment> ims =
            new UnmodifiableAttachments(
                    itemService.getUnsecure(new ItemId(targetItemUuid, targetItemVersion)))
                .getList(AttachmentType.IMS);
        if (ims.size() == 1) // as it should
        {
          content.setAttachmentUuid(ims.get(0).getUuid());
        }
        content.setAttachmentUrl(targetAttachmentUuid);
      } else {
        content.setAttachmentUuid(targetAttachmentUuid);
      }
    }

    content.setExternalTitle(getExternalTitle(attachment));
    // content.setExternalTitle(getExternalTitle(attachment,
    // selectedAttachment, targetItemUuid, targetItemVersion,
    // targetAttachmentUuid));
    // content.setExternalUrl(itemUrls.createItemUrl(info, item.getItemId(),
    // ViewItemUrl.FLAG_FULL_URL).getHref());

    content.setAttribute("owner", getKey("finduses.owner"), owner);
    content.setDateModified(item.getDateModified());
    content.setDateAdded(item.getDateCreated());

    // if( selectedAttachment != null )
    // {
    // final ViewItemUrl attachmentUrl =
    // attachmentResourceService.getViewableResource(
    // info,
    // viewableItemFactory.createNewViewableItem(new ItemId(targetItemUuid,
    // targetItemVersion)), selectedAttachment).createDefaultViewerUrl();
    // content.setFolderUrl(attachmentUrl.getHref());
    // content.setFolder(selectedAttachment.getDescription());
    // }

    content.setAvailable(item.getStatus() == ItemStatus.LIVE);

    return content;
  }

  // private String getExternalTitle(CustomAttachment attachment, Attachment
  // selectedAttachment, String remoteItemUuid,
  // int remoteItemVersion, String remoteAttachmentUuid)
  private String getExternalTitle(CustomAttachment attachment) {
    final String description = attachment.getDescription();
    if (description == null) {
      return attachment.getUuid();
    }
    return description;
    // if( Check.isEmpty(description) )
    // {
    // Attachment sel = selectedAttachment;
    // if( sel == null )
    // {
    // try
    // {
    // sel = itemService.getAttachmentForUuid(new ItemId(remoteItemUuid,
    // remoteItemVersion), remoteAttachmentUuid);
    // }
    // catch( AttachmentNotFoundException a )
    // {
    // return CurrentLocale.get(getKey("finduses.attachmentnotfound"));
    // }
    // }
    // if( sel == null )
    // {
    // return Utils.coalesce(description, attachment.getUrl(),
    // attachment.getUuid());
    // }
    // //get the name of the attachment that is linked to
    // return Utils.coalesce(sel.getDescription(), sel.getUrl(),
    // sel.getUuid());
    // }
    // return description;
  }

  @Override
  public ConnectorFolder addItemToCourse(
      Connector connector,
      String username,
      String courseId,
      String sectionId,
      IItem<?> item,
      SelectedResource selectedResource) {
    throw new UnsupportedOperationException("Not supported yet");
  }

  @Override
  public List<ConnectorCourse> getCourses(
      Connector connector,
      String username,
      boolean editable,
      boolean archived,
      boolean management) {
    throw new UnsupportedOperationException(getString("export.error.notsupported"));
  }

  @Override
  public List<ConnectorFolder> getFoldersForCourse(
      Connector connector, String username, String courseId, boolean management)
      throws LmsUserNotFoundException {
    throw new UnsupportedOperationException("Not supported yet");
  }

  @Override
  public List<ConnectorFolder> getFoldersForFolder(
      Connector connector, String username, String courseId, String folderId, boolean management)
      throws LmsUserNotFoundException {
    throw new UnsupportedOperationException("Not supported yet");
  }

  @Override
  public String getCourseCode(Connector connector, String username, String courseId)
      throws LmsUserNotFoundException {
    throw new UnsupportedOperationException("Not supported yet");
  }

  private String getKey(String partKey) {
    return KEY_PFX + partKey;
  }

  private String getString(String partKey, String... params) {
    return CurrentLocale.get(KEY_PFX + partKey, (Object[]) params);
  }

  @Override
  public ConnectorTerminology getConnectorTerminology() {
    final ConnectorTerminology terms = new ConnectorTerminology();
    terms.setShowArchived(getKey("equella.finduses.showarchived"));
    terms.setCourseHeading(getKey("equella.finduses.course"));
    terms.setLocationHeading(getKey("equella.finduses.location"));
    return terms;
  }

  @Override
  public boolean supportsExport() {
    return false;
  }

  @Override
  public boolean supportsEdit() {
    return false;
  }

  @Override
  public boolean supportsView() {
    return true;
  }

  @Override
  public boolean supportsDelete() {
    return false;
  }

  @Override
  public boolean supportsReverseSort() {
    return true;
  }

  @Override
  public boolean supportsEditDescription() {
    return false;
  }

  @Override
  public boolean deleteContent(Connector connector, String username, String id) {
    throw new UnsupportedOperationException("Not supported yet");
  }

  @Override
  public boolean editContent(
      Connector connector, String username, String contentId, String title, String description) {
    throw new UnsupportedOperationException("Not supported yet");
  }

  @Override
  public boolean moveContent(
      Connector connector, String username, String contentId, String courseId, String locationId) {
    throw new UnsupportedOperationException("Not supported yet");
  }

  @Override
  public boolean supportsCourses() {
    return false;
  }

  @Override
  public boolean supportsFindUses() {
    return true;
  }
}
