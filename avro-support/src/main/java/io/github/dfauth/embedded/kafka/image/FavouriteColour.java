package io.github.dfauth.embedded.kafka.image;

import lombok.extern.slf4j.Slf4j;

public enum FavouriteColour {
    UNKNOWN,
    RED,
    YELLOW,
    BLUE,
    GREEN,
    PURPLE;

    @Slf4j
    public static class Conversion extends EnumConversion<FavouriteColour> {

        public Conversion() {
            super(FavouriteColour.class);
        }
    }

    public static class LogicalTypeFactory extends EnumLogicalTypeFactory {

        public LogicalTypeFactory() {
            super(FavouriteColour.class);
        }
    }
}
