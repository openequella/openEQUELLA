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

package com.tle.hibernate.dialect;

import java.util.HashMap;
import java.util.Map;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitBasicColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitForeignKeyNameSource;
import org.hibernate.boot.model.naming.ImplicitJoinTableNameSource;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the naming strategy prior to the values being sent to the physical naming strategy logic.
 *
 * <p>Currently only deals with ensuring legacy join table and column names are handled
 * appropriately.
 */
public class OeqImplicitNamingStrategy extends ImplicitNamingStrategyJpaCompliantImpl {
  private static final Logger LOGGER = LoggerFactory.getLogger(OeqImplicitNamingStrategy.class);

  private final Map<String, String> joinTableOverrides = new HashMap<>();

  public OeqImplicitNamingStrategy() {
    // The default strategies for join tables are not sufficient for all cases in oEQ.
    // There doesn't appear to be a pattern for the discrepancies, so this override
    // map contains all the special cases.
    joinTableOverrides.put("PortletRecentContrib.ItemDefinition", "portlet_recent_contrib_collect");
    joinTableOverrides.put("Item.HistoryEvent", "item_history");
    joinTableOverrides.put("Item.ReferencedURL", "item_referenced_urls");
    joinTableOverrides.put("TLEGroup.TLEGroup", "tlegroup_all_parents");
    joinTableOverrides.put("HierarchyTopic.HierarchyTopic", "hierarchy_topic_all_parents");
    joinTableOverrides.put("PowerSearch.ItemDefinition", "power_search_itemdefs");
  }

  @Override
  public Identifier determineJoinTableName(ImplicitJoinTableNameSource source) {
    Identifier resp;
    final String joinKey =
        source.getOwningPhysicalTableName() + "." + source.getNonOwningPhysicalTableName();
    if (joinTableOverrides.containsKey(joinKey)) {
      resp = Identifier.toIdentifier(joinTableOverrides.get(joinKey));
    } else {
      resp = super.determineJoinTableName(source);
    }
    if (LOGGER.isTraceEnabled()) {
      final String respText = (resp == null) ? "NULL" : resp.getText();
      LOGGER.trace(
          String.format(
              "determineJoinTableName - %s (%s), %s (%s) - Result=[%s]",
              source.getOwningEntityNaming().getEntityName(),
              source.getOwningEntityNaming().getJpaEntityName(),
              source.getNonOwningEntityNaming().getEntityName(),
              source.getNonOwningEntityNaming().getJpaEntityName(),
              respText));
    }
    return resp;
  }

  @Override
  public Identifier determineBasicColumnName(ImplicitBasicColumnNameSource source) {
    Identifier resp;
    // Needed to map a few column names due to legacy oEQ code
    if (source.getAttributePath().getFullPath().equals("impTransforms.collection&&element.filename")
        || source
            .getAttributePath()
            .getFullPath()
            .equals("expTransforms.collection&&element.filename")) {
      resp = Identifier.toIdentifier("fil");
    } else if (source
            .getAttributePath()
            .getFullPath()
            .equals("impTransforms.collection&&element.type")
        || source
            .getAttributePath()
            .getFullPath()
            .equals("expTransforms.collection&&element.type")) {
      resp = Identifier.toIdentifier("typ");
    } else if (source
        .getAttributePath()
        .getFullPath()
        .equals("citations.collection&&element.transformation")) {
      resp = Identifier.toIdentifier("transfo");
    } else {
      resp = super.determineBasicColumnName(source);
    }

    if (LOGGER.isTraceEnabled()) {
      final String respText = (resp == null) ? "NULL" : resp.getText();
      LOGGER.trace(
          "determineBasicColumnName - "
              + source.getAttributePath().getFullPath()
              + " - result: ["
              + respText
              + "]");
    }
    return resp;
  }

  @Override
  public Identifier determineForeignKeyName(ImplicitForeignKeyNameSource source) {
    Identifier resp = super.determineForeignKeyName(source);
    if (LOGGER.isTraceEnabled()) {
      final String respText = (resp == null) ? "NULL" : resp.getText();
      LOGGER.trace(
          "determineForeignKeyName - "
              + source.getReferencedTableName()
              + ", "
              + source.getReferencedTableName().getText()
              + ", Result="
              + respText);
    }
    return resp;
  }
}
