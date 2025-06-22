package src.navapp.model;

public class Step {
    private final String from, to, modeUsed;
    private final int weightUsed;

    public Step(String from, String to, String modeUsed, int weightUsed) {
        this.from = from;
        this.to = to;
        this.modeUsed = modeUsed;
        this.weightUsed = weightUsed;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getModeUsed() {
        return modeUsed;
    }

    public int getWeightUsed() {
        return weightUsed;
    }
}
