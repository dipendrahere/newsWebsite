package models;

import java.util.Date;

public class ArticleBuilder {
    private Article article;

    public ArticleBuilder(String url){
        article = new Article(url);
    }

    public Article build(){
        Article a = new Article(article.getUrl());
        a.setCategoryType(article.getCategoryType());
        a.setContent(article.getContent());
        a.setPublishedDate(article.getPublishedDate());
        a.setRssLink(article.getRssLink());
        a.setTitle(article.getTitle());
        a.setImageUrl(article.getImageUrl());
        return a;
    }

    public ArticleBuilder setCategoryType(CategoryType categoryType){
        article.setCategoryType(categoryType);
        return this;
    }

    public ArticleBuilder setTitle(String title){
        article.setTitle(title);
        return this;
    }
    public ArticleBuilder setContent(String content){
        article.setContent(content);
        return this;
    }

    public ArticleBuilder setPublishedDate(Date date){
        article.setPublishedDate(date);
        return this;
    }

    public ArticleBuilder setRssLink(String link){
        article.setRssLink(link);
        return this;
    }

    public ArticleBuilder setImageUrl(String imageUrl) {
        article.setImageUrl(imageUrl);
        return this;
    }

    public ArticleBuilder setId(String id){
        article.setId(id);
        return this;
    }

}
