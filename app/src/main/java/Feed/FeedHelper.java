package Feed;


public class FeedHelper {
    private String title, desc, image, username, likes;

    public FeedHelper() {

    }

    public FeedHelper(String username, String title, String desc, String image, String likes) {
        this.username = username;
        this.title = title;
        this.desc = desc;
        this.image = image;
        this.likes = likes;

    }

    public String getImage() {
        return image;
    }

    public void setImage(String status_image) {
        this.image = status_image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String status_title) {
        this.title = status_title;
    }

    public String getDesc() {
        return desc;
    }

    public String getUsername() {
        return username;
    }

    public String getLikes() {
        return likes;
    }


}
