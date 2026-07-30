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

package com.tle.admin.service

import com.tle.admin.graphql.conversion.BaseEntityReferenceViewConverter.toBaseEntityLabel
import com.tle.admin.graphql.conversion.EntityPackBuilder
import com.tle.admin.rest.{
  splitPath,
  RestConfiguration,
  RestError,
  StagingApi,
  StagingFileTree,
  StatusCodeError
}
import com.tle.beans.entity.{BaseEntity, BaseEntityLabel}
import com.tle.common.EntityPack
import com.tle.common.beans.exception.NotFoundException
import com.tle.common.filesystem.FileEntry
import com.tle.core.remoting.RemoteAbstractEntityService
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.{ApiError, BaseEntityApi}
import io.github.openequella.graphql.api.views.{BaseEntityReferenceView, BaseEntitySecurityView}

import sttp.model.StatusCode

import java.{lang, util}
import scala.jdk.CollectionConverters._

/** This base class is used to help with the migration of the HTTP Invoker implementations to
  * GraphQL. Due to the current class hierarchy abstraction, it is (very) challenging to determine
  * which methods are used by which RemoteAbstractEntityServices - e.g. do we need to add GraphQL
  * endpoints for all these methods for AdminSchemaService?
  *
  * The key benefit of this class is that it provides an 'implementation' of every method via the
  * implementMe method. This allows us to override this method and capture the calls to the
  * RemoteAbstractEntityService methods so that we know which methods are used and which methods are
  * not. Thereby generating a list of what needs to be implemented in GraphQL.
  *
  * Long term, this class will likely remain to match the expected interface of the various UI
  * components. But longer term the plan is to replace the Admin Console with a Web implementation,
  * so then all this will go.
  */
abstract class AdminEntityService[E <: BaseEntity] extends RemoteAbstractEntityService[E] {

  /** A short human-readable name for the entity type managed by this service, used in error and log
    * messages produced by the GraphQL helper methods. Override in subclasses with a specific name
    * such as `"collection"` or `"schema"`.
    */
  def entityDescription: String = "entity"

  /** Configuration for the GraphQL client, used by the entity type agnostic operations implemented
    * in this class. Satisfied by subclasses via an injected constructor parameter.
    */
  protected implicit def cfg: ClientConfiguration

  /** Configuration for the REST API client, used by the staging file operations implemented in this
    * class. Satisfied by subclasses via an injected constructor parameter.
    */
  protected implicit def restCfg: RestConfiguration

  /** This method is used to flag which methods in a RemoteAbstractEntityService instance need to be
    * implemented. It is intended to be overridden by the subclasses to provide a map back to an
    * actual RemoteAbstractEntityService as a delegate, while also logging the method name and the
    * NotImplementedError.
    *
    * The default implementation here, though, will throw a NotImplementedError, which will be
    * caught by the caller and logged (ideally).
    *
    * @param f
    *   a function that takes a RemoteAbstractEntityService and returns a value of type T
    * @tparam T
    *   the type of the value returned by the function
    * @return
    *   the value returned by the function
    */
  protected def implementMe[T](f: RemoteAbstractEntityService[E] => T): T =
    throw new NotImplementedError()

  // ---------------------------------------------------------------------------
  // GraphQL helper methods
  // These eliminate the boilerplate `Either` pattern-match in every GraphQL-
  // backed override. Each helper is curried: API function references first,
  // entity operation arguments second.
  // ---------------------------------------------------------------------------

  /** Type alias for the standard GraphQL client result — either a list of API errors or a
    * successful value of type `A`. Used throughout the helper method signatures below to reduce
    * verbosity.
    */
  private type GraphQLClientResult[A] = Either[List[ApiError], A]

