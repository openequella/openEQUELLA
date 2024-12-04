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

package com.tle.core.portal.service;

import com.dytech.edge.common.LockedException;
import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.EntityPack;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.filesystem.handle.BucketFile;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.portal.PortletConstants;
import com.tle.common.portal.PortletTypeDescriptor;
import com.tle.common.portal.PortletTypeTarget;
import com.tle.common.portal.entity.Portlet;
import com.tle.common.portal.entity.PortletPreference;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.TargetList;
import com.tle.common.security.TargetListEntry;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.service.impl.AbstractEntityServiceImpl;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.filesystem.EntityFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.DefaultMessageCallback;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.portal.dao.PortletDao;
import com.tle.core.portal.dao.PortletPreferenceDao;
import com.tle.core.security.impl.SecureAllOnCall;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.core.security.impl.SecureOnReturn;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.java.plugin.registry.Extension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("nls")
@Bind(PortletService.class)
@Singleton
@SecureEntity(PortletService.ENTITY_TYPE)
public class PortletServiceImpl
    extends AbstractEntityServiceImpl<PortletEditingBean, Portlet, PrivatePortletService>
    implements PrivatePortletService {
  private final PortletDao portletDao;
  private final PortletPreferenceDao prefDao;
  private static PluginResourceHelper r =
      ResourcesService.getResourceHelper(PortletServiceImpl.class);

  @Inject private DefaultPortletExtensionService defaultExtension;

  private PluginTracker<PortletServiceExtension> typesTracker;
  private Map<String, PortletTypeDescriptor> typeDescriptorMap;
  private Collection<PortletTypeDescriptor> typeDescriptorList;

  @Inject
  public PortletServiceImpl(PortletDao dao, PortletPreferenceDao prefDao) {
    super(Node.PORTLET, dao);
    portletDao = dao;
    this.prefDao = prefDao;
  }

  @Override
  public List<Class<? extends Portlet>> getAdditionalEntityClasses() {
    return null;
  }

  @Override
  public void afterAdd(EntityPack<Portlet> pack) {
    Portlet portlet = pack.getEntity();
    getExtensionForPortlet(portlet).add(portlet);
  }

  private PortletServiceExtension getExtensionForPortlet(Portlet portlet) {
    return getExtensionForPortlet(portlet.getType());
  }

  private PortletServiceExtension getExtensionForPortlet(String portletType) {
    PortletServiceExtension extension = typesTracker.getBeanMap().get(portletType);
    if (extension == null) {
      extension = defaultExtension;
    }
    return extension;
  }

  private synchronized Map<String, PortletTypeDescriptor> getTypeDescriptorMap() {
    if (typesTracker.needsUpdate() || typeDescriptorMap == null) {
      Map<String, PortletTypeDescriptor> tempTypeDescriptors =
          new HashMap<String, PortletTypeDescriptor>();

      for (Extension ext : typesTracker.getExtensions()) {
        final String type = ext.getParameter("id").valueAsString();
        final String nameKey = ext.getParameter("nameKey").valueAsString();
        final String descriptionKey = ext.getParameter("descriptionKey").valueAsString();
        final String node = ext.getParameter("node").valueAsString();

        tempTypeDescriptors.put(
            type,
            new PortletTypeDescriptor(
                type, nameKey, descriptionKey, Enum.valueOf(Node.class, node)));
      }

      typeDescriptorMap = Collections.unmodifiableMap(tempTypeDescriptors);
      typeDescriptorList = null;
    }

    return typeDescriptorMap;
  }

  private synchronized Collection<PortletTypeDescriptor> getTypeDescriptorList() {
    if (typesTracker.needsUpdate() || typeDescriptorList == null) {
      typeDescriptorList = getTypeDescriptorMap().values();
    }
    return typeDescriptorList;
  }

  @Override
  protected void doValidation(
      EntityEditingSession<PortletEditingBean, Portlet> session,
      Portlet portlet,
      List<ValidationError> errors) {
    // Nothing to validate, see doValidationBean
  }

  @Override
  protected void doValidationBean(PortletEditingBean pbean, List<ValidationError> errors) {
    super.doValidationBean(pbean, errors);
    getExtensionForPortlet(pbean.getType()).doValidation(pbean, errors);
  }

  @Override
  @Transactional
  public List<Portlet> getViewablePortletsForDisplay() {
    List<Portlet> viewablePortlets = getViewablePortlets();
    for (Portlet portlet : viewablePortlets) {
      getExtensionForPortlet(portlet).loadExtra(portlet);
    }
    return viewablePortlets;
  }

  @SecureOnReturn(priv = "VIEW_PORTLET")
  @Override
  public List<Portlet> getViewablePortlets() {
    return portletDao.getForUser(CurrentUser.getUserID());
  }

  @SecureOnReturn(priv = "VIEW_PORTLET")
  @Override
  public List<Portlet> getViewableButClosedPortlets() {
    final String userId = CurrentUser.getUserID();
    final List<PortletPreference> prefs =
        prefDao.getForPortlets(userId, portletDao.getForUser(userId));
    final List<Portlet> closed = new ArrayList<Portlet>();
    for (PortletPreference pref : prefs) {
      if (pref.isClosed()) {
        closed.add(pref.getPortlet());
      }
    }
    return closed;
  }

  @Override
  public PortletSearchResults searchPortlets(PortletSearch search, int offset, int perPage) {
    List<Portlet> results = portletDao.search(search, offset, perPage);
    int totalResults = (int) portletDao.count(search);

    return new PortletSearchResults(results, results.size(), offset, totalResults);
  }

  @Transactional
  @Override
  public void close(String portletUuid) {
    final String userId = CurrentUser.getUserID();
    final Portlet portlet = getByUuid(portletUuid);
    if (portlet.getOwner().equals(userId) && !portlet.isInstitutional()) {
      delete(portlet, true);
    } else {
      if (!portlet.isCloseable()) {
        throw new AccessDeniedException(r.getString("service.error.noclose"));
      }
      PortletPreference portletPref = prefDao.getForPortlet(userId, portlet);
      if (portletPref == null) {
        portletPref = new PortletPreference(portlet, userId);
      }
      portletPref.setClosed(true);
      prefDao.save(portletPref);
    }
  }

  @Transactional
  @Override
  public void restore(String portletUuid) {
    final Portlet portlet = getByUuid(portletUuid);
    final PortletPreference portletPref = prefDao.getForPortlet(CurrentUser.getUserID(), portlet);
    prefDao.delete(portletPref);
  }

  @Transactional
  @Override
  public void restoreAll() {
    final List<PortletPreference> portletPrefs =
        prefDao.getForPortlets(CurrentUser.getUserID(), getViewableButClosedPortlets());
    for (PortletPreference pref : portletPrefs) {
      prefDao.delete(pref);
    }
  }

  @Override
  protected void deleteReferences(Portlet portlet) {
    prefDao.deleteAllForPortlet(portlet);
    getExtensionForPortlet(portlet).deleteExtra(portlet);
  }

  @Override
  protected void beforeAdd(EntityPack<Portlet> pack, boolean lockAfterwards) {
    super.beforeAdd(pack, lockAfterwards);
    checkAdminFields(null, pack.getEntity());
  }

  @Override
  protected void beforeStopEdit(
      EntityPack<Portlet> pack, Portlet persistedPortlet, boolean unlock) {
    super.beforeStopEdit(pack, persistedPortlet, unlock);
    checkAdminFields(persistedPortlet, pack.getEntity());
  }

  private void checkAdminFields(Portlet persistedPortlet, Portlet modifiedPortlet) {
    if (((persistedPortlet != null && persistedPortlet.isInstitutional())
            || modifiedPortlet.isInstitutional())
        && !canModifyAdminFields()) {
      throw new AccessDeniedException(r.getString("service.error.noinstpriv"));
    }
  }

  @Override
  public final EntityPack<Portlet> startEditInternal(Portlet entity) {
    ensureNonSystem(entity);
    EntityPack<Portlet> pack = new EntityPack<Portlet>();
    pack.setEntity(entity);

    // Prepare staging
    EntityFile from = new EntityFile(entity);
    StagingFile staging = stagingService.createStagingArea();
    if (fileSystemService.fileExists(from)) {
      fileSystemService.copy(from, staging);
    }
    pack.setStagingID(staging.getUuid());

    fillTargetLists(pack);
    return pack;
  }

  @Override
  @SecureAllOnCall({
    @SecureOnCall(priv = SecurityConstants.EDIT_VIRTUAL_BASE),
    @SecureOnCall(priv = PortletConstants.PRIV_ADMINISTER_PORTLETS)
  })
  @Transactional(propagation = Propagation.REQUIRED)
  public EntityPack<Portlet> startEdit(Portlet entity) {
    return super.startEdit(entity);
  }

  @Override
  @SecureAllOnCall({
    @SecureOnCall(priv = SecurityConstants.DELETE_VIRTUAL_BASE),
    @SecureOnCall(priv = PortletConstants.PRIV_ADMINISTER_PORTLETS)
  })
  @Transactional(propagation = Propagation.REQUIRED)
  public void delete(Portlet entity, boolean checkReferences) {
    super.delete(entity, checkReferences);
  }

  @Override
  @SecureAllOnCall(
      value = {
        @SecureOnCall(priv = SecurityConstants.EDIT_VIRTUAL_BASE),
        @SecureOnCall(priv = PortletConstants.PRIV_ADMINISTER_PORTLETS)
      })
  @Transactional(propagation = Propagation.REQUIRED)
  protected Portlet stopEdit(
      EntityEditingSession<PortletEditingBean, Portlet> session,
      EntityPack<Portlet> pack,
      Portlet persistedEntity,
      boolean useLock,
      @Nullable String lockId,
      boolean unlock) {
    final PortletEditingBean bean = session.getBean();
    final Portlet persistedPortlet = get(pack.getEntity().getId());

    // This validate is just checking integrity of the UUID, ID etc.
    validate(session, persistedEntity, false);

    validateBean(bean, false);

    beforeStopEdit(pack, persistedPortlet, unlock);

    try {
      if (useLock) {
        lockingService.getLock(persistedEntity, lockId);
      } else {
        lockingService.getLock(persistedPortlet);
      }

      populateEntity(bean, persistedPortlet);
      persistedPortlet.setDateModified(new Date());

      // clone portlet specific props
      PortletServiceExtension portletExtension = getExtensionForPortlet(persistedPortlet);
      portletExtension.edit(persistedPortlet, bean);

      String stagingID = pack.getStagingID();
      StagingFile stagingFile = null;
      if (stagingID != null) {
        stagingFile = new StagingFile(stagingID);
      }
      afterImport(
          stagingFile,
          persistedPortlet,
          new ConverterParams(institutionImportService.getInfoForCurrentInstitution()));

      saveTargetLists(session, pack);

      saveFiles(pack, unlock, unlock, false, persistedPortlet);
    } catch (LockedException e) {
      throw new RuntimeApplicationException(e);
    }

    auditLogService.logEntityModified(persistedPortlet.getId());

    return persistedPortlet;
  }

  @Override
  @SecureAllOnCall(
      value = {
        @SecureOnCall(priv = SecurityConstants.EDIT_VIRTUAL_BASE),
        @SecureOnCall(priv = PortletConstants.PRIV_ADMINISTER_PORTLETS)
      })
  @Transactional(propagation = Propagation.REQUIRED)
  public Portlet doStopEdit(
      EntityPack<Portlet> pack,
      EntityEditingSession<PortletEditingBean, Portlet> session,
      boolean unlock) {
    return super.doStopEdit(pack, session, unlock);
  }

  @Override
  @SecureAllOnCall(
      value = {
        @SecureOnCall(priv = SecurityConstants.CREATE_VIRTUAL_BASE),
        @SecureOnCall(priv = PortletConstants.PRIV_ADMINISTER_PORTLETS)
      })
  @Transactional
  public BaseEntityLabel doAdd(
      EntityPack<Portlet> pack,
      EntityEditingSession<PortletEditingBean, Portlet> session,
      boolean lockAfterwards) {
    return super.doAdd(pack, session, lockAfterwards);
  }

  @Override
  public boolean canModifyAdminFields() {
    return !aclManager
        .filterNonGrantedPrivileges(Arrays.asList(PortletConstants.PRIV_ADMINISTER_PORTLETS))
        .isEmpty();
  }

  @Override
  public boolean canEdit(Portlet portlet) {
    Set<String> privs = new HashSet<String>();
    privs.add(PortletConstants.EDIT_PORTLET);
    privs.add(PortletConstants.PRIV_ADMINISTER_PORTLETS);
    return !aclManager.filterNonGrantedPrivileges(portlet, privs).isEmpty();
  }

  @Override
  public boolean canDelete(Portlet portlet) {
    Set<String> privs = new HashSet<String>();
    privs.add(PortletConstants.DELETE_PORTLET);
    privs.add(PortletConstants.PRIV_ADMINISTER_PORTLETS);
    return !aclManager.filterNonGrantedPrivileges(portlet, privs).isEmpty();
  }

  @Override
  public Map<Portlet, PortletPreference> getPreferences(Collection<Portlet> portlets) {
    final String userId = CurrentUser.getUserID();
    final Map<Portlet, PortletPreference> mapped = new LinkedHashMap<Portlet, PortletPreference>();
    final List<PortletPreference> prefs = prefDao.getForPortlets(userId, portlets);
    for (Portlet p : portlets) {
      mapped.put(p, findPreference(p, prefs));
    }
    return mapped;
  }

  private PortletPreference findPreference(Portlet portlet, List<PortletPreference> prefs) {
    for (PortletPreference pref : prefs) {
      if (pref.getPortlet().getId() == portlet.getId()) {
        return pref;
      }
    }
    return null;
  }

  @Override
  public PortletPreference getPreference(Portlet portlet) {
    return prefDao.getForPortlet(CurrentUser.getUserID(), portlet);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  @Override
  public void savePreference(PortletPreference preference) {
    prefDao.save(preference);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  @Override
  public void savePreferences(Collection<PortletPreference> preferences) {
    for (PortletPreference preference : preferences) {
      if (preference != null) {
        prefDao.save(preference);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  protected <SESSION extends EntityEditingSession<PortletEditingBean, Portlet>>
      SESSION createSession(String sessionId, EntityPack<Portlet> pack, PortletEditingBean bean) {
    return (SESSION) new PortletEditingSessionImpl(sessionId, pack, bean);
  }

  @Override
  protected void populateEditingBean(PortletEditingBean pBean, Portlet entity) {
    super.populateEditingBean(pBean, entity);

    final TargetList targets = aclManager.getTargetList(Node.PORTLET, entity);
    String expression = null;
    for (TargetListEntry target : targets.getEntries()) {
      if (target.getPrivilege().equals("VIEW_PORTLET")) {
        expression = target.getWho();
        break;
      }
    }
    pBean.setTargetExpression(expression);
    pBean.setCloseable(entity.isCloseable());
    pBean.setConfig(entity.getConfig());
    pBean.setEnabled(entity.isEnabled());
    pBean.setExtraData(entity.getExtraData());
    pBean.setInstitutional(entity.isInstitutional());
    pBean.setMinimisable(entity.isMinimisable());
    pBean.setType(entity.getType());
  }

  @Override
  protected void populateEntity(PortletEditingBean pBean, Portlet entity) {
    super.populateEntity(pBean, entity);
    entity.setCloseable(pBean.isCloseable());
    entity.setConfig(pBean.getConfig());
    entity.setEnabled(pBean.isEnabled());
    // TODO: shouldn't really be using the Hibernate extra-data in the
    // editing bean
    entity.setExtraData(pBean.getExtraData());
    entity.setInstitutional(pBean.isInstitutional());
    entity.setMinimisable(pBean.isMinimisable());
    entity.setType(pBean.getType());
  }

  @Override
  protected PortletEditingBean createEditingBean() {
    return new PortletEditingBean();
  }

  @Override
  protected boolean isUseEditingBean() {
    return true;
  }

  @Override
  protected void onStartNewSession(
      EntityEditingSession<PortletEditingBean, Portlet> session, EntityFile entFile) {
    super.onStartNewSession(session, entFile);
    PortletEditingBean portlet = session.getBean();
    portlet.setEnabled(true);
    portlet.setCloseable(true);
    portlet.setMinimisable(true);
  }

  @Override
  public void doAfterImport(
      TemporaryFileHandle portletImportFolder,
      PortletEditingBean bean,
      Portlet portlet,
      ConverterParams params) {
    super.doAfterImport(portletImportFolder, bean, portlet, params);

    final SubTemporaryFile prefsFolder = new SubTemporaryFile(portletImportFolder, "preferences");
    if (fileSystemService.fileExists(prefsFolder)) {
      // delete all existing prefs (e.g. this is an import over the top of
      // an existing entity)
      prefDao.deleteAllForPortlet(portlet);

      final List<String> preferenceFiles = xmlHelper.getXmlFileList(prefsFolder);
      final DefaultMessageCallback message =
          new DefaultMessageCallback(r.key("converter.savingterms.progressmessage"));
      params.setMessageCallback(message);
      message.setTotal(preferenceFiles.size());
      message.setType(r.getString("type.preferences"));
      message.setCurrent(0);

      for (String prefFile : preferenceFiles) {
        final PortletPreference pref = xmlHelper.readXmlFile(prefsFolder, prefFile);
        pref.setPortlet(portlet);
        prefDao.save(pref);
        message.incrementCurrent();
      }

      // remove the terms folder so it doesn't get committed as an entity
      // file
      fileSystemService.removeFile(prefsFolder);
    }
  }

  @Override
  public void prepareExport(
      TemporaryFileHandle exportFolder, Portlet portlet, ConverterParams params) {
    final SubTemporaryFile prefsFolder = new SubTemporaryFile(exportFolder, "preferences");
    final List<PortletPreference> prefs = prefDao.getAllForPortlet(portlet);
    final DefaultMessageCallback message =
        new DefaultMessageCallback("institutions.converter.generic.genericmsg");
    params.setMessageCallback(message);
    message.setTotal(prefs.size());
    message.setType(r.getString("type.preferences"));
    message.setCurrent(0);
    getExtensionForPortlet(portlet).loadExtra(portlet);

    xmlHelper.writeExportFormatXmlFile(prefsFolder, true);

    for (PortletPreference pref : prefs) {
      initialiserService.initialise(pref);

      final BucketFile bucketFolder = new BucketFile(prefsFolder, pref.getUserId());
      xmlHelper.writeXmlFile(bucketFolder, pref.getUserId() + ".xml", pref);
      message.incrementCurrent();
    }

    super.prepareExport(exportFolder, portlet, params);
  }

  @Override
  protected void beforeClone(TemporaryFileHandle staging, EntityPack<Portlet> pack) {
    // export the prefs into the staging area
    prepareExport(
        staging,
        pack.getEntity(),
        new ConverterParams(institutionImportService.getInfoForCurrentInstitution()));
  }

  @Override
  public Collection<PortletTypeDescriptor> listAllAvailableTypes() {
    return getTypeDescriptorList();
  }

  @Override
  public Map<String, PortletTypeDescriptor> mapAllAvailableTypes() {
    return getTypeDescriptorMap();
  }

  @Override
  public List<PortletTypeDescriptor> listContributableTypes(boolean admin) {
    final List<PortletTypeDescriptor> all =
        new ArrayList<PortletTypeDescriptor>(getTypeDescriptorList());
    final Set<PortletTypeTarget> contributableTargets;

    final Collection<PortletTypeTarget> contributableTargetsCollection =
        Collections2.transform(
            all,
            new Function<PortletTypeDescriptor, PortletTypeTarget>() {
              @Override
              public PortletTypeTarget apply(PortletTypeDescriptor type) {
                return type.getTarget();
              }
            });

    contributableTargets =
        admin
            ? filterContributableTypesAdmin(contributableTargetsCollection)
            : filterContributableTypes(contributableTargetsCollection);

    final Iterator<PortletTypeDescriptor> it = all.iterator();
    while (it.hasNext()) {
      if (!contributableTargets.contains(it.next().getTarget())) {
        it.remove();
      }
    }

    return all;
  }

  @Override
  @SecureOnReturn(priv = PortletConstants.CREATE_PORTLET)
  public Set<PortletTypeTarget> filterContributableTypes(Collection<PortletTypeTarget> all) {
    return new HashSet<PortletTypeTarget>(all);
  }

  @SecureOnReturn(priv = PortletConstants.PRIV_ADMINISTER_PORTLETS)
  private Set<PortletTypeTarget> filterContributableTypesAdmin(Collection<PortletTypeTarget> all) {
    return new HashSet<PortletTypeTarget>(all);
  }

  @Inject
  public void setPluginService(PluginService pluginService) {
    typesTracker =
        new PluginTracker<PortletServiceExtension>(
            pluginService, "com.tle.core.portal", "portletType", "id");
    typesTracker.setBeanKey("serviceClass");
  }

  @Override
  public Portlet getForEdit(String portletUuid) {
    Portlet portlet = getByUuid(portletUuid);
    getExtensionForPortlet(portlet).loadExtra(portlet);
    return portlet;
  }

  @Override
  public int countFromFilters(PortletSearch search) {
    return (int) portletDao.count(search);
  }

  @Override
  @Transactional
  public void userDeletedEvent(UserDeletedEvent event) {
    Criterion owner = Restrictions.eq("owner", event.getUserID());
    Criterion inst = Restrictions.eq("institution", CurrentInstitution.get());
    for (Portlet p : portletDao.findAllByCriteria(owner, inst)) {
      delete(p, false);
    }

    prefDao.deleteAllForUser(event.getUserID());

    // We don't want to call super here since we're deleting the portlets.
    // The default behaviour for base entities is to orphan them instead
    //
    // super.userDeletedEvent(event);
  }

  @Override
  @Transactional
  public void userIdChangedEvent(UserIdChangedEvent event) {
    prefDao.changeUserId(event.getFromUserId(), event.getToUserId());

    for (PortletServiceExtension pse : typesTracker.getBeanList()) {
      pse.changeUserId(event.getFromUserId(), event.getToUserId());
    }

    super.userIdChangedEvent(event);
  }
}
