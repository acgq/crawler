package cn.acgq.crewler;


import cn.acgq.dao.CrawlerDao;
import cn.acgq.dao.MybatisCrawlerDao;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        CrawlerDao dao = new MybatisCrawlerDao();
        for (int i = 0; i < 4; i++) {
            new SinaCrawler(dao).start();
            Thread.sleep(5000);
        }
        System.out.println("你好");
    }
}
