package rs.elfak.mosis.greenforce.enums;

public enum VolunteerType {
    LEADER("Leader"),
    FOLLOWER("Follower");

    private String enumString;
    private VolunteerType(String brand) {
        this.enumString = brand;
    }

    @Override
    public String toString(){
        return enumString;
    }
}
