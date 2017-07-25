package com.github.srad.metaquery;

import com.github.srad.metaquery.dbms.executor.GraphQLExecutor;
import com.github.srad.metaquery.net.HttpServer;
import com.github.srad.metaquery.dbms.storage.redis.RedisStorage;

public class main {

    private static HttpServer<GraphQLExecutor, RedisStorage> server;

    public static void main(String[] args) {
        try {
            server = new HttpServer<>(GraphQLExecutor.class, RedisStorage.class);
            server.start();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            server.stop();
            System.exit(1);
        }
    }
}
