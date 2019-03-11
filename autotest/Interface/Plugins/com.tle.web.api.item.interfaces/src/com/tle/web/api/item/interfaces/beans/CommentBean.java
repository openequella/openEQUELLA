package com.tle.web.api.item.interfaces.beans;

import com.tle.web.api.interfaces.beans.AbstractExtendableBean;
import com.tle.web.api.interfaces.beans.UserBean;
import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CommentBean extends AbstractExtendableBean {
  private String uuid;
  private int rating;
  private boolean anonymous;
  private String comment;
  private UserBean postedBy;
  private Date postedDate;

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public int getRating() {
    return rating;
  }

  public void setRating(int rating) {
    this.rating = rating;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public Date getPostedDate() {
    return postedDate;
  }

  public void setPostedDate(Date postedDate) {
    this.postedDate = postedDate;
  }

  public boolean isAnonymous() {
    return anonymous;
  }

  public void setAnonymous(boolean anonymous) {
    this.anonymous = anonymous;
  }

  public UserBean getPostedBy() {
    return postedBy;
  }

  public void setPostedBy(UserBean postedBy) {
    this.postedBy = postedBy;
  }
}
