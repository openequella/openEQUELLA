package com.tle.core.facetedsearch.dao;

import com.google.inject.Singleton;
import com.tle.core.facetedsearch.bean.FacetedSearchClassification;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;

@Bind(FacetedSearchClassificationDao.class)
@Singleton
public class FacetedSearchClassificationDaoImpl
    extends GenericInstitionalDaoImpl<FacetedSearchClassification, Long>
    implements FacetedSearchClassificationDao {

  public FacetedSearchClassificationDaoImpl() {
    super(FacetedSearchClassification.class);
  }
}
