package models;

import com.mysql.cj.xdevapi.Collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NewsModel {
    List<News> news;
    int clusterId;
    ClusterInfo clusterInfo;
    private double clusterScore;

    public double getClusterScore() {
        return clusterScore;
    }

    public void setClusterScore(double clusterScore) {
        this.clusterScore = clusterScore;
    }

    public ClusterInfo getClusterInfo() {
        return clusterInfo;
    }

    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    public NewsModel(){
        news = new ArrayList<>();
    }

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

    public void sortNews(){
        Collections.sort(news, (a, b) -> {
            long time = (b.getPublishedDate().getTime() - a.getPublishedDate().getTime());
            time = Math.min(time / 10000000, Integer.MAX_VALUE);
            return (int)(time);
        });
    }
}
