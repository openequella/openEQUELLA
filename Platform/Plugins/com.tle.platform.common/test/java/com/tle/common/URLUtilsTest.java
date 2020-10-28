package com.tle.common;

import org.junit.Assert;
import org.junit.Test;

public class URLUtilsTest {

  @Test
  public void ConvertItemSummaryUrlTest() {
    final String input1 = "items/74adcaf2-3f24-4a34-85fd-c788953ad649/1/";
    final String input2 = "/items/74adcaf2-3f24-4a34-85fd-c788953ad649/1";
    final String input3 =
        "https://www.thisisanexampleequella.com/vanilla/items/74adcaf2-3f24-4a34-85fd-c788953ad649/1/";

    final String output1 = "74adcaf2-3f24-4a34-85fd-c788953ad649/1";

    Assert.assertEquals(URLUtils.convertItemSummaryURLToItemString(input1), output1);
    Assert.assertEquals(URLUtils.convertItemSummaryURLToItemString(input2), output1);
    Assert.assertEquals(URLUtils.convertItemSummaryURLToItemString(input3), output1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void ConvertItemSummaryUrlNegativeCaseTest() {
    final String input1 = "this is a garbage string we expect to fail";
    final String input2 =
        "https://www.thisisanexampleequella.com/vanilla/items/74adcaf2-3f24-4a34-85fd-c788953ad649/1/?params=notallowed";

    URLUtils.convertItemSummaryURLToItemString(input1);
    URLUtils.convertItemSummaryURLToItemString(input2);
  }
}