  /** Unwraps an `Either` result, delegating successful values to `onSuccess` and throwing the
    * exception produced by `makeException` on errors. The GraphQL and REST handlers below delegate
    * their `Either` pattern-match to this method.
    *
    * @param result
    *   the `Either` result from a client call
    * @param errorMessage
    *   the message to include in the exception if the result is a `Left`
    * @param makeException
    *   builds the exception to throw from the message and the error value
    * @param onSuccess
    *   function applied to the unwrapped value when the result is a `Right`
    * @tparam Err
    *   the type of the error value
    * @tparam A
    *   the type of the successful result
    * @tparam B
    *   the return type of `onSuccess`
    * @return
    *   the value produced by `onSuccess`
    */
  private def unwrapOrThrow[Err, A, B](
      result: Either[Err, A],
      errorMessage: String,
      makeException: (String, Err) => Exception
  )(onSuccess: A => B): B = result match {
    case Right(value) => onSuccess(value)
    case Left(error)  => throw makeException(errorMessage, error)
  }

  /** Unwraps a GraphQL `Either` result, throwing a `ClientRequestException` on errors.
    *
    * @throws ClientRequestException
    *   on GraphQL errors
    */
  private def handleEither[A, B](
      result: GraphQLClientResult[A],
      errorMessage: String
  )(onSuccess: A => B): B =
    unwrapOrThrow(result, errorMessage, new ClientRequestException(_, _))(onSuccess)

  /** Specialized handler for GraphQL operations that return `Unit` (operations with no result
    * payload, such as cancel-edit or delete). Unwraps the result and throws
    * `ClientRequestException` on errors; returns `()` on success.
    *
    * @param result
    *   the `GraphQLClientResult[Unit]` from a GraphQL call
    * @param errorMessage
    *   the message to include in the exception if the result is a `Left`
    * @throws ClientRequestException
    *   on GraphQL errors
    */
  private def handleEitherUnit(
      result: GraphQLClientResult[Unit],
      errorMessage: String
  ): Unit = handleEither(result, errorMessage)(_ => ())

  /** The REST counterpart of [[handleEither]]: unwraps a REST client `Either` result, throwing a
    * `RestRequestException` on errors. Used by the staging file operations, which go over the REST
    * API rather than GraphQL.
    *
    * @throws RestRequestException
    *   on REST errors
    */
  private def handleRestEither[A, B](
      result: Either[RestError, A],
      errorMessage: String
  )(onSuccess: A => B): B =
    unwrapOrThrow(result, errorMessage, new RestRequestException(_, _))(onSuccess)

  /** Lists all entities using a GraphQL lister that returns `BaseEntityReferenceView`, converting
    * each result to a `BaseEntityLabel`.
    *
    * @param result
    *   the pre-evaluated result from the GraphQL list call
    * @return
    *   a Java list of `BaseEntityLabel`
    * @throws ClientRequestException
    *   on GraphQL errors
    */
  protected def listAllFrom(
      result: GraphQLClientResult[List[BaseEntityReferenceView]]
  ): util.List[BaseEntityLabel] =
    handleEither(result, s"Error listing ${entityDescription}s.") {
      _.map(toBaseEntityLabel).asJava
    }

  /** Looks up an entity ID by UUID using a GraphQL getter.
    *
    * @param getter
    *   function that accepts a UUID string and returns the entity ID or None
    * @param uuid
    *   the UUID to look up
    * @return
    *   the entity ID, or `0` if the entity was not found
    * @throws ClientRequestException
    *   on GraphQL errors
    */
  protected def idByUuid(
      getter: String => GraphQLClientResult[Option[Long]]
  )(uuid: String): Long =
    handleEither(getter(uuid), s"Error identifying $entityDescription by UUID: $uuid") {
      _.getOrElse(0L)
    }

  /** Exports an entity as a ZIP file, choosing the appropriate API call based on the `useSecurity`
    * flag.
    *
    * @param withoutSecurity
    *   API function to call when security information should be excluded
    * @param withSecurity
    *   API function to call when security ACLs should be included
    * @param id
    *   the ID of the entity to export
    * @param useSecurity
    *   whether to include security ACLs in the export
    * @return
    *   the exported ZIP bytes
    * @throws NotFoundException
    *   if the entity was not found or the export returned nothing
    * @throws ClientRequestException
    *   on GraphQL errors
    */
  protected def exportWith(
      withoutSecurity: Long => GraphQLClientResult[Option[Array[Byte]]],
      withSecurity: Long => GraphQLClientResult[Option[Array[Byte]]]
  )(id: Long, useSecurity: Boolean): Array[Byte] = {
    val exportResult = if (useSecurity) withSecurity(id) else withoutSecurity(id)

    handleEither(
      exportResult,
      s"Error exporting $entityDescription with ID: $id"
    ) {
      case Some(bytes) => bytes
      case None        =>
        throw new NotFoundException(
          s"${entityDescription.capitalize} with ID: $id not found or export failed."
        )
    }
  }

