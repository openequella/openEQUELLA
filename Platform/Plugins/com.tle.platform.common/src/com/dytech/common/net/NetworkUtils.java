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

package com.dytech.common.net;

import com.google.common.collect.Lists;
import com.tle.common.Pair;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

public class NetworkUtils {
  /**
   * @return a bunch of InetAddresses that are "real", ie, not virtual, loopback, multicast and are
   *     up.
   */
  public static List<Pair<NetworkInterface, InetAddress>> getInetAddresses() {
    List<Pair<NetworkInterface, InetAddress>> addrs = Lists.newArrayList();
    try {
      Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
      while (nis.hasMoreElements()) {
        NetworkInterface ni = nis.nextElement();
        if (!ni.isLoopback() && !ni.isVirtual() && ni.isUp()) {
          for (Enumeration<InetAddress> ias = ni.getInetAddresses(); ias.hasMoreElements(); ) {
            InetAddress ia = ias.nextElement();
            if (!ia.isMulticastAddress() && !ia.isLoopbackAddress()) {
              addrs.add(new Pair<NetworkInterface, InetAddress>(ni, ia));
            }
          }
        }
      }
    } catch (SocketException e) {
      // Carry on
    }
    return addrs;
  }

  private NetworkUtils() {
    throw new Error();
  }
}
