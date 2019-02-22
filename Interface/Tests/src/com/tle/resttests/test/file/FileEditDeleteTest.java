package com.tle.resttests.test.file;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.entity.ItemId;
import com.tle.json.entity.Items;
import com.tle.json.requests.StagingRequests;
import com.tle.resttests.AbstractEntityCreatorTest;
import com.tle.resttests.util.RestTestConstants;
import com.tle.resttests.util.files.Attachments;
import java.io.File;
import org.testng.annotations.Test;

// Uses EPS item/copy endpoint
@Test(groups = "eps")
public class FileEditDeleteTest extends AbstractEntityCreatorTest {
  private StagingRequests staging;

  @Override
  public void customisePageContext() {
    super.customisePageContext();
    staging = builder().staging();
  }

  @Test
  public void testDelete() throws Exception {
    ObjectNode item = Items.json(RestTestConstants.COLLECTION_BASIC);
    String stagingUrl = staging.create();
    String stagingUUID = stagingUrl.substring(stagingUrl.lastIndexOf('/') + 1, stagingUrl.length());

    final File file = new File(getPathFromUrl(Attachments.get("avatar.png")));
    staging.putFile(stagingUrl, "avatar.png", file);

    ItemId itemId = items.getId(items.create(items.createRequest(item, true, "file", stagingUUID)));
    items.file(items.successfulRequest(), itemId, "avatar.png");

    stagingUrl = items.copyFilesForEdit(itemId);
    stagingUUID = stagingUrl.substring(stagingUrl.lastIndexOf('/') + 1, stagingUrl.length());
    staging.deleteFile(stagingUrl, "avatar.png");

    items.editResponse(
        items.editRequest(items.successfulRequest(), item).param("file", stagingUUID), itemId);
    items.file(items.notFoundRequest(), itemId, "avatar.png");
  }
}
