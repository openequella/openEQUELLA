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

package com.tle.core.cluster;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.common.io.ByteStreams;
import com.tle.core.cluster.service.ClusterMessagingService;
import com.tle.core.plugins.PluginAwareObjectInputStream;

@SuppressWarnings("nls")
public class MessageReceiver implements Runnable
{
	private static final int MAX_ATTEMPT = 1000;

	private static final Logger LOGGER = Logger.getLogger(MessageReceiver.class);

	private final String host;
	private final int port;
	private final List<ClusterMessageHandler> handlers;
	private final String myId; // This node
	private final String senderId; // Remote node

	private long messageOffset = -1;
	private boolean die;

	private Executor executor;

	private Thread currentThread;

	public MessageReceiver(String host, int port, String myId, String senderId, List<ClusterMessageHandler> handlers,
		Executor executor)
	{
		this.host = host;
		this.port = port;
		this.myId = myId;
		this.senderId = senderId;
		this.handlers = handlers;
		this.executor = executor;
	}

	@Override
	public void run()
	{
		currentThread = Thread.currentThread();
		long lastConnectionAttempt = 0;
		long lastConnected = 0;
		while( !die )
		{
			long now = System.currentTimeMillis();
			long timeSinceLastAttempt = now - lastConnectionAttempt;

			if( timeSinceLastAttempt < MAX_ATTEMPT )
			{
				try
				{
					Thread.sleep(MAX_ATTEMPT - timeSinceLastAttempt);
				}
				catch( InterruptedException e )
				{
					if( die )
						break;
				}
			}

			lastConnectionAttempt = System.currentTimeMillis();
			try( Socket socket = new Socket(host, port) )
			{
				checkLastConnected(lastConnected);
				lastConnected = System.currentTimeMillis();

				socket.setSoTimeout(30000);
				DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
				dos.writeUTF(senderId);
				dos.writeUTF(myId);
				dos.writeLong(messageOffset);
				dos.flush();

				DataInputStream dis = new DataInputStream(socket.getInputStream());
				while( !die )
				{
					lastConnected = System.currentTimeMillis();
					long cmo = dis.readLong();

					if( cmo == -1 )
					{
						if( LOGGER.isDebugEnabled() )
						{
							LOGGER.debug("Receiving keep alive from NODE: " + senderId);
						}
						continue;
					}

					if( messageOffset != -1 && cmo != messageOffset )
					{
						if( LOGGER.isTraceEnabled() )
						{
							LOGGER.trace(
								MessageFormat.format("Expected offset: {0}, Current offset: {1}", messageOffset, cmo));
						}
						LOGGER.warn(
							"Message offset greater than known offset. Messages may have been missed. Offset expected: "
								+ messageOffset + ", Offset received: " + cmo);
					}

					if( LOGGER.isTraceEnabled() )
					{
						LOGGER.trace(MessageFormat.format("Receiving message from NODE: {0}", senderId));
					}

					int messageSize = dis.readInt();

					if( messageSize > ClusterMessagingService.MAX_MSG_SIZE )
					{
						throw new RuntimeException("Message too big!");
					}

					byte[] data = new byte[messageSize];
					ByteStreams.readFully(dis, data);
					lastConnected = System.currentTimeMillis();

					if( die )
					{
						break;
					}

					Object msg = PluginAwareObjectInputStream.fromBytes(data);
					for( ClusterMessageHandler h : handlers )
					{
						Runnable handler = h.canHandle(msg);
						if( handler != null )
						{
							executor.execute(handler);
						}
					}

					dos.writeBoolean(true);
					dos.flush();
					messageOffset = cmo + 1;
				}
			}
			catch( IOException ex )
			{
				logError(senderId, ex);
			}
			catch( Throwable t )
			{
				LOGGER.error("An unexpected error occurred: ", t);
			}
		}
	}

	private void checkLastConnected(long lastConnected)
	{
		if( lastConnected != 0 && System.currentTimeMillis() - lastConnected > TimeUnit.MINUTES.toMillis(30) )
		{
			LOGGER.warn("This NODE has been unable to connect for more than 30 minutes and may require a restart");
		}
	}

	private void logError(String nodeId, Exception e)
	{
		LOGGER.error(
			MessageFormat.format("Error communicating with NODE: {0}, Error message was: {1}", nodeId, e.getMessage()));
	}

	public void kill()
	{
		this.die = true;
		currentThread.interrupt();
	}
}
