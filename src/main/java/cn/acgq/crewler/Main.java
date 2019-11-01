package cn.acgq.crewler;

import cn.acgq.dao.CrawlerDao;
import cn.acgq.dao.MybatisCrawlerDao;

public class Main {
    public static void main(String[] args) {
        final long start = System.currentTimeMillis();
        CrawlerDao dao = new MybatisCrawlerDao();
        for (int i = 0; i < 10; i++) {
            new SinaCrawler(dao).run();
        }

    }
}
