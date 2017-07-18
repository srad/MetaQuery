package main.java.org.srad.textimager;

import main.java.org.srad.textimager.net.Rest;

public class main {

    private static CasImporter app;

    private static Rest server;

    final private static boolean startServer = false;

    final private static boolean importFiles = true;

    public static void main(String[] args) {
        try {
            String importFolder = "/home/saman/Downloads/bio";
            if (args.length > 0) {
                importFolder = args[0];
            }

            if (startServer) {
                server = new Rest();
            }

            if (importFiles) {
                app = new CasImporter(new CasImporterConfig(importFolder));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
