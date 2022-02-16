package com.tle.beans.newentity

import org.hibernate.annotations.{AttributeAccessor, Type}
import java.time.Instant
import javax.persistence._

@Embeddable
class EntityID extends Serializable {
  @Column(length = 36)
  var uuid: String  = _
  var inst_id: Long = _
}

@javax.persistence.Entity
@AttributeAccessor("field")
@Table(name = "entities", indexes = Array {
  new Index(name = "entityTypeIdx", columnList = "inst_id, typeid")
})
class Entity {
  @EmbeddedId
  var id: EntityID = _
  @Column(length = 20)
  var typeid: String = _
  @Lob
  var name: String = _
  @Type(`type` = "json")
  var name_strings: String = _
  @Lob
  var description: String = _
  @Type(`type` = "json")
  var description_strings: String = _
  @Column
  var owner: String     = _
  var created: Instant  = _
  var modified: Instant = _
  @Type(`type` = "json")
  var data: String = _
}
