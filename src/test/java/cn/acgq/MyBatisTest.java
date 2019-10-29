package cn.acgq;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

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
            int count = sqlSession.selectOne("cn.acgq.dao.NewsMapper.selectLinkFromProcessedLink", "sina.cn");
//            String linkFromProcessedLink = sqlSession.selectOne("cn.acgq.dao.NewsMapper.selectNextLinkToProcess");
            System.out.println(count);
        }
    }
}
