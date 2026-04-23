package io.github.openequella.rest

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.tle.common.URLUtils
import com.tle.webtests.pageobject.AbstractPage
import com.tle.webtests.test.files.Attachments
import org.apache.commons.httpclient.methods._
import org.apache.commons.httpclient.{HttpMethod, HttpStatus, NameValuePair}
import org.testng.Assert._
import org.testng.annotations.Test

import java.io.File

case class StagingFile(name: String, size: Long)
case class StagingArea(uuid: String, files: List[StagingFile])
case class UploadedPart(partNumber: Int, etag: String)
case class ApiResponse[T](status: Int, body: T)

class StagingApiTest extends AbstractRestApiTest {

  private val TEST_FILENAME = "Special characters - хцч test2.jpg"
  private val AVATAR_FILE   = "avatar.png"
  private val PACKAGE_FILE  = "package.zip"

  private val scalaMapper: ObjectMapper = new ObjectMapper()
    .registerModule(DefaultScalaModule)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  override def loginAsLowPrivilegeUser(): Unit =
    makeClientRequest(authHelper.buildLoginMethod("AutoTest_StagingLowPriv", "``````"))

  private def withStaging(testCode: String => Unit): Unit = {
    val response = StagingApi.createStaging()
    assertEquals(response.status, HttpStatus.SC_CREATED, "Staging creation failed")
    assertTrue(response.body.isDefined, "Missing staging UUID in response body")

    val uuid = response.body.get
    try testCode(uuid)
    finally StagingApi.deleteStaging(uuid)
  }

  @Test(description = "Guest users should not be able to access staging endpoints")
  def guestAccessDeniedTest(): Unit = withStaging { stagingUuid =>
    val file = getTestFile(AVATAR_FILE)
    logout()
    assertFalse(hasAuthenticatedSession, "Session should be guest after logout")

    assertEquals(StagingApi.createStaging().status, HttpStatus.SC_FORBIDDEN)
    assertEquals(StagingApi.getStaging(stagingUuid).status, HttpStatus.SC_FORBIDDEN)
    assertEquals(
      StagingApi.uploadFile(stagingUuid, "guest-upload.txt", file, None).status,
      HttpStatus.SC_FORBIDDEN
    )
    assertEquals(StagingApi.headFile(stagingUuid, AVATAR_FILE).status, HttpStatus.SC_FORBIDDEN)
    assertEquals(StagingApi.deleteFile(stagingUuid, AVATAR_FILE).status, HttpStatus.SC_FORBIDDEN)
    assertEquals(StagingApi.deleteStaging(stagingUuid), HttpStatus.SC_FORBIDDEN)

    // Restore the authenticated session so subsequent tests don't run as guest
    login()
    assertTrue(hasAuthenticatedSession, "Authenticated session should be restored")
  }

  @Test(description = "Create an empty staging area")
  def createStagingTest(): Unit = withStaging { stagingUuid =>
    val response = StagingApi.getStaging(stagingUuid)
    assertEquals(response.status, HttpStatus.SC_OK)
    response.body.foreach(staging =>
      assertTrue(staging.files.isEmpty, "New staging area should be empty")
    )
  }

  @Test(description = "Upload a file to the staging area")
  def uploadFileTest(): Unit = withStaging { stagingUuid =>
    val file              = getTestFile(TEST_FILENAME)
    val encodedTargetPath = "folder/" + URLUtils.urlEncode(TEST_FILENAME, false)

    assertEquals(
      StagingApi.uploadFile(stagingUuid, encodedTargetPath, file, None).status,
      HttpStatus.SC_OK
    )

    val response = StagingApi.getStaging(stagingUuid)
    assertEquals(response.status, HttpStatus.SC_OK)
    val uploadedFile = findFileInStaging(response, s"folder/$TEST_FILENAME")
    assertTrue(uploadedFile.isDefined, "Uploaded file is missing")
    assertEquals(uploadedFile.get.size, file.length())
  }

