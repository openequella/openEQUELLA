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

package com.tle.core.json

import com.tle.web.sections.js.generic.expression.{ArrayExpression, ObjectExpression}
import com.tle.web.sections.js.{JSExpression, JSUtils}
import io.circe.syntax._
import io.circe.{Encoder, Json, JsonObject}

object CirceUtils {

  def jsonToExpression(j: Json): JSExpression = {

    def objExpression(jsonObject: JsonObject): ObjectExpression = {
      val o = new ObjectExpression()
      jsonObject.toIterable.foreach { case (k, v) => o.put(k, jsonToExpression(v)) }
      o
    }

    JSUtils.convertExpression(
      j.fold(
        null,
        java.lang.Boolean.valueOf,
        _.toDouble,
        identity,
        a => new ArrayExpression(a.map(jsonToExpression): _*),
        objExpression
      )
    )
  }

  def circeToExpression[A: Encoder](a: A): JSExpression = jsonToExpression(a.asJson)

}
