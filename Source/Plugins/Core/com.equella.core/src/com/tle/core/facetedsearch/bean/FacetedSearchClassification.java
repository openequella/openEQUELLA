package com.tle.core.facetedsearch.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.hibernate.annotations.AccessType;

@Entity
@AccessType("field")
public class FacetedSearchClassification {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @Column(nullable = false)
  private long institutionId;

  @Column @JsonIgnore private Date dateCreated;

  @Column @JsonIgnore private Date dateModified;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String schemaNode;

  private int maxResults;

  private int orderIndex;

  public long getId() {
    return id;
  }

  public void setInstitution_id(long institutionId) {
    this.institutionId = institutionId;
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
