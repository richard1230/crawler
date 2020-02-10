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

public class Main {
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "123456";


    private static String getNextLink(Connection connection, String sql) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return resultSet.getString(1);
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return null;
    }

    private static String getNextLinkThenDelete(Connection connection) throws SQLException {
        String link = getNextLink(connection, "select link from links_to_be_processed limit 1");
        if (link != null) {
            updateDatabase(connection, link, "delete from links_to_be_processed where link = ?");
        }
        return link;
    }

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:file:/Users/mac/github/java/java_30_crawler/crawler/news", USER_NAME, PASSWORD);
        String link;
        //从数据库中加载下一个链接,如果能加载到,则进行循环
        while ((link = getNextLinkThenDelete(connection)) != null) {

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
                StoreIntoDatabaseIfItisNewsPage(connection, doc, link);

                updateDatabase(connection, link, "insert into links_already_processed (link)values (?)");

            }

        }
    }

    private static void parseUrlsFromPageAndStoreIntoDatabase(Connection connection, Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");

            if (href.startsWith("//")) {
                href = "https:" + href;
            }
            if (!(href.toLowerCase().startsWith("javascript"))) {
                updateDatabase(connection, href, "insert into LINKS_TO_BE_PROCESSED (link)values (?)");
            }
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

    private static void updateDatabase(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();

        }
    }


    private static void StoreIntoDatabaseIfItisNewsPage(Connection connection, Document doc, String link) throws SQLException {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTags.get(0).child(0).text();
                String content = articleTag.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));
//                System.out.println(title);

                try (PreparedStatement statement = connection.prepareStatement("insert into NEWS (url,title,CONTENT,CREATED_AT,MODIFIED_AT) values (?,?,?,now(),now());")) {
                    statement.setString(1, link);
                    statement.setString(2, title);
                    statement.setString(3, content);
                    statement.executeUpdate();
                }
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
