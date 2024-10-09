package io.github.dfauth.embedded.kafka.test;

import io.github.dfauth.embedded.kafka.EnumLogicalTypeFactoryAndConversion;

public enum FavouriteColour {
    UNKNOWN,
    RED,
    YELLOW,
    BLUE,
    GREEN,
    PURPLE;

    public static class LogicalTypeFactoryAndConversion extends EnumLogicalTypeFactoryAndConversion<FavouriteColour> {

        public LogicalTypeFactoryAndConversion() {
            super(FavouriteColour.class);
        }
    }
}
