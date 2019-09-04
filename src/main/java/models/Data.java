package models;

import databaseService.GlobalFunctions;

import javax.xml.transform.Result;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class Data {

    private static Connection connnection;
    private static HashMap<Integer, List<CloneResultSet>> map;
    private static HashMap<Integer, List<CloneNewsSet>> news;

    public Data(){
        try {
            map = new HashMap<>();
            news = new HashMap<>();
            Class.forName("com.mysql.jdbc.Driver");
            connnection = DriverManager.getConnection("jdbc:mysql://172.19.33.103:3306/newsaggregator","root","kEMXdVW9vMvQ");
//            connnection = DriverManager.getConnection("jdbc:mysql://localhost/newsaggregator","root","vipin1407");
            Statement statement = connnection.createStatement();
        }
        catch (ClassNotFoundException e) {
            Log.error("JDBC not available");
        } catch (SQLException e) {
            Log.error(e.getMessage());
            e.printStackTrace();
            Log.error("Unable to make connection to DB");
        }
    }

    public static synchronized List<CloneResultSet> getCat(int id) throws SQLException {
        synchronized (map) {
            List<CloneResultSet> rs;
            rs = map.get(id);
            if (rs == null) {
                String query = "select j.cluster_id, j.title, j.imageUrl, j.url, j.publishedDate, j.diameter, j.recency, j.averageDate, j.totalPoints, j.rssLinks, j.category_id, j.content from (select C.cluster_id, A.title, A.imageUrl, A.publishedDate, A.url, CI.diameter, CI.recency, CI.averageDate, CI.totalPoints, CI.rssLinks, A.category_id, A.content from articles A join clusterArticleRelationship C on A.id = C.articleId join clusterInfo CI on CI.id = C.cluster_id order by A.publishedDate) as j where j.category_id = " + id + " and j.cluster_id  in (select (cluster_id) from clusterArticleRelationship group by cluster_id having count(cluster_id) >= 3) order by  recency desc;";
                System.out.println(query);
                PreparedStatement preparedStatement = connnection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();
                List<CloneResultSet> list = new ArrayList<>();
                while (resultSet.next()){
                    int cluster_id = resultSet.getInt(1);
                    String topic = resultSet.getString(2);
                    String url = resultSet.getString(4);
                    String imageUrl = resultSet.getString(3);
                    java.util.Date pubDate = resultSet.getDate(5);
                    String content = resultSet.getString(12);
                    String[] split = topic.split("\\|");
                    topic = split[split.length-1];
                    double diameter = resultSet.getDouble(6);
                    Date recency = resultSet.getDate(7);
                    int totalPoints = resultSet.getInt(9);
                    String rss = resultSet.getString(10);
                    int coverage = rss.split("\\|").length;
                    CloneResultSet crs = new CloneResultSet();
                    crs.setCluster_id(cluster_id);
                    crs.setTitle(topic);
                    crs.setImageUrl(imageUrl);
                    crs.setUrl(url);
                    crs.setPublishedDate(pubDate);
                    crs.setDiameter(diameter);
                    crs.setRecency(recency);
                    crs.setAverageDate(null);
                    crs.setTotalPoints(totalPoints);
                    crs.setRssLinks(rss);
                    crs.setCategory_id(resultSet.getInt(11));
                    crs.setContent(content);
                    list.add(crs);
                }
                rs = list;
                map.put(id, rs);
                Thread r = new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(300000);
                            map.put(id, null);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                r.start();
            }
            return map.get(id);
        }
    }

    public static synchronized List<CloneNewsSet> getCluster(int id) throws SQLException {
        synchronized (news) {
            List<CloneNewsSet> rs;
            rs = news.get(id);
            if (rs == null) {
                String query = "select A.title, A.imageUrl, A.url, A.publishedDate, A.content from (select * from articles A join clusterArticleRelationship C on A.id = C.articleId where C.cluster_id = "+id + ") as A";
                System.out.println(query);
                PreparedStatement preparedStatement = connnection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();
                List<CloneNewsSet> list = new ArrayList<>();

                while(resultSet.next()){
                    String topic = resultSet.getString(1);
                    String[] split = topic.split("\\|");
                    topic = split[split.length-1];
                    String url = resultSet.getString(3);
                    String imageUrl = resultSet.getString(2);
                    Date pubDate = resultSet.getDate(4);
                    String content = resultSet.getString(5);
                    String md5 = GlobalFunctions.getMd5(content);
                    CloneNewsSet s = new CloneNewsSet();
                    s.setTitle(topic);
                    s.setImageUrl(imageUrl);
                    s.setUrl(url);
                    s.setPublishedDate(pubDate);
                    s.setContent(content);
                    list.add(s);
                }
                rs = list;
                news.put(id, rs);
                Thread r = new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100000);
                            news.put(id, null);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                r.start();
            }
            return news.get(id);
        }
    }





