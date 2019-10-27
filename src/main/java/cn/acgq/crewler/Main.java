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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:69.0) Gecko/20100101 Firefox/69.0";
    private static final Pattern pattern = Pattern.compile("http[s]?://(\\w{1,10}\\.)?sina\\.cn.*");

    public static void main(String[] args) throws IOException, SQLException {
        CrewlerDao dao = new JDBCCrewlerDao();
        //create database connection
        Connection connection = DriverManager.getConnection("jdbc:h2:file:D:\\Code\\crawler\\test", "root", "root");

        //get next link to be processed
        String link = getNextLinkToProcess(dao);

        //获取首次要爬取的链接
        if (link == null) {
            link = new String("https://sina.cn");
        }
        while (link != null) {
            if ((!link.equals("https://sina.cn") && dao.isProcessedLink(link)) || !isInterestedSite(link)) {
                link = getNextLinkToProcess(dao);
                continue;
            }
            ArrayList<Element> articles = getArticlesFromLink(link);
            if (!isArticle(articles)) {
                List<String> linksFromPage = getLinksFromPage(link);
                dao.storeLinksToDataBase(linksFromPage);
            } else {
                Map<String, String> articleMap = extractNewsFromArticle(link, articles);
                dao.updateNewsDatabase(articleMap);
            }
            dao.updateProcessedLinkDatabase(link);
            link = getNextLinkToProcess(dao);
        }
    }

    /**
     * 获取下一个爬取链接
     *
     * @param dao
     * @return
     */
    private static String getNextLinkToProcess(CrewlerDao dao) {
        String link = dao.getNextLink();
        dao.deleteLink(link);
        return link;
    }

    /**
     * 从文章网页提取内容
     *
     * @param link
     * @param articles
     * @return
     */
    private static Map<String, String> extractNewsFromArticle(String link, ArrayList<Element> articles) {
        Element article = articles.get(0);
        String title = article.select("h1").text();
        String body = article.select("p").stream()
                .map(element -> element.text())
                .collect(Collectors.joining("\n"));
        System.out.println(title);
        Map<String, String> articleMap = new HashMap<>();
        articleMap.put("title", title);
        articleMap.put("src", link);
        articleMap.put("body", body);
        return articleMap;
    }

    /**
     * 从链接指向的网页中抽取出文章部分
     *
     * @param link
     * @return
     * @throws IOException
     */
    private static ArrayList<Element> getArticlesFromLink(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet linkHttpGet = new HttpGet(link);
        linkHttpGet.setHeader("User-Agent", USER_AGENT);
        CloseableHttpResponse response = httpclient.execute(linkHttpGet);
        HttpEntity entity = response.getEntity();
        String responseHtml = EntityUtils.toString(response.getEntity());
        Document doc = Jsoup.parse(responseHtml);
        return doc.select("article");
    }

    /**
     * 判断是不是文章
     *
     * @param articles
     * @return
     */
    private static boolean isArticle(List<Element> articles) {
        return articles.size() == 0 ? false : true;

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
     * @return
     */
    private static List<String> getLinksFromPage(String url) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        List<String> linkPool;

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", USER_AGENT);
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            System.out.println(response1.getStatusLine());
            HttpEntity entity = response1.getEntity();
            String htmlGetFromLink = EntityUtils.toString(entity);
            Document doc = Jsoup.parse(htmlGetFromLink);
            List<Element> aTag = doc.getElementsByTag("a");
//            过滤掉js,空链接,sina.cn以外的链接
            linkPool = aTag.stream().map(element -> element.attr("href"))
                    .filter(s -> !s.toLowerCase().startsWith("javascript"))
                    .filter(s -> s.contains("sina.cn"))
                    .filter(s -> !s.equals(""))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return linkPool;
    }
}
