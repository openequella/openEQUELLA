package com.tle.core.events.listeners;

import com.tle.core.events.UserSuspendEvent;

public interface UserSuspendListener extends ApplicationListener {
  void userSuspendEvent(UserSuspendEvent event);
}
