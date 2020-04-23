package com.tle.core.facetedsearch.service;

import com.tle.core.facetedsearch.bean.FacetedSearchClassification;
import java.util.List;

public interface FacetedSearchClassificationService {
  FacetedSearchClassification getById(long id);

  List enumerateAll();

  void delete(FacetedSearchClassification facetedSearchClassification);

  void add(FacetedSearchClassification facetedSearchClassification);

  void update(FacetedSearchClassification facetedSearchClassification);
}
