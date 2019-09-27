package com.tle.beans.user;

import com.tle.common.usermanagement.user.valuebean.UserBean;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.AccessType;

@Entity
@AccessType("field")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"username", "institutionId"})})
public class UserInfoBackup implements UserBean {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @Column(nullable = false)
  private String username;

  @Column private String firstName;

  @Column private String lastName;

  @Column private String emailAddress;

  @Column(nullable = false)
  private long institutionId;

  @Column(nullable = false)
  private String uniqueId;

  public void setUsername(String username) {
    this.username = username;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public void setInstitution_id(long institutionId) {
    this.institutionId = institutionId;
  }

  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public String getFirstName() {
    return firstName;
  }

  @Override
  public String getLastName() {
    return lastName;
  }

  @Override
  public String getEmailAddress() {
    return emailAddress;
  }

  @Override
  public String getUniqueID() {
    return uniqueId;
  }
}
