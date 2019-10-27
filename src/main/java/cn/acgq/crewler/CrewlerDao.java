package cn.acgq.crewler;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface CrewlerDao {
    boolean isProcessedLink(String link) throws SQLException;

    boolean updateProcessedLinkDatabase(String link);

    int updateNewsDatabase(Map<String, String> articleMap);

    String getNextLink();

    int deleteLink(String link);

    void stroeLinksToProcessIntoDatabase(List<String> links);
}
