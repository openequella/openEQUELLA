package io.github.openequella.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.tle.common.URLUtils
import com.tle.webtests.pageobject.AbstractPage
import com.tle.webtests.test.files.Attachments
import org.apache.commons.httpclient.methods._
import org.apache.commons.httpclient.{HttpMethod, HttpStatus, NameValuePair}
import org.apache.hc.core5.http.HttpHeaders
import org.testng.Assert._
import org.testng.annotations.{DataProvider, Test}
import org.testng.asserts.SoftAssert

import java.io.File

case class StagingFile(
    name: String,
    size: Long,
    etag: String,
    contentType: String,
    links: Map[String, String],
    folder: Option[Boolean]
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
    val stagingUuid = assertResponseCreated(StagingApi.createStaging(), "Staging creation failed")
    assertTrue(stagingUuid.isDefined, "Missing staging UUID in response body")

    val uuid = stagingUuid.get
    try testCode(uuid)
    finally StagingApi.deleteStaging(uuid)
  }

  @Test(description = "Guest users should not be able to access staging endpoints")
  def guestAccessDeniedTest(): Unit = withStaging { stagingUuid =>
    val file = getTestFile(AVATAR_FILENAME)
    logout()
    assertFalse(hasAuthenticatedSession, "Session should be guest after logout")

    assertResponseForbidden(StagingApi.createStaging())
    assertResponseForbidden(StagingApi.getStaging(stagingUuid))
    assertResponseForbidden(StagingApi.uploadFile(stagingUuid, "guest-upload.txt", file))
    assertResponseForbidden(StagingApi.headFile(stagingUuid, AVATAR_FILENAME))
    assertResponseForbidden(StagingApi.deleteFile(stagingUuid, AVATAR_FILENAME))
    assertResponseForbidden(StagingApi.createFolder(stagingUuid, Some("guest-folder")))
    assertResponseForbidden(StagingApi.deleteStaging(stagingUuid))

    // Restore the authenticated session so subsequent tests don't run as guest
    login()
    assertTrue(hasAuthenticatedSession, "Authenticated session should be restored")
  }

  @Test(description = "Create an empty staging area")
  def createStagingTest(): Unit = withStaging { stagingUuid =>
    assertResponseOk(StagingApi.getStaging(stagingUuid)).foreach(staging =>
      assertTrue(staging.files.isEmpty, "New staging area should be empty")
    )
  }

  @Test(description = "Upload a file to the staging area")
  def uploadFileTest(): Unit = withStaging { stagingUuid =>
    val file              = getTestFile(TEST_FILENAME)
    val encodedTargetPath = "folder/" + URLUtils.urlEncode(TEST_FILENAME, false)

    assertResponseOk(StagingApi.uploadFile(stagingUuid, encodedTargetPath, file))

    val staging      = assertResponseOk(StagingApi.getStaging(stagingUuid))
    val uploadedFile = findFileInStaging(staging, s"folder/$TEST_FILENAME")
    assertTrue(uploadedFile.isDefined, "Uploaded file is missing")
    assertEquals(uploadedFile.get.size, file.length())
  }

  @Test(description = "Upload and automatically unzip a package")
  def uploadAndUnzipTest(): Unit = withStaging { stagingUuid =>
    assertResponseOk(
      StagingApi
        .uploadFile(stagingUuid, PACKAGE_FILENAME, getTestFile(PACKAGE_FILENAME), Some("unzipped"))
    )

    val staging = assertResponseOk(StagingApi.getStaging(stagingUuid))
    assertTrue(findFileInStaging(staging, "unzipped/ConditionsOfUse.html").isDefined)
  }

  @Test(description =
    "Verify that the If-None-Match header correctly prevents existing files from being overwritten"
  )
  def conditionalFileOverwriteTest(): Unit = withStaging { stagingUuid =>
    val file     = getTestFile(AVATAR_FILENAME)
    val filename = "conditional-test.png"

    assertResponseOk(
      StagingApi.uploadFile(stagingUuid, filename, file),
      "Initial file upload should succeed"
    )
    val currentEtag =
      findFileInStaging(assertResponseOk(StagingApi.getStaging(stagingUuid)), filename).get.etag

    val testingScenarios = List(
      ("Wildcard (*) rejection", Some("*"), HttpStatus.SC_PRECONDITION_FAILED),
      ("Exact matching ETag rejection", Some(currentEtag), HttpStatus.SC_PRECONDITION_FAILED),
      (
        "Unquoted matching ETag rejection",
        Some(currentEtag.replace("\"", "")),
        HttpStatus.SC_PRECONDITION_FAILED
      ),
      ("Empty string header allows overwrite", Some(""), HttpStatus.SC_OK),
      ("Mismatching ETag allows overwrite", Some("fake-different-etag"), HttpStatus.SC_OK),
      ("Omitted header allows overwrite", None, HttpStatus.SC_OK)
    )

    val softAssert = new SoftAssert()

    testingScenarios.foreach { case (description, headerValue, expectedStatus) =>
      val actualStatus =
        StagingApi.uploadFile(stagingUuid, filename, file, ifNoneMatch = headerValue).status
      softAssert.assertEquals(actualStatus, expectedStatus, s"Scenario failed: $description")
    }

    softAssert.assertAll()
  }

  /** Runs `testCode` against a staging area pre-populated with a nested file
    * (`scoped/inner/one.png`) and a top-level file (`toplevel.png`) — the fixture shared by the
    * listing query param tests.
    */
  private def withPopulatedStaging(testCode: String => Unit): Unit = withStaging { stagingUuid =>
    val file = getTestFile(AVATAR_FILENAME)
    assertResponseOk(StagingApi.uploadFile(stagingUuid, "scoped/inner/one.png", file))
    assertResponseOk(StagingApi.uploadFile(stagingUuid, "toplevel.png", file))
    testCode(stagingUuid)
  }

  @Test(description =
    "The default-params listing response is unchanged by the query param additions: files only," +
      " named by full path, with etags and no folder flags"
  )
  def defaultListingUnchangedTest(): Unit = withPopulatedStaging { stagingUuid =>
    assertResponseOk(StagingApi.getStaging(stagingUuid)).foreach { staging =>
      assertEquals(
        staging.files.map(_.name).sorted,
        List("scoped/inner/one.png", "toplevel.png"),
        "Default listing should contain only files, named by full path"
      )
      assertTrue(
        staging.files.forall(f => f.etag != null && f.folder.isEmpty),
        "Default listing should have etags and no folder flags"
      )
    }
  }

  @Test(description =
    "A listing scoped with the path param has names relative to the scoped folder and excludes" +
      " entries outside it"
  )
  def scopedListingTest(): Unit = withPopulatedStaging { stagingUuid =>
    // A trailing slash (as sent by the Admin Console) is tolerated
    List("scoped", "scoped/").foreach { scope =>
      assertResponseOk(StagingApi.getStaging(stagingUuid, path = Some(scope))).foreach(staging =>
        assertEquals(
          staging.files.map(_.name),
          List("inner/one.png"),
          s"Scoped listing (path=$scope) should be relative and exclude outside entries"
        )
      )
    }
  }

  @Test(description = "A listing with folders=true includes folder entries, flagged as folders")
  def folderListingTest(): Unit = withPopulatedStaging { stagingUuid =>
    assertResponseOk(StagingApi.getStaging(stagingUuid, folders = Some(true))).foreach { staging =>
      val (folderEntries, fileEntries) = staging.files.partition(_.folder.contains(true))
      assertEquals(
        folderEntries.map(_.name).sorted,
        List("scoped", "scoped/inner"),
        "Folder entries should be listed when folders=true"
      )
      assertEquals(
        fileEntries.map(_.name).sorted,
        List("scoped/inner/one.png", "toplevel.png"),
        "File entries should be unaffected by folders=true"
      )
    }
  }

  @Test(description = "A listing with checksums=false skips etag computation")
  def checksumSkipListingTest(): Unit = withPopulatedStaging { stagingUuid =>
    assertResponseOk(StagingApi.getStaging(stagingUuid, checksums = Some(false))).foreach(staging =>
      assertTrue(
        staging.files.forall(_.etag == null),
        "No etags should be computed when checksums=false"
      )
    )
  }

  @Test(description = "Scoped listing of a non-existent folder returns an empty listing")
  def scopedListingMissingPathTest(): Unit = withStaging { stagingUuid =>
    assertResponseOk(StagingApi.getStaging(stagingUuid, path = Some("does-not-exist"))).foreach(
      staging => assertTrue(staging.files.isEmpty, "Missing folder should list as empty, not error")
    )
  }

  @Test(description =
    "A created empty folder is visible in a folders=true listing but absent from the default" +
      " (files-only) listing"
  )
  def createFolderTest(): Unit = withStaging { stagingUuid =>
    assertResponseCreated(StagingApi.createFolder(stagingUuid, Some("empty-folder")))

    assertEquals(folderNames(stagingUuid), List("empty-folder"), "Created folder should be listed")

    assertResponseOk(StagingApi.getStaging(stagingUuid)).foreach(staging =>
      assertTrue(
        staging.files.isEmpty,
        "Default (files-only) listing should not include the empty folder"
      )
    )
  }

  @Test(description = "Creating a nested folder path creates the missing parent folders")
  def createNestedFolderTest(): Unit = withStaging { stagingUuid =>
    assertResponseCreated(StagingApi.createFolder(stagingUuid, Some("a/b/c")))

    assertEquals(
      folderNames(stagingUuid),
      List("a", "a/b", "a/b/c"),
      "Parent folders should be created alongside the leaf folder"
    )
  }

  @Test(description = "Creating a folder that already exists is idempotent")
  def createFolderIdempotentTest(): Unit = withStaging { stagingUuid =>
    assertResponseCreated(StagingApi.createFolder(stagingUuid, Some("dupe")))
    assertResponseCreated(
      StagingApi.createFolder(stagingUuid, Some("dupe")),
      "Re-creating an existing folder should still succeed"
    )
  }

  @Test(description = "Creating a folder without a path is a bad request")
  def createFolderNoPathTest(): Unit = withStaging { stagingUuid =>
    assertResponseBadRequest(StagingApi.createFolder(stagingUuid, path = None))
  }

  @Test(description = "Delete a specific file from the staging area")
  def deleteFileTest(): Unit = withStaging { stagingUuid =>
    assertResponseOk(
      StagingApi.uploadFile(stagingUuid, TEST_TXT_FILENAME, getTestFile(AVATAR_FILENAME))
    )
    assertEquals(
      StagingApi.deleteFile(stagingUuid, TEST_TXT_FILENAME).status,
      HttpStatus.SC_NO_CONTENT
    )

    assertResponseOk(StagingApi.getStaging(stagingUuid)).foreach(staging =>
      assertTrue(staging.files.isEmpty)
    )
  }

  @Test(description = "Check file metadata using HEAD request")
  def headFileTest(): Unit = withStaging { stagingUuid =>
    val file = getTestFile(AVATAR_FILENAME)
    assertResponseOk(StagingApi.uploadFile(stagingUuid, AVATAR_FILENAME, file))

    assertResponseOk(StagingApi.headFile(stagingUuid, AVATAR_FILENAME)).foreach(contentLength =>
      assertEquals(contentLength.toLong, file.length())
    )
  }

  @Test(description = "Upload a file in multiple parts and stitch them together")
  def multipartUploadTest(): Unit = withStaging { stagingUuid =>
    val filename     = "multipart-dummy-file.txt"
    val expectedText = "First half of the file. Second half of the file."
    val textChunks   = expectedText.grouped(5).toList

    val startBody = assertResponseCreated(StagingApi.startMultipart(stagingUuid))
    assertTrue(startBody.isDefined, "Missing uploadId")
    val uploadId = startBody.get

    val uploadedParts = textChunks.zipWithIndex.map { case (chunk, index) =>
      val partNumber = index + 1
      val part       =
        assertResponseOk(StagingApi.uploadMultipartText(stagingUuid, uploadId, partNumber, chunk))

      assertTrue(part.isDefined, s"Missing part $partNumber")
      part.get
    }

    val location =
      assertResponseOk(
        StagingApi.completeMultipart(stagingUuid, filename, uploadId, uploadedParts: _*)
      )
    assertTrue(
      location.exists(_.endsWith(filename)),
      s"Location header should point to '$filename'"
    )

    assertTrue(
      findFileInStaging(assertResponseOk(StagingApi.getStaging(stagingUuid)), filename).isDefined,
      "Multipart-uploaded file is missing"
    )

    assertResponseOk(StagingApi.getFileContent(stagingUuid, filename)).foreach(content =>
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
    val badPart = UploadedPart(1, incorrectEtag)
    assertResponseBadRequest(
      StagingApi.completeMultipart(stagingUuid, TEST_TXT_FILENAME, uploadId, badPart)
    )
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
  def copyFromItemBadParamsTest(uuid: String, version: String): Unit =
    assertResponseBadRequest(StagingApi.copyFromItem(uuid, version))

  @Test(description = "Copy files from an existing item into a new staging area")
  def copyFromItemTest(): Unit = {
    val stagingUuid = assertResponseCreated(StagingApi.copyFromItem(ITEM_UUID, "1")).get
    assertResponseOk(StagingApi.getStaging(stagingUuid)).foreach { staging =>
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
    assertResponseForbidden(StagingApi.copyFromItem(ITEM_UUID, "1"))

    // Ensure to log in back as normal user
    login()
  }

  /** Asserts the response completed with 200 OK and returns its body for further assertions. */
  private def assertResponseOk[B](response: ApiResponse[B], message: String = null): B = {
    assertEquals(response.status, HttpStatus.SC_OK, message)
    response.body
  }

  /** Asserts the response completed with 201 Created and returns its body for further assertions.
    */
  private def assertResponseCreated[B](response: ApiResponse[B], message: String = null): B = {
    assertEquals(response.status, HttpStatus.SC_CREATED, message)
    response.body
  }

  /** Asserts the request was denied with 403 Forbidden. */
  private def assertResponseForbidden(response: ApiResponse[_], message: String = null): Unit =
    assertEquals(response.status, HttpStatus.SC_FORBIDDEN, message)

  /** Asserts the request was rejected with 400 Bad Request. */
  private def assertResponseBadRequest(response: ApiResponse[_], message: String = null): Unit =
    assertEquals(response.status, HttpStatus.SC_BAD_REQUEST, message)

  private def findFileInStaging(
      staging: Option[StagingArea],
      exactFilePath: String
  ): Option[StagingFile] =
    staging.flatMap(_.files.find(_.name == exactFilePath))

  /** The sorted names of the folder entries in a `folders=true` listing of the staging area. */
  private def folderNames(stagingUuid: String): List[String] =
    assertResponseOk(StagingApi.getStaging(stagingUuid, folders = Some(true))).toList
      .flatMap(_.files)
      .filter(_.folder.contains(true))
      .map(_.name)
      .sorted

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

    def getStaging(
        stagingUuid: String,
        path: Option[String] = None,
        folders: Option[Boolean] = None,
        checksums: Option[Boolean] = None
    ): ApiResponse[Option[StagingArea]] = {
      val method = new GetMethod(endpoint + stagingUuid)
      val params = Seq(
        path.map(p => new NameValuePair("path", p)),
        folders.map(f => new NameValuePair("folders", f.toString)),
        checksums.map(c => new NameValuePair("checksums", c.toString))
      ).flatten.toArray
      if (params.nonEmpty) method.setQueryString(params)

      execute(method) { m =>
        scalaMapper.readValue(m.getResponseBodyAsStream, classOf[StagingArea])
      }
    }

    def uploadFile(
        stagingUuid: String,
        targetPath: String,
        file: File,
        unzipTo: Option[String] = None,
        ifNoneMatch: Option[String] = None
    ): ApiResponse[Option[Unit]] = {
      val method = new PutMethod(stagingUrl(stagingUuid, targetPath))
      method.setRequestEntity(new FileRequestEntity(file, "application/octet-stream"))

      unzipTo.foreach(u => method.setQueryString(Array(new NameValuePair("unzipto", u))))
      ifNoneMatch.foreach(headerVal =>
        method.setRequestHeader(HttpHeaders.IF_NONE_MATCH, headerVal)
      )

      execute(method)(_ => ())
    }

    def deleteFile(stagingUuid: String, filePath: String): ApiResponse[Option[Unit]] =
      execute(new DeleteMethod(stagingUrl(stagingUuid, filePath)))(_ => ())

    def createFolder(stagingUuid: String, path: Option[String]): ApiResponse[Option[Unit]] = {
      val method = new PostMethod(stagingUrl(stagingUuid, "folder"))
      path.foreach(p => method.setQueryString(Array(new NameValuePair("path", p))))
      execute(method)(_ => ())
    }

    def deleteStaging(stagingUuid: String): ApiResponse[Option[Unit]] =
      execute(new DeleteMethod(stagingUrl(stagingUuid)))(_ => ())

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
