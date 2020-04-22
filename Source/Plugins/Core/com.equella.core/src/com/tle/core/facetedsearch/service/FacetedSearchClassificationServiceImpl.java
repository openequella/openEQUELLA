package com.tle.core.facetedsearch.service;

import com.tle.core.facetedsearch.bean.FacetedSearchClassification;
import com.tle.core.facetedsearch.dao.FacetedSearchClassificationDao;
import com.tle.core.guice.Bind;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind(FacetedSearchClassificationService.class)
@Singleton
public class FacetedSearchClassificationServiceImpl implements FacetedSearchClassificationService {
  @Inject private FacetedSearchClassificationDao facetedSearchClassificationDao;

  @Override
  public FacetedSearchClassification getClassificationById(long id) {
    return facetedSearchClassificationDao.getClassificationById((id));
  }

  @Override
  public List getAllClassifications() {
    return facetedSearchClassificationDao.getAllClassifications();
  }

  @Override
  public void addClassification(FacetedSearchClassification classification) {
    facetedSearchClassificationDao.addFacetedSearchClassification(classification);
  }

  @Override
  public void updateFacetedSearchClassification(FacetedSearchClassification classification) {
    facetedSearchClassificationDao.updateFacetedSearchClassification(classification);
  }

  @Override
  public void deleteClassification(FacetedSearchClassification facetedSearchClassification) {
    facetedSearchClassificationDao.deleteClassification(facetedSearchClassification);
  }
}
