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

package com.tle.beans.hierarchy;

import com.tle.beans.Institution;
import com.tle.beans.ItemDefinitionScript;
import com.tle.beans.SchemaScript;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.PowerSearch;
import com.tle.common.Check;
import com.tle.common.Check.FieldEquality;
import com.tle.common.institution.TreeNodeInterface;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.validation.constraints.Min;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.IndexColumn;

/** @author Nicholas Read */
@Entity
@AccessType("field")
public class HierarchyTopic
    implements TreeNodeInterface<HierarchyTopic>, FieldEquality<HierarchyTopic> {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @Column(length = 40, nullable = false)
  @Index(name = "hierarchyUuidIndex")
  private String uuid;

  @JoinColumn(nullable = false)
  @Index(name = "hierarchyInstitutionIndex")
  @ManyToOne(fetch = FetchType.LAZY)
  private Institution institution;

  @ManyToOne(fetch = FetchType.LAZY)
  @Index(name = "parentTopic")
  private HierarchyTopic parent;

  @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
  @OrderBy("listPosition")
  private List<HierarchyTopic> subTopics = new ArrayList<HierarchyTopic>();

  @ManyToMany(fetch = FetchType.LAZY)
  @IndexColumn(name = "list_position")
  private List<HierarchyTopic> allParents = new ArrayList<HierarchyTopic>();

  @Min(0)
  private int listPosition;

  @ManyToOne(fetch = FetchType.LAZY)
  @Index(name = "hiearchyPowerSearch")
  private PowerSearch advancedSearch;

  private boolean showResults = true;

  @Column(length = 100)
  private String freetext;

  private boolean inheritFreetext;

  @ElementCollection(fetch = FetchType.LAZY)
  @Cascade(org.hibernate.annotations.CascadeType.ALL)
  private List<ItemDefinitionScript> addIdefs;

  @ElementCollection(fetch = FetchType.LAZY)
  @Cascade(org.hibernate.annotations.CascadeType.ALL)
  private List<SchemaScript> addSchemas;

  @ElementCollection(fetch = FetchType.LAZY)
  @Cascade(org.hibernate.annotations.CascadeType.ALL)
  private List<ItemDefinitionScript> inhIdefs;

  @ElementCollection(fetch = FetchType.LAZY)
  @Cascade(org.hibernate.annotations.CascadeType.ALL)
  private List<SchemaScript> inhSchemas;

  @Column(length = 100)
  private String virtualisationId;

  @Column(length = 255)
  private String virtualisationPath;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @Index(name = "hierarchyName")
  private LanguageBundle name;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @Index(name = "hierarchyShortDescription")
  private LanguageBundle shortDescription;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @Index(name = "hierarchyLongDescription")
  private LanguageBundle longDescription;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @Index(name = "hierarchySName")
  private LanguageBundle subtopicsSectionName;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @Index(name = "hierarchyRSName")
  private LanguageBundle resultsSectionName;

  private Boolean hideSubtopicsWithNoResults = Boolean.TRUE;

  @JoinColumn
  @ElementCollection(fetch = FetchType.EAGER)
  @Fetch(value = FetchMode.SUBSELECT)
  @JoinTable(name = "HierarchyTopic_attributes")
  private List<Attribute> attributes;

  public HierarchyTopic() {
    super();
  }

  @Override
  public long getId() {
    return id;
  }

  @Override
  public void setId(long id) {
    this.id = id;
  }

  @Override
  public String getUuid() {
    return uuid;
  }

  @Override
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  @Override
  public HierarchyTopic getParent() {
    return parent;
  }

  @Override
  public void setParent(HierarchyTopic parent) {
    this.parent = parent;
  }

  public List<HierarchyTopic> getSubTopics() {
    return subTopics;
  }

  @Override
  public List<HierarchyTopic> getAllParents() {
    return allParents;
  }

  @Override
  public void setAllParents(List<HierarchyTopic> allParents) {
    this.allParents = allParents;
  }

  @Override
  public boolean equals(Object obj) {
    return Check.commonEquals(this, obj);
  }

  @Override
  public boolean checkFields(HierarchyTopic rhs) {
    return Objects.equals(id, rhs.id);
  }

  @Override
  public int hashCode() {
    return Long.valueOf(id).hashCode();
  }

  public int getListPosition() {
    return listPosition;
  }

  public void setListPosition(int position) {
    this.listPosition = position;
  }

  public String getFreetext() {
    return freetext;
  }

  public void setFreetext(String freetext) {
    this.freetext = freetext;
  }

  public boolean isInheritFreetext() {
    return inheritFreetext;
  }

  public void setInheritFreetext(boolean inheritFreetext) {
    this.inheritFreetext = inheritFreetext;
  }

  @Override
  public Institution getInstitution() {
    return institution;
  }

  @Override
  public void setInstitution(Institution institution) {
    this.institution = institution;
  }

  public PowerSearch getAdvancedSearch() {
    return advancedSearch;
  }

  public void setAdvancedSearch(PowerSearch advancedSearch) {
    this.advancedSearch = advancedSearch;
  }

  public List<ItemDefinitionScript> getAdditionalItemDefs() {
    return addIdefs;
  }

  public void setAdditionalItemDefs(List<ItemDefinitionScript> additionalItemDef) {
    this.addIdefs = additionalItemDef;
  }

  public List<SchemaScript> getAdditionalSchemas() {
    return addSchemas;
  }

  public void setAdditionalSchemas(List<SchemaScript> additionalSchema) {
    this.addSchemas = additionalSchema;
  }

  public List<ItemDefinitionScript> getInheritedItemDefs() {
    return inhIdefs;
  }

  public void setInheritedItemDefs(List<ItemDefinitionScript> inheritedItemDef) {
    this.inhIdefs = inheritedItemDef;
  }

  public List<SchemaScript> getInheritedSchemas() {
    return inhSchemas;
  }

  public void setInheritedSchemas(List<SchemaScript> inheritedSchema) {
    this.inhSchemas = inheritedSchema;
  }

  public String getVirtualisationId() {
    return virtualisationId;
  }

  public void setVirtualisationId(String virtualisationId) {
    this.virtualisationId = virtualisationId;
  }

  public String getVirtualisationPath() {
    return virtualisationPath;
  }

  public void setVirtualisationPath(String virtualisationPath) {
    this.virtualisationPath = virtualisationPath;
  }

  public boolean isShowResults() {
    return showResults;
  }

  public void setShowResults(boolean showResults) {
    this.showResults = showResults;
  }

  public LanguageBundle getLongDescription() {
    return longDescription;
  }

  public void setLongDescription(LanguageBundle longDescription) {
    this.longDescription = longDescription;
  }

  public LanguageBundle getName() {
    return name;
  }

  public void setName(LanguageBundle name) {
    this.name = name;
  }

  public LanguageBundle getResultsSectionName() {
    return resultsSectionName;
  }

  public void setResultsSectionName(LanguageBundle resultsSectionName) {
    this.resultsSectionName = resultsSectionName;
  }

  public LanguageBundle getShortDescription() {
    return shortDescription;
  }

  public void setShortDescription(LanguageBundle shortDescription) {
    this.shortDescription = shortDescription;
  }

  public LanguageBundle getSubtopicsSectionName() {
    return subtopicsSectionName;
  }

  public void setSubtopicsSectionName(LanguageBundle subtopicsSectionName) {
    this.subtopicsSectionName = subtopicsSectionName;
  }

  public boolean isHideSubtopicsWithNoResults() {
    return hideSubtopicsWithNoResults != null && hideSubtopicsWithNoResults;
  }

  public void setHideSubtopicsWithNoResults(boolean hideSubtopicsWithNoResults) {
    this.hideSubtopicsWithNoResults = hideSubtopicsWithNoResults;
  }

  public String getAttribute(String key) {
    if (attributes != null) {
      for (Attribute att : attributes) {
        if (att.getKey().equals(key)) {
          return att.getValue();
        }
      }
    }
    return null;
  }

  public void setAttribute(String key, String value) {
    if (attributes == null) {
      attributes = new ArrayList<Attribute>();
    } else {
      removeAttribute(key);
    }
    attributes.add(new Attribute(key, value));
  }

  public void removeAttribute(String key) {
    if (attributes != null) {
      for (final Iterator<Attribute> iter = attributes.iterator(); iter.hasNext(); ) {
        final Attribute att = iter.next();
        if (att.getKey().equals(key)) {
          iter.remove();
        }
      }
    }
  }

  public List<Attribute> getAttributes() {
    return attributes;
  }

  public boolean isVirtual() {
    return !Check.isEmpty(virtualisationPath);
  }

  @Embeddable
  @AccessType("field")
  public static class Attribute implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(length = 64, nullable = false)
    private String key;

    @Column(name = "value", length = 1024)
    private String value;

    public Attribute() {
      super();
    }

    public Attribute(String key, String value) {
      this.value = value;
      this.key = key;
    }

    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }
}
