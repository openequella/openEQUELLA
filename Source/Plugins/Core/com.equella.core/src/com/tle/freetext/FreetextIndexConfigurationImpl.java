package com.tle.freetext;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.tle.core.guice.Bind;
import java.io.File;
import javax.inject.Singleton;

@Bind(FreetextIndexConfiguration.class)
@Singleton
public class FreetextIndexConfigurationImpl implements FreetextIndexConfiguration {

  @Inject
  @Named("freetext.index.location")
  File indexPath;

  @Inject(optional = true)
  @Named("freetextIndex.defaultOperator")
  private String defaultOperator = "AND";

  @Inject(optional = true)
  @Named("freetextIndex.synchroiseMinutes")
  private int synchroniseMinutes = 5;

  @Inject
  @Named("freetext.stopwords.file")
  private File stopWordsFile;

  @Inject
  @Named("freetext.analyzer.language")
  private String analyzerLanguage;

  @Override
  public File getIndexPath() {
    return indexPath;
  }

  @Override
  public String getDefaultOperator() {
    return defaultOperator;
  }

  @Override
  public int getSynchroniseMinutes() {
    return synchroniseMinutes;
  }

  @Override
  public File getStopWordsFile() {
    return stopWordsFile;
  }

  @Override
  public String getAnalyzerLanguage() {
    return analyzerLanguage;
  }
}
