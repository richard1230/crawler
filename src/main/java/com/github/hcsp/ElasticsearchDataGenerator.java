package com.github.hcsp;

import org.apache.http.HttpHost;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElasticsearchDataGenerator extends Thread {

    public static void main(String[] args) throws IOException {
        SqlSessionFactory sqlSessionFactory;

        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<News> newsFromMySQL = getNewsFromMySQL(sqlSessionFactory);
        //RestHighLevelClient是closeable的,故必须放在trywithresources里面
        writeSingleThread(newsFromMySQL);
        //这里的i不宜过大,太大会占据cpu相当多的资源！这样会导致掉脑相当卡
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    writeSingleThread(newsFromMySQL);
                } catch (IOException e) {
                    throw new RuntimeException();
                }
            }).start();
        }
    }

    private static void writeSingleThread(List<News> newsFromMySQL) throws IOException {

        try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            //单线程写入2000*1000 = 200_0000
            for (int i = 0; i < 1000; i++) {
                //批处理
                BulkRequest bulkrequest = new BulkRequest();

                for (News news : newsFromMySQL) {
                    IndexRequest request = new IndexRequest("news");

                    Map<String, Object> data = new HashMap<>();
                    data.put("content", news.getContent().length() > 10 ? news.getContent().substring(0, 10) : news.getContent());
                    data.put("url", news.getUrl());
                    data.put("title", news.getTitle());
                    data.put("createdAt", news.getCreatedAt());
                    data.put("modifiedAt", news.getModifideAt());

                    request.source(data, XContentType.JSON);
                    bulkrequest.add(request);
//                     IndexResponse response = client.index(request, RequestOptions.DEFAULT);
//                     System.out.println(response.status().getStatus());

                }
                BulkResponse bulkResponse = client.bulk(bulkrequest, RequestOptions.DEFAULT);
                System.out.println("Current thread: " + Thread.currentThread().getName() + "finishes" + i + ": " + bulkResponse.status().getStatus());

            }

        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private static List<News> getNewsFromMySQL(SqlSessionFactory sqlSessionFactory) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectList("com.github.hcsp.MockMapper.selectNews");
        }
    }
}
