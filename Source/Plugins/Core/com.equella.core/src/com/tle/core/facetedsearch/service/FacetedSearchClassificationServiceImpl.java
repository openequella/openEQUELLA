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

package com.tle.core.facetedsearch.service;

import com.tle.core.facetedsearch.bean.FacetedSearchClassification;
import com.tle.core.facetedsearch.dao.FacetedSearchClassificationDao;
import com.tle.core.guice.Bind;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.springframework.transaction.annotation.Transactional;

@Bind(FacetedSearchClassificationService.class)
@Singleton
public class FacetedSearchClassificationServiceImpl implements FacetedSearchClassificationService {
  @Inject private FacetedSearchClassificationDao facetedSearchClassificationDao;

  @Override
  public FacetedSearchClassification getById(long id) {
    return facetedSearchClassificationDao.findById(id);
  }

  @Override
  public List enumerateAll() {
    return facetedSearchClassificationDao.enumerateAll();
  }

  @Transactional
  @Override
  public void add(FacetedSearchClassification classification) {
    facetedSearchClassificationDao.save(classification);
  }

  @Transactional
  @Override
  public void update(FacetedSearchClassification classification) {
    facetedSearchClassificationDao.merge(classification);
  }

  @Transactional
  @Override
  public void delete(FacetedSearchClassification facetedSearchClassification) {
    facetedSearchClassificationDao.delete(facetedSearchClassification);
  }
}
