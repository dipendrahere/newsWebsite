import com.google.gson.Gson;
import databaseService.DBConnect;

import java.util.HashMap;

public class NewsService {
        private static NewsService newsService = new NewsService();
        private Gson gson = new Gson();

        private NewsService(){
        }

        public static NewsService getInstance(){
            return newsService;
        }

    public String getTopNews(int categoryId) throws Exception{
            return gson.toJson(DBConnect.getInstance().getTopNews(categoryId));
    }

    public String getAllClusters() {
            return gson.toJson(DBConnect.getInstance().getListOfNews());
        }

    public String getArticles(int clusterId) {
            return gson.toJson(DBConnect.getInstance().getArticles(clusterId));
    }
}