  /** Cancels editing of an entity, optionally force-unlocking it.
    *
    * @param normal
    *   API function for a normal cancel-edit
    * @param forced
    *   API function for a forced cancel-edit (unlocks even if locked by another user)
    * @param id
    *   the ID of the entity to cancel editing on
    * @param force
    *   whether to force-unlock
    * @throws ClientRequestException
    *   on GraphQL errors
    */
  protected def cancelEditWith(
      normal: Long => GraphQLClientResult[Unit],
      forced: Long => GraphQLClientResult[Unit]
  )(id: Long, force: Boolean): Unit = {
    val cancelEditResult = if (force) forced(id) else normal(id)

    handleEitherUnit(
      cancelEditResult,
      s"Error cancelling edit of $entityDescription with ID: $id"
    )
  }

  /** Deletes an entity, optionally checking for references first.
    *
    * @param withoutCheck
    *   API function to delete without checking references
    * @param withCheck
    *   API function to delete only if no references exist
    * @param id
    *   the ID of the entity to delete
    * @param checkReferences
    *   whether to check for references before deleting
    * @throws ClientRequestException
    *   on GraphQL errors
    */
  protected def deleteWith(
      withoutCheck: Long => GraphQLClientResult[Unit],
      withCheck: Long => GraphQLClientResult[Unit]
  )(id: Long, checkReferences: Boolean): Unit = {
    val deleteResult = if (checkReferences) withCheck(id) else withoutCheck(id)

    handleEitherUnit(
      deleteResult,
      s"Error deleting $entityDescription with ID: $id"
    )
  }

  /** Clones an entity using a GraphQL cloner function.
    *
    * @param cloner
    *   API function that clones the entity and returns a reference to the new clone
    * @param id
    *   the ID of the entity to clone
    * @return
    *   a `BaseEntityLabel` for the newly created clone
    * @throws ClientRequestException
    *   on GraphQL errors
    */
  protected def cloneWith(
      cloner: Long => GraphQLClientResult[BaseEntityReferenceView]
  )(id: Long): BaseEntityLabel =
    handleEither(cloner(id), s"Error cloning $entityDescription with ID: $id") {
      toBaseEntityLabel
    }

  /** Imports an entity from a ZIP file using a GraphQL import function.
    *
    * @param importer
    *   API function that accepts ZIP bytes and returns a GraphQL result containing a view
    * @param converter
    *   function to convert the returned view into an `EntityPack[E]`
    * @param zip
    *   the ZIP file bytes to import
    * @tparam V
    *   the type of the view returned by the import API
    * @return
    *   an `EntityPack` for the imported entity
    * @throws ClientRequestException
    *   on GraphQL errors
    */
  protected def importWith[V](
      importer: Array[Byte] => GraphQLClientResult[V]
  )(converter: V => EntityPack[E])(zip: Array[Byte]): EntityPack[E] =
    handleEither(importer(zip), s"Error importing $entityDescription from zip file") { converter }

  /** Assembles the read-only entity pack: the entity from this service's own type specific `get`,
    * plus the access control details from the given fetcher. Mirrors
    * `com.tle.core.entity.service.impl.AbstractEntityServiceImpl#getReadOnlyPack`, which likewise
    * sets neither a staging ID nor a version - nothing is being edited.
    *
    * @param securityFetcher
    *   API function that accepts an entity ID and returns its access control details
    * @param id
    *   the ID of the entity to build a read-only pack for
    * @return
    *   an `EntityPack` holding the entity and its access control details
    * @throws NotFoundException
    *   if there is no such entity
    * @throws ClientRequestException
    *   on GraphQL errors
    */
  protected def readOnlyPackWith(
      securityFetcher: Long => GraphQLClientResult[BaseEntitySecurityView]
  )(id: Long): EntityPack[E] = {
    // Fetched first so that a missing entity is reported as the NotFoundException the legacy
    // remoting contract promised, rather than as an access control lookup failure.
    val entity = get(id)

    handleEither(
      securityFetcher(id),
      s"Error getting the access control details of $entityDescription with ID: $id"
    ) { security =>
      EntityPackBuilder
        .forEntity(entity)
        .withTargetList(security.targetList)
        // Unconditional, unlike MetadataSchemaEditViewConverter.toEntityPack: for an entity type
        // with no sub-entity lists this leaves otherTargetLists unset, which is exactly what the
        // legacy fillTargetLists does when it finds none.
        .withOtherTargetList(security.otherTargetLists)
        .build()
    }
  }

