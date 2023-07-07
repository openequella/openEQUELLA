package com.tle.freetext;

import java.io.File;

public interface FreetextIndexConfiguration {
  File getIndexPath();

  String getDefaultOperator();

  int getSynchroniseMinutes();

  File getStopWordsFile();

  String getAnalyzerLanguage();
}
