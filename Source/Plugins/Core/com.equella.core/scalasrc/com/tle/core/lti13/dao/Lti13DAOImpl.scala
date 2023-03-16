package com.tle.core.lti13.dao

import com.tle.beans.lti.LtiPlatform
import com.tle.common.institution.CurrentInstitution
import com.tle.core.guice.Bind
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl
import org.hibernate.Session

import javax.inject.Singleton
import scala.jdk.CollectionConverters._
import scala.util.Try

@Singleton
@Bind(classOf[Lti13DAO])
class Lti13DAOImpl
    extends GenericInstitionalDaoImpl[LtiPlatform, java.lang.Long](classOf[LtiPlatform])
    with Lti13DAO {

  override def getByPlatformID(platformID: String): Option[LtiPlatform] =
    Try {
      getHibernateTemplate
        .execute(
          (session: Session) =>
            session
              .getNamedQuery("getByPlatformID")
              .setParameter("platformID", platformID)
              .setParameter("institution", CurrentInstitution.get())
              .getResultList)
        .asScala
        .toList
        .asInstanceOf[List[LtiPlatform]]
    }.toEither
      .filterOrElse(_.size <= 1,
                    new Throwable(s"More than one LTI platforms matching ID $platformID"))
      .fold(throw _, _.headOption)
}