  /** Starts editing an entity using a GraphQL start-edit function.
    *
    * @param starter
    *   API function that accepts an entity ID and returns a GraphQL result containing an edit view
    * @param converter
    *   function to convert the returned view into an `EntityPack[E]`
    * @param id
    *   the ID of the entity to start editing
    * @tparam V
    *   the type of the edit view returned by the start-edit API
    * @return
    *   an `EntityPack` for the entity being edited
    * @throws ClientRequestException
    *   on GraphQL errors
    */
  protected def startEditWith[V](
      starter: Long => GraphQLClientResult[V]
  )(converter: V => EntityPack[E])(id: Long): EntityPack[E] =
    handleEither(starter(id), s"Error starting edit of $entityDescription with ID: $id") {
      converter
    }

  /** Stops editing an entity, saving changes and optionally releasing the lock.
    *
    * @param save
    *   API function that saves the changes while keeping the lock
    * @param saveAndUnlock
    *   API function that saves the changes and releases the lock
    * @param fromPack
    *   function to convert the `EntityPack[E]` into the details view expected by the API
    * @param converter
    *   function to convert the returned view into the saved entity `E`
    * @param pack
    *   the entity pack holding the changes to save
    * @param unlock
    *   whether to release the lock after saving
    * @tparam D
    *   the type of the details view sent to the save API
    * @tparam V
    *   the type of the view returned by the save API
    * @return
    *   the saved entity `E`
    * @throws ClientRequestException
    *   on GraphQL errors
    */
  protected def stopEditWith[D, V](
      save: D => GraphQLClientResult[V],
      saveAndUnlock: D => GraphQLClientResult[V]
  )(fromPack: EntityPack[E] => D)(converter: V => E)(pack: EntityPack[E], unlock: Boolean): E = {
    val details        = fromPack(pack)
    val stopEditResult = if (unlock) saveAndUnlock(details) else save(details)

    handleEither(
      stopEditResult,
      s"Error saving changes for $entityDescription with ID: ${pack.getEntity.getId}"
    ) { converter }
  }

  // ---------------------------------------------------------------------------
  // RemoteAbstractEntityService default implementations (delegate to implementMe)
  // ---------------------------------------------------------------------------

  override def get(id: Long): E = implementMe {
    _.get(id)
  }

  override def getByUuid(uuid: String): E = implementMe {
    _.getByUuid(uuid)
  }

  override def identifyByUuid(uuid: String): Long = implementMe {
    _.identifyByUuid(uuid)
  }

  override def getUuidForId(id: Long): String = implementMe {
    _.getUuidForId(id)
  }

  override def add(pack: EntityPack[E], lockAfterwards: Boolean): BaseEntityLabel = implementMe {
    _.add(pack, lockAfterwards)
  }

  override def delete(entityid: Long, checkReferences: Boolean): Unit = implementMe {
    _.delete(entityid, checkReferences)
  }

  override def archive(entityid: Long): Unit = implementMe {
    _.archive(entityid)
  }

  override def archive(ids: util.List[lang.Long]): Unit = implementMe {
    _.archive(ids)
  }

  override def unarchive(entityid: Long): Unit = implementMe {
    _.unarchive(entityid)
  }

  override def unarchive(ids: util.List[lang.Long]): Unit = implementMe {
    _.unarchive(ids)
  }

  override def listEditable(): util.List[BaseEntityLabel] = implementMe {
    _.listEditable()
  }

  override def listAll(): util.List[BaseEntityLabel] = implementMe {
    _.listAll()
  }

  override def listEnabled(): util.List[BaseEntityLabel] = implementMe {
    _.listEnabled()
  }

