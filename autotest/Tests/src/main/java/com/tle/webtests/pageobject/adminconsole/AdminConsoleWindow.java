package com.tle.webtests.pageobject.adminconsole;

import com.tle.client.harness.ClientLauncher;
import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.adminconsole.collection.CollectionDetailsTab;
import com.tle.webtests.pageobject.adminconsole.remoterepo.RemoteRepoDetailsTab;
import com.tle.webtests.pageobject.adminconsole.schema.SchemaDetailsTab;
import java.util.regex.Pattern;
import javax.swing.JComboBox;
import org.fest.swing.core.matcher.FrameMatcher;
import org.fest.swing.core.matcher.JButtonMatcher;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.finder.WindowFinder;
import org.fest.swing.fixture.FrameFixture;

public class AdminConsoleWindow extends AbstractAppletWindow<AdminConsoleWindow> {
  public AdminConsoleWindow(PageContext context) {
    super(context);
    setup();
  }

  public SchemaDetailsTab editSchema(String name) {
    edit("Metadata Schemas", name);
    return new SchemaDetailsTab(context, windowHolder);
  }

  public SchemaDetailsTab addSchema() {
    add("Metadata Schemas");
    return new SchemaDetailsTab(context, windowHolder);
  }

  public AdminConsoleWindow deleteSchema(String name) {
    delete("Metadata Schemas", name);
    return this;
  }

  public RemoteRepoDetailsTab editRemoteRepo(String name) {
    edit("Remote Repositories", name);
    return new RemoteRepoDetailsTab(context, windowHolder);
  }

  public RemoteRepoDetailsTab addRemoteRepo(String type) {
    add("Remote Repositories", type);
    return new RemoteRepoDetailsTab(context, windowHolder);
  }

  public AdminConsoleWindow deleteRemoteRepo(String name) {
    delete("Remote Repositories", name);
    return this;
  }

  public CollectionDetailsTab editCollection(String name) {
    edit("Collection Definitions", name);
    return new CollectionDetailsTab(context, windowHolder);
  }

  public CollectionDetailsTab addCollection() {
    add("Collection Definitions");
    return new CollectionDetailsTab(context, windowHolder);
  }

  public AdminConsoleWindow deleteCollection(String name) {
    delete("Collection Definitions", name);
    return this;
  }

  public void add(String type) {
    add(type, null);
  }

  public void add(String type, String subtype) {
    clickButton(type);
    waitForDownload();
    clickButton("Add");
    if (!Check.isEmpty(subtype)) {
      clickList(subtype);
      clickButton("OK");
    }
    waitForWindow("Editor");
  }

  private void delete(String type, String name) {
    clickButton(type);
    waitForDownload();
    clickList(name);
    clickButton("Remove");
    clickButton("Yes");
    clickButton("OK");
  }

  private void edit(String type, String name) {
    clickButton(type);
    waitForDownload();
    clickList(name);
    clickButton("Edit");
    waitForWindow("Editor");
  }

  public void toDo() {
    add("Collection Definitions");
    clickButton("Close");
    clickButton("Don't Save");

    add("Advanced Searches");
    clickButton("Close");

    add("Taxonomies");
    clickButton("Close");

    clickButton("Hierarchy Editor");
    waitForWindow("Hierarchy Editor");
    clickButton("Close");

    add("Reporting");
    clickButton("Close");

    add("Harvester Profiles", "EQUELLA Harvester");
    clickButton("Close");

    add("Courses");
    clickButton("Close");

    clickButton("User Management");
    clickTableCell("LDAP");
    clickButton("Security Manager");
    waitForWindow("Security Manager");
    clickTreePath("Institution");
    clickButton("Close");
  }

  private void setup() {
    ClientLauncher frame =
        GuiActionRunner.execute(
            new GuiQuery<ClientLauncher>() {
              protected ClientLauncher executeInEDT() {
                return new ClientLauncher();
              }
            });

    FrameFixture window = new FrameFixture(frame);
    window.show();

    windowHolder = window;
  }

  public AdminConsoleWindow launch(String institutionName) {
    windowHolder =
        WindowFinder.findFrame(
                FrameMatcher.withTitle(Pattern.compile("Client Launcher.*")).andShowing())
            .withTimeout(30000)
            .using(windowHolder.robot);

    windowHolder
        .comboBox(wait(JComboBox.class))
        .selectItem(institutionName + " - " + context.getBaseUrl() + institutionName + "/");
    windowHolder.button(wait(JButtonMatcher.withText("Launch").andShowing())).click();
    windowHolder =
        WindowFinder.findFrame(
                FrameMatcher.withTitle(Pattern.compile("Administration Console.*")).andShowing())
            .withTimeout(30000)
            .using(windowHolder.robot);
    windowHolder.focus();
    return this;
  }
}
