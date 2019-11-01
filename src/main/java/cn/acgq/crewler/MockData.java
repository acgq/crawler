package cn.acgq.crewler;

import cn.acgq.model.News;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

//创建一个包含百万数据的表
public class MockData {
    public static void main(String[] args) throws IOException {
        int totalAmount = 100_0000;
        String resource = "db/mybatis/mybatis-config.xml";
        InputStream resourceStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(resourceStream);
        mock(sessionFactory, totalAmount);

    }

    private static void mock(SqlSessionFactory sqlSessionFactory, int totalAmountInDatabase) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            List<News> news = sqlSession.selectList("cn.acgq.dao.News.selectNews");
            int size = news.size();
            Random random = new Random();
            int count = totalAmountInDatabase - size;
            try {
                while (count-- > 0) {
                    News one = news.get(random.nextInt(size));
                    Instant modifyDate = one.getModifyDate();
                    modifyDate.minusSeconds(random.nextInt(1000)*3600);
                    one.setModifyDate(modifyDate);
                    sqlSession.insert("cn.acgq.dao.News.insertNews", one);
                    System.out.println("left:" + count);
                    if (count % 5000 == 0) {
                        sqlSession.flushStatements();
                    }
                }
                sqlSession.commit();
            } catch (Exception e) {
                sqlSession.rollback();
                e.printStackTrace();
            }
        }
    }
}
