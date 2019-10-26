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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:69.0) Gecko/20100101 Firefox/69.0";
    private static final Pattern pattern = Pattern.compile("http[s]?://(\\w{1,10}\\.)?sina\\.cn.*");

    public static void main(String[] args) throws IOException {

        //
        List<String> linkPool;
        //
        Set<String> processedLinks = new HashSet<>();
        //创建Http客户端
        CloseableHttpClient httpclient = HttpClients.createDefault();
        //获取首次要爬取的链接
        linkPool = getLinksFromPage("https://sina.cn", httpclient);

        while (linkPool.size() > 0) {
            String link = linkPool.remove(linkPool.size() - 1);

            if (processedLinks.contains(link) || !isInterestedSite(link)) {
                continue;
            }
            List<Element> articles = parseAndGetArticle(httpclient, link);
            processedLinks.add(link);
            if (articles.size() == 0) {
                continue;
            }
            for (Element article : articles) {
                System.out.println(article.select("h1").text());
            }

        }
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
        System.out.println(link);
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
     * @return
     */
    private static List<String> getLinksFromPage(String url, CloseableHttpClient httpclient) {
        List<String> linkPool = new ArrayList<>();
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

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return linkPool;
    }
}
