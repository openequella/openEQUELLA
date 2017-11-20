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

package com.tle.core.cluster.service.impl;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.utils.ZKPaths;
import org.apache.log4j.Logger;

import com.dytech.common.net.NetworkUtils;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.tle.common.Check;
import com.tle.common.NamedThreadFactory;
import com.tle.common.Pair;
import com.tle.core.application.StartupBean;
import com.tle.core.cluster.ClusterMessageHandler;
import com.tle.core.cluster.MessageReceiver;
import com.tle.core.cluster.MessageSender;
import com.tle.core.cluster.service.ClusterMessagingService;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginAwareObjectOutputStream;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.zookeeper.ZookeeperService;

@Singleton
@SuppressWarnings("nls")
@Bind(ClusterMessagingService.class)
public class ClusterMessagingServiceImpl implements ClusterMessagingService, StartupBean, PathChildrenCacheListener
{
	private static final Logger LOGGER = Logger.getLogger(ClusterMessagingServiceImpl.class);
	private static final String MESSAGING_ZKPATH = "messaging";

	@Inject(optional = true)
	@Named("messaging.bindAddress")
	private String bindAddress;
	@Inject(optional = true)
	@Named("messaging.useHostname")
	private boolean useHostname;
	@Inject(optional = true)
	@Named("messaging.bindPort")
	private int bindPort;

	@Inject
	private PluginTracker<ClusterMessageHandler> handlerTracker;
	@Inject
	private ZookeeperService zookeeperService;

	private final ExecutorService msgExecutor = Executors.newFixedThreadPool(
		Runtime.getRuntime().availableProcessors() * 2, new NamedThreadFactory("ClusterMessagingServiceImpl.handlers"));
	private final ExecutorService senderExecutor = Executors
		.newCachedThreadPool(new NamedThreadFactory("ClusterMessagingServiceImpl.sender"));
	private final ExecutorService receiverExecutor = Executors
		.newCachedThreadPool(new NamedThreadFactory("ClusterMessagingServiceImpl.receiver"));

	private final Map<String, MessageReceiver> receivers = Maps.newConcurrentMap();
	private final LoadingCache<String, MessageSender> senders = CacheBuilder.newBuilder()
		.expireAfterAccess(30, TimeUnit.MINUTES).removalListener(new RemovalListener<String, MessageSender>()
		{
			@Override
			public void onRemoval(RemovalNotification<String, MessageSender> notification)
			{
				if( LOGGER.isDebugEnabled() )
				{
					LOGGER.debug("Removing stale sender from cache for NODE: " + notification.getKey());
				}
			}

		}).build(new CacheLoader<String, MessageSender>()
		{
			@Override
			public MessageSender load(String receiverId) throws Exception
			{
				if( LOGGER.isDebugEnabled() )
				{
					LOGGER.debug("Loading sender cache for NODE: " + receiverId);
				}
				return new MessageSender(receiverId);
			}
		});

	@Override
	public void startup()
	{
		if( !zookeeperService.hasStarted() )
		{
			throw new RuntimeException("Dependent ZK service not started!");
		}
		if( zookeeperService.isCluster() )
		{
			Thread thread = new Thread()
			{
				@Override
				public void run()
				{
					setupBindAddress();
					try
					{
						@SuppressWarnings("resource")
						ServerSocket server = new ServerSocket();
						LOGGER.info("Binding to " + bindAddress + ":" + bindPort);
						server.bind(new InetSocketAddress(bindAddress, bindPort));
						zookeeperService.createNode(MESSAGING_ZKPATH, bindAddress + ":" + bindPort);
						zookeeperService.createPathCache(MESSAGING_ZKPATH, true, ClusterMessagingServiceImpl.this);

						// Keep accepting connections while the socket is valid.
						do
						{
							final Socket sock = server.accept();
							senderExecutor.execute(new Runnable()
							{
								String receiverId;

								@Override
								public void run()
								{
									try( Socket s = sock;
										DataInputStream dis = new DataInputStream(sock.getInputStream());
										DataOutputStream dos = new DataOutputStream(
											new BufferedOutputStream(sock.getOutputStream())); )
									{
										sock.setSoTimeout(10000);
										String thisId = dis.readUTF();
										if( !isThisNode(thisId) )
										{
											throw new IOException(
												"Remote NODE trying to communicate with stale reference to this NODE");
										}
										receiverId = dis.readUTF();
										LOGGER.info("Successful connection from NODE: " + receiverId);
										MessageSender ms = senders.get(receiverId);
										ms.checkExpectedOffset(dis);
										while( true )
										{
											ms.sendMessages(dos, dis);
											ms = senders.get(receiverId);
										}
									}
									catch( IOException ex )
									{
										logError(receiverId, ex);
									}
									catch( Throwable t )
									{
										LOGGER.error("An unexpected error occurred: ", t);
									}
								}

								private void logError(String nodeId, Exception e)
								{
									LOGGER.error(MessageFormat.format(
										"Error communicating with NODE: {0}, Error message was: {1}", nodeId,
										e.getMessage()));
								}
							});
						}
						while( !server.isClosed() && server.isBound() );
					}
					catch( IOException ex )
					{
						LOGGER.error("Error while binding or accepting socket " + bindAddress + ":" + bindPort, ex);
						Throwables.propagate(ex);
					}
				}
			};
			thread.setName("ClusterMessagingServiceImpl.serverSocket");
			thread.start();
		}
	}

