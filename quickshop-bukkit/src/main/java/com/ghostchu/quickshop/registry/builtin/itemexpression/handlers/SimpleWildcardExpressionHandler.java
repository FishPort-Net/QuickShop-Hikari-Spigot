package com.ghostchu.quickshop.registry.builtin.itemexpression.handlers;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Matches material names using shell-style {@code *} and {@code ?} wildcards.
 */
public class SimpleWildcardExpressionHandler implements ItemExpressionHandler {

  private final QuickShop plugin;

  public SimpleWildcardExpressionHandler(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public @NotNull Plugin getPlugin() {

    return plugin.getJavaPlugin();
  }

  @Override
  public String getPrefix() {

    return "*";
  }

  @Override
  public String getInternalPrefix0() {

    return "";
  }

  @Override
  public boolean match(final ItemStack stack, final String expression) {

    final StringBuilder regex = new StringBuilder(expression.length() * 2);
    for(int index = 0; index < expression.length(); index++) {
      final char character = expression.charAt(index);
      switch(character) {
        case '*' -> regex.append(".*");
        case '?' -> regex.append('.');
        case '\\', '.', '^', '$', '|', '(', ')', '[', ']', '{', '}', '+' ->
                regex.append('\\').append(character);
        default -> regex.append(character);
      }
    }
    return Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE)
            .matcher(stack.getType().name())
            .matches();
  }
}
