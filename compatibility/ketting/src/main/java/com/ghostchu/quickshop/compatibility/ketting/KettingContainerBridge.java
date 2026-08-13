package com.ghostchu.quickshop.compatibility.ketting;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

final class KettingContainerBridge {

  private static final String NMS_CONTAINER_CLASS = "net.minecraft.world.Container";
  private final Map<Class<?>, Optional<Accessor>> accessors = new ConcurrentHashMap<>();
  private final Set<Class<?>> loggedFailures = ConcurrentHashMap.newKeySet();
  private final Logger logger;

  KettingContainerBridge(@NotNull final Logger logger) {

    this.logger = logger;
  }

  Optional<ResolvedContainer> resolve(@NotNull final BlockState state) {

    final Optional<Accessor> optionalAccessor = accessors.computeIfAbsent(state.getClass(), this::discoverAccessor);
    if(optionalAccessor.isEmpty()) {
      return Optional.empty();
    }
    try {
      final Accessor accessor = optionalAccessor.get();
      final Object container = accessor.getTileEntity(state);
      if(container == null || !accessor.containerClass().isInstance(container)) {
        return Optional.empty();
      }
      final Object craftInventory = accessor.inventoryConstructor().newInstance(container);
      if(!(craftInventory instanceof final Inventory inventory)) {
        logFailure(state.getClass(), "Reflected CraftInventory does not implement Bukkit Inventory", null);
        return Optional.empty();
      }
      return Optional.of(new ResolvedContainer(inventory, container));
    } catch(IllegalAccessException | InstantiationException | InvocationTargetException | RuntimeException | LinkageError exception) {
      logFailure(state.getClass(), "Failed to resolve live NMS Container", exception);
      return Optional.empty();
    }
  }

  private Optional<Accessor> discoverAccessor(final Class<?> stateClass) {

    try {
      final ClassLoader classLoader = stateClass.getClassLoader() == null
                                      ? Bukkit.getServer().getClass().getClassLoader()
                                      : stateClass.getClassLoader();
      final Class<?> containerClass = Class.forName(NMS_CONTAINER_CLASS, false, classLoader);
      final String craftBukkitPackage = Bukkit.getServer().getClass().getPackage().getName();
      final Class<?> craftInventoryClass = Class.forName(craftBukkitPackage + ".inventory.CraftInventory", false, classLoader);
      final Constructor<?> inventoryConstructor = craftInventoryClass.getConstructor(containerClass);

      final Method tileEntityGetter = findTileEntityGetter(stateClass);
      if(tileEntityGetter != null) {
        tileEntityGetter.setAccessible(true);
        return Optional.of(new Accessor(containerClass, inventoryConstructor, tileEntityGetter, null));
      }

      final Field tileEntityField = findTileEntityField(stateClass);
      if(tileEntityField != null) {
        tileEntityField.setAccessible(true);
        return Optional.of(new Accessor(containerClass, inventoryConstructor, null, tileEntityField));
      }
    } catch(ClassNotFoundException | NoSuchMethodException | RuntimeException | LinkageError exception) {
      logFailure(stateClass, "Ketting container reflection is unavailable for this block state", exception);
      return Optional.empty();
    }

    logFailure(stateClass, "Live tileEntity accessor not found", null);
    return Optional.empty();
  }

  private Method findTileEntityGetter(final Class<?> stateClass) {

    Class<?> current = stateClass;
    while(current != null && current != Object.class) {
      try {
        final Method method = current.getDeclaredMethod("getTileEntity");
        if(method.getParameterCount() == 0) {
          return method;
        }
      } catch(final NoSuchMethodException ignored) {
      }
      current = current.getSuperclass();
    }
    return null;
  }

  private Field findTileEntityField(final Class<?> stateClass) {

    Class<?> current = stateClass;
    while(current != null && current != Object.class) {
      try {
        return current.getDeclaredField("tileEntity");
      } catch(final NoSuchFieldException ignored) {
      }
      current = current.getSuperclass();
    }
    return null;
  }

  private void logFailure(final Class<?> stateClass, final String message, final Throwable throwable) {

    if(!loggedFailures.add(stateClass)) {
      return;
    }
    if(throwable == null) {
      logger.fine(message + ": " + stateClass.getName());
    } else {
      logger.log(Level.FINE, message + ": " + stateClass.getName(), throwable);
    }
  }

  record ResolvedContainer(@NotNull Inventory inventory, @NotNull Object handle) {
  }

  private record Accessor(
          Class<?> containerClass,
          Constructor<?> inventoryConstructor,
          Method tileEntityGetter,
          Field tileEntityField) {

    Object getTileEntity(final Object state) throws IllegalAccessException, InvocationTargetException {

      if(tileEntityGetter != null) {
        return tileEntityGetter.invoke(state);
      }
      return tileEntityField.get(state);
    }
  }
}
