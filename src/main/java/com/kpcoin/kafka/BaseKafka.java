package com.kpcoin.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.serializer.Decoder;
import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;

public abstract class BaseKafka {

	protected static Logger logger = Logger.getLogger(BaseKafka.class);

    protected static final String configFile = "kafka.properties";

    protected static String zkConnect;
    protected static String metadataBrokerList;
    protected static String brokerList;
    protected static String serialClass;
    protected static String keySerialClass;
    protected static String acks;

    protected static PropertiesConfiguration KAFKA_CONFIG;

    static {
        try {
            KAFKA_CONFIG = new PropertiesConfiguration(configFile);
            KAFKA_CONFIG.setReloadingStrategy(new FileChangedReloadingStrategy());

            zkConnect           = KAFKA_CONFIG.getString("zookeeper.connect");
            metadataBrokerList  = KAFKA_CONFIG.getString("metadata.broker.list");
            brokerList          = KAFKA_CONFIG.getString("broker.list");
            serialClass         = KAFKA_CONFIG.getString("serializer.class");
            keySerialClass      = KAFKA_CONFIG.getString("key.serializer.class");
            acks                = KAFKA_CONFIG.getString("request.required.acks");

        } catch (ConfigurationException e) {
            e.printStackTrace();
            logger.error(e);
        }
    }

    /**
     * 创建consumerConfig
     * @param groupId
     * @param offset
     * @return
     */
    public static ConsumerConfig createConsumerConfig(String groupId, String offset) {
        Properties props = new Properties();

        props.put("zookeeper.connect", KAFKA_CONFIG.getString("zookeeper.connect"));
        props.put("serializer.class", KAFKA_CONFIG.getString("serializer.class"));
        props.put("group.id", groupId);//kafka根据groupId区分连接的客户端
        props.put("auto.offset.reset", offset);//smallest从上次停止的数据开始消费

        return new ConsumerConfig(props);
    }

    /**
     *
     * @param groupId
     * @param offset
     * @return
     */
    public static ConsumerConnector createConsumer(String groupId, String offset) {
        Properties properties = new Properties();
        
        properties.put("zookeeper.connect", zkConnect);//声明zk
        properties.put("group.id", groupId);// 必须要使用别的组名称， 如果生产者和消费者都在同一组，则不能访问同一组内的topic数据
        properties.put("auto.offset.reset", offset);//smallest,largest
        properties.put("bootstrap.servers", brokerList);
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        
        return Consumer.createJavaConsumerConnector(new ConsumerConfig(properties));
    }

    /**
     * 创建kafka消费流
     * @param topicName
     * @param groupId
     * @param offset
     * @param workNum
     * @return
     */
    public static Map<String, List<KafkaStream<String, String>>> createMessageStream(String topicName, String groupId, String offset, int workNum) {
        ConsumerConfig config = createConsumerConfig(groupId, offset);
        ConsumerConnector consumer = Consumer.createJavaConsumerConnector(config);

        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topicName, workNum);//1代表的是单线程

        Decoder<String> keyDecoder = new StringDecoder(new VerifiableProperties());
        Decoder<String> valueDecoder = new StringDecoder(new VerifiableProperties());

        Map<String, List<KafkaStream<String, String>>> createMessageStreams = consumer.createMessageStreams(topicCountMap, keyDecoder, valueDecoder);

        return createMessageStreams;
    }

    public static void main(String[] args) {
        System.out.println(zkConnect);
        System.out.println(metadataBrokerList);
        System.out.println(brokerList);
        System.out.println(serialClass);
        System.out.println(keySerialClass);
        System.out.println(acks);
    }
	
}
