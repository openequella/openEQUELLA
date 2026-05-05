package io.github.openequella.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.tle.common.URLUtils
import com.tle.webtests.pageobject.AbstractPage
import com.tle.webtests.test.files.Attachments
import org.apache.commons.httpclient.methods._
import org.apache.commons.httpclient.{HttpMethod, HttpStatus, NameValuePair}
import org.testng.Assert._
import org.testng.annotations.{DataProvider, Test}

import java.io.File

case class StagingFile(
    name: String,
    size: Long,
    etag: String,
    contentType: String,
    links: Map[String, String]
)
case class StagingArea(
    uuid: String,
    files: List[StagingFile],
    directUrl: String,
    links: Map[String, String]
)
case class UploadedPart(partNumber: Int, etag: String)
case class ApiResponse[T](status: Int, body: T)

class StagingApiTest extends AbstractRestApiTest {

  private val TEST_FILENAME         = "Special characters - хцч test2.jpg"
  private val AVATAR_FILENAME       = "avatar.png"
  private val PACKAGE_FILENAME      = "package.zip"
  private val TEST_TXT_FILENAME     = "test.txt"
  private val ITEM_UUID             = "2f6e9be8-897d-45f1-98ea-7aa31b449c0e"
  private val HEADER_EPS_STAGING_ID = "x-eps-stagingid"

