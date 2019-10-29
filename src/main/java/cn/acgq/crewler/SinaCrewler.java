package cn.acgq.crewler;

import cn.acgq.dao.CrewlerDao;
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
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SinaCrewler extends Thread {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:69.0) Gecko/20100101 Firefox/69.0";
//    private static final Pattern pattern = Pattern.compile("http[s]?://news\\.sina\\.cn.*");
    private static int count = 1;

    private CrewlerDao dao;

    public SinaCrewler(CrewlerDao dao) {
        this.dao = dao;
    }

    @Override
    public void run() {
        //get next link to be processed
        String link = getNextLinkToProcess(dao);
        //获取首次要爬取的链接
        if (link == null) {
            link = "https://sina.cn";
        }
        try {
            while (link != null) {
                if (!isFirstVisit(link) && (dao.isProcessedLink(link) || !isInterestedSite(link))) {
                    link = getNextLinkToProcess(dao);
                    continue;
                }
                System.out.println(link);
                HttpEntity entity = getEntityFromLink(link);
                if (entity != null) {
                    String htmlGetFromURL = EntityUtils.toString(entity);
                    Document doc = Jsoup.parse(htmlGetFromURL);
                    //将链接存入待处理,新闻存入数据库
                    storeLinksToProcess(dao, doc);
                    storeNewsToDatabase(dao, doc, link);
                }
                dao.insertLinkToProcessedLinks(link);
                link = getNextLinkToProcess(dao);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isFirstVisit(String link) {
        if ("https://sina.cn".equals(link)) {
            if (count > 0) {
                count--;
                return true;
            }
            return false;
        }
        return false;
    }

    private void storeLinksToProcess(CrewlerDao dao, Document doc) throws IOException {
        List<Element> aTag = doc.getElementsByTag("a");
//            过滤掉js,空链接,sina.cn以外的链接
        List<String> linkPool = aTag.stream().map(element -> element.attr("href"))
                .filter(s -> !s.toLowerCase().startsWith("javascript"))
//                .filter(s -> !s.equals(""))
                .filter(s -> s.contains("sina.cn"))
                //删除请求的参数
                .map(s -> s.split("\\?")[0])
                .collect(Collectors.toList());
        for (String link : linkPool) {
            dao.insertLinksToProcess(link);
        }
    }

    /**
     * 访问链接,返回网页内容
     *
     * @param link
     * @return
     * @throws IOException
     */
    private HttpEntity getEntityFromLink(String link) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet linkHttpGet = new HttpGet(link);
        linkHttpGet.setHeader("User-Agent", USER_AGENT);
        CloseableHttpResponse response = httpClient.execute(linkHttpGet);
        return response.getEntity();
    }

    /**
     * 获取下一个爬取链接
     *
     * @param dao
     * @return
     */
    private synchronized String getNextLinkToProcess(CrewlerDao dao) {
        String link = dao.getFromLinkToBeProcessed();
        dao.deleteFromLinkToBeProcessed(link);
        return link;
    }

    /**
     * 从文章网页提取内容
     *
     * @param link
     * @param articles
     * @return
     */
    private Map<String, String> extractNewsFromArticle(String link, ArrayList<Element> articles) {
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
        articleMap.put("createDate", new Date().toString());
        articleMap.put("modifyDate", new Date().toString());
        return articleMap;
    }

    /**
     * 将新闻存储到数据库中
     *
     * @param dao
     * @param doc
     * @param link
     */
    private void storeNewsToDatabase(CrewlerDao dao, Document doc, String link) {
        ArrayList<Element> articles = doc.select("article");
        if (isArticle(articles)) {
            Map<String, String> articleMap = extractNewsFromArticle(link, articles);
            dao.insertNewsToDatabase(articleMap);
        } else {
            //不处理
        }
    }

    /**
     * 判断是不是文章
     *
     * @param articles
     * @return
     */
    private boolean isArticle(List<Element> articles) {
        return articles.size() == 0 ? false : true;
    }

    /**
     * 是否是需要爬取页面
     *
     * @param link
     * @return
     */
    private boolean isInterestedSite(String link) {
        return link.contains("news.sina.cn");
    }
}
