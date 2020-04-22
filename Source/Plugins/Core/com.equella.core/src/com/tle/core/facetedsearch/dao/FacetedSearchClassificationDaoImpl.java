package com.tle.core.facetedsearch.dao;

import com.google.inject.Singleton;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.facetedsearch.bean.FacetedSearchClassification;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

@Bind(FacetedSearchClassificationDao.class)
@Singleton
public class FacetedSearchClassificationDaoImpl
    extends GenericDaoImpl<FacetedSearchClassification, Long>
    implements FacetedSearchClassificationDao {

  public FacetedSearchClassificationDaoImpl() {
    super(FacetedSearchClassification.class);
  }

  @Override
  public FacetedSearchClassification getClassificationById(long id) {
    return (FacetedSearchClassification)
        getHibernateTemplate()
            .execute(
                new HibernateCallback() {
                  @Override
                  public Object doInHibernate(Session session) {
                    Query query =
                        session.createQuery(
                            "FROM FacetedSearchClassification WHERE id = :id AND institution_id = :institutionId");
                    query.setParameter("id", id);
                    query.setParameter("institutionId", CurrentInstitution.get().getUniqueId());
                    return query.uniqueResult();
                  }
                });
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<FacetedSearchClassification> getAllClassifications() {
    return getHibernateTemplate()
        .find(
            "from FacetedSearchClassification where institution_id = ?",
            CurrentInstitution.get().getUniqueId());
  }

  @Transactional
  @Override
  public void deleteClassification(FacetedSearchClassification facetedSearchClassification) {
    delete(facetedSearchClassification);
  }

  @Transactional
  @Override
  public void addFacetedSearchClassification(
      FacetedSearchClassification facetedSearchClassification) {
    save(facetedSearchClassification);
  }

  @Transactional
  @Override
  public void updateFacetedSearchClassification(
      FacetedSearchClassification facetedSearchClassification) {
    merge(facetedSearchClassification);
  }
}
