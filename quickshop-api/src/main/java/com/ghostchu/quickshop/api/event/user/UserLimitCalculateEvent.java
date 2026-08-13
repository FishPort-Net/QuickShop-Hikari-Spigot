package com.ghostchu.quickshop.api.event.user;

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import org.jetbrains.annotations.NotNull;

/** Allows integrations to adjust the calculated shop limit for a user. */
public class UserLimitCalculateEvent extends AbstractQSEvent {

  private final QUser user;
  private int limit;

  public UserLimitCalculateEvent(@NotNull final QUser user, final int limit) {

    this.user = user;
    this.limit = limit;
  }

  public @NotNull QUser user() {

    return user;
  }

  public int limit() {

    return limit;
  }

  public void limit(final int limit) {

    this.limit = limit;
  }
}
