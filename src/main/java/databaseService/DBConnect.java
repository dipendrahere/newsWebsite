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
    private static ResultSet resultSet;
    private static DBConnect db;

    public static synchronized DBConnect getInstance(){
        if (db==null)
            db = new DBConnect();
        return db;
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

    // Todo You transaction to insert to avoid inconsistacy in the database
    public static synchronized void insertArticles(List<Article> articles){
        Log.debug("INSERTING ARTICLES");
        if(articles.size() == 0){
            return;
        }
        try{
            java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String format = "(id, title, category_id, url, publishedDate, rssLink, content, imageUrl)";
            PreparedStatement preparedStatement = connnection.prepareStatement("insert into articles "+ format +" values (?, ?, ?, ?, ?, ?, ?, ?);");
            PreparedStatement clusterPreparedStatement = connnection.prepareStatement("insert into clusterArticleRelationship values (?,?);");
            for(int i=0;i<articles.size();i++) {
                Article article = articles.get(i);
                String exactDate = null;
                if(article.getPublishedDate() != null){
                    exactDate = simpleDateFormat.format(article.getPublishedDate());
                }
                preparedStatement.setString(1, article.getId());
                preparedStatement.setString(2, article.getTitle());
                preparedStatement.setInt(3, article.getCategoryType().value.getKey());
                preparedStatement.setString(4, article.getUrl());
                preparedStatement.setString(5, exactDate);
                preparedStatement.setString(6, article.getRssLink());
                preparedStatement.setString(7, article.getContent());
                preparedStatement.setString(8, article.getImageUrl());
                preparedStatement.addBatch();

                clusterPreparedStatement.setString(1, article.getId());
                clusterPreparedStatement.setString(2,null);
                clusterPreparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
            clusterPreparedStatement.executeBatch();

            Log.debug("ARTICLES INSERTION PROCESSED: "+articles.size());

        }
        catch (SQLException e){
            Log.error(e.getMessage());
            e.printStackTrace();
            Log.error("Unable to insert bulk Articles");
        }
    }

    public void insertArticle(Article article){
        try{
            java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String exactDate = simpleDateFormat.format(article.getPublishedDate());
            PreparedStatement preparedStatement = connnection.prepareStatement("insert into articles values (?, ?, ?, ?, ?, ?, ?, ?);");
            preparedStatement.setString(1, article.getId());
            preparedStatement.setString(2, article.getTitle());
            preparedStatement.setInt(3, article.getCategoryType().value.getKey());
            preparedStatement.setString(4, article.getUrl());
            preparedStatement.setString(5, exactDate);
            preparedStatement.setString(6, article.getRssLink());
            preparedStatement.setString(7, article.getContent());
            preparedStatement.setString(8, article.getImageUrl());
            int count = preparedStatement.executeUpdate();
            System.out.println("insert row "+count);
        }
        catch (SQLException e){
            Log.error("Unable to insert Article: "+ article);
        }
    }

    public static synchronized boolean isArticlePresent(String url){
        boolean ret = true;
        try {
            String query = "select * from articles where url = \""+url+"\";";
            resultSet = statment.executeQuery(query);
            if (!resultSet.isBeforeFirst() ) {
                ret = false;
            }
        } catch (SQLException e) {
            Log.error("Unable to query for article present url: " + url + e.getMessage());
        }
        return ret;
    }

    // Todo Donot remove this function
    public static synchronized List<Article> fetchArticles(CategoryType categoryType){
        List<Article> ret = new ArrayList<>();
        try{
            PreparedStatement preparedStatement = connnection.prepareStatement("select * from articles where category_id = "+categoryType.value.getKey());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                Article a = new ArticleBuilder(resultSet.getString(4))
                        .setTitle(resultSet.getString(2))
                        .setCategoryType(CategoryType.values()[resultSet.getInt(3)-1])
                        .setPublishedDate(resultSet.getDate(5))
                        .setRssLink(resultSet.getString(6))
                        .setContent(resultSet.getString(7))
                        .setImageUrl(resultSet.getString(8))
                        .build();
                a.setId(resultSet.getString(1));
                ret.add(a);
            }
        }
        catch (SQLException e){
            Log.error("unable to fetch Article " +e.getMessage());
        }
        return ret;
    }

    public static synchronized List<Article> fetchArticlesRecent(CategoryType categoryType){
        List<Article> ret = new ArrayList<>();
        try{
            PreparedStatement preparedStatement = connnection.prepareStatement("select * from articles where category_id = "+categoryType.value.getKey() + " order by publishedDate desc limit 2000");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                Article a = new ArticleBuilder(resultSet.getString(4))
                        .setTitle(resultSet.getString(2))
                        .setCategoryType(CategoryType.values()[resultSet.getInt(3)-1])
                        .setPublishedDate(resultSet.getDate(5))
                        .setRssLink(resultSet.getString(6))
                        .setContent(resultSet.getString(7))
                        .setImageUrl(resultSet.getString(8))
                        .build();
                a.setId(resultSet.getString(1));
                ret.add(a);
            }
        }
        catch (SQLException e){
            Log.error("unable to fetch Article " +e.getMessage());
        }
        return ret;
    }


    public static synchronized void updateClusterIDs(HashMap<String,Integer> hashMap){
        Log.debug("Update in Db");
//        System.out.println(hashMap);
        if(hashMap.size() == 0){
            return;
        }
        try{
            PreparedStatement preparedStatement = connnection.prepareStatement("Update clusterArticleRelationship set cluster_id = ? where articleId = ?");
            Iterator iterator = hashMap.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry mapElement = (Map.Entry)iterator.next();
                preparedStatement.setInt(1,(Integer)mapElement.getValue());
                preparedStatement.setString(2,(String)mapElement.getKey());
                preparedStatement.addBatch();
            }
            preparedStatement.executeLargeBatch();
        }
        catch (SQLException e){
            Log.error(e.getMessage());
            e.printStackTrace();
            Log.error("Unable to update Cluster id of Articles");
        }

    }

    public static synchronized HashMap<Article, Integer> articleClusterRelationship(CategoryType categoryType){
        HashMap<Article,Integer> ret = new HashMap<>();
        try{
            PreparedStatement preparedStatement = connnection.prepareStatement("select * from articles join clusterArticleRelationship on articles.id = clusterArticleRelationship.articleId where articles.category_id = ?");
            preparedStatement.setInt(1,categoryType.value.getKey());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                Article a = new ArticleBuilder(resultSet.getString(4))
                        .setTitle(resultSet.getString(2))
                        .setCategoryType(CategoryType.values()[resultSet.getInt(3)-1])
                        .setPublishedDate(resultSet.getDate(5))
                        .setRssLink(resultSet.getString(6))
                        .setContent(resultSet.getString(7))
                        .setImageUrl(resultSet.getString(8))
                        .build();
                a.setId(resultSet.getString(1));
                int cluster_id = resultSet.getInt(10);
                ret.put(a,cluster_id);
            }
        }
        catch (SQLException e){
            Log.error("unable to fetch Article "+ e.getMessage());
            e.printStackTrace();
        }
        return ret;
    }

    public static synchronized int maxClusterId(){
        int ret = 1;
        try {
            PreparedStatement preparedStatement = connnection.prepareStatement(" select max(cluster_id) from clusterArticleRelationship;");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                ret = resultSet.getInt(1);
            }
        }
        catch (SQLException e){
            Log.error("unable to find max clusterId "+ e.getMessage());
        }
        return ret;

    }


    public static synchronized List<NewsModel> getListOfNews(){
        HashMap<Integer, List<News>> ret = new HashMap<>();
        try{
            PreparedStatement preparedStatement = connnection.prepareStatement("select j.cluster_id, j.title, j.imageUrl, j.url, j.publishedDate, j.diameter, j.recency, j.averageDate, j.totalPoints, j.rssLinks from (select C.cluster_id, A.title, A.imageUrl, A.publishedDate, A.url, CI.diameter, CI.recency, CI.averageDate, CI.totalPoints, CI.rssLinks from articles A join clusterArticleRelationship C on A.id = C.articleId join clusterInfo CI on CI.id = C.cluster_id order by A.publishedDate) as j where j.cluster_id  in (select (cluster_id) from clusterArticleRelationship group by cluster_id having count(cluster_id) >= 3) order by recency desc");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                int cluster_id = resultSet.getInt(1);
                String topic = resultSet.getString(2);
                String url = resultSet.getString(4);
                String imageUrl = resultSet.getString(3);
                Date pubDate = resultSet.getDate(5);
                News news = new News();
                news.setImageUrl(imageUrl);
                news.setTitle(topic);
                news.setPublishedDate(pubDate);
                news.setOrderScore(0);
                news.setUrl(url);
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
        }
        catch (SQLException e){
            Log.error("unable to fetch Article "+ e.getMessage());
            e.printStackTrace();
        }
        List<NewsModel> modelList = new ArrayList<>();
        for(Integer i: ret.keySet()) {
            NewsModel newsModel = new NewsModel();
            newsModel.setClusterId(i);
            List<News> strings = ret.get(i);
            newsModel.setNews(strings);
            modelList.add(newsModel);
        }
        return modelList;
    }

    public static synchronized List<NewsModel> getTopNews(int categoryId) {
        HashMap<Integer, List<News>> ret = new HashMap<>();
        HashMap<Integer,ClusterInfo> infoMap = new HashMap<>();
        Set<String> contents = new HashSet<String>();
        try {
            String query = "select j.cluster_id, j.title, j.imageUrl, j.url, j.publishedDate, j.diameter, j.recency, j.averageDate, j.totalPoints, j.rssLinks, j.category_id, j.content from (select C.cluster_id, A.title, A.imageUrl, A.publishedDate, A.url, CI.diameter, CI.recency, CI.averageDate, CI.totalPoints, CI.rssLinks, A.category_id, A.content from articles A join clusterArticleRelationship C on A.id = C.articleId join clusterInfo CI on CI.id = C.cluster_id order by A.publishedDate) as j where j.category_id = "+categoryId+" and j.cluster_id  in (select (cluster_id) from clusterArticleRelationship group by cluster_id having count(cluster_id) >= 3) order by  recency desc;";
            System.out.println(query);
            PreparedStatement preparedStatement = connnection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                int cluster_id = resultSet.getInt(1);
                String topic = resultSet.getString(2);
                String url = resultSet.getString(4);
                String imageUrl = resultSet.getString(3);
                Date pubDate = resultSet.getDate(5);
                String content = resultSet.getString(12);
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
                double diameter = resultSet.getDouble(6);
                Date recency = resultSet.getDate(7);
                int totalPoints = resultSet.getInt(9);
                int coverage = resultSet.getString(10).split("\\|").length;
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
            PreparedStatement preparedStatement = connnection.prepareStatement("select A.title, A.imageUrl, A.url, A.publishedDate, A.content from (select * from articles A join clusterArticleRelationship C on A.id = C.articleId where C.cluster_id = "+clusterId + ") as A");
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                String topic = resultSet.getString(1);
                String[] split = topic.split("\\|");
                topic = split[split.length-1];
                String url = resultSet.getString(3);
                String imageUrl = resultSet.getString(2);
                Date pubDate = resultSet.getDate(4);
                String content = resultSet.getString(5);
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
