package com.tle.web.filemanager.applet.backend;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dytech.edge.exceptions.BannedFileException;
import com.tle.common.applet.client.ClientProxyFactory;
import com.tle.web.appletcommon.io.InputStreamChucker;
import com.tle.web.filemanager.common.FileInfo;
import com.tle.web.filemanager.common.ServerBackend;

public class ServerBackendConnector extends AbstractRemoteBackendImpl
{
	private static final Logger LOGGER = Logger.getLogger(ServerBackendConnector.class.getName());

	private final ServerBackend serverBackend;
	private final String wizardId;

	@SuppressWarnings("nls")
	public ServerBackendConnector(URL serverUrl, String wizardId) throws MalformedURLException
	{
		this.wizardId = wizardId;
		this.serverBackend = ClientProxyFactory.createProxy(ServerBackend.class, new URL(serverUrl, "invoker/"
			+ ServerBackend.class.getName() + ".service"));
	}

	@Override
	public List<FileInfo> listFiles(String directory)
	{
		return serverBackend.listFiles(wizardId, directory);
	}

	@Override
	@SuppressWarnings("nls")
	public InputStream readFile(String filename)
	{
		try
		{
			URL url = serverBackend.getDownloadUrl(wizardId, filename);
			return url.openConnection().getInputStream();
		}
		catch( Exception ex )
		{
			throw logException("Error downloading URL for download: " + filename, ex);
		}
	}

	@Override
	public void writeFile(final String filename, long length, InputStream content)
	{
		InputStreamChucker.chunk(length, content, new InputStreamChucker.ChunkHandler()
		{
			@Override
			public void handleChunk(byte[] bytes, boolean append) throws IOException, BannedFileException
			{
				serverBackend.write(wizardId, filename, append, bytes);
			}
		});
	}

	private RuntimeException logException(String message, Exception ex)
	{
		LOGGER.log(Level.WARNING, message, ex);
		throw new RuntimeException(message, ex);
	}

	@Override
	public void delete(FileInfo info)
	{
		serverBackend.delete(wizardId, info.getFullPath());
	}

	@Override
	public boolean move(FileInfo sourceFile, FileInfo destinationFile)
	{
		return serverBackend.renameFile(wizardId, sourceFile.getFullPath(), destinationFile.getFullPath());
	}

	@Override
	public void copy(FileInfo sourceFile, FileInfo destFile)
	{
		serverBackend.copy(wizardId, sourceFile.getFullPath(), destFile.getFullPath());
	}

	@Override
	public void toggleMarkAsResource(FileInfo info)
	{
		serverBackend.markAsResource(wizardId, !info.isMarkAsAttachment(), info.getFullPath());
	}

	@Override
	public void newFolder(FileInfo fileInfo)
	{
		serverBackend.newFolder(wizardId, fileInfo.getFullPath());
	}

	@Override
	public void extractArchive(FileInfo info)
	{
		serverBackend.extractArchive(wizardId, info.getFullPath(), info.getPath());
	}
}
