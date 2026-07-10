package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;

import com.dytech.devlib.PropBagEx;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.common.Pair;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import org.testng.annotations.Test;

/**
 * End-to-end control test for XML External Entity (XXE) injection through the {@code POST
 * /api/item/} metadata field. Before the XML parser is hardened, an external general entity
 * declared in an inline DOCTYPE is resolved server-side and the referenced file's contents are
 * stored in (and returned with) the created item's metadata. After the fix the parser rejects the
 * DOCTYPE, which surfaces to the client as a {@code 400 Bad Request} ({@link
 * com.dytech.devlib.XmlParseException} mapped by {@code RestEasyExceptionMapper}) - no item is
 * created and no expansion occurs.
 *
 * <p>The test and the server run on the same host, so the entity references a temporary file this
 * test writes, keyed by a random sentinel - it never reads a real system file.
 */
public class ItemApiXxeTest extends AbstractItemApiTest {
  private static final String OAUTH_CLIENT_ID = "ItemApiXxeTestClient";

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  /** Creates an item with the given metadata XML and registers it for cleanup after the test. */
  private ObjectNode createItemWithMetadata(String token, String metadataXml) throws Exception {
    final ObjectNode item = createItemJson(COLLECTION_ATTACHMENTS);
    item.put("metadata", metadataXml);

    final ObjectNode created = createItem(item.toString(), token, "draft", true);
    addDeletable(created);

    return created;
  }

  @Test
  public void xxeExternalEntityRejectedWithBadRequest() throws Exception {
    final String token = getToken();
    final File secret = File.createTempFile("itemapi-xxe-secret", ".txt");
    try {
      final String sentinel = "XXE_SENTINEL_" + System.nanoTime();
      Files.writeString(secret.toPath(), sentinel);

      final String metadata =
          "<?xml version=\"1.0\"?>"
              + "<!DOCTYPE foo [ <!ENTITY xxe SYSTEM \""
              + secret.toURI()
              + "\"> ]>"
              + "<xml><item><name>&xxe;</name></item></xml>";

      final ObjectNode item = createItemJson(COLLECTION_ATTACHMENTS);
      item.put("metadata", metadata);

      // The hardened parser rejects the DOCTYPE, which must surface as a 400 Bad Request (not a
      // 500, and not a 201 with the file contents leaked as in the vulnerable pre-fix behaviour).
      assertResponse(
          postItem(item.toString(), token, "draft", true),
          400,
          "XXE payload (DOCTYPE / external entity) should be rejected with 400 Bad Request");
    } finally {
      secret.delete();
    }
  }

  @Test
  public void normalItemStillCreatable() throws Exception {
    final String token = getToken();
    final ObjectNode created =
        createItemWithMetadata(
            token, "<xml><item><name>ItemApiXxeTest - normal &amp; ok</name></item></xml>");

    final PropBagEx metadata = new PropBagEx(created.get("metadata").asText());
    assertEquals(metadata.getNode("item/name"), "ItemApiXxeTest - normal & ok");
  }

  /**
   * Regression for the PUT (edit) path: editing an existing item's metadata re-parses it via {@link
   * PropBagEx} (a different code path to item creation - see {@code ItemEditorImpl.editMetadata}),
   * so it must still work after the XXE hardening.
   */
  @Test
  public void normalItemStillEditable() throws Exception {
    final String token = getToken();
    final ObjectNode created =
        createItemWithMetadata(
            token, "<xml><item><name>ItemApiXxeTest - before edit</name></item></xml>");

    created.put(
        "metadata", "<xml><item><name>ItemApiXxeTest - after edit &amp; ok</name></item></xml>");
    final ObjectNode edited = editItem(created, token);

    final PropBagEx metadata = new PropBagEx(edited.get("metadata").asText());
    assertEquals(metadata.getNode("item/name"), "ItemApiXxeTest - after edit & ok");
  }
}
