package com.github.hcsp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import java.util.stream.Collectors;

public class Crawler extends Thread {


   private CrawlerDao dao ;

    public Crawler(CrawlerDao dao) {
        this.dao = dao;
    }

    @Override
    public void run() {
        try {
            String link;
            //从数据库中加载下一个链接,如果能加载到,则进行循环
            while ((link = dao.getNextLinkThenDelete()) != null) {

                //如果处理过了，什么都不做
                if (dao.isLinkProcessed(link)) {
                    continue;
                }
                //这里的\\/里面的第一个符号为转移符号
                if (isInterestingLink(link)) {

                    Document doc = httpGetAndParseHtml(link);

                    parseUrlsFromPageAndStoreIntoDatabase(doc);

                    //假如这是一个新闻的详情页面,就存入数据库,否则,就什么都不做
                    //有注释的地方就可以会被重构
                    StoreIntoDatabaseIfItisNewsPage(doc, link);
                    dao.insertProcessedLink(link);
//                dao.updateDatabase(link, "insert into links_already_processed (link)values (?)");

                }

            }
        }catch (Exception e){
            //实在不知道就写下面这个
            throw new RuntimeException(e);
        }
    }

    private void parseUrlsFromPageAndStoreIntoDatabase(Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");

            if (href.startsWith("//")) {
                href = "https:" + href;
            }
            if (!(href.toLowerCase().startsWith("javascript"))) {
                dao.insertLinkToBeProcessed(href);
//                dao.updateDatabase(href, "insert into LINKS_TO_BE_PROCESSED (link)values (?)");
            }
        }
    }


    private void StoreIntoDatabaseIfItisNewsPage(Document doc, String link) throws SQLException {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTags.get(0).child(0).text();
                String content = articleTag.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));
//                System.out.println(title);
                dao.insertNewsIntoDatabase(link, title, content);

            }
        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {

        //这是我们感兴趣的,我们只处理新浪站内的链接
        //就是拿到它的数据
        CloseableHttpClient httpclient = HttpClients.createDefault();
        System.out.println(link);

        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36");
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            System.out.println(response1.getStatusLine());
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);
            return Jsoup.parse(html);
            //select：css选择器

        }
    }

    private static boolean isInterestingLink(String link) {
        return IsHomePage(link) && IsNewsPage(link) && IsillegalString(link) || IsIndexPage(link)
                && IsNotLoginPage(link);
    }

    private static boolean IsHomePage(String link) {
        return link.contains("sina.cn");
    }

    private static boolean IsIndexPage(String link) {
        return "https://sina.cn/".equals(link);
    }

    private static boolean IsillegalString(String link) {
        return !link.contains("\\/");
    }

    private static boolean IsNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean IsNotLoginPage(String link) {
        return !link.contains("passport.sina.cn");

    }
}
