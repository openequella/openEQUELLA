package com.tle.core.freetext.index;

import com.tle.beans.item.ItemIdKey;
import com.tle.core.services.item.FreetextResult;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.junit.Assert;
import org.junit.Test;

public class LanguageConfigurationTest {
  private static final String GERMAN_ANALYSER_PACKAGE =
      "org.apache.lucene.analysis.de.GermanAnalyzer";
  private static final String DEFAULT_ANALYSER_PACKAGE = "com.tle.freetext.TLEAnalyzer";

  @Test
  public void languageConfigurationTest() {
    // English is the default language so the default analyser is TLEAnylyzer
    Analyzer defaultAnalyser = getAnalyser("en");
    Assert.assertTrue(defaultAnalyser.toString().contains(DEFAULT_ANALYSER_PACKAGE));

    Analyzer germanAnalyser = getAnalyser("de");
    // If ClassGraph is working, the analyser for German is GermanAnalyser.
    Assert.assertTrue(germanAnalyser.toString().contains(GERMAN_ANALYSER_PACKAGE));
  }

  private Analyzer getAnalyser(String language) {
    TestIndexEngine testIndexEngine = new TestIndexEngine();
    testIndexEngine.setAnalyzerLanguage(language);
    return testIndexEngine.getAnalyser();
  }

  class TestIndexEngine extends ItemIndex<FreetextResult> {
    @Override
    protected FreetextResult createResult(
        ItemIdKey key, Document doc, float relevance, boolean sortByRelevance) {
      return new FreetextResult(key, relevance, sortByRelevance);
    }
  }
}
