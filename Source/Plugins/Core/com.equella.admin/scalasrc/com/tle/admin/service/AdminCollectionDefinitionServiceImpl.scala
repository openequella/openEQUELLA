package com.tle.admin.service

import com.tle.beans.entity.BaseEntityLabel
import com.tle.beans.entity.itemdef.ItemDefinition
import com.tle.core.remoting.{RemoteAbstractEntityService, RemoteItemDefinitionService}
import io.github.openequella.graphql.ClientConfiguration
import org.slf4j.{Logger, LoggerFactory}

import java.util
import javax.inject.{Inject, Singleton}

@Singleton
class AdminCollectionDefinitionServiceImpl @Inject() (val delegate: RemoteItemDefinitionService)(
    implicit val cfg: ClientConfiguration
) extends AdminEntityService[ItemDefinition]
    with AdminCollectionDefinitionService {
  private implicit val LOGGER: Logger =
    LoggerFactory.getLogger(classOf[AdminCollectionDefinitionServiceImpl])

  override def enumerateCategories: util.Set[String] = withDelegate {
    _.enumerateCategories()
  }

  override def listUsableItemDefinitionsForSchema(schemaID: Long): util.List[BaseEntityLabel] =
    withDelegate {
      _.listUsableItemDefinitionsForSchema(schemaID)
    }

  override def getSchemaIdForCollectionUuid(value: String): Long = withDelegate {
    _.getSchemaIdForCollectionUuid(value)
  }

  override def exportControl(controlXml: String): Array[Byte] = withDelegate {
    _.exportControl(controlXml)
  }

  override def importControl(zipFileData: Array[Byte]): String = withDelegate {
    _.importControl(zipFileData)
  }

  override def implementMe[T](f: RemoteAbstractEntityService[ItemDefinition] => T): T = {
    logNotImplemented("RemoteAbstractEntityService[ItemDefinition]")
    f(delegate)
  }

  private def withDelegate[T](f: RemoteItemDefinitionService => T): T = {
    logNotImplemented("RemoteItemDefinitionService")
    f(delegate)
  }

  private def logNotImplemented(forInterface: String): Unit = {
    LOGGER.warn(
      "Missing implementation of [{}] for {}, will try delegate.",
      getCallerMethodName,
      forInterface,
      new NotImplementedError()
    )
  }

  private def getCallerMethodName: String = {
    Thread.currentThread().getStackTrace()(4).getMethodName
  }
}
