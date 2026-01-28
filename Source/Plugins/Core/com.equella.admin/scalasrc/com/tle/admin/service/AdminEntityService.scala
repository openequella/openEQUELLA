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

import com.tle.beans.entity.{BaseEntity, BaseEntityLabel}
import com.tle.common.EntityPack
import com.tle.common.filesystem.FileEntry
import com.tle.core.remoting.RemoteAbstractEntityService

import java.{lang, util}

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

  override def listAllIncludingSystem(): util.List[BaseEntityLabel] = implementMe {
    _.listAllIncludingSystem()
  }

  override def enumerateEditable(): util.List[E] = implementMe {
    _.enumerateEditable()
  }

  override def enumerateEnabled(): util.List[E] = implementMe {
    _.enumerateEnabled()
  }

  override def getReadOnlyPack(id: Long): EntityPack[E] = implementMe {
    _.getReadOnlyPack(id)
  }

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

  override def uploadFile(stagingID: String, filename: String, bytes: Array[Byte]): Unit =
    implementMe {
      _.uploadFile(stagingID, filename, bytes)
    }

  override def downloadFile(stagingID: String, filename: String): Array[Byte] = implementMe {
    _.downloadFile(stagingID, filename)
  }

  override def deleteFileFolder(stagingID: String, path: String): Unit = implementMe {
    _.deleteFileFolder(stagingID, path)
  }

  override def createFolder(stagingID: String, path: String, name: String): Unit = implementMe {
    _.createFolder(stagingID, path, name)
  }

  override def buildStagingTree(stagingID: String, path: String): FileEntry = implementMe {
    _.buildStagingTree(stagingID, path)
  }
}
