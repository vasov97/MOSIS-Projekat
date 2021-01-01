package rs.elfak.mosis.greenforce.models;

public class VoteCount {
    int likes;
    int dislikes;

    public VoteCount(){
        likes=0;
        dislikes=0;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }
}
