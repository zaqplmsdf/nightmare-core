package com.nightmarecore;

/**
 * The core no longer modifies attributes. Its balance is now expressed entirely
 * through the eight curses and the three blessing slots that can suppress them.
 */
public final class ModBalance {

    public static final int BLESSING_SLOTS = 3;

    public static void verify() {
        if (CurseType.values().length != 8) {
            throw new IllegalStateException("Expected exactly eight curses");
        }
        NightmareCore.LOGGER.info(
                "Curses-only balance audit passed: 8 curses, 3 blessing suppression slots.");
    }

    private ModBalance() {
    }
}
