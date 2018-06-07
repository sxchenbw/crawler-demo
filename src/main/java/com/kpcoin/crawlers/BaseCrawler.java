package com.kpcoin.crawlers;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kpcoin.kafka.KafkaCommonProducer;
import com.kpcoin.utils.ImageUtil;

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
			String keywords, String label, String sourceLink, String category) {
		
		if (StringUtils.isNoneBlank(title, source, content)) {
			JSONArray imgs = processImgs(content);
			JSONArray thumbnailImgs = processThumbnailImgs(imgs);
			
			JSONObject json = new JSONObject();
			
			json.put("title", title);
			json.put("source", source);
			json.put("pubtime", pubtime);
			json.put("thumbnail", thumbnail);
			json.put("content", content);
			json.put("imgs", imgs);
			json.put("thumbnailImgs", thumbnailImgs);
			json.put("keywords", keywords);
			json.put("label", label);
			json.put("sourceLink", sourceLink);
			json.put("category", category);
			String writeKafka = System.getProperties().getProperty("writeToKafka");
			logger.info("write to kafka ==> " + writeKafka);
			logger.info("write to kafka data****************************************");
			logger.info("title:" + title);
			logger.info("thumbnail:" + thumbnail);
			logger.info("thumbnailImgs:" + thumbnailImgs);
			logger.info("imgs:" + imgs);
//			if ("yes".equals(writeKafka)) {
				writeNewsInfoToKafka(topicName, key, json);
//			} else {
//				logger.info("not write to kafka!");
//				logger.info("ready to write msg is : " + json);
//			}
			
		}
		
	}
	
	/**
	 *
	 * @param content
	 * @return
    */
	public static JSONArray processImgs(String content) {
		JSONArray imgs = new JSONArray();
		if (StringUtils.isNotBlank(content)) {
			try {
				Document doc = Jsoup.parse(content);
				Elements imgEles = doc.select("img");
				if (imgs != null) {
					for (Element img : imgEles) {
						String src = img.attr("src");
						if (StringUtils.isNotBlank(src) && src.startsWith("//")) {
							src = "http:" + src;
						}
						if (StringUtils.isNotBlank(src)) {
							JSONObject imgJson = ImageUtil.calcImgSize(src);
							if (imgJson == null) {
								imgJson = new JSONObject();
								imgJson.put("url", src);
							}
							imgs.add(imgJson);
						}
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return imgs;
	}
	
	/**
	 * 
	 * @param imgs
	 * @return
	 */
	public static JSONArray processThumbnailImgs(JSONArray imgs) {
		JSONArray timgsArray = new JSONArray();
		if (imgs != null && imgs.size() > 0) {
			for (int i = 0; i < imgs.size(); i++) {
				JSONObject item = imgs.getJSONObject(i);
				String url = item.getString("url");
				if (StringUtils.isNotBlank(url)) {
					if (url.indexOf("?") > 0) {
						url = url + "&type=thumbnail_324x210";
					} else {
						url = url + "?type=thumbnail_324x210";
					}
					JSONObject timg = new JSONObject();
					timg.put("url", url);
					timg.put("width", item.get("width"));
					timg.put("height", item.get("height"));
					timgsArray.add(timg);
				}
			}
		}
		return timgsArray;
		
	}
}
