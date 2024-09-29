package io.github.dfauth.embedded.kafka.image;

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
