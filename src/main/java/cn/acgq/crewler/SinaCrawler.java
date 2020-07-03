package cn.acgq.crewler;

import cn.acgq.dao.CrawlerDao;
import cn.acgq.model.News;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SinaCrawler extends Thread {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:69.0) Gecko/20100101 Firefox/69.0";
    //    private static final Pattern pattern = Pattern.compile("http[s]?://news\\.sina\\.cn.*");
    private static int count = 1;
    
    private CrawlerDao dao;
    
    public SinaCrawler(CrawlerDao dao) {
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
    
    private void storeLinksToProcess(CrawlerDao dao, Document doc) throws IOException {
        List<Element> aTag = doc.getElementsByTag("a");
//            过滤掉js,空链接,sina.cn以外的链接
        List<String> linkPool = aTag.stream().map(element -> element.attr("href"))
                .filter(s -> !s.toLowerCase().startsWith("javascript"))
//                .filter(s -> !s.equals(""))
                .map(s -> {
                    if (s.startsWith("//")) {
                        s.replace("//", "https://");
                    }
                    return s;
                })
                .filter(s -> s.contains("sina.cn"))
                .filter(s -> s.length() < 1000)
                .map(s -> s.replace("\\/", "/"))
                //删除请求参数
//                .map(s -> s.split("\\?")[0])
                .collect(Collectors.toList());
        for (String link : linkPool) {
//            System.out.println(link);
            dao.insertLinksToProcess(link);
        }
    }
    
    /**
     * 访问链接,返回网页内容
     *
     * @param link
     * @return
     */
    private HttpEntity getEntityFromLink(String link) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            HttpGet linkHttpGet = new HttpGet(link);
            linkHttpGet.setHeader("User-Agent", USER_AGENT);
            response = httpClient.execute(linkHttpGet);
        } catch (Exception e) {
            System.out.println("链接无法访问:" + link);
            return null;
        }
        return response.getEntity();
    }
    
    /**
     * 获取下一个爬取链接
     *
     * @param dao
     * @return
     */
    
    private String getNextLinkToProcess(CrawlerDao dao) {
        String link;
        synchronized (this.getClass()) {
            link = dao.getFromLinkToBeProcessed();
            dao.deleteFromLinkToBeProcessed(link);
        }
        return link;
    }
    
    /**
     * 从文章网页提取内容
     *
     * @param link
     * @param articles
     * @return
     */
    private News extractNewsFromArticle(String link, ArrayList<Element> articles) {
        Element article = articles.get(0);
        String title = article.select("h1").text();
        String body = article.select("p").stream()
                .map(element -> element.text())
                .collect(Collectors.joining("\n"));
        System.out.println(title);
        News news = new News();
        news.setTitle(title);
        news.setBody(body);
        news.setSrc(link);
        return news;
    }
    
    /**
     * 将新闻存储到数据库中
     *
     * @param dao
     * @param doc
     * @param link
     */
    private void storeNewsToDatabase(CrawlerDao dao, Document doc, String link) {
        ArrayList<Element> articles = doc.select("article");
        if (isArticle(articles)) {
            News news = extractNewsFromArticle(link, articles);
            dao.insertNewsToDatabase(news);
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
