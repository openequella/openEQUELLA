package com.tle.webtests.test.contribute;

import static org.testng.Assert.assertEquals;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.viewitem.MoveCloneDialog;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import java.text.MessageFormat;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class WizardBreadcrumbs extends AbstractCleanupAutoTest {
  @Test
  public void testCrumbs() {
    final String FULL_NAME = context.getFullName("").trim();

    // Browse to contribute page
    ContributePage contribute = new ContributePage(context).load();

    // Start wizard. Bread crumbs = (Contribute > [Collection Name])
    WizardPageTab wizard = contribute.openWizard("Basic Items");
    assertEquals(wizard.getBreadcrumbs(), "Contribute > Basic Items");

    // Save item
    wizard.editbox(1, FULL_NAME);
    SummaryPage summary = wizard.save().publish().get();

    // Edit. Bread crumbs = ([Collection] > [Item name] > Editing)
    wizard = summary.edit().get();
    assertEquals(
        wizard.getBreadcrumbs(), MessageFormat.format("Basic Items > {0} > Editing", FULL_NAME));

    // Save. Re draft. Bread crumbs = ([Collection] > [Item name] > Editing)
    wizard.saveNoConfirm();
    wizard = summary.redraft();
    assertEquals(
        wizard.getBreadcrumbs(), MessageFormat.format("Basic Items > {0} > Editing", FULL_NAME));

    // Save. New version. Bread crumbs = (Contribute > [Collection Name])
    summary = wizard.save().publish().get();
    wizard = summary.newVersion();
    assertEquals(wizard.getBreadcrumbs(), "Contribute > Basic Items");

    // Save. Clone.
    // Bread crumbs = [Collection] > [Item name] > Clone item into a
    // collection
    summary = wizard.save().publish().get();
    MoveCloneDialog clone = summary.cloneAction();
    assertEquals(
        clone.getBreadcrumbs(),
        MessageFormat.format("Basic Items > {0} > Clone item into a collection", FULL_NAME));

    // Save. Move. Bread crumbs = ([Collection Name] > [Item name] > Move
    // the current item)
    MoveCloneDialog move = summary.move(clone.updateWaiter());
    assertEquals(
        move.getBreadcrumbs(),
        MessageFormat.format("Basic Items > {0} > Move item into another collection", FULL_NAME));

    // Contribute a new item with no name and edit
    contribute = new ContributePage(context).load();
    wizard = contribute.openWizard("No mandatory");
    summary = wizard.save().publish();
    String uuid = summary.getItemId().getUuid();
    clone = summary.cloneAction();
    // Check bread crumbs ([Collection Name] > [Item UUID])
    assertEquals(
        clone.getBreadcrumbs(),
        MessageFormat.format(
            "No mandatory > {0}… > Clone item into a collection",
            uuid.substring(0, uuid.length() - 1)));
  }
}
