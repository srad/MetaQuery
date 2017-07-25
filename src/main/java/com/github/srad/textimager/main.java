package com.github.srad.textimager;

import com.github.srad.textimager.model.graphql.GraphQLExecutor;
import com.github.srad.textimager.net.Rest;
import com.github.srad.textimager.storage.redis.RedisStorage;

public class main {

    private static CasImporter app;

    private static Rest<GraphQLExecutor, RedisStorage> server;

    final private static boolean startServer = true;

    final private static boolean importFiles = false;

    public static void main(String[] args) {
        try {
            String importFolder = "/home/saman/Downloads/bio";
            if (args.length > 0) {
                importFolder = args[0];
            }

            if (startServer) {
                server = new Rest<>(GraphQLExecutor.class, RedisStorage.class);
                server.start();
            }

            if (importFiles) {
                app = new CasImporter(new CasImporterConfig(importFolder));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            server.stop();
            System.exit(1);
        }
    }
}
