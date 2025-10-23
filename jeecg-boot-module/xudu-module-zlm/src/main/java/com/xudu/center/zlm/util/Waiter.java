package com.xudu.center.zlm.util;

import java.time.Instant;
import java.util.function.BooleanSupplier;

public final class Waiter {
  private Waiter(){}
  public static void waitOrThrow(BooleanSupplier ready, int waitMs, int intervalMs, String err) {
    Instant end = Instant.now().plusMillis(Math.max(waitMs, 1));
    while (Instant.now().isBefore(end)) {
      try { if (ready.getAsBoolean()) return; } catch (Exception ignored) {}
      sleep(intervalMs);
    }
    throw new RuntimeException(err);
  }
  public static void waitSilently(BooleanSupplier ready, int waitMs, int intervalMs) {
    Instant end = Instant.now().plusMillis(Math.max(waitMs, 1));
    while (Instant.now().isBefore(end)) {
      if (ready.getAsBoolean()) return;
      sleep(intervalMs);
    }
  }
  private static void sleep(int ms){ try { Thread.sleep(Math.max(ms,1)); } catch (InterruptedException ignored) {} }
}
