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

package com.tle.admin.tools.common;

import com.dytech.edge.common.LockedException;
import com.dytech.gui.workers.GlassSwingWorker;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.tle.admin.AdminTool;
import com.tle.admin.Driver;
import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.i18n.Lookup;
import com.tle.admin.service.ClientRequestException;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.EntityPack;
import com.tle.common.NameValue;
import com.tle.common.Utils;
import com.tle.common.adminconsole.RemoteAdminService;
import com.tle.common.applet.client.ClientService;
import com.tle.common.applet.client.DialogUtils;
import com.tle.common.applet.client.DialogUtils.DialogResult;
import com.tle.common.beans.exception.InvalidDataException;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.StringLookup;
import com.tle.common.security.SecurityConstants;
import com.tle.core.remoting.RemoteAbstractEntityService;
import com.tle.i18n.BundleCache;
import io.github.openequella.graphql.api.LockedError;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for Admin Console tools that manage {@link BaseEntity} entities within openEQUELLA.
 *
 * <p>This is a key architectural component in the Admin Console that provides a complete CRUD
 * (Create, Read, Update, Delete) interface for managing entities such as collections, schemas,
 * workflows, power searches, taxonomies, and other system-level resources.
 *
 * <p><b>Class Hierarchy:</b>
 *
 * <ul>
 *   <li>{@link AdminTool} - Top-level interface for all admin tools
 *   <li>{@link AdminToolList} - Provides list-based UI with add, edit, remove functionality
 *   <li>{@link AdminToolListClone} - Adds clone, import, export, archive, and unarchive
 *       capabilities
 *   <li><b>{@code BaseEntityTool}</b> - Implements full entity lifecycle management with server
 *       interaction, locking, and validation
 * </ul>
 *
 * <p><b>Key Responsibilities:</b>
 *
 * <ul>
 *   <li><b>Entity Lifecycle Management:</b> Create, retrieve, update, delete entities with proper
 *       server synchronisation
 *   <li><b>Edit Locking:</b> Manage entity edit locks to prevent concurrent modification conflicts
 *   <li><b>Import/Export:</b> Serialize/deserialize entities to/from ZIP files with optional
 *       security rules
 *   <li><b>Cloning:</b> Duplicate existing entities for reuse
 *   <li><b>Archive/Unarchive:</b> Toggle entity availability without deletion
 *   <li><b>Security:</b> Check privileges before allowing create/modify operations
 *   <li><b>User Experience:</b> Coordinate background operations with {@link GlassSwingWorker},
 *       handle errors gracefully, provide user feedback
 * </ul>
 *
 * <p><b>Subclass Implementation Pattern:</b>
 *
 * <p>Concrete subclasses must implement four abstract methods to specialise for a specific entity
 * type:
 *
 * <pre>{@code
 * public class ItemDefinitionTool extends BaseEntityTool<ItemDefinition> {
 *   public ItemDefinitionTool() {
 *     super(ItemDefinition.class, AdminCollectionDefinitionService.ENTITY_TYPE());
 *   }
 *
 *   @Override
 *   protected RemoteAbstractEntityService<ItemDefinition> getService(ClientService client) {
 *     return client.getService(AdminCollectionDefinitionService.class);
 *   }
 *
 *   @Override
 *   protected BaseEntityEditor<ItemDefinition> createEditor(boolean readonly) {
 *     return new ItemEditor(this, readonly);
 *   }
 *
 *   @Override
 *   protected String getEntityName() {
 *     return CurrentLocale.get("com.tle.admin.gui.itemdefinitiontool.collection");
 *   }
 *
 *   @Override
 *   protected String getErrorPath() {
 *     return "itemEditor";
 *   }
 * }
 * }</pre>
 *
 * <p><b>Existing Implementations:</b>
 *
 * <ul>
 *   <li>{@code ItemDefinitionTool} - Item collections
 *   <li>{@code SchemaTool} - Metadata schemas
 *   <li>{@code WorkflowTool} - Workflow definitions
 *   <li>{@code PowerSearchTool} - Advanced search pages
 *   <li>{@code TaxonomyTool} - Taxonomy/vocabulary management
 *   <li>{@code HarvesterProfileTool} - Content harvesting profiles
 *   <li>{@code CourseInfoTool} - Course/class management
 *   <li>{@code SearchTool} - Federated search configurations
 *   <li>{@code ReportingTool} - BIRT report definitions
 *   <li>{@code DynaCollectionTool} - Dynamic collections
 *   <li>{@code BaseEntityWithAddDialogTool} - Variant supporting type selection on creation
 * </ul>
 *
 * <p><b>When to Add Code Here:</b>
 *
 * <ul>
 *   <li>Adding generic entity management functionality needed by <i>all</i> or <i>most</i> entity
 *       types
 *   <li>Fixing bugs in core entity lifecycle operations (create, edit, delete, clone, import,
 *       export)
 *   <li>Enhancing error handling or user feedback for entity operations
 *   <li>Improving locking/unlocking logic
 * </ul>
 *
 * <p><b>When to Add Code Elsewhere:</b>
 *
 * <ul>
 *   <li>Entity-specific behaviour → Implement in the concrete subclass (e.g., {@code
 *       ItemDefinitionTool})
 *   <li>Custom UI or validation → Implement in the {@link BaseEntityEditor} subclass (e.g., {@code
 *       ItemEditor})
 *   <li>Server-side business logic → Implement in the service layer (e.g., {@code
 *       AdminCollectionDefinitionService} and the GraphQL query behind it)
 *   <li>List display changes → Override methods in {@link AdminToolList} or {@link
 *       AdminToolListClone}
 * </ul>
 *
 * <p><b>Threading Model:</b>
 *
 * <p>All server communication and potentially long-running operations are executed on background
 * threads via {@link GlassSwingWorker}. This prevents UI blocking while displaying a glass pane
 * with progress indicator.
 *
 * @param <T> The specific {@link BaseEntity} subclass this tool manages (e.g., {@code
 *     ItemDefinition}, {@code Schema}, {@code Workflow})
 * @see BaseEntity
 * @see AdminToolList
 * @see AdminToolListClone
 * @see BaseEntityEditor
 * @see RemoteAbstractEntityService
 */
