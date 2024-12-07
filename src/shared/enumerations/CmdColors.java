package shared.enumerations;

public enum CmdColors {
    RESET("\u001B[0m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    ORANGE("\u001B[38;5;208m"),
    BLUE("\u001B[34m"),
    PURPLE("\u001B[35m"),
    CYAN("\u001B[36m"),
    WHITE("\u001B[37m"),
    PINK("\u001B[95;1m"),
    TEAL("\u001B[36;1m"),
    LIME("\u001B[32;1m"),
    TURQUOISE("\u001B[36;3m"),
    VIOLET("\u001B[38;5;129m"),
    MAGENTA("\u001B[35;1m"),
    INDIGO("\u001B[38;5;93m"),
    CRIMSON("\u001B[38;5;196m"),
    AQUA("\u001B[38;5;51m"),
    FOREST_GREEN("\u001B[38;5;22m"),
    OLIVE("\u001B[38;5;100m");

    private final String color;

    CmdColors(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return color;
    }
}
