package com.tle.webtests.pageobject.integration.blackboard;

import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.SkipException;
import com.tle.webtests.framework.TestConfig;
import com.tle.webtests.pageobject.AbstractPage;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class BlackboardUploadBuildingBlockPage
    extends AbstractPage<BlackboardUploadBuildingBlockPage> {
  private static String BB9_BLOCK_URL =
      "http://dev-builds.equella.com/job/EQUELLA-Build/lastSuccessfulBuild/artifact/Source/Integration/Blackboard/bb91/product/equella-bb91.war";

  @FindBy(id = "pluginFile_chooseLocalFile1")
  private WebElement fileUpload;

  @FindBy(name = "bottom_Submit")
  private WebElement submitButton;

  public BlackboardUploadBuildingBlockPage(PageContext context) {
    super(context, BlackboardPageUtils.pageTitleBy("Install Building Block"));
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getIntegUrl() + "webapps/blackboard/admin/install_plugin.jsp");
  }

  public BlackboardBuildingBlockListPage uploadLatest() {
    try {
      // The most amazing dogical hax to work around blackboards file
      // upload in chrome;
      JavascriptExecutor js = (JavascriptExecutor) driver;
      js.executeScript(
          "document.getElementById('top_submitButtonRow').innerHTML += \"<input type='file'"
              + " name='pluginFile_LocalFile0' id='pluginFile_chooseLocalFile1'"
              + " title='Browse'>\";");
      js.executeScript(
          "widget.InlineSingleLocalFilePicker.pickerMap['pluginFile'].required = false;");
      js.executeScript("document.getElementById('pluginFile_attachmentType').value = 'L';");
      js.executeScript(
          "document.getElementById('pluginFile_fileId').value = arguments[0].value;", fileUpload);
      js.executeScript(
          "var anElement = document.getElementById('pluginFile_chooseLocalFile');"
              + " anElement.parentNode.removeChild(anElement);");

      final File block;
      TestConfig testConfig = getContext().getTestConfig();
      String blockFile = testConfig.getProperty("blackboard.buildingblock.download.file", null);
      if (blockFile == null) {
        String blockUrl =
            testConfig.getProperty("blackboard.buildingblock.download.url", BB9_BLOCK_URL);
        block = downloadFile(new File(Files.createTempDir(), "equella-bb91.war"), blockUrl);
      } else {
        block = new File(blockFile);
      }
      fileUpload.sendKeys(block.getAbsolutePath());

      js.executeScript(
          "document.getElementById('pluginFile_fileId').value = arguments[0].value;", fileUpload);

      submitButton.click();
      waitForElement(By.name("bottom_Approve")).click();
      waitForElement(
          By.xpath(
              "//span[normalize-space(text())="
                  + AbstractPage.quoteXPath("Plugin enabled. Task complete.")
                  + "]"));
      waitForElement(By.name("bottom_Return")).click();

    } catch (Exception e) {
      e.printStackTrace();
      throw new SkipException(
          "An error occurred downloading the building block: " + e.getLocalizedMessage());
    }

    return new BlackboardBuildingBlockListPage(context).get();
  }

  private File downloadFile(File file, String url) throws Exception {
    TestConfig testConfig = getContext().getTestConfig();
    String username =
        testConfig.getProperty("blackboard.buildingblock.download.username", "autotest");
    String password =
        testConfig.getProperty(
            "blackboard.buildingblock.download.password", "cb7967ae9be763b30aeef126c775128a");

    HttpClient httpClient = new DefaultHttpClient();
    HttpGet httpGet = new HttpGet(url);
    httpGet.addHeader(
        BasicScheme.authenticate(
            new UsernamePasswordCredentials(username, password), "UTF-8", false));

    HttpResponse httpResponse = httpClient.execute(httpGet);
    HttpEntity responseEntity = httpResponse.getEntity();

    ReadableByteChannel rbc = Channels.newChannel(responseEntity.getContent());
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(file);
      fos.getChannel().transferFrom(rbc, 0, 1 << 24);
    } finally {
      Closeables.close(fos, true);
    }
    return file;
  }
}
