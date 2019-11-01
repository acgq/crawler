package cn.acgq.elasticSearch;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 使用elasticsearch对数据进行分析处理
 */
public class ElasticsearchEngine {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String keyword = reader.readLine();
            if (keyword == "/quit") {
                break;
            }
            search(keyword);
        }
    }

    /**
     * 连接elasticsearch根据关键词在新闻标题和内容中查找相关项
     *
     * @param keyword
     */
    private static void search(String keyword) {
        try (RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")));) {

            SearchRequest searchRequest = new SearchRequest("news");

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(new MultiMatchQueryBuilder(keyword, "title", "body"));
            searchRequest.source(searchSourceBuilder);
            final SearchResponse result = client.search(searchRequest, RequestOptions.DEFAULT);
            result.getHits().forEach(documentFields -> System.out.println(documentFields.getSourceAsString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
