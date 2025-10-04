package colosseum.construction;

import colosseum.utility.UtilPlayerBase;
import colosseum.utility.arcade.GameType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GameTypeUtils {
    public static void printValidGameTypes(CommandSender caller) {
        UtilPlayerBase.sendMessage(caller, String.format("&cValid game types: &e%s&r", String.join("&r, &e", GameTypeUtils.getGameTypes().stream().map(Enum::toString).toArray(String[]::new))));
    }

    public static List<GameType> getGameTypes() {
        return GameType.getEntries().stream().filter(v -> v != GameType.None).toList();
    }

    public static GameType determineGameType(String raw, boolean noneOnError) {
        if (StringUtils.isBlank(raw)) {
            return GameType.None;
        }
        if (noneOnError) {
            try {
                return GameType.valueOf(raw);
            } catch (Exception e) {
                return GameType.None;
            }
        }
        return GameType.valueOf(raw);
    }
}
