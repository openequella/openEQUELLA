package com.tle.core.events;

import com.tle.core.events.listeners.UserSuspendListener;
import java.util.Set;

public class UserSuspendEvent extends ApplicationEvent<UserSuspendListener> {
  private static final long serialVersionUID = 1L;

  private final Set<String> suspendedUserId;

  public UserSuspendEvent(Set<String> suspendedUserId) {
    super(PostTo.POST_TO_ALL_CLUSTER_NODES);

    this.suspendedUserId = suspendedUserId;
  }

  public Set<String> getSuspendedUserId() {
    return suspendedUserId;
  }

  @Override
  public Class<UserSuspendListener> getListener() {
    return UserSuspendListener.class;
  }

  @Override
  public void postEvent(UserSuspendListener listener) {
    listener.userSuspendEvent(this);
  }
}
