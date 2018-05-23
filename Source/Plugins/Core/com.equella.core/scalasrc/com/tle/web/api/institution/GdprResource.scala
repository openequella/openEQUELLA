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

package com.tle.web.api.institution

import java.io.{OutputStream, PrintStream}
import java.util.Date
import java.util.zip.{ZipEntry, ZipOutputStream}

import com.fasterxml.jackson.databind.util.StdDateFormat
import com.tle.beans.audit.AuditLogEntry
import com.tle.common.institution.CurrentInstitution
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.common.util.Dates
import com.tle.exceptions.AccessDeniedException
import com.tle.legacy.LegacyGuice
import com.tle.web.api.users.UserQueryResult
import com.tle.web.remoting.resteasy.ISO8061DateFormatWithTZ
import io.circe.generic.auto._
import io.circe.syntax._
import io.swagger.annotations.Api
import javax.ws.rs._
import javax.ws.rs.core.{Response, StreamingOutput}

import scala.collection.JavaConverters._

@Produces(value = Array("application/json"))
@Path("userdata/")
@Api(value = "Privacy")
class GdprResource {

  val auditLogDao = LegacyGuice.auditLogDao
  val tleUserDao = LegacyGuice.tleUserDao

  case class AuditEntry(category: String, `type`: String, timestamp: String, sessionId: String, data: Map[String, String])

  object AuditEntry {
    def apply(ale: AuditLogEntry): AuditEntry = {
      val data = Map("1" -> Option(ale.getData1),
        "2" -> Option(ale.getData2),
        "3" -> Option(ale.getData3),
        "4" -> Option(ale.getData4)).collect {
        case (k, Some(v)) => (k, v)
      }
      AuditEntry(ale.getEventCategory, ale.getEventType, StdDateFormat.instance.clone().format(ale.getTimestamp), ale.getSessionId, data)
    }
  }

  def checkPriv(): Unit = {
    if (!CurrentUser.getUserState.isSystem) throw new AccessDeniedException("Only TLE_ADMINISTRATOR can call this")
  }

  @DELETE
  @Path("{user}")
  def delete(@PathParam("user") user: String): Response = {
    checkPriv()
    auditLogDao.deleteForUser(CurrentInstitution.get(), user)
    Response.ok().build()
  }

  @GET
  @Path("{user}")
  @Produces(Array("application/zip"))
  def retrieve(@PathParam("user") user: String): Response = {
    checkPriv()
    Response.ok(
      new StreamingOutput {

        override def write(output: OutputStream): Unit = {

          val zip = new ZipOutputStream(output)
          zip.putNextEntry(new ZipEntry("data.json"))
          val print = new PrintStream(zip, true, "UTF8")

          print.println("{")
          def writeUser() = Option(tleUserDao.findByUuid(user)).foreach {
            tleUser =>
              print.print("\"user\": ")
              print.print(UserQueryResult.apply(tleUser).asJson.spaces2)
              print.println("\n, ")
          }
          def writeLogs() = {
            print.println("\"auditlog\": [")
            var first = true
            auditLogDao.listForUser(CurrentInstitution.get, user).asScala.foreach { ale =>
              if (!first) print.print(", ");
              print.print(AuditEntry(ale).asJson.spaces2)
              first = false
              auditLogDao.clear()
            }
            print.print("]")
          }
          writeUser()
          writeLogs()
          print.println("}")
          zip.closeEntry()
          zip.close()
        }
      }
    ).build()
  }
}
