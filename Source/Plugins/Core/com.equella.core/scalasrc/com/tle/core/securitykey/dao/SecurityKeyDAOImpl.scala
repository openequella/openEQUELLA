package com.tle.core.securitykey.dao

import com.tle.beans.securitykey.SecurityKey
import com.tle.core.guice.Bind
import com.tle.core.hibernate.dao.GenericDaoImpl
import org.hibernate.Session
import javax.inject.Singleton
import scala.util.Try

@Bind(classOf[SecurityKeyDAO])
@Singleton
class SecurityKeyDAOImpl
    extends GenericDaoImpl[SecurityKey, java.lang.Long](classOf[SecurityKey])
    with SecurityKeyDAO {
  override def getByKeyID(keyId: String): Option[SecurityKey] =
    Try {
      getHibernateTemplate
        .execute(
          (session: Session) =>
            session
              .getNamedQuery("getByKeyID")
              .setParameter("keyId", keyId)
              .getSingleResult)
        .asInstanceOf[SecurityKey]
    }.toOption
}
