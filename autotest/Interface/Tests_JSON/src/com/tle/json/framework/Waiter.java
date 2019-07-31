package com.tle.json.framework;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import java.util.concurrent.TimeUnit;

public class Waiter<T> {
  private final T obj;
  private long timeout = TimeUnit.SECONDS.toMillis(60);

  public Waiter(T obj) {
    this.obj = obj;
  }

  public Waiter<T> withTimeout(long duration, TimeUnit unit) {
    this.timeout = unit.toMillis(duration);
    return this;
  }

  public void until(final Predicate<T> func) throws TimeoutException {
    until(
        new Function<T, Boolean>() {
          @Override
          public Boolean apply(T input) {
            return func.apply(input);
          }
        });
  }

  public <U> U until(Function<T, U> func) throws TimeoutException {
    long end = System.currentTimeMillis() + timeout;

    while (System.currentTimeMillis() < end) {
      U value = func.apply(obj);
      if (value instanceof Boolean) {
        if (Boolean.TRUE.equals(value)) {
          return value;
        }
      } else if (value != null) {
        return value;
      }
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {

      }
    }
    throw new TimeoutException("Timed out after " + timeout + "ms.");
  }
}
