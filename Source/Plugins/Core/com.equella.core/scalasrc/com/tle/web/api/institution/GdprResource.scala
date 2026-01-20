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

package com.tle.web.api.institution

import java.io.{OutputStream, PrintStream}
import java.util.zip.{ZipEntry, ZipOutputStream}
import scala.jdk.CollectionConverters._
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.tle.beans.audit.AuditLogEntry
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.exceptions.AccessDeniedException
import com.tle.legacy.LegacyGuice
import com.tle.web.api.users.UserDetails
import io.circe.generic.auto._
import io.circe.syntax._
import io.swagger.annotations.{Api, ApiParam}
import javax.ws.rs._
import javax.ws.rs.core.{Response, StreamingOutput}

@Produces(value = Array("application/json"))
@Path("userdata/")
@Api("Privacy")
class GdprResource {

  val tleUserDao = LegacyGuice.tleUserDao
  val mapper = JsonMapper
    .builder()
    .addModule(DefaultScalaModule)
    .build()

  case class AuditEntry(
      category: String,
      `type`: String,
      timestamp: String,
      sessionId: String,
      data: Map[String, String]
  )

  object AuditEntry {
    def apply(ale: AuditLogEntry): AuditEntry = {
      val data = Map(
        ("1" -> Option(ale.getData1)),
        ("2" -> Option(ale.getData2)),
        ("3" -> Option(ale.getData3)),
        ("4" -> Option(ale.getData4))
      ).collect { case (key, Some(value)) =>
        (key, value)
      }

      val meta = Option(ale.getMeta) match {
        case Some(meta) =>
          mapper.readValue(meta, classOf[Map[String, String]]).collect {
            case (k, v) if Option(v).isDefined => (k, v)
          }
        case None => Map.empty
      }

      AuditEntry(
        ale.getEventCategory,
        ale.getEventType,
        StdDateFormat.instance.clone().format(ale.getTimestamp),
        ale.getSessionId,
        data ++ meta
      )
    }
  }

  def checkPriv(): Unit = {
    if (!CurrentUser.getUserState.isSystem)
      throw new AccessDeniedException("Only TLE_ADMINISTRATOR can call this")
  }

  @DELETE
  @Path("{user}")
  def delete(
      @PathParam("user") @ApiParam(
        value = "An ID (not a username) of a user",
        required = true
      ) user: String
  ): Response = {
    checkPriv()
    LegacyGuice.auditLogService.removeEntriesForUser(user)
    Response.ok().build()
  }

  @GET
  @Path("{user}")
  @Produces(Array("application/zip"))
  def retrieve(
      @PathParam("user") @ApiParam(
        value = "An ID (not a username) of a user",
        required = true
      ) user: String
  ): Response = {
    checkPriv()
    Response
      .ok(
        new StreamingOutput {

          override def write(output: OutputStream): Unit = {

            val zip = new ZipOutputStream(output)
            zip.putNextEntry(new ZipEntry("data.json"))
            val print = new PrintStream(zip, true, "UTF8")

            print.println("{")
            def writeUser() = Option(tleUserDao.findByUuid(user)).foreach { tleUser =>
              print.print("\"user\": ")
              print.print(UserDetails.apply(tleUser).asJson.spaces2)
              print.println("\n, ")
            }

            def writeLogs() = {
              val auditLogs =
                LegacyGuice.auditLogService
                  .findByUser(user)
                  .asScala
                  .map { ale =>
                    AuditEntry(ale).asJson.spaces2
                  }
                  .mkString(", ")

              print.println("\"auditlog\": [")
              print.print(auditLogs)
              print.print("]")
            }
            writeUser()
            writeLogs()
            print.println("}")
            zip.closeEntry()
            zip.close()
          }
        }
      )
      .build()
  }
}