public abstract class BaseEntityTool<T extends BaseEntity> extends AdminToolListClone {
  static final Log LOGGER = LogFactory.getLog(BaseEntityTool.class);

  private static final String KEY_PREFIX = "com.tle.admin.gui.baseentitytool.";

  protected static final StringLookup strings = Lookup.lookup;

  /** Shorthand for {@code CurrentLocale.get("com.tle.admin.gui.baseentitytool." + suffix, ...)}. */
  static String s(String suffix, Object... values) {
    return CurrentLocale.get(KEY_PREFIX + suffix, values);
  }

  // //////// ABSTRACT ///////////

  protected abstract BaseEntityEditor<T> createEditor(boolean readonly);

  protected abstract String getEntityName();

  protected abstract String getErrorPath();

  protected abstract RemoteAbstractEntityService<T> getService(ClientService client);

  // /////////////////////////

  protected final Class<T> entityClass;
  protected final String createPrivilege;

  protected RemoteAbstractEntityService<T> service;
  protected boolean canAddNew;

  public BaseEntityTool(Class<T> entityClass, String entityType) {
    this.entityClass = entityClass;
    this.createPrivilege = SecurityConstants.CREATE_PFX + entityType;
  }

  public Class<T> getEntityClass() {
    return entityClass;
  }

  @Override
  public void setup(Set<String> grantedPrivileges, String name) {
    clientService = driver.getClientService();
    service = getService(clientService);

    canAddNew = grantedPrivileges.contains(createPrivilege);
    addAction.setEnabled(canAddNew);
    importAction.setEnabled(canAddNew);
    enableClone = canAddNew;

    super.setup(grantedPrivileges, name);
  }

  protected String getEntityNameLower() {
    return getEntityName().toLowerCase();
  }

  protected String getEntityNameNormal() {
    return Utils.capitaliseWords(getEntityName());
  }

  // TO BE OVERRIDDEN
  protected T process(T e) {
    return e;
  }

  protected final void remove(long uuid) {
    service.delete(uuid, true);
  }

  protected final EntityPack<T> getEntity(long uuid) {
    EntityPack<T> result = service.getReadOnlyPack(uuid);
    result.setEntity(process(result.getEntity()));
    return result;
  }

  protected final void forceUnlock(long id) {
    service.cancelEdit(id, true);
  }

  public final T stopEdit(EntityPack<T> entity, boolean unlock) {
    return service.stopEdit(entity, unlock);
  }

  public final EntityPack<T> startEdit(long uuid) {
    EntityPack<T> pack = service.startEdit(uuid);
    pack.setEntity(process(pack.getEntity()));
    return pack;
  }

