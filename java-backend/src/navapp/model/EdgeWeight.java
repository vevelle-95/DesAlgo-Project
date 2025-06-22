package src.navapp.model;

public class EdgeWeight {
    private int walk;
    private int mixcommute;
    private Integer jeepney;

    public EdgeWeight(int walk, int mixcommute, Integer jeepney) {
        this.walk = walk;
        this.mixcommute = mixcommute;
        this.jeepney = jeepney;
    }

    public int getWalk() {
        return walk;
    }

    public int getMixcommute() {
        return mixcommute;
    }

    public Integer getJeepney() {
        return jeepney;
    }

    public int getMode(String mode) {
        return switch (mode) {
            case "walk" -> walk;
            case "mixcommute" -> mixcommute;
            case "jeep" -> (jeepney != null ? jeepney : Integer.MAX_VALUE);
            default -> throw new IllegalArgumentException("Invalid mode: " + mode);
        };
    }
}
