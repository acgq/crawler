package cn.acgq.dao;

import cn.acgq.model.News;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class MybatisCrawlerDao implements CrawlerDao {

    private static SqlSessionFactory sessionFactory;

    static {
        String resource = "db/mybatis/mybatis-config.xml";
        try {
            InputStream resourceStream = Resources.getResourceAsStream(resource);
            sessionFactory = new SqlSessionFactoryBuilder().build(resourceStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isProcessedLink(String link) throws SQLException {
        try (SqlSession sqlSession = sessionFactory.openSession()) {
            int count = sqlSession.selectOne("cn.acgq.dao.NewsMapper.selectLinkFromProcessedLink", link);
            if (count != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void insertLinkToProcessedLinks(String link) {
        try (SqlSession sqlSession = sessionFactory.openSession(true)) {
            sqlSession.insert("cn.acgq.dao.NewsMapper.insertLinkProcessed", link);
        }
    }


    @Override
    public void insertNewsToDatabase(News news) {
        try (SqlSession sqlSession = sessionFactory.openSession(true)) {
            sqlSession.insert("cn.acgq.dao.NewsMapper.insertNewsIntoDataBase", news);
        }
    }

    @Override
    public String getFromLinkToBeProcessed() {
        try (SqlSession sqlSession = sessionFactory.openSession(true)) {
            return sqlSession.selectOne("cn.acgq.dao.NewsMapper.selectNextLinkToProcess");
        }
    }

    @Override
    public void deleteFromLinkToBeProcessed(String link) {
        try (SqlSession sqlSession = sessionFactory.openSession(true)) {
            sqlSession.selectOne("cn.acgq.dao.NewsMapper.deleteLinkProcessed", link);
        }
    }


    @Override
    public void insertLinksToProcess(String link) {
        try (SqlSession sqlSession = sessionFactory.openSession(true)) {
            sqlSession.insert("cn.acgq.dao.NewsMapper.insertLinkToBeProcessed", link);
        }
    }
}

