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

package com.tle.core.plugins;

import static java.io.ObjectStreamConstants.SC_SERIALIZABLE;
import static java.io.ObjectStreamConstants.STREAM_MAGIC;
import static java.io.ObjectStreamConstants.STREAM_VERSION;
import static java.io.ObjectStreamConstants.TC_CLASSDESC;
import static java.io.ObjectStreamConstants.TC_OBJECT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * Control tests for the deserialization denylist enforced by {@link PluginAwareObjectInputStream}
 * (the stream behind the HTTP-invoker endpoint {@code /invoker/*}).
 *
 * <p>{@code resolveClass} rejects a class purely by its fully-qualified name, so the denylist must
 * cover two kinds of class. First, code-execution <em>sinks</em>: the XSLTC {@code TemplatesImpl}
 * (present under both the JDK-internal name and the standalone Apache Xalan name bundled by the
 * reporting and z3950 plugins) and the JNDI sink {@code JdbcRowSetImpl}. Second, nested-
 * deserialization <em>smuggling gadgets</em> ({@code SignedObject}, {@code MarshalledObject}) whose
 * inner {@code ObjectInputStream} is not this stream and would otherwise tunnel any sink past the
 * check. Each name is rejected before the class is loaded, so these tests need no gadget on the
 * classpath and execute no payload.
 *
 * <p>Written as JUnit 5 (Jupiter) because that is the engine the build runs (see {@code
 * com.dytech.devlib.PropBagExXxeTest} for the same rationale).
 */
public class PluginAwareObjectInputStreamTest {

  private static final long ANY_SERIAL_VERSION_UID = 0L;
  private static final short NO_FIELDS = 0;

  @TestFactory
  Stream<DynamicTest> deniesKnownSinksAndNestingGadgets() {
    return Stream.of(
            // code-execution sinks (both TemplatesImpl names, and the JNDI sink)
            "com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl",
            "org.apache.xalan.xsltc.trax.TemplatesImpl",
            "com.sun.rowset.JdbcRowSetImpl",
            // nested-deserialization smuggling gadgets
            "java.security.SignedObject",
            "java.rmi.MarshalledObject")
        .map(
            className ->
                DynamicTest.dynamicTest("denies " + className, () -> assertDenied(className)));
  }

  @Test
  public void permittedObjectRoundTrips() {
    List<Integer> original = new ArrayList<>(List.of(1, 2, 3));

    byte[] serialized = PluginAwareObjectOutputStream.toBytes(original);
    Object restored = PluginAwareObjectInputStream.fromBytes(serialized);

    assertEquals(original, restored);
  }

  private static void assertDenied(String bannedClassName) {
    byte[] stream = serializedStreamNaming(bannedClassName);

    RuntimeException thrown =
        assertThrows(RuntimeException.class, () -> PluginAwareObjectInputStream.fromBytes(stream));

    assertTrue(
        thrown.getMessage() != null
            && thrown.getMessage().contains("Class is banned: " + bannedClassName),
        "Expected the denylist to reject "
            + bannedClassName
            + " but the failure was: "
            + thrown.getMessage());
  }

  /**
   * Builds the leading bytes of a serialization stream describing a single object of {@code
   * className}. Only the stream header and class descriptor are needed: {@link
   * PluginAwareObjectInputStream#resolveClass} consults the denylist on the class name before it
   * reads anything further, so a full, loadable payload is unnecessary - and would otherwise
   * require the sink class on the test classpath. This uses the stable Object Serialization Stream
   * Protocol (via {@link java.io.ObjectStreamConstants}), not a JDK implementation detail.
   */
  private static byte[] serializedStreamNaming(String className) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (DataOutputStream out = new DataOutputStream(bytes)) {
      out.writeShort(STREAM_MAGIC);
      out.writeShort(STREAM_VERSION);
      out.writeByte(TC_OBJECT);
      out.writeByte(TC_CLASSDESC);
      out.writeUTF(className);
      out.writeLong(ANY_SERIAL_VERSION_UID);
      out.writeByte(SC_SERIALIZABLE);
      out.writeShort(NO_FIELDS);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return bytes.toByteArray();
  }
}
