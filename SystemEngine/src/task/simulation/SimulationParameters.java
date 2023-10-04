package task.simulation;

public class SimulationParameters {
    private int processingTime;
    private Boolean isRandom;
    private Double successRate;
    private Double successWithWarnings;

    public SimulationParameters() {
        this.processingTime = 0;
        this.isRandom = false;
        this.successRate = 0.0;
        this.successWithWarnings = 0.0;
    }

    public SimulationParameters(int processingTime, Boolean isRandom, Double successRate, Double successWithWarnings) {
        this.processingTime = processingTime;
        this.isRandom = isRandom;
        this.successRate = successRate;
        this.successWithWarnings = successWithWarnings;
    }

    public int getProcessingTime() {
        return this.processingTime;
    }

    public Boolean isRandom() {
        return this.isRandom;
    }

    public void setRandom(Boolean random) {
        this.isRandom = random;
    }

    public Double getSuccessRate() {
        return this.successRate;
    }

    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }

    public Double getSuccessWithWarnings() {
        return this.successWithWarnings;
    }

    public void setSuccessWithWarnings(Double successWithWarnings) {
        this.successWithWarnings = successWithWarnings;
    }

    public void setProcessingTime(int processingTime) {
        this.processingTime = processingTime;
    }
}