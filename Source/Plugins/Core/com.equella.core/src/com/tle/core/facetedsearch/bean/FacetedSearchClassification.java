package com.tle.core.facetedsearch.bean;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.Institution;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

@Entity
@AccessType("field")
public class FacetedSearchClassification {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @JoinColumn(nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  @Index(name = "facetSearchClassificationInstitutionIndex")
  @XStreamOmitField
  private Institution institution;

  @Column private Date dateCreated;

  @Column private Date dateModified;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String schemaNode;

  private int maxResults;

  private int orderIndex;

  public void setInstitution(Institution institution) {
    this.institution = institution;
  }

  public long getId() {
    return id;
  }

  public Date getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(Date dateCreated) {
    this.dateCreated = dateCreated;
  }

  public Date getDateModified() {
    return dateModified;
  }

  public void setDateModified(Date dateModified) {
    this.dateModified = dateModified;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSchemaNode() {
    return schemaNode;
  }

  public void setSchemaNode(String schemaNode) {
    this.schemaNode = schemaNode;
  }

  public int getMaxResults() {
    return maxResults;
  }

  public void setMaxResults(int maxResults) {
    this.maxResults = maxResults;
  }

  public int getOrderIndex() {
    return orderIndex;
  }

  public void setOrderIndex(int orderIndex) {
    this.orderIndex = orderIndex;
  }
}
