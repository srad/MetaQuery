package main.java.org.srad.textimager;

public class main {

    private static Application app;

    public static void main(String[] args) {
        try {
            String importFolder = "/home/saman/Downloads/bio";
            if (args.length > 0) {
                importFolder = args[0];
            }
            app = new Application(importFolder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
