package rs.elfak.mosis.greenforce.enums;

public enum EventImageType {
    BEFORE("/before"),
    AFTER("/after");
    private String enumString;
    private EventImageType(String brand) {
        this.enumString = brand;
    }

    @Override
    public String toString(){
        return enumString;
    }
}
