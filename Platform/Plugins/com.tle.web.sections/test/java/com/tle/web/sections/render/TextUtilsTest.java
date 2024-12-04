package com.tle.web.sections.render;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;

@SuppressWarnings("nls")
public class TextUtilsTest extends TestCase {
  public void testEnsureWrap() {
    assertEquals(
        "Dr Dan Rowle y, <b><i>[Copy</i></b>&hellip;",
        TextUtils.INSTANCE.ensureWrap(
            "Dr Dan Rowley, <b><i>[Copyright]</i></b>, p. 1000", 20, 5, true));
  }

  private final String testDescription =
      "start nothing nothing nothing nothing nothing nothing nothing nothing nothing nothing"
          + " nothing nothing nothing nothing first nothing nothing nothing nothing nothing nothing"
          + " nothing nothing nothing nothing nothing nothing nothing nothing nothing nothing"
          + " nothing nothing nothing nothing nothing nothing nothing nothing nothing nothing"
          + " nothing nothing nothing nothing nothing nothing nothing nothing nothing nothing"
          + " nothing nothing nothing nothing nothing nothing nothing nothing nothing nothing"
          + " nothing nothing nothing nothing nothing nothing nothing nothing nothing nothing"
          + " nothing nothing nothing nothing nothing nothing nothing nothing nothing nothing"
          + " nothing nothing nothing nothing nothing nothing nothing nothing nothing nothing"
          + " nothing nothing nothing nothing nothing nothing nothing nothing nothing nothing"
          + " nothing nothing nothing nothing nothing nothing nothing nothing nothing nothing"
          + " nothing nothing nothing nothing nothing nothing nothing nothing nothing nothing"
          + " nothing nothing nothing nothing nothing nothing nothing nothing nothing nothing"
          + " nothing nothing nothing nothing nothing nothing nothing nothing nothing nothing"
          + " nothing nothing nothing nothing nothing nothing nothing nothing nothing nothing"
          + " nothing nothing nothing nothing nothing nothing nothing nothing nothing nothing"
          + " nothing nothing nothing nothing nothing nothing nothing middle nothing nothing"
          + " nothing nothing nothing nothing nothing nothing nothing nothing nothing nothing"
          + " nothing nothing nothing nothing nothing nothing nothing nothing nothing nothing"
          + " nothing nothing nothing nothing nothing nothing nothing nothing nothing nothing"
          + " nothing nothing nothing nothing nothing nothing nothing nothing nothing nothing"
          + " nothing nothing nothing nothing nothing nothing nothing nothing nothing nothing end";

  public void testMostOccurences() {
    List<String> testCaseStart = Arrays.asList("start");
    List<String> testCaseFirst = Arrays.asList("first");
    List<String> testCaseMiddle = Arrays.asList("middle");
    List<String> testCaseEnd = Arrays.asList("end");
    List<String> testCaseStartMiddleEnd = Arrays.asList("start", "middle", "end");
    List<String> emptyTestCase = Collections.emptyList();

    int maxLength = TextUtils.DESCRIPTION_LENGTH;
    int wordsSpaceLength = TextUtils.WORDS_SPACE_LENGTH;
    final char ELLIPSIS_ENTITY = (char) 0x2026;
    int ellipsLength = 1;
    TextUtils textUtils = TextUtils.INSTANCE;

    // Should truncate from start
    String truncatedString = textUtils.mostOccurences(testDescription, maxLength, testCaseStart);
    assertEquals(truncatedString.length(), maxLength + ellipsLength);
    assertEquals(testDescription.substring(0, maxLength) + ELLIPSIS_ENTITY, truncatedString);

    // Should add ellipsis at the beginning
    truncatedString = textUtils.mostOccurences(testDescription, maxLength, testCaseFirst);
    assertEquals(truncatedString.length(), maxLength + ellipsLength);
    int wordFirstIndex = testDescription.indexOf("first");

    int curPosition = wordFirstIndex + "first".length();
    String expectedString =
        ELLIPSIS_ENTITY
            + testDescription.substring(curPosition - wordsSpaceLength, curPosition)
            + testDescription.substring(
                curPosition, maxLength + (curPosition - wordsSpaceLength - ellipsLength))
            + ELLIPSIS_ENTITY;
    assertEquals(expectedString, truncatedString);

    // Should truncate from last occurence of "middle"
    truncatedString = textUtils.mostOccurences(testDescription, maxLength, testCaseMiddle);
    int middleIndex = testDescription.indexOf("middle") + "middle".length();
    String assumedMiddleTruncation =
        ELLIPSIS_ENTITY
            + testDescription.substring(middleIndex - wordsSpaceLength, testDescription.length());
    assertEquals(
        truncatedString.length(),
        ellipsLength + wordsSpaceLength + (testDescription.length() - middleIndex));
    assertEquals(assumedMiddleTruncation, truncatedString);

    // Should truncate from (wordsSpaceLength + ellipsLength)
    // to the end
    truncatedString = textUtils.mostOccurences(testDescription, maxLength, testCaseEnd);
    int endIndex = testDescription.length();
    String assumedEndTruncation =
        ELLIPSIS_ENTITY + testDescription.substring(endIndex - wordsSpaceLength, endIndex);
    assertEquals(truncatedString.length(), ellipsLength + wordsSpaceLength);
    assertEquals(assumedEndTruncation, truncatedString);

    // should have all the search words
    truncatedString = textUtils.mostOccurences(testDescription, maxLength, testCaseStartMiddleEnd);
    assertEquals(
        truncatedString.length(),
        "start".length()
            + wordsSpaceLength
            + ellipsLength
            + wordsSpaceLength
            + "middle".length()
            + wordsSpaceLength
            + ellipsLength
            + wordsSpaceLength
            + "end".length());

    assertEquals(
        testDescription.substring(0, wordsSpaceLength + "start".length())
            + ELLIPSIS_ENTITY
            + testDescription.subSequence(
                middleIndex - "middle".length() - wordsSpaceLength, middleIndex + wordsSpaceLength)
            + ELLIPSIS_ENTITY
            + testDescription.substring(endIndex - wordsSpaceLength - "end".length(), endIndex),
        truncatedString);

    // empty terms, should truncate from start
    truncatedString = textUtils.mostOccurences(testDescription, maxLength, emptyTestCase);
    assertEquals(truncatedString.length(), maxLength + ellipsLength);
    assertEquals(testDescription.substring(0, maxLength) + ELLIPSIS_ENTITY, truncatedString);
  }
}
