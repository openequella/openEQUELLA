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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.tle.core.cluster.exception.MessagingException;
import com.tle.core.cluster.service.ClusterMessagingService;

@SuppressWarnings("nls")
public class MessageSender
{
	private static final Logger LOGGER = Logger.getLogger(MessageSender.class);

	private String receiverId; // Remote node
	private final BlockingDeque<byte[]> msgQueue = new LinkedBlockingDeque<>();
	private long totalQueueSize;
	private long headOffset = 0;

	public MessageSender(String receiverId)
	{
		this.receiverId = receiverId;
	}

	public void sendMessages(DataOutputStream dos, DataInputStream dis) throws IOException, InterruptedException
	{
		byte[] msg = msgQueue.poll(5, TimeUnit.SECONDS);
		if( msg == null )
		{
			if( LOGGER.isDebugEnabled() )
			{
				LOGGER.debug(MessageFormat.format("Sending keepalive to NODE: {0}", receiverId));
			}
			dos.writeLong(-1);
			dos.flush();
			return;
		}

		boolean processed = false;
		try
		{
			dos.writeLong(headOffset);
			int msgSize = msg.length;
			dos.writeInt(msgSize);
			dos.write(msg);
			dos.flush();

			if( LOGGER.isTraceEnabled() )
			{
				LOGGER.trace(MessageFormat.format("Sending message to NODE: {0}", receiverId));
			}

			dis.readBoolean();

			synchronized( this )
			{
				totalQueueSize -= msgSize;
				headOffset++;
				processed = true;
			}
		}
		finally
		{
			if( !processed )
			{
				msgQueue.addFirst(msg);
			}
		}
	}

	public void checkExpectedOffset(DataInputStream dis) throws IOException
	{
		long expectedOffset = dis.readLong();
		if( LOGGER.isTraceEnabled() )
		{
			LOGGER.trace(MessageFormat.format("Expected offset: {0}, Head offset: {1}", expectedOffset, headOffset));
		}
		if( expectedOffset != -1 && expectedOffset != headOffset )
		{
			LOGGER.warn(MessageFormat.format("NODE: {0} was down for too long. {1} messages have been missed",
				receiverId, (headOffset - expectedOffset)));
		}
	}

	public synchronized void queueMessage(byte[] msg)
	{
		totalQueueSize += msg.length;

		if( msg.length > ClusterMessagingService.MAX_MSG_SIZE )
		{
			throw new MessagingException("Message is too large");
		}

		int droppedMsgs = 0;
		while( totalQueueSize > ClusterMessagingService.MAX_QUEUE_SIZE && !msgQueue.isEmpty() )
		{
			byte[] firstMsg = msgQueue.removeFirst();
			totalQueueSize -= firstMsg.length;
			headOffset++;
			droppedMsgs++;
		}
		if( droppedMsgs > 0 )
		{
			LOGGER.warn("Dropped " + droppedMsgs + " messages from queue for NODE: " + receiverId);
		}

		if( LOGGER.isTraceEnabled() )
		{
			LOGGER.trace(MessageFormat.format("Queueing message of size: {0}, Total queue size: {1}", msg.length,
				totalQueueSize));
		}

		msgQueue.add(msg);
	}
}
