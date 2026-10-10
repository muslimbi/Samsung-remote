package com.example.model

enum class SamsungKey(val keyCode: String, val label: String) {
    // Volume & Sound
    VOL_UP("KEY_VOLUP", "Volume Up"),
    VOL_DOWN("KEY_VOLDOWN", "Volume Down"),
    MUTE("KEY_MUTE", "Mute"),

    // Channel
    CH_UP("KEY_CHUP", "Channel Up"),
    CH_DOWN("KEY_CHDOWN", "Channel Down"),
    PRE_CH("KEY_PRECH", "Previous Channel"),
    GUIDE("KEY_GUIDE", "Guide"),
    CH_LIST("KEY_CH_LIST", "Channel List"),

    // Power
    POWER("KEY_POWER", "Power"),
    POWER_OFF("KEY_POWEROFF", "Power Off"),

    // Navigation
    UP("KEY_UP", "Up"),
    DOWN("KEY_DOWN", "Down"),
    LEFT("KEY_LEFT", "Left"),
    RIGHT("KEY_RIGHT", "Right"),
    ENTER("KEY_ENTER", "Select / OK"),
    RETURN("KEY_RETURN", "Back"),
    HOME("KEY_HOME", "Smart Hub / Home"),
    MENU("KEY_MENU", "Menu"),
    SOURCE("KEY_SOURCE", "Source"),
    INFO("KEY_INFO", "Info"),
    EXIT("KEY_EXIT", "Exit"),

    // Digits
    NUM_0("KEY_0", "0"),
    NUM_1("KEY_1", "1"),
    NUM_2("KEY_2", "2"),
    NUM_3("KEY_3", "3"),
    NUM_4("KEY_4", "4"),
    NUM_5("KEY_5", "5"),
    NUM_6("KEY_6", "6"),
    NUM_7("KEY_7", "7"),
    NUM_8("KEY_8", "8"),
    NUM_9("KEY_9", "9"),

    // Media
    PLAY("KEY_PLAY", "Play"),
    PAUSE("KEY_PAUSE", "Pause"),
    STOP("KEY_STOP", "Stop"),
    REWIND("KEY_REWIND", "Rewind"),
    FF("KEY_FF", "Fast Forward"),

    // Color buttons
    RED("KEY_RED", "Red"),
    GREEN("KEY_GREEN", "Green"),
    YELLOW("KEY_YELLOW", "Yellow"),
    BLUE("KEY_BLUE", "Blue")
}

enum class RemoteControlMode {
    TACTILE_ROCKER,  // Samsung OneRemote style vertical flipper switches
    JOG_WHEEL,       // Circular rotary scrub wheel for volume and channel
    NUMERIC_PAD      // 1-9 direct channel and digits pad
}

enum class ConnectionStatus {
    DISCONNECTED,
    DISCOVERING,
    CONNECTING,
    CONNECTED,
    VIRTUAL_SIMULATOR,
    PAIRING_REQUIRED,
    ERROR
}
