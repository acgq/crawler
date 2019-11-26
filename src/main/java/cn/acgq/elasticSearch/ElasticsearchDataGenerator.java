package cn.acgq.elasticSearch;

import cn.acgq.model.News;
import org.apache.http.HttpHost;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 往elasticsearch中插入大量数据
 */
public class ElasticsearchDataGenerator {
    public static void main(String[] args) {
        SqlSessionFactory sessionFactory;
        try {
            String resource = "db/mybatis/mybatis-config.xml";
            InputStream resourceStream = Resources.getResourceAsStream(resource);
            sessionFactory = new SqlSessionFactoryBuilder().build(resourceStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<News> newsFromDatabase = getNewsFromDatabase(sessionFactory);
        //开启5个线程进行写入操作
        for (int i = 0; i < 5; i++) {
            new Thread(() -> writeSingleThread(newsFromDatabase)).start();
        }
    }

    private static void writeSingleThread(List<News> newsFromDatabase) {
        try (RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")))) {
            //单线程写入10倍数据库中数据
            for (int i = 0; i < 100; i++) {
                BulkRequest bulkRequest = new BulkRequest();
                for (News news : newsFromDatabase) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("title", news.getTitle());
                    map.put("src", news.getSrc());
                    map.put("body", news.getBody().length() > 10 ? news.getBody().substring(0, 10) : news.getBody());
                    map.put("createDate", news.getCreateDate());
                    map.put("modifyDate", news.getModifyDate());
                    IndexRequest request = new IndexRequest("news");
                    request.id();
                    request.source(map, XContentType.JSON);
                    bulkRequest.add(request);
                }
                BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                System.out.println(Thread.currentThread().getName() + "第"+i+"写入状态" + bulkResponse.status().getStatus());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<News> getNewsFromDatabase(SqlSessionFactory sessionFactory) {
        try (SqlSession sqlSession = sessionFactory.openSession()) {
            List<News> news = sqlSession.selectList("cn.acgq.dao.News.selectNewsFromOrigin");
            return news;
        }
    }


}
