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

public class Main {
    private static final String USER_NAME ="root";
    private static final String PASSWORD ="123456";


    private static List<String> loadUrlFromDatabase(Connection connection, String sql) throws SQLException {
        List<String> results = new ArrayList<>();
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
             resultSet = statement.executeQuery();
            while (resultSet.next()) {
                results.add(resultSet.getString(1));
            }
        }finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return results;
    }

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:file:/Users/mac/github/java/java_30_crawler/crawler/news", USER_NAME, PASSWORD);

        while (true) {
            //待处理的链接池
            List<String> linkpool = loadUrlFromDatabase(connection, "select link from links_to_be_processed");
            if (linkpool.isEmpty()) {
                break;
            }

            //从待处理池子里面捞一个来处理
            //处理完之后从池子中(包含数据库)中删除
            String link = linkpool.remove(linkpool.size() - 1);
            insertLinkIntoDatabase(connection, link, "delete from links_to_be_processed where link = ?");

            //如果处理过了，什么都不做
            if (isLinkProcessed(connection, link)) {
                continue;
            }
            //这里的\\/里面的第一个符号为转移符号
            if (isInterestingLink(link)) {

                Document doc = httpGetAndParseHtml(link);

                parseUrlsFromPageAndStoreIntoDatabase(connection, doc);

                //假如这是一个新闻的详情页面,就存入数据库,否则,就什么都不做
                //有注释的地方就可以会被重构
                StoreIntoDatabaseIfItisNewsPage(doc);

                insertLinkIntoDatabase(connection, link, "insert into links_already_processed (link)values (?)");

            }

        }
    }

    private static void parseUrlsFromPageAndStoreIntoDatabase(Connection connection, Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            insertLinkIntoDatabase(connection, href, "insert into LINKS_TO_BE_PROCESSED (link)values (?)");
        }
    }

    private static boolean isLinkProcessed(Connection connection, String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("select link from LINKS_ALREADY_PROCESSED where link = ?")) {
            statement.setString(1, link);
             resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return false;
    }

    private static void insertLinkIntoDatabase(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();

        }
    }


    private static void StoreIntoDatabaseIfItisNewsPage(Document doc) {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTags.get(0).child(0).text();
                System.out.println(title);
            }
        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {

        //这是我们感兴趣的,我们只处理新浪站内的链接
        //就是拿到它的数据
        CloseableHttpClient httpclient = HttpClients.createDefault();
        System.out.println(link);
        if (link.startsWith("//")) {
            link = "https:" + link;
        }
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