  /** The default implementation of this method is to delegate to listAll, as in most cases the
    * system entities are not relevant to the UI. Override in subclasses if a different approach is
    * needed. Indeed, the only known instances of a 'system type' entity is the "My Content" schema
    * used for Scrapbook items via MyContentService. There's also some ID constants for it in
    * `com.tle.mycontent.MyContentConstants`.
    */
  override def listAllIncludingSystem(): util.List[BaseEntityLabel] = implementMe {
    _.listAllIncludingSystem()
  }

  override def enumerateEditable(): util.List[E] = implementMe {
    _.enumerateEditable()
  }

  override def enumerateEnabled(): util.List[E] = implementMe {
    _.enumerateEnabled()
  }

  /** Implemented once here for every entity type: the entity comes from this service's own type
    * specific `get`, and the access control half from the entity type agnostic
    * `baseEntities.securityById` query.
    */
  override def getReadOnlyPack(id: Long): EntityPack[E] =
    readOnlyPackWith(BaseEntityApi.getSecurityById)(id)

  override def startEdit(id: Long): EntityPack[E] = implementMe {
    _.startEdit(id)
  }

  override def startCreate(): EntityPack[E] = implementMe {
    _.startCreate()
  }

  override def isStartCreateSupported: Boolean = implementMe {
    _.isStartCreateSupported
  }

  override def cancelEdit(id: Long, force: Boolean): Unit = implementMe {
    _.cancelEdit(id, force)
  }

  override def stopEdit(pack: EntityPack[E], unlock: Boolean): E = implementMe {
    _.stopEdit(pack, unlock)
  }

  override def hasReferencingClasses(id: Long): Boolean = implementMe {
    _.hasReferencingClasses(id)
  }

  override def exportEntity(id: Long, withSecurity: Boolean): Array[Byte] = implementMe {
    _.exportEntity(id, withSecurity)
  }

  override def importEntity(zip: Array[Byte]): EntityPack[E] = implementMe {
    _.importEntity(zip)
  }

  override def clone(id: Long): BaseEntityLabel = implementMe {
    _.clone(id)
  }

  // ---------------------------------------------------------------------------
  // Staging file operations (REST implementations)
  // These operate purely on the staging area identified by the staging ID — no entity type is
  // involved — so they are implemented once here, over the REST staging API, for all entities.
  // ---------------------------------------------------------------------------

  override def uploadFile(stagingID: String, filename: String, bytes: Array[Byte]): Unit =
    handleRestEither(
      StagingApi.putFile(stagingID, filename, bytes),
      s"Error uploading file '$filename' to staging area: $stagingID"
    )(identity)

  override def downloadFile(stagingID: String, filename: String): Array[Byte] =
    handleRestEither(
      StagingApi.getFile(stagingID, filename),
      s"Error downloading file '$filename' from staging area: $stagingID"
    )(identity)

  /** Treats a NotFound response as success: deleting a path that no longer exists (e.g. a stale
    * tree entry) is a silent no-op, matching the legacy invoker behaviour.
    */
  private def ignoreNotFound(result: Either[RestError, Unit]): Either[RestError, Unit] =
    result match {
      case Left(StatusCodeError(_, code)) if code == StatusCode.NotFound => Right(())
      case other                                                         => other
    }

  override def deleteFileFolder(stagingID: String, path: String): Unit =
    handleRestEither(
      ignoreNotFound(StagingApi.deleteFile(stagingID, path)),
      s"Error deleting '$path' from staging area: $stagingID"
    )(identity)

  override def createFolder(stagingID: String, path: String, name: String): Unit =
    handleRestEither(
      // path + name preserves the caller's concatenation (path arrives with a trailing slash)
      StagingApi.createFolder(stagingID, path + name),
      s"Error creating folder '$name' in staging area: $stagingID"
    )(identity)

  override def buildStagingTree(stagingID: String, path: String): FileEntry =
    handleRestEither(
      StagingApi.getStaging(stagingID, Some(path)),
      s"Error listing files in staging area: $stagingID"
    ) { listing =>
      StagingFileTree.buildFileEntryTree(listing.files, splitPath(path).lastOption.getOrElse(""))
    }
}
