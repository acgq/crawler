package cn.acgq;

import cn.acgq.model.News;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MyBatisTest {

    private static SqlSessionFactory sessionFactory;

    static {
        String resourse = "db/mybatis/mybatis-config.xml";
        try {
            InputStream resourceStream = Resources.getResourceAsStream(resourse);
            sessionFactory = new SqlSessionFactoryBuilder().build(resourceStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSelect() {
        try (SqlSession sqlSession = sessionFactory.openSession()) {
            final List<News> objects = sqlSession.selectList("cn.acgq.dao.News.selectNews");
//            String linkFromProcessedLink = sqlSession.selectOne("cn.acgq.dao.NewsMapper.selectNextLinkToProcess");
        }
    }
}
