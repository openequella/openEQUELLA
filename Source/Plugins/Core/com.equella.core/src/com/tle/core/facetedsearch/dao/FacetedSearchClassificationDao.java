package com.tle.core.facetedsearch.dao;

import com.tle.core.facetedsearch.bean.FacetedSearchClassification;
import com.tle.core.hibernate.dao.GenericDao;
import java.util.List;

public interface FacetedSearchClassificationDao
    extends GenericDao<FacetedSearchClassification, Long> {

  FacetedSearchClassification getClassificationById(long id);

  List getAllClassifications();

  void deleteClassification(FacetedSearchClassification facetedSearchClassification);

  void addFacetedSearchClassification(FacetedSearchClassification facetedSearchClassification);

  void updateFacetedSearchClassification(FacetedSearchClassification facetedSearchClassification);
}
