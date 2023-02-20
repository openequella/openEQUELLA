/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.cluster.service;

import com.tle.core.cluster.MessageSender;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.function.Function;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for establishing a connection - and then maintaining it - with a calling node for the
 * sending of messages to that node.
 *
 * <p>Each connection starts first with the identifier of the target node (which should match the
 * processing node), followed by the identifier of the connecting node (receiverId). Both of these
 * are UTF strings prefixed with 2 bytes specifying their length. Lastly, a Long (8 bytes) is sent
 * which is the offset into the number of messages that the connecting node has received.
 *
 * <p>After this processing, the connection is considered established and an infinite loop is
 * established which sends messages to the connecting node when new messages are added. Or, in
 * between messages will send a keep alive message.
 */
public class ClusterMessageProcessor implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClusterMessageProcessor.class);

  private final Socket socket;
  private final Function<String, MessageSender> getMessageSender;
  private final Predicate<String> isThisNode;

  public ClusterMessageProcessor(
      Socket socket,
      Function<String, MessageSender> getMessageSender,
      Predicate<String> isThisNode) {
    this.socket = socket;
    this.getMessageSender = getMessageSender;
    this.isThisNode = isThisNode;
  }

  @Override
  public void run() {
    String receiverId = "unknown";

    try (DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos =
            new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {
      socket.setSoTimeout(10000);

      // Confirm request is for this NODE
      String thisId = dis.readUTF();
      if (!isThisNode.test(thisId)) {
        throw new IOException(
            "Remote NODE trying to communicate with stale reference to this NODE");
      }

      // Find out who the connecting node is
      receiverId = dis.readUTF();
      LOGGER.info("Successful connection from NODE: " + receiverId);

      // Get the first message to determine where the caller is up to
      MessageSender ms = getMessageSender.apply(receiverId);
      ms.checkExpectedOffset(dis);

      // Process messages until process finishes - i.e. the server stops or an exception is thrown
      while (true) {
        ms.sendMessages(dos, dis);
        ms = getMessageSender.apply(receiverId);
      }
    } catch (IOException ex) {
      LOGGER.error(
          MessageFormat.format(
              "Error communicating with NODE: {0}, Error message was: {1}",
              receiverId, ex.getMessage()));
    } catch (Throwable t) {
      LOGGER.error("An unexpected error occurred: ", t);
    }
  }
}
