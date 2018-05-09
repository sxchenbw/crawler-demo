package com.kpcoin.crawlers;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.kpcoin.kafka.KafkaCommonProducer;

public class BaseCrawler {

	/**
	 * 
	 * @param topicName
	 * @param json
	 */
	public static void writeNewsInfoToKafka(String topicName, JSONObject json) {
		if (StringUtils.isNotBlank(topicName) && json != null && !json.isEmpty()) {
			KafkaCommonProducer.writeMsg(topicName, json.toJSONString());
		}
	}
	
	/**
	 * topicName:ToutiaoYidianNewsInfo
	 * @param topicName
	 * @param title
	 * @param source
	 * @param pubtime
	 * @param thumbnail
	 * @param content
	 * @param keywords
	 * @param label
	 * @param sourceLink
	 */
	public static void writeNewsInfoToKafka(String topicName, String title, String source, String pubtime, String thumbnail, String content,
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
			//writeNewsInfoToKafka(topicName, json);
		}
		
	}
}
