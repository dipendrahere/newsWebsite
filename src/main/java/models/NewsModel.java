package models;

import java.util.List;

public class NewsModel {
    List<News> news;
    int clusterId;

    public void addNews(News news){
        this.news.add(news);
    }

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public List<News> getNews() {
        return news;
    }

    public void setNews(List<News> topics) {
        this.news = topics;
    }
}
