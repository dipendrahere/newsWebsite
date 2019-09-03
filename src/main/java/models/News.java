package models;

import java.util.Date;

public class News {
    private String url;
    private String title;
    private String imageUrl;
    private Date publishedDate;
    private double orderScore;
    private double articleRank;

    public double getArticleRank() {
        return articleRank;
    }

    public void setArticleRank(double articleRank) {
        this.articleRank = articleRank;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(Date publishedDate) {
        this.publishedDate = publishedDate;
    }

    public double getOrderScore() {
        return orderScore;
    }

    public void setOrderScore(double orderScore) {
        this.orderScore = orderScore;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
