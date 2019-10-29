package cn.acgq.crewler;

import cn.acgq.dao.CrewlerDao;
import cn.acgq.dao.MybatisCrewlerDao;

public class Main {
    public static void main(String[] args) {
        final long start = System.currentTimeMillis();
        CrewlerDao dao = new MybatisCrewlerDao();
        for (int i = 0; i < 10; i++) {
            new SinaCrewler(dao).run();
        }

    }
}
