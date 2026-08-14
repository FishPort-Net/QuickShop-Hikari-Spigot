package com.ghostchu.quickshop.api.event.economy;

/**
 * The terminal reason for a shop transaction that did not complete successfully.
 */
public enum ShopTransactionFailureReason {
  /** A plugin cancelled the pre-transaction event. */
  CANCELLED,
  /** The account funding the transaction did not have enough money. */
  INSUFFICIENT_FUNDS,
  /** The economy provider could not commit the money transaction. */
  ECONOMY_TRANSACTION_FAILED,
  /** The item transfer failed after the money transaction and triggered a rollback. */
  SHOP_OPERATION_FAILED,
  /** An unexpected exception interrupted the transaction. */
  INTERNAL_ERROR
}
