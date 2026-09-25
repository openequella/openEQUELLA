package com.tle.core.util.ims;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dytech.devlib.PropBagEx;
import com.tle.core.util.ims.IMSUtilities.FileManifestResolver;
import com.tle.core.util.ims.beans.IMSItem;
import com.tle.core.util.ims.beans.IMSManifest;
import com.tle.core.util.ims.beans.IMSOrganisation;
import com.tle.core.xstream.TLEXStream;
import com.tle.core.xstream.XMLCompare;
import com.tle.core.xstream.XMLDataConverter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;
import org.xmlpull.v1.XmlPullParserException;

public class IMSTest {

  // Marker held by {@code /test-secret-xml/secret.xml}, the document the traversal fixtures try to
  // reach.
  private static final String TRAVERSAL_PROOF_MARKER = "OEQ-SCORM-PATH-TRAVERSAL-PROOF-b3d1f7";

  private IMSManifest manifest;

  private void loadManifest(String file) {
    TLEXStream xstream = new TLEXStream();
    xstream.registerConverter(new XMLDataConverter());
    manifest =
        (IMSManifest)
            xstream.fromXML(
                new InputStreamReader(getClass().getResourceAsStream(file)), IMSManifest.class);
  }

  @Test
  public void testManifest() {
    loadManifest("imsmanifest.xml");

    assertEquals(1, manifest.getOrganisations().size());

    IMSOrganisation org = manifest.getOrganisations().get(0);
    assertEquals("Alien life form", org.getTitle());

    assertEquals(1, org.getItems().size());

    IMSItem item = org.getItems().get(0);
    assertEquals("Start: Alien life form", item.getTitle());
  }

  @Test
  public void testGetTitleFromManifest() throws XmlPullParserException, IOException, Exception {
    String title =
        IMSUtilities.getTitleFromManifest(
            new InputStreamReader(getClass().getResourceAsStream("imsmanifest.xml")));
    assertEquals("Alien life form", title);

    title =
        IMSUtilities.getTitleFromManifest(
            new InputStreamReader(getClass().getResourceAsStream("basicchinese.xml")));
    assertEquals("Basic Chinese", title);

    title =
        IMSUtilities.getTitleFromManifest(
            new InputStreamReader(getClass().getResourceAsStream("scorm13-ieee.xml")));
    assertEquals("Glide: take a flight [no spoken instructions]", title);

    title =
        IMSUtilities.getTitleFromManifest(
            new InputStreamReader(getClass().getResourceAsStream("metadata_in_org.xml")));
    assertEquals("Another Alien life form", title);
  }

  @Test
  public void testCombineManifest() throws IOException, XmlPullParserException {
    try (Writer imsOutput = new StringWriter()) {
      IMSUtilities.combine(createResolver("basicchinese.xml"), imsOutput);
      String title = IMSUtilities.getTitleFromManifest(new StringReader(imsOutput.toString()));
      assertEquals("Basic Chinese", title);
    }
  }

  @Test
  public void testCombineManifest2() throws IOException, XmlPullParserException {
    try (Writer imsOutput = new StringWriter()) {
      IMSUtilities.combine(createResolver("foodmaker.xml"), imsOutput);
      String string = imsOutput.toString();
      // This is to ensure namespaces are kept!!!
      assertEquals(51, count(string, "xmlns"));
      assertEquals(-1, string.indexOf("<imsmd:lom>"));

      String title = IMSUtilities.getTitleFromManifest(new StringReader(string));
      assertEquals("The foul food maker", title);
    }
  }

  @Test
  public void testCombineManifest3() throws Exception {
    testCombine("combine1");
  }

  // A manifest must not include a document outside its package root. An invalid reference is
  // ignored without aborting the combine, so external content is excluded while valid manifest
  // content is still preserved.
  @Test
  public void testCombineIgnoresReferenceOutsidePackage() throws Exception {
    try (StringWriter output = new StringWriter()) {
      IMSUtilities.combine(
          createResolver("/combine-direct-reference-outside-package/imsmanifest.xml"), output);

      String combined = output.toString();
      assertFalse(combined.contains(TRAVERSAL_PROOF_MARKER));
      // Confirms that rejecting the invalid reference does not abort the rest of the combine.
      assertTrue(combined.contains("OEQ-SCORM-IN-PACKAGE-CONTENT-a7c2e9"));
    }
  }

  // A document included from within the package must not be able to reference content outside the
  // package root.
  @Test
  public void testCombineIgnoresNestedReferenceOutsidePackage() throws Exception {
    try (StringWriter output = new StringWriter()) {
      IMSUtilities.combine(
          createResolver("/combine-nested-reference-outside-package/imsmanifest.xml"), output);

      assertFalse(output.toString().contains(TRAVERSAL_PROOF_MARKER));
    }
  }

  // References from included documents may leave their own directory as long as they remain within
  // the package root. This verifies that the package root, rather than each document's directory,
  // is used as the containment boundary.
  @Test
  public void testCombineAllowsNestedReferenceWithinPackageRoot() throws Exception {
    try (StringWriter output = new StringWriter()) {
      IMSUtilities.combine(
          createResolver("/combine-nested-reference-within-package/imsmanifest.xml"), output);

      assertTrue(output.toString().contains("Referenced document within package"));
    }
  }

  private void testCombine(String folder) throws Exception {
    StringWriter output = new StringWriter();
    IMSUtilities.combine(createResolver("/" + folder + "/imsmanifest.xml"), output);

    Reader result =
        new InputStreamReader(getClass().getResourceAsStream("/" + folder + "/results.xml"));

    assertTrue(new XMLCompare().compare(new StringReader(output.getBuffer().toString()), result));
  }

  private int count(String string, String value) {
    int count = 0;
    for (int index = string.indexOf(value); index > 0; index = string.indexOf(value, index + 1)) {
      count++;
    }
    return count;
  }

  @Test
  public void testShrinkXML() {
    loadManifest("imsmanifest.xml");
    PropBagEx xml = IMSUtilities.shrinkXML(manifest);
    assertEquals(2, xml.getIntNode("wrapper/@type"));
    assertEquals(1, xml.getIntNode("wrapper/wrapper/@type"));
    assertEquals(3, xml.getIntNode("wrapper/wrapper/wrapper/@type"));
    assertTrue(xml.isNodeTrue("wrapper/wrapper/wrapper/@isvisible"));
    assertEquals("Start: Alien life form", xml.getNode("wrapper/wrapper/wrapper"));
    assertEquals(
        "Content/LV532/LO_10/20030130/LO10/index.htm", xml.getNode("wrapper/wrapper/wrapper/file"));
    assertEquals(15, xml.nodeCount("wrapper/file"));
  }

  private FileManifestResolver createResolver(String relPath) {
    try {
      return new FileManifestResolver(new File(this.getClass().getResource(relPath).toURI()));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
