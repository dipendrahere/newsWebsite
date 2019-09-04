package databaseService;

import models.*;

import javax.management.modelmbean.ModelMBean;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.stream.Collectors;

public class DBConnect {
    private static Connection connnection;
    private static Statement statment;
    private static DBConnect db;
    private static Data data;

    public static synchronized DBConnect getInstance(){
        if (db==null) {
            db = new DBConnect();
            db.setData(new Data());
        }
        return db;
    }

    public static synchronized Data getData() {
        return data;
    }

    public static synchronized void setData(Data data) {
        DBConnect.data = data;
    }

    private DBConnect(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connnection = DriverManager.getConnection("jdbc:mysql://172.19.33.103:3306/newsaggregator","root","kEMXdVW9vMvQ");
//            connnection = DriverManager.getConnection("jdbc:mysql://localhost/newsaggregator","root","vipin1407");
            statment = connnection.createStatement();
        }
        catch (ClassNotFoundException e) {
            Log.error("JDBC not available");
        } catch (SQLException e) {
            Log.error(e.getMessage());
            e.printStackTrace();
            Log.error("Unable to make connection to DB");
        }
    }

//    // Todo You transaction to insert to avoid inconsistacy in the database
//
//    public static synchronized List<NewsModel> getListOfNews(){
//        HashMap<Integer, List<News>> ret = new HashMap<>();
//        try{
//            PreparedStatement preparedStatement = connnection.prepareStatement("select j.cluster_id, j.title, j.imageUrl, j.url, j.publishedDate, j.diameter, j.recency, j.averageDate, j.totalPoints, j.rssLinks from (select C.cluster_id, A.title, A.imageUrl, A.publishedDate, A.url, CI.diameter, CI.recency, CI.averageDate, CI.totalPoints, CI.rssLinks from articles A join clusterArticleRelationship C on A.id = C.articleId join clusterInfo CI on CI.id = C.cluster_id order by A.publishedDate) as j where j.cluster_id  in (select (cluster_id) from clusterArticleRelationship group by cluster_id having count(cluster_id) >= 3) order by recency desc");
//            resultSet = preparedStatement.executeQuery();
//            while (resultSet.next()){
//                int cluster_id = resultSet.getInt(1);
//                String topic = resultSet.getString(2);
//                String url = resultSet.getString(4);
//                String imageUrl = resultSet.getString(3);
//                Date pubDate = resultSet.getDate(5);
//                News news = new News();
//                news.setImageUrl(imageUrl);
//                news.setTitle(topic);
//                news.setPublishedDate(pubDate);
//                news.setOrderScore(0);
//                news.setUrl(url);
//                if(ret.get(cluster_id) == null){
//                    List<News> list = new ArrayList<>();
//                    list.add(news);
//                    ret.put(cluster_id, list);
//                }
//                else {
//                    List<News> list = ret.get(cluster_id);
//                    list.add(news);
//                    ret.put(cluster_id, list);
//                }
//            }
//        }
//        catch (SQLException e){
//            Log.error("unable to fetch Article "+ e.getMessage());
//            e.printStackTrace();
//        }
//        List<NewsModel> modelList = new ArrayList<>();
//        for(Integer i: ret.keySet()) {
//            NewsModel newsModel = new NewsModel();
//            newsModel.setClusterId(i);
//            List<News> strings = ret.get(i);
//            newsModel.setNews(strings);
//            modelList.add(newsModel);
//        }
//        return modelList;
//    }

    public static synchronized List<NewsModel> getTopNews(int categoryId) {
        HashMap<Integer, List<News>> ret = new HashMap<>();
        HashMap<Integer,ClusterInfo> infoMap = new HashMap<>();
        Set<String> contents = new HashSet<String>();
        try {
            List<CloneResultSet> res = data.getCat(categoryId);
            for (CloneResultSet set : res){
                int cluster_id = set.getCluster_id();
                String topic = set.getTitle();
                String url = set.getUrl();
                String imageUrl =set.getImageUrl();
                Date pubDate =set.getPublishedDate();
                String content = set.getContent();
                String md5 = GlobalFunctions.getMd5(content);
                if(contents.contains(md5)){
                    continue;
                }
                else{
                    contents.add(md5);
                }
                News news = new News();
                news.setImageUrl(imageUrl);
                String[] split = topic.split("\\|");
                topic = split[split.length-1];
                news.setTitle(topic);
                news.setPublishedDate(pubDate);
//                news.setOrderScore(0);
                news.setUrl(url);
                double diameter = set.getDiameter();
                Date recency = set.getRecency();
                int totalPoints = set.getTotalPoints();
                int coverage = set.getRssLinks().split("\\|").length;
                ClusterInfo  ci = new ClusterInfo();
                ci.setDiameter(diameter);
                ci.setClusterId(cluster_id);
                ci.setRecency(recency);
                ci.setTotalPoints(totalPoints);
                ci.setCoverage(coverage);
                infoMap.put(cluster_id, ci);
                if(ret.get(cluster_id) == null){
                    List<News> list = new ArrayList<>();
                    list.add(news);
                    ret.put(cluster_id, list);
                }
                else {
                    List<News> list = ret.get(cluster_id);
                    list.add(news);
                    ret.put(cluster_id, list);
                }
            }
        } catch (SQLException e) {
            Log.error("unable to fetch News "+ e.getMessage());
            e.printStackTrace();
        }
        List<NewsModel> modelList = new ArrayList<>();
        for(Integer i: ret.keySet()){
            NewsModel newsModel = new NewsModel();
            newsModel.setClusterId(i);
            List<News> strings = ret.get(i);
            newsModel.setClusterInfo(infoMap.get(i));
            newsModel.setNews(strings);
            newsModel.sortNews();
            int min = Math.min(3, newsModel.getNews().size());
            newsModel.setNews(newsModel.getNews().subList(0, min));
            modelList.add(newsModel);
        }
        if(modelList.size() == 0){
            return null;
        }
        Date d = modelList.get(0).getClusterInfo().getRecency();
        for(NewsModel nm: modelList){
            if(d.compareTo(nm.getClusterInfo().getRecency()) > 0){
                d = nm.getClusterInfo().getRecency();
            }
        }
        final Date mr = d;
        Collections.sort(modelList, (a, b)->{
            return (int)(b.getClusterInfo().score(mr)*1000 - a.getClusterInfo().score(mr)*1000);
        });
        for(NewsModel nm: modelList){
            nm.setClusterScore(nm.getClusterInfo().sc);
        }
        return modelList;
    }

    public static synchronized NewsModel getArticles(int clusterId){
        NewsModel model = new NewsModel();
        Set<String> contents = new HashSet<>();
        try {
            List<CloneNewsSet> res = data.getCluster(clusterId);
            for(CloneNewsSet set: res){
                String topic = set.getTitle();
                String[] split = topic.split("\\|");
                topic = split[split.length-1];
                String url = set.getUrl();
                String imageUrl = set.getImageUrl();
                Date pubDate = set.getPublishedDate();
                String content = set.getContent();
                String md5 = GlobalFunctions.getMd5(content);
                if(contents.contains(md5)){
                    continue;
                }
                else{
                    contents.add(md5);
                }
                News news = new News();
                news.setImageUrl(imageUrl);
                news.setTitle(topic);
                news.setPublishedDate(pubDate);
//                news.setOrderScore(0);
                news.setUrl(url);
                model.addNews(news);
                model.sortNews();
            }

        } catch (SQLException e) {
            Log.error("unable to fetch Article "+ e.getMessage());
            e.printStackTrace();
        }
        return model;
    }
}
