package es.um.unosql.redis2unosql.spark;

import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.storage.StorageLevel;

import com.redislabs.provider.redis.ReadWriteConfig;
import com.redislabs.provider.redis.RedisConfig;
import com.redislabs.provider.redis.RedisContext;

import es.um.unosql.redis2unosql.spark.map.IdKeyMapping;
import es.um.unosql.redis2unosql.spark.map.JSONMapping;
import scala.Tuple2;

public class SparkProcess implements ISparkProcess
{
    private static final String SPARK_REDIS_HOST = "spark.redis.host";
    private static final String SPARK_REDIS_PORT = "spark.redis.port";
    // private static final String SPARK_REDIS_AUTH = "spark.redis.auth";
    private static final String SPARK_REDIS_TIMEOUT = "spark.redis.timeout";
    private static final String SPARK_REDIS_SCAN_COUNT = "spark.redis.scan.count";

    private static final String APP_NAME = "redis2unosql";
    private static final String MASTER = String.format("local[%d]", Runtime.getRuntime().availableProcessors());

    @Override
    public Map<String, Long> process(String databaseUrl, int databasePort, String databaseName)
    {
        SparkConf sparkConf = new SparkConf().setAppName(APP_NAME)
                .setMaster(MASTER)
                .set(SPARK_REDIS_HOST, databaseUrl)
                .set(SPARK_REDIS_PORT, Integer.toString(databasePort))
                .set(SPARK_REDIS_TIMEOUT, Integer.toString(99999999))
                .set(SPARK_REDIS_SCAN_COUNT, Integer.toString(1000))
                .set("spark.executor.heartbeatInterval", Integer.toString(2000000))
                .set("spark.local.dir", "./tmp")
                .set("spark.network.timeout", Integer.toString(99999999))
                .set("spark.driver.memory", "24g")
                .set("spark.rdd.compress", String.valueOf(true))
                .set("spark.ui.enabled", String.valueOf(false))
                .set("spark.executor.memory", "24g");
//	      		.set(SPARK_REDIS_AUTH, "passwd");

        return process(sparkConf, databaseName);
    }

    private Map<String, Long> process(SparkConf sparkConf, String databaseName)
    {
        RedisConfig redisConfig = RedisConfig.fromSparkConf(sparkConf);
        ReadWriteConfig readWriteConfig = ReadWriteConfig.fromSparkConf(sparkConf);

        try (JavaSparkContext jsc = new JavaSparkContext(sparkConf))
        {
            RedisContext redisContext = new RedisContext(jsc.sc());
            int partitionNum = 1;// Runtime.getRuntime().availableProcessors() / 2;

            var result = redisContext.fromRedisKeyPattern("*", partitionNum, redisConfig, readWriteConfig).toJavaRDD()
                    .persist(StorageLevel.MEMORY_AND_DISK()).mapToPair(new IdKeyMapping()).reduceByKey((k1, k2) -> {
                        TreeSet<String> ts;
                        if (k1 instanceof TreeSet<?>)
                        {
                            ts = (TreeSet<String>) k1;
                            ts.addAll(k2);
                        } else if (k2 instanceof TreeSet<?>)
                        {
                            ts = (TreeSet<String>) k2;
                            ts.addAll(k1);
                        } else
                        {
                            ts = new TreeSet<>(k1);
                            ts.addAll(k2);
                        }
                        return ts;
                    }).persist(StorageLevel.MEMORY_AND_DISK())
                    .mapToPair(p -> new Tuple2<Collection<String>, Long>(p._2, 1L))
                    .reduceByKey((p1, p2) -> p1 + p2).mapToPair(new JSONMapping()).collectAsMap();

            jsc.close();

            return result;
        }
    }
}
