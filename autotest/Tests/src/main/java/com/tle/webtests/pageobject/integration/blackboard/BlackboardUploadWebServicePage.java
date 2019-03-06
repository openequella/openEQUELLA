package com.tle.webtests.pageobject.integration.blackboard;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import com.tle.webtests.framework.SkipException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class BlackboardUploadWebServicePage extends AbstractPage<BlackboardUploadWebServicePage>
{
	private static String BB9_SERVICE_URL = "http://dev-builds.equella.com/job/EQUELLA-Build/lastSuccessfulBuild/artifact/Source/Integration/Blackboard/bb91/product/webservice.jar";

	@FindBy(id = "wsFile")
	private WebElement fileUpload;
	@FindBy(id = "overwrite")
	private WebElement overwriteCheck;
	@FindBy(name = "bottom_Submit")
	private WebElement submitButton;

	public BlackboardUploadWebServicePage(PageContext context)
	{
		super(context, BlackboardPageUtils.pageTitleBy("Install Web Service"));
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getIntegUrl() + "webapps/ws/wsadmin/wsinstall");
	}

	public BlackboardWebServicesListPage uploadLatest()
	{
		try
		{
			File block = downloadFile(new File(Files.createTempDir(), "webservice.jar"), BB9_SERVICE_URL);
			fileUpload.sendKeys(block.getAbsolutePath());
			overwriteCheck.click();

			submitButton.click();
		}
		catch( Exception e )
		{
			e.printStackTrace();
			throw new SkipException("An error occurred downloading the webservice " + e.getLocalizedMessage());
		}

		waiter.until(new ExpectedCondition<Boolean>()
		{
			@Override
			public Boolean apply(WebDriver d)
			{
				BlackboardWebServicesListPage listPage = new BlackboardWebServicesListPage(context).get();
				if( !listPage.isEquellaInstalled() )
				{
					listPage.refresh();
					return Boolean.FALSE;
				}
				else
				{
					return Boolean.TRUE;
				}
			}
		});

		return new BlackboardWebServicesListPage(context).get();
	}

	private File downloadFile(File file, String url) throws Exception
	{

		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		httpGet.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials("autotest",
			"cb7967ae9be763b30aeef126c775128a"), "UTF-8", false));

		HttpResponse httpResponse = httpClient.execute(httpGet);
		HttpEntity responseEntity = httpResponse.getEntity();

		ReadableByteChannel rbc = Channels.newChannel(responseEntity.getContent());
		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream(file);
			fos.getChannel().transferFrom(rbc, 0, 1 << 24);
		}
		finally
		{
			Closeables.close(fos, true);
		}
		return file;
	}

}
