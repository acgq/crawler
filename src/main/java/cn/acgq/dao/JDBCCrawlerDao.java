package cn.acgq.dao;

import cn.acgq.model.News;

import java.sql.*;
import java.util.Map;

public class JDBCCrawlerDao implements CrawlerDao {
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
    public void insertLinkToProcessedLinks(String link) {
        //添加已爬取链接

        try (PreparedStatement preparedStatement = connection.prepareStatement("insert into LINKS_PROCESSED (LINK) values (?)")) {
            preparedStatement.setString(1, link);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }
    }

    @Override
    public void insertNewsToDatabase(News news) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("insert into NEWS (TITLE,SRC,BODY) values (?,?,?)")) {
            preparedStatement.setString(1, news.getTitle());
            preparedStatement.setString(2, news.getSrc());
            preparedStatement.setString(3, news.getBody());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getFromLinkToBeProcessed() {
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
    public void deleteFromLinkToBeProcessed(String link) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("delete from LINKS_TO_BE_PROCESSED where LINK=?")) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void insertLinksToProcess(String link) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("insert into LINKS_TO_BE_PROCESSED (LINK) values ( ? )")) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
