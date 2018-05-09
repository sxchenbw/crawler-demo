package com.kpcoin.kafka;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbw on 2017/8/25.
 */
public class KafkaCommonConsumer extends BaseKafka {

    /**
     * 从指定topic消费数据
     * @param topicName
     * @param groupId
     * @param offset
     * @param workerNum
     * @return
     */
    public static ConsumerIterator<byte[], byte[]> topicStreamIterator(String topicName, String groupId, String offset, int workerNum) {
        ConsumerIterator<byte[], byte[]> iterator = null;
        try {
            ConsumerConnector consumer = createConsumer(groupId, offset);

            Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
            topicCountMap.put(topicName, workerNum); // 一次从主题中获取n个数据

            Map<String, List<KafkaStream<byte[], byte[]>>> messageStreams = consumer.createMessageStreams(topicCountMap);

            KafkaStream<byte[], byte[]> stream = messageStreams.get(topicName).get(0);// 获取每次接收到的这个数据
            iterator = stream.iterator();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return iterator;
    }
}
