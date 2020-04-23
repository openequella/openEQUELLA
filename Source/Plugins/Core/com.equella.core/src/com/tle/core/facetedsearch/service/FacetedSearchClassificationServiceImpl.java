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
