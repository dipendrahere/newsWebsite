import databaseService.DBConnect;
import models.Log;

import static spark.Spark.*;

public class Setup {
    private static final String version = "/v1";
    public static void main(String[] args) {

        port(9000);
        options("/*",
                (request, response) -> {

                    String accessControlRequestHeaders = request
                            .headers("Access-Control-Request-Headers");
                    if (accessControlRequestHeaders != null) {
                        response.header("Access-Control-Allow-Headers",
                                accessControlRequestHeaders);
                    }

                    String accessControlRequestMethod = request
                            .headers("Access-Control-Request-Method");
                    if (accessControlRequestMethod != null) {
                        response.header("Access-Control-Allow-Methods",
                                accessControlRequestMethod);
                    }

                    return "OK";
                });

        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));

        NewsService newsService = NewsService.getInstance();
        get("/news", ((request, response) -> {
            try {
                if (request.queryParams().contains("categoryId")) {
                    int category_id = Integer.parseInt(request.queryParams("categoryId"));
                    if (category_id < 1 || category_id > 4) {
                        throw new Exception("Bad category ID");
                    }
                    return NewsService.getInstance().getTopNews(category_id);
                }
                response.status(400);
                return "Send category Id";
            }
            catch (Exception e){
                return e.getMessage();
            }
        }));

        get("/articles",(((request, response) -> {
            try {
                if (request.queryParams().contains("clusterId")) {
                    int clusterId = Integer.parseInt(request.queryParams("clusterId"));
                    if (clusterId < 1) {
                        throw new Exception("Bad cluster ID");
                    }
                    return NewsService.getInstance().getArticles(clusterId);
                }
                response.status(400);
                return "Send category Id";
            }
            catch (Exception e){
                return e.getMessage();
            }
        })));


        get("/all", ((request, response) -> {
            return NewsService.getInstance().getAllClusters();
        }));

    }
}
