package com.ghostchu.quickshop.api.event.economy;

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * Called exactly once when a transaction that reached the pre-transaction event does not succeed.
 */
public class ShopTransactionFailedEvent extends AbstractQSEvent {

  private final ShopTransactionContext context;
  private final ShopTransactionFailureReason reason;
  private final String errorMessage;
  private final Throwable cause;

  /**
   * Creates a failed transaction event.
   *
   * @param context      the shared transaction context
   * @param reason       the terminal failure category
   * @param errorMessage an optional provider or exception detail for diagnostics
   * @param cause        the optional exception that interrupted the transaction
   */
  public ShopTransactionFailedEvent(
          @NotNull final ShopTransactionContext context,
          @NotNull final ShopTransactionFailureReason reason,
          @Nullable final String errorMessage,
          @Nullable final Throwable cause) {

    this.context = Objects.requireNonNull(context, "context");
    this.reason = Objects.requireNonNull(reason, "reason");
    this.errorMessage = errorMessage;
    this.cause = cause;
  }

  /**
   * @return the shared transaction context
   */
  public @NotNull ShopTransactionContext getContext() {

    return context;
  }

  /**
   * @return the transaction id shared with the pre-transaction event
   */
  public @NotNull UUID getTransactionId() {

    return context.getTransactionId();
  }

  /**
   * @return the terminal failure category
   */
  public @NotNull ShopTransactionFailureReason getReason() {

    return reason;
  }

  /**
   * @return an optional diagnostic detail
   */
  public @Nullable String getErrorMessage() {

    return errorMessage;
  }

  /**
   * @return the optional exception that interrupted the transaction
   */
  public @Nullable Throwable getCause() {

    return cause;
  }
}
