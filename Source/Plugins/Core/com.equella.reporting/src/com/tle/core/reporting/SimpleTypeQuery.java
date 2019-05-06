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

package com.tle.core.reporting;

import com.tle.common.URLUtils;
import com.tle.reporting.MetadataBean;
import com.tle.reporting.MetadataBean.Definition;
import java.sql.ParameterMetaData;
import java.util.List;
import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

public abstract class SimpleTypeQuery implements QueryDelegate {
  protected static final int TYPE_STRING = 1;
  protected static final int TYPE_LONG = 2;
  protected static final int TYPE_INT = 3;
  protected static final int TYPE_DATE = 4;
  protected static final int TYPE_BOOLEAN = 5;
  private static final String DIVIDER_REGEX = "\\|"; // $NON-NLS-1$

  protected Definition addColumn(String name, int type, MetadataBean bean) {
    Definition d = new Definition();
    d.setLabel(name);
    d.setName(name);
    d.setType(type);
    String typename = null;
    switch (type) {
      case TYPE_STRING:
        typename = "String"; // $NON-NLS-1$
        break;
      case TYPE_INT:
        typename = "Integer"; // $NON-NLS-1$
        break;
      case TYPE_LONG:
        typename = "Long"; // $NON-NLS-1$
        break;
      case TYPE_DATE:
        typename = "Date"; // $NON-NLS-1$
        break;

      default:
        break;
    }
    d.setTypename(typename);
    bean.addDefinition(d);
    return d;
  }

  /*
   * (non-Javadoc)
   * @see
   * com.tle.core.reporting.QueryDelegate#getParamterMetadata(java.lang.String
   * , java.util.List)
   */
  @Override
  public IParameterMetaData getParameterMetadata(String query, List<Object> params)
      throws OdaException {
    MetadataBean bean = new MetadataBean();
    int paramcount = 1;
    String[] queryStrings = getQueryStrings(query, null);
    for (String queryString : queryStrings) {
      int sz = queryString.length();
      for (int i = 0; i < sz; i++) {
        char c = queryString.charAt(i);
        if (c == '?') {
          Definition definition =
              addColumn("param" + (paramcount++), TYPE_STRING, bean); // $NON-NLS-1$
          definition.setMode(ParameterMetaData.parameterModeIn);
        }
      }
    }
    return bean;
  }

  protected String[] getQueryStrings(String query, List<Object> params) {
    query = query.substring(query.indexOf(':') + 1);
    String[] queryStrings = query.split(DIVIDER_REGEX);
    int paramcount = 0;
    int i = 0;
    while (i < queryStrings.length) {
      String queryString = URLUtils.basicUrlDecode(queryStrings[i]);
      StringBuilder sbuf = new StringBuilder();
      int sz = queryString.length();
      for (int j = 0; j < sz; j++) {
        char c = queryString.charAt(j);
        if (c == '\\') {
          sbuf.append(queryString.charAt(++j));
        } else if (c == '?') {
          if (params != null) {
            sbuf.append(params.get(paramcount++));
          } else {
            sbuf.append('?');
          }
        } else {
          sbuf.append(c);
        }
      }
      queryStrings[i++] = sbuf.toString();
    }

    if (queryStrings.length == 0) {
      return new String[] {""}; // $NON-NLS-1$
    }
    return queryStrings;
  }
}
