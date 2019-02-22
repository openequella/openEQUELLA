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
