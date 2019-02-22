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

package com.tle.web.api.entity.resource;

import com.dytech.edge.common.LockedException;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.EntityLock;
import com.tle.beans.security.AccessEntry;
import com.tle.common.beans.exception.InvalidDataException;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.TargetList;
import com.tle.common.security.TargetListEntry;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.entity.service.EntityLockingService;
import com.tle.core.i18n.CoreStrings;
import com.tle.core.security.TLEAclManager;
import com.tle.core.security.impl.RequiresPrivilege;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.api.baseentity.serializer.BaseEntitySerializer;
import com.tle.web.api.entity.PagedResults;
import com.tle.web.api.interfaces.BaseEntityResource;
import com.tle.web.api.interfaces.beans.*;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import com.tle.web.api.interfaces.beans.security.TargetListEntryBean;
import com.tle.web.remoting.rest.service.RestImportExportHelper;
import com.tle.web.remoting.rest.service.UrlLinkService;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.springframework.transaction.annotation.Transactional;

/**
 * FIXME: I'm not sure about having all these Transactionals in the REST resource...
 *
 * @author Aaron
 * @param <BE> Base Entity type
 * @param <SB> Security Bean type
 * @param <B> Serialized bean type
 */
public abstract class AbstractBaseEntityResource<
        BE extends BaseEntity, SB extends BaseEntitySecurityBean, B extends BaseEntityBean>
    implements BaseEntityResource<B, SB> {
  // private static final Logger LOGGER =
  // Logger.getLogger(BaseEntityResource.class);

  protected abstract Node[] getAllNodes();

  protected abstract SB createAllSecurityBean();

  public abstract AbstractEntityService<?, BE> getEntityService();

  protected abstract BaseEntitySerializer<BE, B> getSerializer();

  protected abstract Class<?> getResourceClass();

  public String getPrivilegeType() {
    return getEntityService().getPrivilegeType();
  }

  @Inject protected TLEAclManager aclManager;
  @Inject private EntityLockingService lockingService;
  @Inject private UrlLinkService urlLinkService;

  protected SB serializeAcls() {
    if (aclManager
        .filterNonGrantedPrivileges("VIEW_SECURITY_TREE", "EDIT_SECURITY_TREE")
        .isEmpty()) {
      throw new AccessDeniedException(getString("error.acls.viewpriv"));
    }

    Node[] allNodes = getAllNodes();
    ListMultimap<String, AccessEntry> entries =
        aclManager.getExistingEntriesForVirtualNodes(allNodes);
    SB securityBean = createAllSecurityBean();

    List<AccessEntry> list = entries.get(aclManager.getKeyForVirtualNode(allNodes[0], null));

    List<TargetListEntryBean> entryBeans = Lists.newLinkedList();
    for (AccessEntry accessEntry : list) {
      TargetListEntryBean entryBean = new TargetListEntryBean();
      entryBean.setGranted(accessEntry.isGrantRevoke() == SecurityConstants.GRANT);
      entryBean.setWho(accessEntry.getExpression().getExpression());
      entryBean.setPrivilege(accessEntry.getPrivilege());
      entryBean.setOverride(accessEntry.getAclPriority() > 0);
      entryBeans.add(entryBean);
    }

    securityBean.setRules(entryBeans);
    return securityBean;
  }

  @RequiresPrivilege(priv = "EDIT_SECURITY_TREE")
  @Transactional
  protected void updateAcls(SB securityBean) {
    List<TargetListEntry> entries = new ArrayList<>();
    List<TargetListEntryBean> rules = securityBean.getRules();
    for (TargetListEntryBean rule : rules) {
      entries.add(
          new TargetListEntry(
              rule.isGranted(), rule.isOverride(), rule.getPrivilege(), rule.getWho()));
    }
    aclManager.setTargetList(getAllNodes()[0], null, new TargetList(entries));
  }

  @Transactional
  public PagingBean<B> list(
      UriInfo uriInfo,
      String q,
      List<String> privilege,
      String resumption,
      int length,
      boolean full) {
    final boolean isExport = RestImportExportHelper.isExport(uriInfo);
    return PagedResults.pagedResults(
        this, q, privilege, resumption, length, full | isExport, isExport, true);
  }

  public B serialize(BE entity, Object data, boolean heavy) {
    B bean = getSerializer().serialize(entity, data, heavy);
    final Map<String, String> links =
        Collections.singletonMap("self", getGetUri(entity.getUuid()).toString());
    bean.set("links", links);
    return bean;
  }

  public SB getAcls(UriInfo uriInfo) {
    return serializeAcls();
  }

  public Response editAcls(UriInfo uriInfo, SB security) {
    updateAcls(security);
    return Response.ok().build();
  }

  /**
   * Implementation for various entity endpoints.
   *
   * @param uriInfo
   * @param uuid
   * @return
   */
  public Response getLock(UriInfo uriInfo, String uuid) {
    final BE entity = getEntityService().getByUuid(uuid);
    if (entity == null) {
      throw entityNotFound(uuid);
    }
    final EntityLock lock = lockingService.getLockUnbound(entity);
    if (lock == null) {
      throw new NotFoundException(getString("error.entity.notlocked"));
    }
    return Response.ok().entity(convertLock(lock, entity)).build();
  }

  /**
   * Implementation for various entity endpoints.
   *
   * @param uriInfo
   * @param uuid
   * @return
   */
  public Response lock(UriInfo uriInfo, String uuid) {
    try {
      BE entity = getEntityService().getByUuid(uuid);
      if (entity == null) {
        throw entityNotFound(uuid);
      }
      final EntityLock lock = lockingService.lockEntity(entity);
      return Response.status(Status.CREATED).entity(convertLock(lock, entity)).build();
    } catch (LockedException ex) {
      throw processLocked(ex);
    }
  }

  /**
   * Implementation for various entity endpoints.
   *
   * @param uuid
   * @return
   */
  public Response unlock(UriInfo uriInfo, String uuid) {
    try {
      BE entity = getEntityService().getByUuid(uuid);
      if (entity == null) {
        throw new NotFoundException(getString("error.entity.notlocked"));
      }
      lockingService.unlockEntity(entity, true);
    } catch (LockedException ex) {
      throw processLocked(ex);
    }
    // Should be 'no content'??
    return Response.noContent().build();
  }

  private LockedException processLocked(LockedException ex) {
    if (CurrentUser.getUserID().equals(ex.getUserID())) {
      return new LockedException(
          getString("error.entity.lockeddifferentsession"),
          ex.getUserID(),
          ex.getSessionID(),
          ex.getEntityId());
    } else {
      return new LockedException(
          getString("error.entity.lockeddifferentuser", ex.getUserID()),
          ex.getUserID(),
          ex.getSessionID(),
          ex.getEntityId());
    }
  }

  private EntityLockBean convertLock(EntityLock lock, BE entity) {
    final EntityLockBean lockBean = new EntityLockBean();
    lockBean.setOwner(new UserBean(lock.getUserID()));
    lockBean.setUuid(lock.getUserSession());
    final Map<String, String> links = new HashMap<>();
    final String lockUrl =
        urlLinkService
            .getMethodUriBuilder(getResourceClass(), "getLock")
            .build(entity.getUuid())
            .toString();
    links.put("self", lockUrl);
    lockBean.setLinks(links);
    return lockBean;
  }

  /**
   * Implementation for various entity endpoints.
   *
   * @param uriInfo
   * @param uuid
   * @return
   */
  @Transactional
  public B get(UriInfo uriInfo, String uuid) {
    final AbstractEntityService<?, BE> entityService = getEntityService();
    final BE entity = entityService.getByUuid(uuid);
    if (entity == null) {
      throw entityNotFound(uuid);
    }
    if (!entityService.canViewOrEdit(entity)) {
      throw new AccessDeniedException(getString("error.entity.viewpriv"));
    }
    return serialize(entity, null, true);
  }

  /**
   * Implementation for various entity endpoints.
   *
   * @param uuid
   * @return
   */
  public Response delete(UriInfo uriInfo, String uuid) {
    final AbstractEntityService<?, BE> entityService = getEntityService();
    BE entity = entityService.getByUuid(uuid);
    if (entity == null) {
      throw entityNotFound(uuid);
    }
    entityService.delete(entity, true);
    return Response.noContent().build();
  }

  /**
   * Implementation for various entity endpoints.
   *
   * @param bean
   * @param stagingUuid
   * @return
   */
  public Response create(UriInfo uriInfo, B bean, String stagingUuid) {
    validate(bean.getUuid(), bean, true);
    BE entity =
        getSerializer().deserializeNew(bean, stagingUuid, RestImportExportHelper.isImport(uriInfo));
    String uuid = entity.getUuid();
    return Response.created(getGetUri(uuid)).build();
  }

  public Response edit(
      UriInfo uriInfo, String uuid, B bean, String stagingUuid, String lockId, boolean keepLocked) {
    validate(uuid, bean, false);
    BE entity =
        getSerializer()
            .deserializeEdit(
                uuid,
                bean,
                stagingUuid,
                lockId,
                keepLocked,
                RestImportExportHelper.isImport(uriInfo));
    if (entity == null) {
      throw entityNotFound(uuid);
    }
    return Response.ok().location(getGetUri(uuid)).build();
  }

  /**
   * No-op by default, rely on entityService to validate. You may need to override this for checking
   * uniqueness before Hibernate bullshit comes into play
   *
   * @param uuid
   * @param bean
   */
  protected void validate(String uuid, B bean, boolean isNew) throws InvalidDataException {
    // Empty
  }

  protected URI getGetUri(String uuid) {
    return urlLinkService.getMethodUriBuilder(getResourceClass(), "get").build(uuid);
  }

  protected boolean isLocked(BE entity) {
    return lockingService.isEntityLocked(entity, CurrentUser.getUserID(), null);
  }

  protected NotFoundException entityNotFound(String uuid) {
    return new NotFoundException(getString("error.entity.notfound", uuid));
  }

  protected String getString(String keyPart, Object... params) {
    return CoreStrings.lookup().getString(keyPart, params);
  }
}
