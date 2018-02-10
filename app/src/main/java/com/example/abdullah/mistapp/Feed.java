package com.example.abdullah.mistapp;

/**
 * Created by Abdullah on 3/16/2017.
 */

public class Feed {

    public Feed() {

    }

    private String Title;
    private String Image;
    private String Description;
    private String Username;

    private String Comments;

    public Feed(String title, String image, String description) {
        Title = title;
        Image = image;
        Description = description;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getComments() {
        return Comments;
    }

    public void setComments(String comments) {
        Comments = comments;
    }

}