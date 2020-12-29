package rs.elfak.mosis.greenforce.enums;

public enum EventTypes
{
    REFORESTATION("Reforestation"),
    WATER_POLLUTION("Water pollution"),
    LAND_POLLUTION("Land pollution"),
    OTHER("Other");
    private String eventType;
    EventTypes(String s) {eventType=s;}

    @Override
    public String toString() {return eventType;}
}
