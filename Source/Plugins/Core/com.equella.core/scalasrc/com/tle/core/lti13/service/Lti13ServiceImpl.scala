package com.tle.core.lti13.service

import com.tle.beans.lti.LtiPlatform
import com.tle.core.guice.Bind
import com.tle.core.lti13.dao.Lti13DAO
import org.springframework.transaction.annotation.Transactional
import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._

@Singleton
@Bind(classOf[Lti13Service])
class Lti13ServiceImpl extends Lti13Service {
  @Inject var lti13Dao: Lti13DAO = _

  override def getByPlatformID(platformID: String): Option[LtiPlatform] =
    lti13Dao.getByPlatformID(platformID)

  override def getAll: List[LtiPlatform] = lti13Dao.enumerateAll.asScala.toList

  @Transactional
  override def create(ltiPlatform: LtiPlatform): Long = lti13Dao.save(ltiPlatform)

  @Transactional
  override def update(ltiPlatform: LtiPlatform): Unit = lti13Dao.update(ltiPlatform)

  @Transactional
  override def delete(ltiPlatform: LtiPlatform): Unit = lti13Dao.delete(ltiPlatform)
}
