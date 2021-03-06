package edu.bigdata.kafkaspark;

import edu.bigdata.kafkaspark.helper.Constants;
import edu.bigdata.kafkaspark.spark.SerializableSparkModule;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import scala.Tuple2;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Main implements Serializable {

    private static SerializableSparkModule sparkModule = new SerializableSparkModule(Constants.kafkaProducer(), Constants.aspectCategory());

    public static void main(String[] args) {
        Logger.getLogger("org").setLevel(Level.OFF);
        Logger.getLogger("akka").setLevel(Level.OFF);

        SparkConf sparkConfig = new SparkConf().setAppName("kafka_spark_integration")
                                               .setMaster("local[*]");
        JavaSparkContext sparkContext = new JavaSparkContext(sparkConfig);
        JavaStreamingContext streamingContext = new JavaStreamingContext(sparkContext, new Duration(500));

        String hostname = Constants.zooKeeperHostName();
        String consumerGroupId = Constants.consumerGroupId();
        Map<String, Integer> topicsConfig = new HashMap<>();
        topicsConfig.put(Constants.RECEIVING_TOPIC, Constants.NUMBER_OF_PARTITIONS_TO_CONSUME);
        JavaPairInputDStream<String, String> kafkaStream = KafkaUtils.createStream(streamingContext, hostname,
                                                                                consumerGroupId, topicsConfig,
                                                                            StorageLevel.MEMORY_AND_DISK_SER());

        kafkaStream.foreachRDD((VoidFunction<JavaPairRDD<String, String>>) rdd -> {
            if (rdd.count() > 0) {
                int size = rdd.partitions().size();
                System.out.println("Partition size: " + size);
                rdd.foreach(record -> process(sparkModule, record));
            }
        });

        streamingContext.start();
        streamingContext.awaitTermination();
    }

    // Doing this because a Static closure is needed by the lambda using the function
    // This is because the Kafka producer cannot be serialized!
    private static void process(SerializableSparkModule sparkModule, Tuple2<String, String> record) {
        sparkModule.process(record);
    }
}
