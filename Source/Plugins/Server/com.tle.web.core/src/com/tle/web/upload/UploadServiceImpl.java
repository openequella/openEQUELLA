package com.tle.web.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

import org.apache.commons.fileupload.FileItemStream;

import com.google.common.io.Closeables;
import com.tle.core.events.UserSessionLogoutEvent;
import com.tle.core.events.listeners.UserSessionLogoutListener;
import com.tle.core.guice.Bind;

/**
 * @author Aaron
 */
@Bind(UploadService.class)
@Singleton
public class UploadServiceImpl implements UploadService, UserSessionLogoutListener
{
	private final Map<String, Map<String, Upload>> uploads = new ConcurrentHashMap<String, Map<String, Upload>>();

	@Override
	public Upload addUpload(String sessionId, FileItemStream stream)
	{
		synchronized( uploads )
		{
			Map<String, Upload> userUploads = uploads.get(sessionId);
			if( userUploads == null )
			{
				userUploads = new ConcurrentHashMap<String, Upload>();
				uploads.put(sessionId, userUploads);
			}
			Upload killable = new UploadImpl(stream, new Date());
			userUploads.put(killable.getUuid(), killable);
			return killable;
		}
	}

	@Override
	public void removeUpload(String sessionId, String uuid)
	{
		Map<String, Upload> userUploads = uploads.get(sessionId);
		if( userUploads != null )
		{
			Upload stream = userUploads.get(uuid);
			if( stream != null )
			{
				if( !stream.isClosed() )
				{
					try
					{
						stream.close();
					}
					catch( Exception e )
					{
					}
				}
				userUploads.remove(uuid);
			}
		}
	}

	@Override
	public void killUpload(String sessionId, String uuid)
	{
		Map<String, Upload> userUploads = uploads.get(sessionId);
		if( userUploads != null )
		{
			Upload stream = userUploads.get(uuid);
			if( stream != null )
			{
				try
				{
					stream.kill();
				}
				catch( Exception e )
				{
				}
				userUploads.remove(uuid);
			}
		}
	}

	@Override
	public Map<String, Upload> getUserUploads(String sessionId)
	{
		Map<String, Upload> userUploads = uploads.get(sessionId);
		if( userUploads == null )
		{
			return Collections.unmodifiableMap(Collections.<String, Upload> emptyMap());
		}
		return Collections.unmodifiableMap(userUploads);
	}

	public static class UploadImpl extends Upload
	{
		private boolean closed = false;
		private boolean killed = false;
		private final FileItemStream wrapped;
		private final String uuid;
		private InputStream openedStream;
		private final Date startTime;

		protected UploadImpl(FileItemStream wrapped, Date startTime)
		{
			this.wrapped = wrapped;
			this.uuid = UUID.randomUUID().toString();
			this.startTime = startTime;
		}

		@Override
		public int read() throws IOException
		{
			if( killed )
			{
				throw new StreamKilledException();
			}
			return getOpenedStream().read();
		}

		@Override
		public void close() throws IOException
		{
			if( openedStream != null )
			{
				closed = true;
				Closeables.close(openedStream, true);
			}
		}

		@Override
		public boolean isClosed()
		{
			return closed;
		}

		private InputStream getOpenedStream() throws IOException, StreamKilledException
		{
			if( closed )
			{
				throw new IOException("Stream has been closed");
			}
			if( openedStream == null )
			{
				openedStream = wrapped.openStream();
			}
			return openedStream;
		}

		@Override
		public void kill() throws IOException, StreamKilledException
		{
			closed = true;
			killed = true;
			if( openedStream != null )
			{
				openedStream.close();
			}
			else
			{
				wrapped.kill();
			}
		}

		@Override
		public String getUuid()
		{
			return uuid;
		}

		@Override
		public String getControlId()
		{
			return wrapped.getFieldName();
		}

		@Override
		public Date getStartTime()
		{
			return startTime;
		}
	}

	@Override
	public void userSessionDestroyedEvent(UserSessionLogoutEvent event)
	{
		final String sessionId = event.getSessionId();
		synchronized( uploads )
		{
			uploads.remove(sessionId);
		}
	}
}