	private void setupBindAddress()
	{
		try
		{
			// Verify the bind address
			if( Check.isEmpty(bindAddress) )
			{
				if (useHostname)
				{
					bindAddress = InetAddress.getLocalHost().getHostName();
				}
				else
				{
					List<Pair<NetworkInterface, InetAddress>> inetAddresses = NetworkUtils.getInetAddresses();
					if (inetAddresses.size() == 1) {
						bindAddress = inetAddresses.get(0).getSecond().getHostAddress();
					} else {
						throw new RuntimeException("messaging.bindAddress has not been defined in"
								+ " optional-config.properties, and EQUELLA could not determine a suitable"
								+ " network interface to bind to.");
					}
				}
			}
			else
			{
				bindAddress = InetAddress.getByName(bindAddress).getHostAddress();
			}
		}
		catch( UnknownHostException e )
		{
			Throwables.propagate(e);
		}
	}

	@Override
	public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception
	{
		Type type = event.getType();
		if( type.equals(Type.CHILD_ADDED) || type.equals(Type.CHILD_UPDATED) || type.equals(Type.CHILD_REMOVED) )
		{
			String remoteId = ZKPaths.getNodeFromPath(event.getData().getPath());
			String[] clientInfo = new String(event.getData().getData()).split(":");
			if( !isThisNode(remoteId) && !hasSameInfo(clientInfo) )
			{
				if( type.equals(Type.CHILD_ADDED) )
				{
					senders.get(remoteId);
					addReceiver(remoteId, clientInfo);
				}
				else if( type.equals(Type.CHILD_UPDATED) )
				{
					senders.get(remoteId);
					removeReceiver(remoteId);
					addReceiver(remoteId, clientInfo);
				}
				else
				{
					removeReceiver(remoteId);
				}
			}
		}
	}

	private boolean hasSameInfo(String[] clientInfo)
	{
		return clientInfo[0].equals(bindAddress) && (Integer.parseInt(clientInfo[1]) == bindPort);
	}

	private void addReceiver(String remoteId, String[] clientInfo)
	{
		MessageReceiver messageReceiver = new MessageReceiver(clientInfo[0], Integer.parseInt(clientInfo[1]),
			zookeeperService.getNodeId(), remoteId, handlerTracker.getBeanList(), msgExecutor);
		receiverExecutor.submit(messageReceiver);
		receivers.put(remoteId, messageReceiver);
	}

	private void removeReceiver(String nodeId)
	{
		MessageReceiver removed = receivers.remove(nodeId);
		if( removed != null )
		{
			removed.kill();
		}
	}

	private boolean isThisNode(String nodeId)
	{
		return zookeeperService.getNodeId().equals(nodeId);
	}

	@Override
	public void postMessage(Serializable msg)
	{
		postMessage(null, msg);
	}

	@Override
	public void postMessage(String toNodeIdOnly, Serializable msg)
	{
		Collection<String> recipients;
		byte[] message = PluginAwareObjectOutputStream.toBytes(msg);
		if( toNodeIdOnly != null )
		{
			recipients = Collections.singletonList(toNodeIdOnly);
		}
		else
		{
			recipients = senders.asMap().keySet();
		}
		for( String nodeId : recipients )
		{
			MessageSender ms = senders.getIfPresent(nodeId);
			if( ms != null )
			{
				ms.queueMessage(message);
			}
		}
	}
}
