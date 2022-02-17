package com.tle.beans.viewcount;

import java.time.Instant;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractViewcount {
  private int count;
  private Instant lastViewed;

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public Instant getLastViewed() {
    return lastViewed;
  }

  public void setLastViewed(Instant lastViewed) {
    this.lastViewed = lastViewed;
  }
}
