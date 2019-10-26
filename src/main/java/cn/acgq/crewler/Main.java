package cn.acgq.crewler;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:69.0) Gecko/20100101 Firefox/69.0";
    private static final Pattern pattern = Pattern.compile("http[s]?://(\\w{1,10}\\.)?sina\\.cn.*");

    public static void main(String[] args) throws IOException, SQLException {
        List<String> linkPool;

        Connection connection = DriverManager.getConnection("jdbc:h2:file:D:\\Code\\crawler\\test", "root", "root");
        linkPool = getLinksFromDatabase(connection, "select LINK from LINKS_TO_BE_PROCESSED");
        //
        //从数据库获取已爬取的链接
        Set<String> processedLinks = new HashSet<>(getLinksFromDatabase(connection, "select LINK from LINKS_PROCESSED"));

        //创建Http客户端
        CloseableHttpClient httpclient = HttpClients.createDefault();
        //获取首次要爬取的链接
        if (linkPool.size() == 0) {
            linkPool = getLinksFromPage("https://sina.cn", httpclient, connection);
        }
        while (linkPool.size() > 0) {
            String link = linkPool.remove(linkPool.size() - 1);
            if (processedLinks.contains(link) || !isInterestedSite(link)) {
                continue;
            }
            List<Element> articles = parseAndGetArticle(httpclient, link);
            processedLinks.add(link);
            updateLinkDatabase(connection, link);
            if (articles.size() == 0) {
                continue;
            }
            for (Element article : articles) {
                String title = article.select("h1").text();
                System.out.println(title);
                Map<String, String> articleMap = new HashMap<>();
                articleMap.put("title", title);
                articleMap.put("src", link);
                updateNews(connection, articleMap);
            }

        }
    }

    private static int updateNews(Connection connection, Map<String, String> articleMap) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("insert into NEWS (TITLE,SRC) values (?,?)");
        preparedStatement.setString(1, articleMap.get("title"));
        preparedStatement.setString(2, articleMap.get("src"));
        return preparedStatement.executeUpdate();
    }

    private static void updateLinkDatabase(Connection connection, String link) throws SQLException {
        //添加已爬取链接
        PreparedStatement preparedStatement = connection.prepareStatement("insert into LINKS_PROCESSED (LINK) values (?)");
        preparedStatement.setString(1, link);
        preparedStatement.execute();
    }

    private static List<String> getLinksFromDatabase(Connection connection, String sql) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        List<String> linkPool = new ArrayList<>();
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            linkPool.add(resultSet.getString(1));
        }
        return linkPool;
    }

    /**
     * 从页面中提取文章
     *
     * @param httpclient
     * @param link
     * @return
     * @throws IOException
     */
    private static List<Element> parseAndGetArticle(CloseableHttpClient httpclient, String link) throws IOException {
        HttpGet linkHttpGet = new HttpGet(link);
        linkHttpGet.setHeader("User-Agent", USER_AGENT);
        CloseableHttpResponse response = httpclient.execute(linkHttpGet);
        String responseHtml = EntityUtils.toString(response.getEntity());
        return Jsoup.parse(responseHtml).getElementsByTag("article");
    }

    /**
     * 是否是需要爬取页面
     *
     * @param link
     * @return
     */
    private static boolean isInterestedSite(String link) {
        Matcher matcher = pattern.matcher(link);
        return matcher.matches();
    }

    /**
     * 获取页面中所有链接
     *
     * @param url
     * @param httpclient
     * @param connection
     * @return
     */
    private static List<String> getLinksFromPage(String url, CloseableHttpClient httpclient, Connection connection) throws SQLException {
        List<String> linkPool;
        PreparedStatement preparedStatement = connection.prepareStatement("insert into LINKS_TO_BE_PROCESSED (LINK) values (?)");
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", USER_AGENT);
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            System.out.println(response1.getStatusLine());
            HttpEntity entity = response1.getEntity();
            String htmlGetFromLink = EntityUtils.toString(entity);
            Document doc = Jsoup.parse(htmlGetFromLink);
            List<Element> aTag = doc.getElementsByTag("a");
//            过滤掉js,空链接,
            linkPool = aTag.stream().map(element -> element.attr("href"))
                    .filter(s -> !s.toLowerCase().startsWith("javascript"))
                    .filter(s -> !s.equals(""))
                    .collect(Collectors.toList());
            for (String s : linkPool) {
                try {
                    preparedStatement.setString(1, s);
                    preparedStatement.execute();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return linkPool;
    }
}
