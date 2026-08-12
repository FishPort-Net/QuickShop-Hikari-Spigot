package com.ghostchu.quickshop.util;

import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Bridges the potion API changed in Minecraft 1.20.5 without linking the
 * Java 17 build against methods absent from the 1.20.1 Spigot API.
 */
public final class PotionCompat {

  private PotionCompat() {

  }

  /**
   * Gets the effects supplied by the base potion on the new potion API.
   *
   * @param potionMeta potion metadata
   * @return base potion effects, or an empty list when the new API is absent
   */
  @NotNull
  public static List<PotionEffect> getBasePotionEffects(@NotNull final PotionMeta potionMeta) {

    final List<PotionEffect> effects = new ArrayList<>();
    try {
      final Object potionType = getBasePotionType(potionMeta);
      if(potionType == null) {
        return effects;
      }
      final Method getPotionEffects = potionType.getClass().getMethod("getPotionEffects");
      final Object returnedEffects = getPotionEffects.invoke(potionType);
      if(returnedEffects instanceof final Collection<?> collection) {
        for(final Object effect : collection) {
          if(effect instanceof final PotionEffect potionEffect) {
            effects.add(potionEffect);
          }
        }
      }
    } catch(final ReflectiveOperationException | LinkageError ignore) {
      // The caller only uses this method when the new potion API is present.
    }
    return effects;
  }

  /**
   * Gets the stable enum name of the base potion across the old and new APIs.
   *
   * @param potionMeta potion metadata
   * @return base potion name
   */
  @NotNull
  public static String getBasePotionName(@NotNull final PotionMeta potionMeta) {

    try {
      final Object potionType = getBasePotionType(potionMeta);
      if(potionType instanceof final Enum<?> enumValue) {
        return enumValue.name();
      }
    } catch(final ReflectiveOperationException | LinkageError ignore) {
      // Fall through to the legacy API used by Spigot 1.20.1.
    }
    return potionMeta.getBasePotionData().getType().name();
  }

  private static Object getBasePotionType(@NotNull final PotionMeta potionMeta) throws ReflectiveOperationException {

    return potionMeta.getClass().getMethod("getBasePotionType").invoke(potionMeta);
  }
}
