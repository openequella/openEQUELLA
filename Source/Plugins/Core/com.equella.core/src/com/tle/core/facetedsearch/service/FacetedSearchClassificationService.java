package com.tle.core.facetedsearch.service;

import com.tle.core.facetedsearch.bean.FacetedSearchClassification;
import java.util.List;

public interface FacetedSearchClassificationService {
  FacetedSearchClassification getClassificationById(long id);

  List getAllClassifications();

  void deleteClassification(FacetedSearchClassification facetedSearchClassification);

  void addClassification(FacetedSearchClassification facetedSearchClassification);

  void updateFacetedSearchClassification(FacetedSearchClassification facetedSearchClassification);
}
