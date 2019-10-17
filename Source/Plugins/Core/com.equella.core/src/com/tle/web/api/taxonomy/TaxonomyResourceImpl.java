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

package com.tle.web.api.taxonomy;

import com.dytech.edge.exceptions.WebException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.taxonomy.TaxonomyBean;
import com.tle.beans.taxonomy.TermBean;
import com.tle.common.Pair;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.taxonomy.SelectionRestriction;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.taxonomy.TaxonomyService;
import com.tle.core.taxonomy.TermResult;
import com.tle.core.taxonomy.TermService;
import com.tle.web.api.baseentity.serializer.BaseEntitySerializer;
import com.tle.web.api.entity.resource.AbstractBaseEntityResource;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import com.tle.web.api.taxonomy.interfaces.TaxonomyResource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@SuppressWarnings("nls")
@NonNullByDefault
@Bind(TaxonomyResource.class)
@Singleton
public class TaxonomyResourceImpl
    extends AbstractBaseEntityResource<Taxonomy, BaseEntitySecurityBean, TaxonomyBean>
    implements TaxonomyResource {

  private enum PrivCheck {
    VIEW,
    EDIT,
    DELETE
  }

  @Inject private TaxonomyService taxonomyService;
  @Inject private TermService termService;
  @Inject private InstitutionService institutionService;
  @Inject private TaxonomyBeanSerializer taxonomySerializer;

  /**
   * Search terms
   *
   * @param uuid
   * @param query
   * @param restriction
   * @param limit
   * @param searchfullterm
   * @return Response
   */
  @Override
  public Response searchTaxonomyTerms(
      String uuid, String query, String restriction, int limit, boolean searchfullterm) {
    ensureTaxonomy(uuid, PrivCheck.VIEW);

    SelectionRestriction restrict =
        (restriction == null
            ? SelectionRestriction.UNRESTRICTED
            : SelectionRestriction.valueOf(restriction.toUpperCase()));
    int max = (limit <= 0 ? 20 : limit);

    Pair<Long, List<TermResult>> searchTerms =
        taxonomyService.searchTerms(uuid, query, restrict, max, searchfullterm);

    List<TermBean> beans = Lists.newArrayList();
    for (TermResult term : searchTerms.getSecond()) {
      TermBean bean = beanFromTaxonomyTerm(term, uuid);
      beans.add(bean);
    }

    final SearchBean<TermBean> result = new SearchBean<>();
    result.setAvailable((int) (long) searchTerms.getFirst());
    result.setLength(beans.size());
    result.setResults(beans);
    return Response.ok(result).build();
  }

  /**
   * Returns terms
   *
   * @return Response encapsulating TermBeans
   */
  @Override
  public Response getTaxonomyTerms(String taxonomyUuid, String path) {
    ensureTaxonomy(taxonomyUuid, PrivCheck.VIEW);

    Collection<TermResult> terms = taxonomyService.getChildTerms(taxonomyUuid, path);
    final List<TermBean> beans = Lists.newArrayList();
    for (TermResult term : terms) {
      TermBean bean = beanFromTaxonomyTerm(term, taxonomyUuid);
      beans.add(bean);
    }
    return Response.ok(beans).build();
  }

  /**
   * Insert new term
   *
   * @param taxonomyUuid
   * @param termBean
   * @return
   */
  @Override
  public Response createTaxonomyTerm(String taxonomyUuid, TermBean termBean) {
    if (taxonomyService.isTaxonomyReadonly(taxonomyUuid)) {
      throw new WebException(
          Status.METHOD_NOT_ALLOWED.getStatusCode(),
          Status.METHOD_NOT_ALLOWED.getReasonPhrase(),
          "Taxonomy is readonly");
    }

    final Taxonomy taxonomy = ensureTaxonomy(taxonomyUuid, PrivCheck.EDIT);
    final String parentTermUuid = termBean.getParentUuid();
    TermResult parentTerm = null;
    if (parentTermUuid != null) {
      parentTerm = taxonomyService.getTermResultByUuid(taxonomyUuid, termBean.getParentUuid());
      if (parentTerm == null) {
        throw new WebException(
            Status.NOT_FOUND.getStatusCode(),
            Status.NOT_FOUND.getReasonPhrase(),
            "Parent term not found");
      }
    }

    int index = termBean.getIndex();

    if (index < 0) {
      index = 0;
    }

    try {
      TermResult termResult =
          termService.insertTerm(
              taxonomy, parentTerm, termBean.getUuid(), termBean.getTerm(), index);
      return Response.created(getTermUrl(taxonomyUuid, termResult.getUuid())).build();
    } catch (Exception e) {
      throw new WebException(
          Status.NOT_ACCEPTABLE.getStatusCode(),
          Status.NOT_ACCEPTABLE.getReasonPhrase(),
          e.getMessage());
    }
  }

  /**
   * Update term
   *
   * @param taxonomyUuid
   * @param termUuid
   * @param termBean
   * @return
   */
  @Override
  public Response updateTaxonomyTerm(String taxonomyUuid, String termUuid, TermBean termBean) {
    if (taxonomyService.isTaxonomyReadonly(taxonomyUuid)) {
      throw new WebException(
          Status.METHOD_NOT_ALLOWED.getStatusCode(),
          Status.METHOD_NOT_ALLOWED.getReasonPhrase(),
          "Taxonomy is readonly");
    }
    final Taxonomy taxonomy = ensureTaxonomy(taxonomyUuid, PrivCheck.EDIT);

    TermResult term = taxonomyService.getTermResultByUuid(taxonomyUuid, termUuid);
    TermResult parentTerm =
        taxonomyService.getTermResultByUuid(taxonomyUuid, termBean.getParentUuid());

    if (term != null) {
      int index = termBean.getIndex();
      if (index < 0) {
        index = 0;
      }
      if (term.getFullTerm() != termBean.getFullTerm()) {
        termService.move(taxonomy, term, parentTerm, index);
      }

      return Response.ok().location(getTermUrl(taxonomyUuid, termUuid)).build();
    } else {
      throw new WebException(
          Status.NOT_FOUND.getStatusCode(),
          Status.NOT_FOUND.getReasonPhrase(),
          "termUuid given is not valid");
    }
  }

  /**
   * Remove term
   *
   * @param taxonomyUuid
   * @param termUuid
   * @return
   */
  @Override
  public Response rmTaxonomyTerm(String taxonomyUuid, String termUuid) {
    if (taxonomyService.isTaxonomyReadonly(taxonomyUuid)) {
      throw new WebException(
          Status.METHOD_NOT_ALLOWED.getStatusCode(),
          Status.METHOD_NOT_ALLOWED.getReasonPhrase(),
          "Taxonomy is readonly");
    }

    final Taxonomy taxonomy = ensureTaxonomy(taxonomyUuid, PrivCheck.EDIT);

    TermResult term = taxonomyService.getTermResultByUuid(taxonomyUuid, termUuid);
    if (term == null) {
      throw new WebException(
          Status.NOT_FOUND.getStatusCode(),
          Status.NOT_FOUND.getReasonPhrase(),
          "termUuid given is not valid");
    }
    termService.deleteTerm(taxonomy, term.getFullTerm());

    return Response.ok().build();
  }

  /**
   * Returns terms
   *
   * @return Response encapsulating TermBeans
   */
  @Override
  public Response getTermByUuid(String taxonomyUuid, String termUuid) {
    ensureTaxonomy(taxonomyUuid, PrivCheck.VIEW);
    TermResult term = taxonomyService.getTermResultByUuid(taxonomyUuid, termUuid);

    if (term != null) {
      final TermBean bean = beanFromTaxonomyTerm(term, taxonomyUuid);
      return Response.ok(bean).build();
    } else {
      throw new WebException(
          Status.NOT_FOUND.getStatusCode(),
          Status.NOT_FOUND.getReasonPhrase(),
          "termUuid given is not valid");
    }
  }

  /**
   * Returns term's all data
   *
   * @param taxonomyUuid
   * @param termUuid
   * @return
   */
  @Override
  public Response getAllTermData(String taxonomyUuid, String termUuid) {
    ensureTaxonomy(taxonomyUuid, PrivCheck.VIEW);
    try {
      Map<String, String> data = taxonomyService.getAllDataByTermUuid(taxonomyUuid, termUuid);

      return Response.ok(data).build();
    } catch (IllegalArgumentException ex) {
      throw new WebException(
          Status.NOT_FOUND.getStatusCode(),
          Status.NOT_FOUND.getReasonPhrase(),
          "termUuid given is not valid");
    }
  }

  /**
   * Returns term's data value
   *
   * @param taxonomyUuid
   * @param termUuid
   * @param dataKey
   * @return
   */
  @Override
  public Response getTermDataByKey(String taxonomyUuid, String termUuid, String dataKey) {
    ensureTaxonomy(taxonomyUuid, PrivCheck.VIEW);

    String value;
    try {
      value = taxonomyService.getDataByTermUuid(taxonomyUuid, termUuid, dataKey);
    } catch (IllegalArgumentException ex) {
      throw new WebException(
          Status.NOT_FOUND.getStatusCode(),
          Status.NOT_FOUND.getReasonPhrase(),
          "termUuid given is not valid");
    }
    if (value == null) {
      return Response.status(Status.NOT_FOUND).build();
    } else {
      Map<String, String> data = Maps.newHashMap();
      data.put(dataKey, value);
      return Response.ok(data).build();
    }
  }

  /**
   * Set term data value
   *
   * @param taxonomyUuid
   * @param termUuid
   * @param dataKey
   * @return
   */
  @Override
  public Response setTermData(
      String taxonomyUuid, String termUuid, String dataKey, String dataValue) {
    if (taxonomyService.isTaxonomyReadonly(taxonomyUuid)) {
      throw new WebException(
          Status.METHOD_NOT_ALLOWED.getStatusCode(),
          Status.METHOD_NOT_ALLOWED.getReasonPhrase(),
          "Taxonomy is readonly");
    }
    final Taxonomy taxonomy = ensureTaxonomy(taxonomyUuid, PrivCheck.EDIT);
    try {
      termService.setDataByTermUuid(taxonomy, termUuid, dataKey, dataValue);
    } catch (IllegalArgumentException ex) {
      throw new WebException(
          Status.NOT_FOUND.getStatusCode(),
          Status.NOT_FOUND.getReasonPhrase(),
          "termUuid given is not valid");
    }

    return Response.ok().build();
  }

  /**
   * Remove term data
   *
   * @param taxonomyUuid
   * @param termUuid
   * @param dataKey
   * @return
   */
  @Override
  public Response deleteTermData(String taxonomyUuid, String termUuid, String dataKey) {
    if (taxonomyService.isTaxonomyReadonly(taxonomyUuid)) {
      throw new WebException(
          Status.METHOD_NOT_ALLOWED.getStatusCode(),
          Status.METHOD_NOT_ALLOWED.getReasonPhrase(),
          "Taxonomy is readonly");
    }
    final Taxonomy taxonomy = ensureTaxonomy(taxonomyUuid, PrivCheck.EDIT);
    try {
      termService.setDataByTermUuid(taxonomy, termUuid, dataKey, null);
    } catch (IllegalArgumentException ex) {
      throw new WebException(
          Status.NOT_FOUND.getStatusCode(),
          Status.NOT_FOUND.getReasonPhrase(),
          "termUuid given is not valid");
    }

    return Response.ok().build();
  }

  /**
   * Returns terms
   *
   * @return OK
   */
  @Override
  public Response sortChildTerms(String taxonomyUuid, String path) {
    final Taxonomy taxonomy = ensureTaxonomy(taxonomyUuid, PrivCheck.VIEW);

    termService.sortChildren(taxonomy, path);
    return Response.ok().build();
  }

  private TermBean beanFromTaxonomyTerm(TermResult term, String taxonomyUuid) {
    TermBean bean = new TermBean();
    bean.setTerm(term.getTerm());
    bean.setFullTerm(term.getFullTerm());
    bean.setUuid(term.getUuid());
    Map<String, String> links = Maps.newHashMap();
    links.put("self", getTermUrl(taxonomyUuid, term.getUuid()).toString());
    return bean;
  }

  private URI getTermUrl(String taxonomyUuid, String termUuid) {
    try {
      String url =
          institutionService.institutionalise("api/taxonomy/" + taxonomyUuid + "/term/" + termUuid);
      return new URI(url);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private Taxonomy ensureTaxonomy(String taxonomyUuid, PrivCheck pc) {
    final Taxonomy taxonomy = taxonomyService.getByUuid(taxonomyUuid);
    if (taxonomy == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    if ((pc == PrivCheck.VIEW && !taxonomyService.canView(taxonomy))
        || (pc == PrivCheck.EDIT && !taxonomyService.canEdit(taxonomy))
        || (pc == PrivCheck.DELETE && !taxonomyService.canDelete(taxonomy))) {
      throw new WebApplicationException(Status.FORBIDDEN);
    }
    return taxonomy;
  }

  @Override
  protected Node[] getAllNodes() {
    return new Node[] {Node.ALL_TAXONOMIES};
  }

  @Override
  protected BaseEntitySecurityBean createAllSecurityBean() {
    return new BaseEntitySecurityBean();
  }

  @Override
  public AbstractEntityService<?, Taxonomy> getEntityService() {
    return taxonomyService;
  }

  @Override
  protected BaseEntitySerializer<Taxonomy, TaxonomyBean> getSerializer() {
    return taxonomySerializer;
  }

  @Override
  protected Class<?> getResourceClass() {
    return TaxonomyResource.class;
  }
}