  public BaseEntityLabel add(EntityPack<T> entity, boolean lockAfterwards) {
    entity.getEntity().setId(0);
    return service.add(entity, lockAfterwards);
  }

  protected final long identifyEntityByUuid(String uuid) {
    return service.identifyByUuid(uuid);
  }

  public final void cancelEdit(long id) {
    service.cancelEdit(id, false);
  }

  protected String getLoadingErrorMessage() {
    return getErrorPath() + "/loading";
  }

  protected String getCloningErrorMessage() {
    return getErrorPath() + "/cloning";
  }

  protected String getDeletingErrorMessage() {
    return getErrorPath() + "/deleting";
  }

  protected String getExportingErrorMessage() {
    return getErrorPath() + "/exporting";
  }

  protected String getImportingErrorMessage() {
    return getErrorPath() + "/importing";
  }

  public String getSavingErrorMessage() {
    return getErrorPath() + "/saving";
  }

  public String getValidationErrorMessage() {
    return getErrorPath() + "/validation";
  }

  /** Populate the list with all of the entities. @ If something goes wrong with Driver. */
  @Override
  protected final Collection<NameValue> fillList() {
    return BundleCache.getNameValues(service.listEditable());
  }

  @Override
  protected void onAdd() {
    final GlassSwingWorker<EntityPack<T>> worker =
        new GlassSwingWorker<EntityPack<T>>() {
          private BaseEntityEditor<T> editor;

          @Override
          public EntityPack<T> construct() {
            editor = createEditor(false);
            return create();
          }

          @Override
          public void finished() {
            EntityPack<T> pack = get();
            if (pack != null) {
              editor.load(pack, false);
              boolean saved = editor.showEditor(parentFrame);
              if (saved) {
                addToList(editor.getEntityDetails());
              }
            }
          }

          @Override
          public void exception() {
            Exception exception = getException();
            LOGGER.warn("Error creating GUI", exception);
            Driver.displayInformation(parentFrame, s("error"));
          }
        };

    worker.setComponent(parentFrame);
    worker.start();
  }

  @Override
  protected final void onEdit() {
    final long uuid = Long.parseLong(getSelectedObjects().getFirst().getValue());
    final GlassSwingWorker<LockedException> worker =
        new GlassSwingWorker<>() {
          EntityPack<T> original;

          @Override
          public LockedException construct() {
            try {
              original = startEdit(uuid);
              return null;
            } catch (LockedException ex) {
              return ex;
            } catch (ClientRequestException cex) {
              scala.Option<LockedError> lockedError = cex.getApiErrorOfType(LockedError.class);
              if (lockedError.isDefined()) {
                // Only the message is needed, the rest of the LockedException details are not used
                // where this 'exception' is handled down in 'finished()' - see 'msg' and 'locked1'
                // variables.
                return new LockedException(lockedError.get().message(), null, null, 0);
              } else {
                throw cex;
              }
            }
          }

          @Override
          public void finished() {
            LockedException locked1 = get();
            if (locked1 != null) {
              String[] buttons = {s("open"), s("unlock"), s("donotopen")};
              String msg = locked1.getMessage() + s("message");

              final int result =
                  JOptionPane.showOptionDialog(
                      parentFrame,
                      msg,
                      s("locked"),
                      JOptionPane.YES_NO_CANCEL_OPTION,
                      JOptionPane.QUESTION_MESSAGE,
                      null,
                      buttons,
                      buttons[0]);

              switch (result) {
                case JOptionPane.YES_OPTION:
                  doEdit(true);
                  break;

                case JOptionPane.NO_OPTION:
                  doEdit(false);
                  break;

                default:
                  // MAYBE_OPTION ...?
                  break;
              }
            } else {
              doEdit(false);
            }
          }

          @Override
          public void exception() {
            Exception exception = getException();
            Driver.displayError(parentFrame, getLoadingErrorMessage(), exception);
            LOGGER.warn("Could not retrieve " + getEntityNameLower() + " " + uuid, exception);
          }

          private void doEdit(final boolean readonly) {
            final GlassSwingWorker<?> editWorker =
                new GlassSwingWorker<Object>() {
                  @Override
                  public Object construct() {
                    if (original == null) {
                      if (readonly) {
                        original = getEntity(uuid);
                      } else {
                        forceUnlock(uuid);
                        original = startEdit(uuid);
                      }
                    }
                    return null;
                  }

                  @Override
                  public void finished() {
                    BaseEntityEditor<T> manager = createEditor(readonly);

                    try {
                      manager.load(original, true);
                    } catch (Exception e) {
                      setActualException(e);
                      exception();
                      return;
                    }

                    boolean saved = manager.showEditor(parentFrame);
                    if (saved) {
                      removeSelectedObjects();
                      addToList(manager.getEntityDetails());
                    }
                  }

                  @Override
                  public void exception() {
                    Exception exception = getException();
                    Driver.displayError(parentFrame, getLoadingErrorMessage(), exception);
                    LOGGER.warn(
                        "Could not retrieve " + getEntityNameLower() + " " + uuid,
                        exception); //$NON-NLS-2$
                    if (!readonly) {
                      try {
                        forceUnlock(uuid);
                      } catch (LockedException e) {
                        // IGNORE
                      }
                    }
                  }
                };

            editWorker.setComponent(parentFrame);
            editWorker.start();
          }
        };

    worker.setComponent(parentFrame);
    worker.start();
  }