  @Test(description = "Upload and automatically unzip a package")
  def uploadAndUnzipTest(): Unit = withStaging { stagingUuid =>
    assertEquals(
      StagingApi
        .uploadFile(stagingUuid, PACKAGE_FILE, getTestFile(PACKAGE_FILE), Some("unzipped"))
        .status,
      HttpStatus.SC_OK
    )

    val response = StagingApi.getStaging(stagingUuid)
    assertEquals(response.status, HttpStatus.SC_OK)
    assertTrue(findFileInStaging(response, "unzipped/ConditionsOfUse.html").isDefined)
  }

  @Test(description = "Delete a specific file from the staging area")
  def deleteFileTest(): Unit = withStaging { stagingUuid =>
    assertEquals(
      StagingApi.uploadFile(stagingUuid, "test.txt", getTestFile(AVATAR_FILE), None).status,
      HttpStatus.SC_OK
    )
    assertEquals(StagingApi.deleteFile(stagingUuid, "test.txt").status, HttpStatus.SC_NO_CONTENT)

    val response = StagingApi.getStaging(stagingUuid)
    assertEquals(response.status, HttpStatus.SC_OK)
    response.body.foreach(staging => assertTrue(staging.files.isEmpty))
  }

  @Test(description = "Check file metadata using HEAD request")
  def headFileTest(): Unit = withStaging { stagingUuid =>
    val file = getTestFile(AVATAR_FILE)
    assertEquals(
      StagingApi.uploadFile(stagingUuid, AVATAR_FILE, file, None).status,
      HttpStatus.SC_OK
    )

    val response = StagingApi.headFile(stagingUuid, AVATAR_FILE)
    assertEquals(response.status, HttpStatus.SC_OK)
    response.body.foreach(contentLength => assertEquals(contentLength.toLong, file.length()))
  }

  @Test(description = "Upload a file in multiple parts and stitch them together")
  def multipartUploadTest(): Unit = withStaging { stagingUuid =>
    val filename     = "multipart-dummy-file.txt"
    val expectedText = "First half of the file. Second half of the file."

    val startResponse = StagingApi.startMultipart(stagingUuid)
    assertEquals(startResponse.status, HttpStatus.SC_CREATED)
    assertTrue(startResponse.body.isDefined, "Missing uploadId")
    val uploadId = startResponse.body.get

    val p1Response =
      StagingApi.uploadMultipartText(stagingUuid, uploadId, 1, "First half of the file. ")
    assertEquals(p1Response.status, HttpStatus.SC_OK)
    assertTrue(p1Response.body.isDefined, "Missing part1")
    val part1 = p1Response.body.get

    val p2Response =
      StagingApi.uploadMultipartText(stagingUuid, uploadId, 2, "Second half of the file.")
    assertEquals(p2Response.status, HttpStatus.SC_OK)
    assertTrue(p2Response.body.isDefined, "Missing part2")
    val part2 = p2Response.body.get

    val compResponse = StagingApi.completeMultipart(stagingUuid, filename, uploadId, part1, part2)
    assertEquals(compResponse.status, HttpStatus.SC_OK)
    assertTrue(
      compResponse.body.exists(_.endsWith(filename)),
      s"Location header should point to '$filename'"
    )

    val getResponse = StagingApi.getStaging(stagingUuid)
    assertEquals(getResponse.status, HttpStatus.SC_OK)
    assertTrue(
      findFileInStaging(getResponse, filename).isDefined,
      "Multipart-uploaded file is missing"
    )

    val fileResponse = StagingApi.getFileContent(stagingUuid, filename)
    assertEquals(fileResponse.status, HttpStatus.SC_OK)
    fileResponse.body.foreach(content =>
      assertEquals(content, expectedText, "Assembled file content should match the uploaded parts")
    )
  }

  private def findFileInStaging(
      response: ApiResponse[Option[StagingArea]],
      exactFilePath: String
  ): Option[StagingFile] =
    response.body.flatMap(_.files.find(_.name == exactFilePath))