//    public static synchronized ResultSet getWorld() throws SQLException {
//        ResultSet sports;
//        synchronized (map) {
//            sports = map.get(2);
//            if (sports == null) {
//                String query = "select j.cluster_id, j.title, j.imageUrl, j.url, j.publishedDate, j.diameter, j.recency, j.averageDate, j.totalPoints, j.rssLinks, j.category_id, j.content from (select C.cluster_id, A.title, A.imageUrl, A.publishedDate, A.url, CI.diameter, CI.recency, CI.averageDate, CI.totalPoints, CI.rssLinks, A.category_id, A.content from articles A join clusterArticleRelationship C on A.id = C.articleId join clusterInfo CI on CI.id = C.cluster_id order by A.publishedDate) as j where j.category_id = " + 2 + " and j.cluster_id  in (select (cluster_id) from clusterArticleRelationship group by cluster_id having count(cluster_id) >= 3) order by  recency desc;";
//                System.out.println(query);
//                PreparedStatement preparedStatement = connnection.prepareStatement(query);
//                sports = preparedStatement.executeQuery();
//                map.put(2, sports);
//                Thread r = new Thread() {
//                    @Override
//                    public void run() {
//                        try {
//                            Thread.sleep(300000);
//                            map.put(2, null);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                };
//                r.start();
//            }
//        }
//        return map.get(2);
//    }
//
//
//    public static synchronized ResultSet getBusiness() throws SQLException {
//        ResultSet sports;
//        synchronized (map) {
//            sports = map.get(3);
//            if (sports == null) {
//                String query = "select j.cluster_id, j.title, j.imageUrl, j.url, j.publishedDate, j.diameter, j.recency, j.averageDate, j.totalPoints, j.rssLinks, j.category_id, j.content from (select C.cluster_id, A.title, A.imageUrl, A.publishedDate, A.url, CI.diameter, CI.recency, CI.averageDate, CI.totalPoints, CI.rssLinks, A.category_id, A.content from articles A join clusterArticleRelationship C on A.id = C.articleId join clusterInfo CI on CI.id = C.cluster_id order by A.publishedDate) as j where j.category_id = " + 3 + " and j.cluster_id  in (select (cluster_id) from clusterArticleRelationship group by cluster_id having count(cluster_id) >= 3) order by  recency desc;";
//                System.out.println(query);
//                PreparedStatement preparedStatement = connnection.prepareStatement(query);
//                sports = preparedStatement.executeQuery();
//                map.put(3, sports);
//                Thread r = new Thread() {
//                    @Override
//                    public void run() {
//                        try {
//                            Thread.sleep(300000);
//                            map.put(3, null);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                };
//                r.start();
//            }
//        }
//        return map.get(3);
//    }
//
//
//    public static  synchronized ResultSet getScitech() throws SQLException {
//        ResultSet sports;
//        synchronized (map) {
//            sports = map.get(4);
//            if (sports == null) {
//                String query = "select j.cluster_id, j.title, j.imageUrl, j.url, j.publishedDate, j.diameter, j.recency, j.averageDate, j.totalPoints, j.rssLinks, j.category_id, j.content from (select C.cluster_id, A.title, A.imageUrl, A.publishedDate, A.url, CI.diameter, CI.recency, CI.averageDate, CI.totalPoints, CI.rssLinks, A.category_id, A.content from articles A join clusterArticleRelationship C on A.id = C.articleId join clusterInfo CI on CI.id = C.cluster_id order by A.publishedDate) as j where j.category_id = " + 4 + " and j.cluster_id  in (select (cluster_id) from clusterArticleRelationship group by cluster_id having count(cluster_id) >= 3) order by  recency desc;";
//                System.out.println(query);
//                PreparedStatement preparedStatement = connnection.prepareStatement(query);
//                sports = preparedStatement.executeQuery();
//                map.put(4, sports);
//                Thread r = new Thread() {
//                    @Override
//                    public void run() {
//                        try {
//                            Thread.sleep(300000);
//                            map.put(4, null);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                };
//                r.start();
//            }
//        }
//        return map.get(4);
//    }

}
