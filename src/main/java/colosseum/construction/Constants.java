package colosseum.construction;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {
    public static final String LOCATIONS_DELIMITER = ";";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ConfigKeys {
        public static final String PARSE_MAXIMUM_RADIUS = "parse.maximum_radius";
    }
}