  private def getTestFile(filename: String): File =
    new File(AbstractPage.getPathFromUrl(Attachments.get(filename)))

  private object StagingApi {
    private val endpoint = getTestConfig.getInstitutionUrl + "api/staging/"

    private def getHeader(m: HttpMethod, name: String): Option[String] =
      Option(m.getResponseHeader(name)).map(_.getValue)

    private def execute[T](
        method: HttpMethod
    )(onSuccess: HttpMethod => T): ApiResponse[Option[T]] = {
      val status = makeClientRequest(method)
      val result = if (status >= 200 && status < 300) Some(onSuccess(method)) else None
      ApiResponse(status, result)
    }

    def createStaging(): ApiResponse[Option[String]] =
      execute(new PostMethod(endpoint))(m => getHeader(m, "x-eps-stagingid").get)

    def getStaging(stagingUuid: String): ApiResponse[Option[StagingArea]] =
      execute(new GetMethod(endpoint + stagingUuid)) { m =>
        scalaMapper.readValue(m.getResponseBodyAsStream, classOf[StagingArea])
      }

    def uploadFile(
        stagingUuid: String,
        targetPath: String,
        file: File,
        unzipTo: Option[String]
    ): ApiResponse[Option[Unit]] = {
      val method = new PutMethod(s"$endpoint$stagingUuid/$targetPath")
      method.setRequestEntity(new FileRequestEntity(file, "application/octet-stream"))
      unzipTo.foreach(u => method.setQueryString(Array(new NameValuePair("unzipto", u))))
      execute(method)(_ => ())
    }

    def deleteFile(stagingUuid: String, filePath: String): ApiResponse[Option[Unit]] =
      execute(new DeleteMethod(s"$endpoint$stagingUuid/$filePath"))(_ => ())

    def deleteStaging(stagingUuid: String): Int =
      makeClientRequest(new DeleteMethod(endpoint + stagingUuid))

    def headFile(stagingUuid: String, filePath: String): ApiResponse[Option[String]] =
      execute(new HeadMethod(s"$endpoint$stagingUuid/$filePath"))(m =>
        getHeader(m, "Content-Length").get
      )

    def getFileContent(stagingUuid: String, filePath: String): ApiResponse[Option[String]] =
      execute(new GetMethod(s"$endpoint$stagingUuid/$filePath"))(_.getResponseBodyAsString)

    def startMultipart(stagingUuid: String): ApiResponse[Option[String]] =
      execute(new PostMethod(s"$endpoint$stagingUuid/multipart")) { m =>
        mapper.readTree(m.getResponseBodyAsStream).get("uploadId").asText()
      }

    def uploadMultipartText(
        stagingUuid: String,
        uploadId: String,
        partNumber: Int,
        content: String
    ): ApiResponse[Option[UploadedPart]] = {
      val method = new PutMethod(s"$endpoint$stagingUuid/multipart/$uploadId/$partNumber")
      method.setRequestEntity(new StringRequestEntity(content, "text/plain", "UTF-8"))
      execute(method)(m => UploadedPart(partNumber, getHeader(m, "ETag").getOrElse("")))
    }

    def completeMultipart(
        stagingUuid: String,
        targetPath: String,
        uploadId: String,
        parts: UploadedPart*
    ): ApiResponse[Option[String]] = {
      val method = new PostMethod(s"$endpoint$stagingUuid/$targetPath/complete")
      method.setQueryString(Array(new NameValuePair("uploadId", uploadId)))

      val payload   = mapper.createObjectNode()
      val partArray = payload.putArray("parts")
      parts.foreach(p =>
        partArray
          .addObject()
          .put("partNumber", p.partNumber)
          .put("etag", p.etag.replace("\"", ""))
      )

      method.setRequestEntity(
        new StringRequestEntity(payload.toString, "application/json", "UTF-8")
      )
      execute(method)(m => getHeader(m, "Location").get)
    }
  }
}
