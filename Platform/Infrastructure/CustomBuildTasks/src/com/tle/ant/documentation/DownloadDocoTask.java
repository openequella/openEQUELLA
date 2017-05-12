package com.tle.ant.documentation;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class DownloadDocoTask extends Task
{
	private String url = "http://maestro.equella.com";
	private File cachePath;
	private String oauthClient = "downloaddocotask";
	private String oauthSecret = "660eb7db-be92-45eb-be27-dc88703b0fa7";
	private String query;
	private String filename;
	private String collectionUuid = "b4c2ddf0-b647-db8b-1e0d-d899d851724a";
	private String proxyHost;
	private int proxyPort;
	private String equellaVersion;
	private String out;
	private boolean errorOnNotFound = false;

	private final Gson gson = new Gson();
	private String token;

	public static void main(String[] args)
	{
		DownloadDocoTask task = new DownloadDocoTask();
		//		task.setUrl("http://origliasso/sotd/");
		//		task.setOauthClient("docotask");
		//		task.setOauthSecret("2e640456-1433-4943-aa44-d54a32123f13");
		//		task.setQuery("The Veronicas");
		//		task.setFilename("01-the-veronicas-082907.jpg");
		//		task.setCollectionUuid("3d4ed0a0-def9-b45d-000c-ef9adc628f04");
		//		task.setEquellaVersion("6.2-Alpha");
		//		task.setProxyHost("origliasso");
		//		task.setProxyPort(8888);

		// MAESTRO
		// task.setOut("out");
		task.setQuery("\"EQUELLA 6.4 C# SOAP Tutorial\"");
		task.setFilename("EQUELLA 6.4 C# SOAP Tutorial.pdf");
		task.setEquellaVersion("6.4");
		// task.setProxyHost("origliasso");
		// task.setProxyPort(8888);

		task.execute();
	}

	@Override
	public void execute() throws BuildException
	{
		try
		{
			log("Checking documentation for " + getFilename());

			final URL baseUrl = new URL(getUrl());
			final Map<String, FileInfo> cacheDb = readCache();

			token = obtainToken(baseUrl);

			final List<NameValuePair> qs = Lists.newArrayList();
			qs.add(new BasicNameValuePair("info", "basic,attachment"));
			if( getCollectionUuid() != null )
			{
				qs.add(new BasicNameValuePair("collections", getCollectionUuid()));
			}
			if( getQuery() != null )
			{
				qs.add(new BasicNameValuePair("q", getQuery()));
			}
			final URL searchUrl = new URL(baseUrl, "api/search?" + URLEncodedUtils.format(qs, "UTF-8"));
			log("Performing search:", Project.MSG_DEBUG);
			log(searchUrl.toString(), Project.MSG_DEBUG);

			final HttpGet search = new HttpGet(searchUrl.toURI());
			search.addHeader("X-Authorization", "access_token=" + token);

			final HttpResponse searchResponse = createClient().execute(search);
			try( InputStream in = searchResponse.getEntity().getContent();
				InputStreamReader rdr = new InputStreamReader(in) )
			{
				final SearchBean res = gson.fromJson(rdr, SearchBean.class);
				if( res.getLength() == 0 )
				{
					log("No results for " + getQuery() + ". Not downloading anything.", Project.MSG_DEBUG);
					notFound();
					return;
				}

				log("Results found: " + res.getAvailable(), Project.MSG_DEBUG);

				int ctr = 0;
				final List<ItemBean> results = res.getResults();
				for( ItemBean itemBean : results )
				{
					ctr++;
					log("Looking at result " + ctr, Project.MSG_DEBUG);

					final String itemUuid = itemBean.getUuid();
					final int itemVersion = itemBean.getVersion();

					log("Comparing attachments of " + itemUuid + "/" + itemVersion, Project.MSG_DEBUG);
					final List<AttachmentBean> attachments = itemBean.getAttachments();
					if( attachments != null )
					{
						for( AttachmentBean attachmentBean : attachments )
						{
							final String attachmentFilename = attachmentBean.getFilename();
							final String attachmentUuid = attachmentBean.getUuid();
							if( attachmentFilename.equalsIgnoreCase(getFilename()) )
							{
								log("Found match for filename " + getFilename(), Project.MSG_DEBUG);

								// download it if changed
								boolean download = false;
								FileInfo cacheEntry = cacheDb.get(attachmentFilename);
								if( cacheEntry != null )
								{
									final String cachedMd5 = cacheEntry.getMd5();
									String attachmentMd5 = attachmentBean.getMd5();
									// Seems md5 can't be relied upon.
									if( attachmentMd5 == null || attachmentMd5.trim().length() == 0 )
									{
										// Need to do a HEAD req and check
										// Last-Modified
										String lastModified = lastModified(baseUrl, itemUuid, itemVersion,
											attachmentUuid, attachmentFilename);
										if( !cacheEntry.getLastUpdate().equals(lastModified) )
										{
											log("Found in cache, but Last-Modified not matching.");
											download = true;
										}
										else
										{
											// Make sure the file actually
											// exists
											// too!
											File cacheFile = new File(getCachePath(), getFilename());
											if( !cacheFile.exists() )
											{
												log("Found in cache, but file is missing.");
												download = true;
											}
											else
											{
												log("Found in cache and up to date.");
											}
										}
									}
									else
									{
										if( !cachedMd5.equals(attachmentMd5) )
										{
											log("Found in cache, but MD5 not matching.");
											download = true;
										}
										else
										{
											// Make sure the file actually
											// exists
											// too!
											File cacheFile = new File(getCachePath(), getFilename());
											if( !cacheFile.exists() )
											{
												log("Found in cache, but file is missing.");
												download = true;
											}
											else
											{
												log("Found in cache and up to date.");
											}
										}
									}
								}
								else
								{
									log("Not found in cache.");
									cacheEntry = new FileInfo();
									download = true;
								}
								if( download )
								{
									log("Downloading from server.");
									// download it
									cacheEntry = download(baseUrl, itemUuid, itemVersion, attachmentUuid,
										attachmentFilename);
									cacheDb.put(attachmentFilename, cacheEntry);
									writeCache(cacheDb);
								}

								final File absPath = new File(getCachePath(), cacheEntry.getFilename());
								log("Setting out variable: " + out + "=" + absPath.getAbsolutePath(), Project.MSG_DEBUG);

								Project project = getProject();
								if( project != null )
								{
									project.setNewProperty(out, absPath.getAbsolutePath());
								}
								return;
							}
						}
					}
				}
			}
			notFound();
		}
		catch( Exception e )
		{
			throw new BuildException(e);
		}
	}

	private void notFound() throws BuildException
	{
		if( isErrorOnNotFound() )
		{
			throw new BuildException("Not found on server: " + getFilename());
		}
		else
		{
			log("WARNING: Not found on server: " + getFilename(), Project.MSG_WARN);
		}
	}

	private String obtainToken(URL baseUrl) throws Exception
	{
		URL tokenUrl = new URL(baseUrl,
			"oauth/access_token?grant_type=client_credentials&redirect_uri=default&client_id=" + getOauthClient()
				+ "&client_secret=" + getOauthSecret());

		log("Obtaining token from " + baseUrl.toString(), Project.MSG_DEBUG);

		DefaultHttpClient client = createClient();
		HttpGet method = new HttpGet(tokenUrl.toURI());

		final HttpResponse response = client.execute(method);

		try( InputStream in = response.getEntity().getContent(); InputStreamReader rdr = new InputStreamReader(in) )
		{
			TokenResponse res = gson.fromJson(rdr, TokenResponse.class);
			return res.getAccess_token();
		}
	}

	private DefaultHttpClient createClient()
	{
		final DefaultHttpClient client = new DefaultHttpClient();
		// Allows a slightly lenient cookie acceptance
		client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
		// Allows follow of redirects on POST
		client.setRedirectStrategy(new LaxRedirectStrategy());

		if( getProxyHost() != null )
		{
			final HttpHost proxyHost = new HttpHost(getProxyHost(), getProxyPort());
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHost);
		}
		return client;
	}

	@SuppressWarnings("unchecked")
	private Map<String, FileInfo> readCache()
	{
		log("Reading cache db", Project.MSG_DEBUG);
		File cacheDb = new File(getCachePath(), "cachedb");
		if( cacheDb.exists() )
		{
			try( FileInputStream fin = new FileInputStream(cacheDb); ObjectInputStream ois = new ObjectInputStream(fin) )
			{
				return (Map<String, FileInfo>) ois.readObject();
			}
			catch( Exception ex )
			{
				throw new BuildException("Could not read the cache", ex);
			}
		}
		return Maps.newHashMap();
	}

	private void writeCache(Map<String, FileInfo> regMap)
	{
		log("Writing cache db", Project.MSG_DEBUG);
		File cacheDb = new File(getCachePath(), "cachedb");
		try( FileOutputStream fout = new FileOutputStream(cacheDb);
			ObjectOutputStream oos = new ObjectOutputStream(fout) )
		{
			// FIXME: file locking
			oos.writeObject(regMap);
		}
		catch( Exception ex )
		{
			throw new BuildException("Could not write the cache", ex);
		}
	}

	private URL generateDownloadUrl(URL baseUrl, String uuid, int version, String attachmentUuid,
		String attachmentFilename) throws Exception
	{
		return new URL(baseUrl, "file/" + uuid + "/" + version + "/"
			+ URLEncoder.encode(attachmentFilename, "UTF-8").replaceAll("\\+", "%20"));
	}

	private String lastModified(URL baseUrl, String uuid, int version, String attachmentUuid, String attachmentFilename)
		throws Exception
	{
		final URL url = generateDownloadUrl(baseUrl, uuid, version, attachmentUuid, attachmentFilename);
		log("HEAD URL: " + url.toString(), Project.MSG_DEBUG);

		final HttpHead head = new HttpHead(url.toURI());
		head.addHeader("X-Authorization", "access_token=" + token);

		final HttpResponse response = createClient().execute(head);
		final String lm = response.getFirstHeader("Last-Modified").getValue();
		log("Last-Modified: " + lm, Project.MSG_DEBUG);
		return lm;
	}

	private FileInfo download(URL baseUrl, String uuid, int version, String attachmentUuid, String attachmentFilename)
		throws Exception
	{
		final URL url = generateDownloadUrl(baseUrl, uuid, version, attachmentUuid, attachmentFilename);
		log("Download URL: " + url.toString(), Project.MSG_DEBUG);

		final HttpGet get = new HttpGet(url.toURI());
		get.addHeader("X-Authorization", "access_token=" + token);

		final File cachedFile = new File(getCachePath(), attachmentFilename);
		final HttpResponse response = createClient().execute(get);

		final MessageDigest md5 = MessageDigest.getInstance("md5");
		try( InputStream in = response.getEntity().getContent();
			OutputStream out = new BufferedOutputStream(new FileOutputStream(cachedFile)) )
		{
			final int bufferSize = 4096;
			final byte buffer[] = new byte[bufferSize];
			for( int bytes = in.read(buffer, 0, buffer.length); bytes != -1; bytes = in.read(buffer, 0, buffer.length) )
			{
				out.write(buffer, 0, bytes);
				md5.update(buffer, 0, bytes);
			}
			out.flush();

			FileInfo fileInfo = new FileInfo();
			fileInfo.setMd5(Md5.stringify(md5.digest()));
			fileInfo.setUuid(attachmentUuid);
			fileInfo.setFilename(attachmentFilename);
			fileInfo.setLastUpdate(response.getFirstHeader("Last-Modified").getValue());
			return fileInfo;
		}
	}

	public synchronized File getCachePath()
	{
		if( cachePath == null )
		{
			final String home = System.getProperty("user.home");
			final File cacheRoot = new File(home, ".equella_build_doc_cache");
			if( !cacheRoot.exists() )
			{
				cacheRoot.mkdir();
			}

			cachePath = new File(cacheRoot, getDiscriminator());
			if( !cachePath.exists() )
			{
				cachePath.mkdir();
			}
			log("Cache path is " + cachePath.getAbsolutePath(), Project.MSG_DEBUG);
		}
		return cachePath;
	}

	private String getDiscriminator()
	{
		final String version = getEquellaVersion();
		if( version == null )
		{
			return "unversioned";
		}
		return version;
	}

	public void setCachePath(File cachePath)
	{
		this.cachePath = cachePath;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getOauthClient()
	{
		return oauthClient;
	}

	public void setOauthClient(String oauthClient)
	{
		this.oauthClient = oauthClient;
	}

	public String getOauthSecret()
	{
		return oauthSecret;
	}

	public void setOauthSecret(String oauthSecret)
	{
		this.oauthSecret = oauthSecret;
	}

	public String getQuery()
	{
		return query;
	}

	public void setQuery(String query)
	{
		this.query = query;
	}

	public String getFilename()
	{
		return filename;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public String getCollectionUuid()
	{
		return collectionUuid;
	}

	public void setCollectionUuid(String collectionUuid)
	{
		this.collectionUuid = collectionUuid;
	}

	public String getEquellaVersion()
	{
		return equellaVersion;
	}

	public void setEquellaVersion(String equellaVersion)
	{
		this.equellaVersion = equellaVersion;
	}

	public String getProxyHost()
	{
		return proxyHost;
	}

	public void setProxyHost(String proxyHost)
	{
		this.proxyHost = proxyHost;
	}

	public int getProxyPort()
	{
		return proxyPort;
	}

	public void setProxyPort(int proxyPort)
	{
		this.proxyPort = proxyPort;
	}

	public String getOut()
	{
		return out;
	}

	public void setOut(String out)
	{
		this.out = out;
	}

	public boolean isErrorOnNotFound()
	{
		return errorOnNotFound;
	}

	public void setErrorOnNotFound(boolean errorOnNotFound)
	{
		this.errorOnNotFound = errorOnNotFound;
	}

	public static class FileInfo implements Serializable
	{
		private String md5;
		private String uuid;
		private String filename;
		private String lastUpdate;

		public String getMd5()
		{
			return md5;
		}

		public void setMd5(String md5)
		{
			this.md5 = md5;
		}

		public String getUuid()
		{
			return uuid;
		}

		public void setUuid(String uuid)
		{
			this.uuid = uuid;
		}

		public String getFilename()
		{
			return filename;
		}

		public void setFilename(String filename)
		{
			this.filename = filename;
		}

		public String getLastUpdate()
		{
			return lastUpdate;
		}

		public void setLastUpdate(String lastUpdate)
		{
			this.lastUpdate = lastUpdate;
		}
	}

	// Gson sucks
	public static class AttachmentBean
	{
		private String uuid;
		private String description;
		private String viewer;
		private boolean preview;
		private String filename;
		private long size;
		private String md5;
		private String parentZip;
		private String thumbFilename;
		private boolean conversion;

		public String getUuid()
		{
			return uuid;
		}

		public void setUuid(String uuid)
		{
			this.uuid = uuid;
		}

		public String getDescription()
		{
			return description;
		}

		public void setDescription(String description)
		{
			this.description = description;
		}

		public String getViewer()
		{
			return viewer;
		}

		public void setViewer(String viewer)
		{
			this.viewer = viewer;
		}

		public boolean isPreview()
		{
			return preview;
		}

		public void setPreview(boolean preview)
		{
			this.preview = preview;
		}

		public String getFilename()
		{
			return filename;
		}

		public void setFilename(String filename)
		{
			this.filename = filename;
		}

		public long getSize()
		{
			return size;
		}

		public void setSize(long size)
		{
			this.size = size;
		}

		public String getMd5()
		{
			return md5;
		}

		public void setMd5(String md5)
		{
			this.md5 = md5;
		}

		public boolean isConversion()
		{
			return conversion;
		}

		public void setConversion(boolean conversion)
		{
			this.conversion = conversion;
		}

		public String getThumbFilename()
		{
			return thumbFilename;
		}

		public void setThumbFilename(String thumbFilename)
		{
			this.thumbFilename = thumbFilename;
		}

		public String getParentZip()
		{
			return parentZip;
		}

		public void setParentZip(String parentZip)
		{
			this.parentZip = parentZip;
		}
	}

	// Gson sucks
	public static class ItemBean
	{
		private String uuid;
		private int version;
		private String name;
		private List<AttachmentBean> attachments;

		public String getUuid()
		{
			return uuid;
		}

		public void setUuid(String uuid)
		{
			this.uuid = uuid;
		}

		public int getVersion()
		{
			return version;
		}

		public void setVersion(int version)
		{
			this.version = version;
		}

		public List<AttachmentBean> getAttachments()
		{
			return attachments;
		}

		public void setAttachments(List<AttachmentBean> attachments)
		{
			this.attachments = attachments;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}
	}

	// Gson sucks
	public static class SearchBean
	{
		private int start;
		private int length;
		private int available;
		private List<ItemBean> results;

		public int getStart()
		{
			return start;
		}

		public void setStart(int start)
		{
			this.start = start;
		}

		public int getLength()
		{
			return length;
		}

		public void setLength(int length)
		{
			this.length = length;
		}

		public int getAvailable()
		{
			return available;
		}

		public void setAvailable(int available)
		{
			this.available = available;
		}

		public List<ItemBean> getResults()
		{
			return results;
		}

		public void setResults(List<ItemBean> results)
		{
			this.results = results;
		}
	}

	// Gson sucks
	public static class TokenResponse
	{
		private String access_token;
		private String refresh_token;
		private String token_type;
		private long expires_in;
		private String scope;
		private String state;

		public String getAccess_token()
		{
			return access_token;
		}

		public void setAccess_token(String access_token)
		{
			this.access_token = access_token;
		}

		public String getRefresh_token()
		{
			return refresh_token;
		}

		public void setRefresh_token(String refresh_token)
		{
			this.refresh_token = refresh_token;
		}

		public String getToken_type()
		{
			return token_type;
		}

		public void setToken_type(String token_type)
		{
			this.token_type = token_type;
		}

		public long getExpires_in()
		{
			return expires_in;
		}

		public void setExpires_in(long expires_in)
		{
			this.expires_in = expires_in;
		}

		public String getScope()
		{
			return scope;
		}

		public void setScope(String scope)
		{
			this.scope = scope;
		}

		public String getState()
		{
			return state;
		}

		public void setState(String state)
		{
			this.state = state;
		}
	}
}
