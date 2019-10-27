package cn.acgq.crewler;

import java.sql.*;
import java.util.List;
import java.util.Map;

public class JDBCCrewlerDao implements CrewlerDao {
    //create database connection
    private static Connection connection;

    static {
        try {
            connection = DriverManager.getConnection("jdbc:h2:file:D:\\Code\\crawler\\test", "root", "root");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isProcessedLink(String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement("select LINK from LINKS_PROCESSED where LINK= ?")) {
            preparedStatement.setString(1, link);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        } finally {
            resultSet.close();
        }
        return false;
    }

    @Override
    public boolean updateProcessedLinkDatabase(String link) {
        //添加已爬取链接

        try (PreparedStatement preparedStatement = connection.prepareStatement("insert into LINKS_PROCESSED (LINK) values (?)")) {
            preparedStatement.setString(1, link);
            return preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }
    }

    @Override
    public int updateNewsDatabase(Map<String, String> articleMap) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("insert into NEWS (TITLE,SRC,BODY) values (?,?,?)")) {
            preparedStatement.setString(1, articleMap.get("title"));
            preparedStatement.setString(2, articleMap.get("src"));
            preparedStatement.setString(3, articleMap.get("body"));
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getNextLink() {
        try (PreparedStatement preparedStatement = connection.prepareStatement("select LINK from LINKS_TO_BE_PROCESSED limit 1")) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public int deleteLink(String link) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("delete from LINKS_TO_BE_PROCESSED where LINK=?")) {
            preparedStatement.setString(1, link);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void storeLinksToDataBase(List<String> links) {
        for (String link : links) {
            this.insertLinkToDatabase(link);
        }
    }

    @Override
    public void insertLinkToDatabase(String link) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("insert into LINKS_TO_BE_PROCESSED (LINK) values ( ? )")) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
