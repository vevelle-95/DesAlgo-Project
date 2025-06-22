package src.navapp.model;

public class SimulationEvent {
    private final String type;
    private final String from;
    private final String to;
    private final String message;

    public SimulationEvent(String type, String from, String to, String message) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getMessage() {
        return message;
    }
}
