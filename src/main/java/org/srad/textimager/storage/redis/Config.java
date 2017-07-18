package main.java.org.srad.textimager.storage.redis;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;

import java.util.concurrent.TimeUnit;

public class Config {
    final public static int RedisPort = 6379;
    final public static int ArdbPort = 16379;

    public static RedisClient createClient() {
        return RedisClient.create(new RedisURI("localhost", ArdbPort, 10, TimeUnit.MINUTES));
    }
}
