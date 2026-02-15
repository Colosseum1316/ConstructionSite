package colosseum.construction;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RevConstants {
    public static final String BUILD_VERSION = "${ColosseumConstructionSite.buildVersion}";
}
