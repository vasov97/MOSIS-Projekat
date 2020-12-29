package rs.elfak.mosis.greenforce.enums;

public enum NotificationType {
    EVENT("event"),
    FRIEND("friend");
    private String eventType;
    NotificationType(String s) {eventType=s;}

    @Override
    public String toString() {return eventType;}
}
