package colosseum.construction;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@SuppressWarnings("unused")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RevConstants {
    public static final String BUILD_VERSION = "${ColosseumConstructionSite.buildVersion}";

    public static final String BUILD_TIMESTAMP = "${project.build.outputTimestamp}";
}
