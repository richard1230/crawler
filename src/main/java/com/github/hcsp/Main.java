package com.github.hcsp;
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
import java.util.*;
public class Main {
    public static void main(String[] args) throws IOException {

        //待处理的链接池
        List<String> linkpool = new ArrayList<>();
        //判断一个东西是不是在一个集合里面,用set
        //已经处理的链接池
        Set<String> processedLinks = new HashSet<>();
        linkpool.add("https://sina.cn/");

        while (true){
            if (linkpool.isEmpty()){
               break;
            }
            //Arraylist从尾部删除更有效率
            //remove会返回删除的那个元素,故这里不需要使用get函数
            String link = linkpool.remove(linkpool.size()-1);

            //如果处理过了，什么都不做
            if (processedLinks.contains(link)){
                continue;
            }
            //这里的\\/里面的第一个符号为转移符号
            if (link.contains("sina.cn") && !link.contains("passport.sina.cn") && link.contains("news.sina.cn") && !link.contains("\\/")|| "https://sina.cn/".equals(link) ) {

                //这是我们感兴趣的,我们只处理新浪站内的链接
                //就是拿到它的数据
                CloseableHttpClient httpclient = HttpClients.createDefault();

                System.out.println(link);
                if (link.startsWith("//")){
                    link = "https:" + link;
                }

                HttpGet httpGet = new HttpGet(link);
                httpGet.addHeader("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36");

                try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
                    System.out.println(response1.getStatusLine());
                    HttpEntity entity1 = response1.getEntity();
                     String html = EntityUtils.toString(entity1);

                    Document doc = Jsoup.parse(html);
                    //select：css选择器
                   ArrayList<Element> links  = doc.select("a");

                   for (Element aTag: links){
                      linkpool.add(aTag.attr("href"));

                   }

                   //假如这是一个新闻的详情页面,就存入数据库,否则,就什么都不做
                    ArrayList<Element> articleTags = doc.select("article");
                    if (!articleTags.isEmpty()){
                        for (Element articleTag:articleTags){
                            String title = articleTags.get(0).child(0).text();
                            System.out.println(title);
                        }
                    }
                        processedLinks.add(link);
                }
            } else {
                //这是我们不敢兴趣的,不处理它
                continue;
            }


        }



    }
}
