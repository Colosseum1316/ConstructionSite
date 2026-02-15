package colosseum.construction;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PluginConstants {
    public static final String LOCATIONS_DELIMITER = ";";

    public static final String UNTITLED = "Untitled";
    public static final String NULL = "null";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ConfigKeys {
        public static final String PARSE__MAXIMUM_RADIUS = "parse.maximum_radius";
    }
}
