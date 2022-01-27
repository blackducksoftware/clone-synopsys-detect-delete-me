package com.synopsys.integration.detect.configuration.properties;

import org.jetbrains.annotations.NotNull;

import com.synopsys.integration.configuration.property.types.enums.EnumProperty;
import com.synopsys.integration.configuration.property.types.integer.IntegerProperty;

public class EnumDetectProperty<E extends Enum<E>> extends DetectProperty<EnumProperty<E>> {
    public EnumDetectProperty(@NotNull String key, @NotNull E defaultValue, @NotNull Class<E> enumClass) {
        super(new EnumProperty<>(key, defaultValue, enumClass));
    }

    public static <E extends Enum<E>> DetectPropertyBuilder<EnumProperty<E>, EnumDetectProperty<E>> newBuilder(@NotNull String key, @NotNull E defaultValue, @NotNull Class<E> enumClass) {
        DetectPropertyBuilder<EnumProperty<E>, EnumDetectProperty<E>> builder = new DetectPropertyBuilder<>();
        builder.setCreator(() -> new EnumDetectProperty<E>(key, defaultValue, enumClass));
        return builder;
    }
}
