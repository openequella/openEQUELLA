package com.tle.core.connectors.utils;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import com.tle.common.connectors.ConnectorCourse;
import com.tle.common.connectors.ConnectorFolder;
import com.tle.core.connectors.blackboard.beans.Availability;
import com.tle.core.connectors.blackboard.beans.Content;
import com.tle.core.connectors.blackboard.beans.Content.ContentHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

@SuppressWarnings("nls")
public class ConnectorEntityUtilsTest {
  @Test
  public void testParseFolderAllNull() {
    assertFalse(ConnectorEntityUtils.parseFolder(null, null, null).isPresent());
  }

  @Test
  public void testGeneralFlow() {
    ConnectorCourse course = new ConnectorCourse("_test_course_id_1");
    course.setName("Test Course 1");

    List<Content> content = new ArrayList<>();
    content.add(
        buildTestContent(
            "1", "Base folder 1", null, ContentHandler.RESOURCE_FOLDER, Availability.YES, false));
    content.add(
        buildTestContent(
            "2", "lti 2", "1", ContentHandler.RESOURCE_LTI_LINK, Availability.YES, false));
    content.add(
        buildTestContent(
            "7", "Child folder 1.A", "1", ContentHandler.RESOURCE_FOLDER, Availability.NO, true));
    content.add(
        buildTestContent(
            "3", "Base folder 3", null, ContentHandler.RESOURCE_FOLDER, Availability.NO, true));
    content.add(
        buildTestContent(
            "4", "lti 4", "3", ContentHandler.RESOURCE_LTI_LINK, Availability.YES, false));
    content.add(
        buildTestContent(
            "5", "Child folder 3.A", "3", ContentHandler.RESOURCE_FOLDER, Availability.NO, true));
    content.add(
        buildTestContent(
            "6",
            "Child folder 3.A.1",
            "5",
            ContentHandler.RESOURCE_LESSON,
            Availability.YES,
            false));

    List<ConnectorFolder> parsed = ConnectorEntityUtils.parseFolders(content, course);

    assertEquals(parsed.size(), 2);
    assertEquals(0, parsed.get(0).getFolders().size());
    assertEquals(1, parsed.get(1).getFolders().size());
    assertEquals(1, parsed.get(1).getFolders().get(0).getFolders().size());

    Optional<ConnectorFolder> foundFolder = ConnectorEntityUtils.findFolder(parsed.get(0), "6");
    assertFalse(foundFolder.isPresent());

    foundFolder = ConnectorEntityUtils.findFolder(parsed.get(1), "6");
    assertTrue(foundFolder.isPresent());
    assertEquals("Child folder 3.A.1", foundFolder.get().getName());
  }

  private Content buildTestContent(
      String id,
      String title,
      String parentId,
      String contentHandlerType,
      String available,
      boolean hasChildren) {
    Content c = new Content();
    c.setTitle(title);
    c.setId(id);

    Availability av = new Availability();
    av.setAvailable(available);
    c.setAvailability(av);

    ContentHandler ch = new ContentHandler();
    ch.setId(contentHandlerType);
    c.setContentHandler(ch);

    c.setParentId(parentId);

    c.setHasChildren(hasChildren);

    return c;
  }
}
