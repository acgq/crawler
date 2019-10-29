package cn.acgq.dao;

import java.sql.SQLException;
import java.util.Map;

public interface CrewlerDao {
    boolean isProcessedLink(String link) throws SQLException;

    void insertLinkToProcessedLinks(String link);

    void insertNewsToDatabase(Map<String, String> articleMap);

    String getFromLinkToBeProcessed();

    void deleteFromLinkToBeProcessed(String link);

    void insertLinksToProcess(String link);
}
