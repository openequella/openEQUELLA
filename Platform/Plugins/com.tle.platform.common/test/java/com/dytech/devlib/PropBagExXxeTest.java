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

package com.dytech.devlib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingConsumer;

/**
 * Control tests for XML External Entity (XXE) injection through {@link PropBagEx}. Before the
 * parser is hardened these tests fail (an external general entity is resolved and its file contents
 * leak into the document); after adding {@code disallow-doctype-decl} they pass.
 *
 * <p>Written as JUnit 5 (Jupiter) because this is the engine the build actually runs - the legacy
 * {@code junit.framework.TestCase} tests in this module (e.g. {@link PropBagExTest}) are not
 * currently discovered by the Jupiter runner.
 */
public class PropBagExXxeTest {

  /**
   * An external general entity declared in an inline DOCTYPE must not be resolved into the parsed
   * document (that would leak file contents). A hardened parser rejects the DOCTYPE outright
   * (throwing), which is also an acceptable secure outcome.
   */
  @Test
  public void externalGeneralEntityIsNotExpanded() throws Throwable {
    withSecretFile(
        secret -> {
          final String payload =
              "<?xml version=\"1.0\"?>"
                  + "<!DOCTYPE foo [ <!ENTITY xxe SYSTEM \""
                  + secret.uri()
                  + "\"> ]>"
                  + "<xml><item>&xxe;</item></xml>";

          tryParse(payload)
              .ifPresent(
                  bag ->
                      assertFalse(
                          bag.toString().contains(secret.sentinel()),
                          "XXE: the external entity was resolved and the file contents leaked into"
                              + " the document"));
        });
  }

  /** A hardened parser must reject any DOCTYPE declaration. */
  @Test
  public void doctypeDeclarationIsRejected() {
    final String payload = "<!DOCTYPE foo [ <!ENTITY x \"expanded\"> ]><xml><item>&x;</item></xml>";
    assertThrows(XmlParseException.class, () -> new PropBagEx(payload));
  }

  /**
   * Regression: normal metadata using the five predefined XML entities and numeric character
   * references (neither of which needs a DOCTYPE/ENTITY declaration) must still parse correctly
   * after the fix.
   */
  @Test
  public void normalMetadataWithPredefinedEntitiesStillParses() {
    final PropBagEx bag = new PropBagEx("<xml><item><name>a &amp; b &#x41;</name></item></xml>");
    assertEquals("a & b A", bag.getNode("item/name"));
  }

  /** A temporary file containing a random sentinel value, along with its URI. */
  private record SecretFile(String sentinel, URI uri) {}

  /**
   * Creates a temporary file containing a random sentinel value, passes it to {@code body}, and
   * deletes it afterwards regardless of outcome.
   */
  private static void withSecretFile(ThrowingConsumer<SecretFile> body) throws Throwable {
    final File file = File.createTempFile("propbagex-xxe-secret", ".txt");
    try {
      final String sentinel = "XXE_SENTINEL_" + System.nanoTime();
      Files.writeString(file.toPath(), sentinel);
      body.accept(new SecretFile(sentinel, file.toURI()));
    } finally {
      file.delete();
    }
  }

  /**
   * Attempts to parse the given XML, returning empty if the parser rejects it via {@link
   * XmlParseException} (e.g. the XXE hardening rejecting a DOCTYPE) - the one outcome this test
   * treats as an acceptable secure result. Any other exception is not caught here, so it fails the
   * test as usual.
   */
  private static Optional<PropBagEx> tryParse(String xml) {
    try {
      return Optional.of(new PropBagEx(xml));
    } catch (final XmlParseException e) {
      return Optional.empty();
    }
  }
}
