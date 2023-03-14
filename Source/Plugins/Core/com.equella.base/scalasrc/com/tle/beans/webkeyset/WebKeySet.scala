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

package com.tle.beans.webkeyset

import org.hibernate.annotations.NamedQuery
import java.time.Instant
import javax.persistence.{Column, Entity, GeneratedValue, GenerationType, Id, Index, Lob, Table}

/**
  * The table generated from this Entity is used to store cryptographic keys where the algorithm is asymmetric and generates
  * a key pair of a private key and a public key. This is designed to be used to back the JWKS endpoint.
  */
@Entity
@Table(indexes = Array {
  new Index(name = "web_key_set_id", columnList = "keyId")
})
@NamedQuery(name = "getByKeyID", query = "from WebKeySet WHERE keyId = :keyId")
class WebKeySet {

  /**
    * Database automatically generated ID used as the primary key.
    */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  /**
    * Unique ID of the key pair.
    */
  @Column(nullable = false, unique = true)
  var keyId: String = _

  /**
    * The algorithm used to generate the key pair.
    */
  @Column(nullable = false, columnDefinition = "VARCHAR(5)")
  var algorithm: String = _

  /**
    * The public key in PEM format.
    */
  @Lob
  @Column(nullable = false)
  var publicKey: String = _

  /**
    * The private key in PEM format but encrypted.
    */
  @Lob
  @Column(nullable = false)
  var privateKey: String = _

  /**
    * The date when the key pair is generated.
    */
  @Column(nullable = false)
  var created: Instant = _

  /**
    * The date when the key pair is deactivated. Null if it is still active.
    */
  var deactivated: Instant = _

  /**
    * ID of the institution which the key set belongs to.
    */
  var inst_id: Long = _
}
