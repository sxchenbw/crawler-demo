package com.kpcoin.crawlers;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.kpcoin.kafka.KafkaCommonProducer;

public class BaseCrawler {
	
	public static Logger logger = Logger.getLogger(BaseCrawler.class);
	
	public static final String toutiaoNewsKey = "crawlToutiaoNews";
	public static final String yidianNewsKey = "crawlYidianNews";

	/**
	 * 
	 * @param topicName
	 * @param json
	 */
	public static void writeNewsInfoToKafka(String topicName, String key, JSONObject json) {
		if (StringUtils.isNotBlank(topicName) && json != null && !json.isEmpty()) {
			KafkaCommonProducer.writeMsg(topicName, key, json.toJSONString());
		}
	}
	
	/**
	 * topicName:ToutiaoYidianNewsInfo
	 * @param topicName
	 * @param key
	 * @param title
	 * @param source
	 * @param pubtime
	 * @param thumbnail
	 * @param content
	 * @param keywords
	 * @param label
	 * @param sourceLink
	 */
	public static void writeNewsInfoToKafka(String topicName, String key, String title, String source, String pubtime, String thumbnail, String content,
			String keywords, String label, String sourceLink) {
		
		if (StringUtils.isNoneBlank(title, source, content)) {
			JSONObject json = new JSONObject();
			
			json.put("title", title);
			json.put("source", source);
			json.put("pubtime", pubtime);
			json.put("thumbnail", thumbnail);
			json.put("content", content);
			json.put("keywords", keywords);
			json.put("label", label);
			json.put("sourceLink", sourceLink);
			String writeKafka = System.getProperties().getProperty("writeToKafka");
			logger.info("write to kafka ==> " + writeKafka);
			if ("yes".equals(writeKafka)) {
				writeNewsInfoToKafka(topicName, key, json);
			} else {
				logger.info("not write to kafka!");
				logger.info("ready to write msg is : " + json);
			}
			
		}
		
	}
}
