package cn.acgq.dao;

import cn.acgq.model.News;

import java.sql.SQLException;
import java.util.Map;

public interface CrawlerDao {
    boolean isProcessedLink(String link) throws SQLException;

    void insertLinkToProcessedLinks(String link);

    void insertNewsToDatabase(News news);

    String getFromLinkToBeProcessed();

    void deleteFromLinkToBeProcessed(String link);

    void insertLinksToProcess(String link);
}