  private val scalaMapper: ObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)

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
    val file = getTestFile(AVATAR_FILENAME)
    logout()
    assertFalse(hasAuthenticatedSession, "Session should be guest after logout")

    assertEquals(StagingApi.createStaging().status, HttpStatus.SC_FORBIDDEN)
    assertEquals(StagingApi.getStaging(stagingUuid).status, HttpStatus.SC_FORBIDDEN)
    assertEquals(
      StagingApi.uploadFile(stagingUuid, "guest-upload.txt", file, None).status,
      HttpStatus.SC_FORBIDDEN
    )
    assertEquals(StagingApi.headFile(stagingUuid, AVATAR_FILENAME).status, HttpStatus.SC_FORBIDDEN)
    assertEquals(
      StagingApi.deleteFile(stagingUuid, AVATAR_FILENAME).status,
      HttpStatus.SC_FORBIDDEN
    )
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
        .uploadFile(stagingUuid, PACKAGE_FILENAME, getTestFile(PACKAGE_FILENAME), Some("unzipped"))
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
      StagingApi
        .uploadFile(stagingUuid, TEST_TXT_FILENAME, getTestFile(AVATAR_FILENAME), None)
        .status,
      HttpStatus.SC_OK
    )
    assertEquals(
      StagingApi.deleteFile(stagingUuid, TEST_TXT_FILENAME).status,
      HttpStatus.SC_NO_CONTENT
    )

    val response = StagingApi.getStaging(stagingUuid)
    assertEquals(response.status, HttpStatus.SC_OK)
    response.body.foreach(staging => assertTrue(staging.files.isEmpty))
  }

  @Test(description = "Check file metadata using HEAD request")
  def headFileTest(): Unit = withStaging { stagingUuid =>
    val file = getTestFile(AVATAR_FILENAME)
    assertEquals(
      StagingApi.uploadFile(stagingUuid, AVATAR_FILENAME, file, None).status,
      HttpStatus.SC_OK
    )

    val response = StagingApi.headFile(stagingUuid, AVATAR_FILENAME)
    assertEquals(response.status, HttpStatus.SC_OK)
    response.body.foreach(contentLength => assertEquals(contentLength.toLong, file.length()))
  }

  @Test(description = "Upload a file in multiple parts and stitch them together")
  def multipartUploadTest(): Unit = withStaging { stagingUuid =>
    val filename     = "multipart-dummy-file.txt"
    val expectedText = "First half of the file. Second half of the file."
    val textChunks   = expectedText.grouped(5).toList

    val startResponse = StagingApi.startMultipart(stagingUuid)
    assertEquals(startResponse.status, HttpStatus.SC_CREATED)
    assertTrue(startResponse.body.isDefined, "Missing uploadId")
    val uploadId = startResponse.body.get

    val uploadedParts = textChunks.zipWithIndex.map { case (chunk, index) =>
      val partNumber = index + 1
      val response   =
        StagingApi.uploadMultipartText(stagingUuid, uploadId, partNumber, chunk)

      assertEquals(response.status, HttpStatus.SC_OK)
      assertTrue(response.body.isDefined, s"Missing part $partNumber")
      response.body.get
    }

    val compResponse =
      StagingApi.completeMultipart(stagingUuid, filename, uploadId, uploadedParts: _*)
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

  @Test(description =
    "Completing a multipart upload with a mismatched ETag should return 400 Bad Request"
  )
  def multipartEtagMismatchTest(): Unit = withStaging { stagingUuid =>
    val incorrectEtag = "bad-etag"
    val chunkContent  = "Valid chunk content"
    val uploadId      = StagingApi.startMultipart(stagingUuid).body.get

    // Upload a valid chunk
    StagingApi.uploadMultipartText(stagingUuid, uploadId, 1, chunkContent)

    // Attempt to complete it using an incorrect ETag
    val badPart  = UploadedPart(1, incorrectEtag)
    val response = StagingApi.completeMultipart(stagingUuid, TEST_TXT_FILENAME, uploadId, badPart)

    assertEquals(response.status, HttpStatus.SC_BAD_REQUEST)
  }

  @DataProvider(name = "badCopyData")
  def badCopyData(): Array[Array[AnyRef]] = Array(
    Array(ITEM_UUID, null),
    Array(null, "1"),
    Array("", "1"),
    Array(ITEM_UUID, "0")
  )

  @Test(
    description = "Attempt to copy an item with missing or invalid parameters",
    dataProvider = "badCopyData"
  )
  def copyFromItemBadParamsTest(uuid: String, version: String): Unit = {
    val response = StagingApi.copyFromItem(uuid, version)
    assertEquals(response.status, HttpStatus.SC_BAD_REQUEST)
  }

  @Test(description = "Copy files from an existing item into a new staging area")
  def copyFromItemTest(): Unit = {
    val response = StagingApi.copyFromItem(ITEM_UUID, "1")
    assertEquals(response.status, HttpStatus.SC_CREATED)

    val stagingUuid = response.body.get
    val getResponse = StagingApi.getStaging(stagingUuid)
    assertEquals(getResponse.status, HttpStatus.SC_OK)

    getResponse.body.foreach { staging =>
      assertFalse(staging.files.isEmpty, "Files should have been copied from the item")
      assertTrue(
        staging.files.exists(_.name == AVATAR_FILENAME),
        s"$AVATAR_FILENAME should have been copied"
      )
    }

    // Cleanup staging area
    StagingApi.deleteStaging(stagingUuid)
  }

  @Test(description = "Low-privilege user is denied access to copy item files")
  def lowPrivilegeCopyFromItemTest(): Unit = {
    loginAsLowPrivilegeUser()
    val response = StagingApi.copyFromItem(ITEM_UUID, "1")
    assertEquals(response.status, HttpStatus.SC_FORBIDDEN)

    // Ensure to log in back as normal user
    login()
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

    private def stagingUrl(uuid: String, path: String = ""): String =
      if (path.isEmpty) s"$endpoint$uuid" else s"$endpoint$uuid/$path"

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
      execute(new PostMethod(endpoint))(m => getHeader(m, HEADER_EPS_STAGING_ID).get)

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
      val method = new PutMethod(stagingUrl(stagingUuid, targetPath))
      method.setRequestEntity(new FileRequestEntity(file, "application/octet-stream"))
      unzipTo.foreach(u => method.setQueryString(Array(new NameValuePair("unzipto", u))))
      execute(method)(_ => ())
    }

    def deleteFile(stagingUuid: String, filePath: String): ApiResponse[Option[Unit]] =
      execute(new DeleteMethod(stagingUrl(stagingUuid, filePath)))(_ => ())

    def deleteStaging(stagingUuid: String): Int =
      makeClientRequest(new DeleteMethod(stagingUrl(stagingUuid)))

    def headFile(stagingUuid: String, filePath: String): ApiResponse[Option[String]] =
      execute(new HeadMethod(stagingUrl(stagingUuid, filePath)))(m =>
        getHeader(m, "Content-Length").get
      )

    def getFileContent(stagingUuid: String, filePath: String): ApiResponse[Option[String]] =
      execute(new GetMethod(stagingUrl(stagingUuid, filePath)))(_.getResponseBodyAsString)

    def startMultipart(stagingUuid: String): ApiResponse[Option[String]] =
      execute(new PostMethod(stagingUrl(stagingUuid, "multipart"))) { m =>
        mapper.readTree(m.getResponseBodyAsStream).get("uploadId").asText()
      }

    def uploadMultipartText(
        stagingUuid: String,
        uploadId: String,
        partNumber: Int,
        content: String
    ): ApiResponse[Option[UploadedPart]] = {
      val method = new PutMethod(stagingUrl(stagingUuid, s"multipart/$uploadId/$partNumber"))
      method.setRequestEntity(new StringRequestEntity(content, "text/plain", "UTF-8"))
      execute(method)(m => UploadedPart(partNumber, getHeader(m, "ETag").getOrElse("")))
    }

    def completeMultipart(
        stagingUuid: String,
        targetPath: String,
        uploadId: String,
        parts: UploadedPart*
    ): ApiResponse[Option[String]] = {
      val method = new PostMethod(stagingUrl(stagingUuid, s"$targetPath/complete"))
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

    def copyFromItem(itemUuid: String, itemVersion: String): ApiResponse[Option[String]] = {
      val method = new PostMethod(s"${endpoint}copy")
      val params = Seq(
        Option(itemUuid).map(u => new NameValuePair("itemUuid", u)),
        Option(itemVersion).map(v => new NameValuePair("itemVersion", v))
      ).flatten.toArray

      method.setQueryString(params)
      execute(method)(m => getHeader(m, HEADER_EPS_STAGING_ID).get)
    }
  }
}
