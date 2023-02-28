package com.tle.beans.securitykey

import org.hibernate.annotations.NamedQuery
import java.time.Instant
import javax.persistence.{Column, Entity, GeneratedValue, GenerationType, Id, Index, Lob, Table}

@Entity
@Table(indexes = Array {
  new Index(name = "security_key_id", columnList = "keyId")
})
@NamedQuery(name = "getByKeyID", query = "from SecurityKey WHERE keyId = :keyId")
class SecurityKey {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  @Column(nullable = false, unique = true)
  var keyId: String = _

  @Column(nullable = false, columnDefinition = "VARCHAR(5)")
  var algorithm: String = _

  @Lob
  @Column(nullable = false)
  var publicKey: String = _

  @Lob
  @Column(nullable = false)
  var privateKey: String = _

  @Column(nullable = false)
  var created: Instant = _

  var deactivated: Instant = _
}
