package com.github.srad.metaquery;

import com.github.srad.metaquery.dbms.executor.GraphQLExecutor;
import com.github.srad.metaquery.net.HttpServer;
import com.github.srad.metaquery.dbms.storage.redis.RedisStorage;

public class main {

    private static MetaQuery app;

    private static HttpServer<GraphQLExecutor, RedisStorage> server;

    final private static boolean startServer = true;

    final private static boolean importFiles = false;

    public static void main(String[] args) {
        try {
            String importFolder = "/home/saman/Downloads/bio";
            if (args.length > 0) {
                importFolder = args[0];
            }

            if (startServer) {
                server = new HttpServer<>(GraphQLExecutor.class, RedisStorage.class);
                server.start();
            }

            if (importFiles) {
                app = new MetaQuery(new MetaQueryConfig(importFolder));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            server.stop();
            System.exit(1);
        }
    }
}