  @Override
  protected final void onRemove() {
    var onRemoveHandler =
        new BaseEntityToolOnRemoveHandler(parentFrame, getSelectedObjects(), this);
    onRemoveHandler.start();
  }

  @Override
  protected final void onClone() {
    final long oldUuid = Long.parseLong(getSelectedObjects().get(0).getValue());

    final GlassSwingWorker<NameValue> worker =
        new GlassSwingWorker<NameValue>() {
          @Override
          public NameValue construct() {
            BaseEntityLabel label = service.clone(oldUuid);
            BundleCache.invalidate(label);
            return BundleCache.getNameValue(label);
          }

          @Override
          public void finished() {
            addToList(get());
            Driver.displayInformation(parentFrame, s("cloned", getEntityNameNormal()));
          }

          @Override
          public void exception() {
            Exception ex = getException();
            Driver.displayError(parentFrame, getCloningErrorMessage(), ex);
            LOGGER.warn("Could not clone " + getEntityNameLower() + ' ' + oldUuid, ex);
          }
        };

    worker.setComponent(parentFrame);
    worker.start();
  }

  @Override
  protected final void onExport() {
    NameValue pair = getSelectedObjects().get(0);

    DialogResult result =
        DialogUtils.saveDialog(
            parentFrame,
            null,
            new ZipFileFilter(),
            DialogUtils.getSuggestedFileName(pair.getName(), "zip"));

    if (result.isOkayed()) {
      boolean writeFile = true;
      File file = result.getFile();
      if (file.exists()) {
        final int result2 =
            JOptionPane.showConfirmDialog(
                parentFrame,
                s("confirmoverwrite"),
                s("overwrite"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result2 != JOptionPane.YES_OPTION) {
          writeFile = false;
        }
      }

      if (writeFile) {
        doWrite(Long.parseLong(pair.getValue()), file);
      }
    }
  }

  private void doWrite(final long uuid, final File file) {
    final boolean exportSecurity =
        JOptionPane.showConfirmDialog(
                parentFrame, s("exportrules"), s("export"), JOptionPane.YES_NO_OPTION)
            == JOptionPane.YES_OPTION;

    final GlassSwingWorker<?> worker =
        new GlassSwingWorker<Object>() {
          @Override
          public Object construct() throws IOException {
            try (OutputStream out = new FileOutputStream(file)) {
              byte[] xml = service.exportEntity(uuid, exportSecurity);
              ByteArrayInputStream stream = new ByteArrayInputStream(xml);
              ByteStreams.copy(stream, out);
            }
            return null;
          }

          @Override
          public void finished() {
            Driver.displayInformation(parentFrame, s("exported", getEntityNameNormal()));
          }

          @Override
          public void exception() {
            Driver.displayError(parentFrame, getExportingErrorMessage(), getException());
            LOGGER.error("Error exporting " + getEntityNameLower(), getException());
          }
        };

    worker.setComponent(parentFrame);
    worker.start();
  }

  @Override
  protected final void onImport() {
    final int confirm =
        JOptionPane.showConfirmDialog(
            parentFrame,
            s("warningimport", getEntityNameLower(), CurrentLocale.get("com.tle.application.name")),
            s("import", getEntityNameNormal()),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

    if (confirm == JOptionPane.YES_OPTION) {
      FileFilter filter = new ZipFileFilter();
      final DialogResult result = DialogUtils.openDialog(parentFrame, null, filter, null);
      if (result.isOkayed()) {
        try (InputStream in = new FileInputStream(result.getFile())) {
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          ByteStreams.copy(in, out);

          doImport(service.importEntity(out.toByteArray()));
        } catch (Exception ex) {
          Driver.displayInformation(parentFrame, s("notvalid"));
          LOGGER.error("Couldn't load selected file", ex);
        }
      }
    }
  }

  private void doImport(final EntityPack<T> pack) {
    final GlassSwingWorker<Long> worker =
        new GlassSwingWorker<Long>() {
          @Override
          public Long construct() {
            String uuid = pack.getEntity().getUuid();
            return identifyEntityByUuid(uuid);
          }

          @Override
          public void finished() {
            long id = get();
            boolean newEntity = id == 0;
            if (newEntity && !canAddNew) {
              JOptionPane.showMessageDialog(parentFrame, s("notallowed"));
              return;
            }

            // Check if they also want to import security details for the entity
            if (JOptionPane.showConfirmDialog(
                    parentFrame, s("confirmimport"), s("importsecurity"), JOptionPane.YES_NO_OPTION)
                != JOptionPane.YES_OPTION) {
              pack.setTargetList(null);
              pack.setOtherTargetLists(null);
            }

            if (newEntity) {
              doNormalAdd(pack);
            } else {
              confirmBeforeOverwrite(pack, id);
            }
          }

          @Override
          public void exception() {
            Driver.displayError(parentFrame, getImportingErrorMessage(), getException());
            LOGGER.error("Error importing " + getEntityNameLower(), getException());
          }
        };

    worker.setComponent(parentFrame);
    worker.start();
  }

  final void doNormalAdd(final EntityPack<T> pack) {
    final T entity = pack.getEntity();
    final GlassSwingWorker<NameValue> worker =
        new GlassSwingWorker<NameValue>() {
          @Override
          public NameValue construct() {
            BaseEntityLabel label = add(pack, false);
            entity.setId(label.getId());
            BundleCache.invalidate(label);
            return BundleCache.getNameValue(label);
          }

          @Override
          public void finished() {
            NameValue nv = get();
            addToList(nv);

            Driver.displayInformation(parentFrame, s("importsuccess", nv.getName()));
          }

          @Override
          public void exception() {
            Exception e = getException();
            if (e instanceof InvalidDataException) {
              InvalidDataException ex = (InvalidDataException) e;
              for (ValidationError error : ex.getErrors()) {
                LOGGER.error("Validation error in " + error.getField() + ": " + error.getMessage());
              }
              Driver.displayError(parentFrame, getValidationErrorMessage(), ex);
            } else {
              Driver.displayError(parentFrame, getImportingErrorMessage(), getException());
              LOGGER.error("Error importing " + getEntityNameLower(), getException());
            }
          }
        };

    worker.setComponent(parentFrame);
    worker.start();
  }

  final void confirmBeforeOverwrite(final EntityPack<T> pack, final long id) {
    int confirm =
        JOptionPane.showConfirmDialog(
            parentFrame,
            s("warningoverwrite", getEntityNameLower()),
            s("overwriteentity", getEntityNameNormal()),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

    if (confirm == JOptionPane.YES_OPTION) {
      final GlassSwingWorker<?> worker =
          new GlassSwingWorker<Object>() {
            @Override
            public Object construct() {
              try {
                pack.getEntity().setId(id);
                startEdit(id);
                return null;
              } catch (LockedException ex) {
                return ex;
              }
            }

            @Override
            public void finished() {
              boolean unlockFirst = false;

              LockedException locked = (LockedException) get();
              if (locked != null) {
                String msg = locked.getMessage() + s("forcefully");
                int confirm1 =
                    JOptionPane.showConfirmDialog(
                        parentFrame, msg, s("locked"), JOptionPane.YES_NO_OPTION);
                unlockFirst = confirm1 == JOptionPane.YES_OPTION;
              }
              doOverwrite(pack, unlockFirst);
            }

            @Override
            public void exception() {
              Driver.displayError(parentFrame, getImportingErrorMessage(), getException());
              LOGGER.error("Error importing " + getEntityNameLower(), getException());
            }
          };

      worker.setComponent(parentFrame);
      worker.start();
    }
  }

  final void doOverwrite(final EntityPack<T> pack, final boolean unlockFirst) {
    final T entity = pack.getEntity();
    final GlassSwingWorker<?> worker =
        new GlassSwingWorker<Object>() {
          @Override
          public Object construct() {
            if (unlockFirst) {
              forceUnlock(entity.getId());
              startEdit(entity.getId());
            }

            stopEdit(pack, true);
            return null;
          }

          @Override
          public void finished() {
            Driver.displayInformation(
                parentFrame, s("importsuccess", CurrentLocale.get(entity.getName())));
          }

          @Override
          public void exception() {
            Driver.displayError(parentFrame, getImportingErrorMessage(), getException());
            LOGGER.error("Error importing " + getEntityNameLower(), getException());
          }
        };

    worker.setComponent(parentFrame);
    worker.start();
  }

  @Override
  protected final void onArchive() {
    final List<Long> ids =
        Lists.newArrayList(
            Lists.transform(
                getSelectedObjects(),
                new Function<NameValue, Long>() {
                  @Override
                  public Long apply(NameValue pair) {
                    return Long.valueOf(pair.getValue());
                  }
                }));

    final GlassSwingWorker<?> worker =
        new GlassSwingWorker<Object>() {
          private boolean didArchive = false;

          @Override
          public Object construct() {
            boolean fin = false;
            while (!fin) {
              try {
                service.archive(ids);
                didArchive = true;
                fin = true;
              } catch (LockedException locked) {
                final String msg = locked.getMessage() + s("forcefully");
                final int confirmUnlock =
                    JOptionPane.showConfirmDialog(
                        parentFrame, msg, s("locked"), JOptionPane.YES_NO_OPTION);
                if (confirmUnlock == JOptionPane.YES_OPTION) {
                  forceUnlock(locked.getEntityId());
                } else {
                  fin = true;
                }
              }
            }
            return null;
          }

          @Override
          public void finished() {
            if (didArchive) {
              Driver.displayInformation(
                  parentFrame, s("archived", ids.size(), getEntityNameLower()));
            }
          }

          @Override
          public void exception() {
            Driver.displayError(parentFrame, "", getException());
            LOGGER.error("Error archiving " + getEntityNameLower(), getException());
          }
        };

    worker.setComponent(parentFrame);
    worker.start();
  }

  @Override
  protected final void onUnarchive() {
    final List<Long> ids =
        Lists.newArrayList(
            Lists.transform(
                getSelectedObjects(),
                new Function<NameValue, Long>() {
                  @Override
                  public Long apply(NameValue pair) {
                    return Long.valueOf(pair.getValue());
                  }
                }));

    final GlassSwingWorker<?> worker =
        new GlassSwingWorker<Object>() {
          private boolean didUnarchive = false;

          @Override
          public Object construct() {
            boolean fin = false;
            while (!fin) {
              try {
                service.unarchive(ids);
                didUnarchive = true;
                fin = true;
              } catch (LockedException locked) {
                final String msg = locked.getMessage() + s("forcefully");
                final int confirmUnlock =
                    JOptionPane.showConfirmDialog(
                        parentFrame, msg, s("locked"), JOptionPane.YES_NO_OPTION);
                if (confirmUnlock == JOptionPane.YES_OPTION) {
                  forceUnlock(locked.getEntityId());
                } else {
                  fin = true;
                }
              }
            }
            return null;
          }

          @Override
          public void finished() {
            if (didUnarchive) {
              Driver.displayInformation(
                  parentFrame, s("unarchived", ids.size(), getEntityNameLower()));
            }
          }

          @Override
          public void exception() {
            Driver.displayError(parentFrame, "", getException());
            LOGGER.error("Error unarchiving " + getEntityNameLower(), getException());
          }
        };

    worker.setComponent(parentFrame);
    worker.start();
  }

  protected class ZipFileFilter extends FileFilter {
    @Override
    public boolean accept(File f) {
      if (f.isFile()) {
        return f.getName().endsWith(".zip");
      } else {
        return true;
      }
    }

    @Override
    public String getDescription() {
      return s("exported", getEntityName()) + " (*.zip)";
    }
  }

  @Deprecated
  public final Driver getDriver() {
    return driver;
  }

  public ClientService getClientService() {
    return clientService;
  }

  /** Create a new entity pack, either via the service or directly. */
  protected EntityPack<T> create() {
    return service.isStartCreateSupported() ? service.startCreate() : createEntityDirect();
  }

  private EntityPack<T> createEntityDirect() {
    T entity;
    try {
      entity = entityClass.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    entity.setUuid(UUID.randomUUID().toString());
    entity.setOwner(driver.getLoggedInUserUUID());

    return new EntityPack<>(
        entity, clientService.getService(RemoteAdminService.class).createStaging());
  }
}
