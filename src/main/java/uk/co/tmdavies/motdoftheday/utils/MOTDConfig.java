package uk.co.tmdavies.motdoftheday.utils;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class MOTDConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("Enable the MOTD Changer?")
            .define("Enabled", true);

    public static final ModConfigSpec.IntValue CHANGE_TIME = BUILDER
            .comment("The interval which the MOTD changes (in milliseconds)")
            .defineInRange("MOTD.Change-Interval", 86400000, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> MOTD_STRINGS = BUILDER
            .comment("All the MOTDs you want to use")
            .defineListAllowEmpty("MOTD.List", List.of("MOTD", "Here", "Bozo", "xoxo"), () -> "", MOTDConfig::validateString);
    
    public static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateString(final Object obj) {
        return obj instanceof String;
    }
}
