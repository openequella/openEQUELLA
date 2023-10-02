/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
