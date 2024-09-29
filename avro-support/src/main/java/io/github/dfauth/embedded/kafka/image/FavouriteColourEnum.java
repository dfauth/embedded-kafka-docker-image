package io.github.dfauth.embedded.kafka.image;

import lombok.extern.slf4j.Slf4j;

public enum FavouriteColourEnum {
    UNKNOWN,
    RED,
    YELLOW,
    BLUE,
    GREEN,
    PURPLE;

    @Slf4j
    public static class Conversion extends EnumConversion<FavouriteColourEnum> {

        public Conversion() {
            super(FavouriteColourEnum.class, LogicalTypeFactory.NAME);
        }
    }

    public static class LogicalTypeFactory extends EnumLogicalTypeFactory {

        public static final String NAME = "favourite-colour";

        public LogicalTypeFactory() {
            super(NAME);
        }
    }
}
