package rs.elfak.mosis.greenforce.enums;

public enum EventStatus
{
    AVAILABLE("Available"),
    IN_PROGRESS("In progress"),
    PENDING("Pending"),
    COMPLETED("Completed");
    private String enumString;
    private EventStatus(String brand) {
        this.enumString = brand;
    }

    @Override
    public String toString(){
        return enumString;
    }
}
