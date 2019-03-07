/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.db.types

import java.time.Instant
import java.util.GregorianCalendar

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.{DeserializationContext, JsonDeserializer}

@JsonDeserialize(using = classOf[CalendarDateJackson])
case class CalendarDate(year: Int, month: Int, day: Int) {
  def toInstant: Instant = new GregorianCalendar(year, month, day).toInstant

  @JsonValue
  def toJson() = {
    s"$year-$month-$day"
  }
}

class CalendarDateJackson extends JsonDeserializer[CalendarDate] {
  override def deserialize(p: JsonParser, ctxt: DeserializationContext): CalendarDate = {
    p.readValueAs(classOf[String]).split('-') match {
      case Array(sYear, sMonth, sDay) => CalendarDate(sYear.toInt, sMonth.toInt, sDay.toInt)
    }
  }
}
