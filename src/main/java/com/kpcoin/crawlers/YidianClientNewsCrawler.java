package com.kpcoin.crawlers;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kpcoin.kafka.KafkaTopics;

public class YidianClientNewsCrawler extends BaseCrawler {

	public static final Logger logger = Logger.getLogger(YidianClientNewsCrawler.class);
	
	public static void main(String[] args){
		doCrawl();
//		String docId = "0IzvTBf1";
//		String content = crawlAndExtractNewsDetailContent(docId);
//		System.out.println(content);
	} 
	
	public static void doCrawl() {
//		String url = "http://www.yidianzixun.com/home/q/news_list_for_channel?channel_id=best&cstart=0&cend=10" +
//				"&infinite=true&refresh=1&__from__=wap&appid=web_yidian&_=" + System.currentTimeMillis();
		String url = "https://a1.go2yd.com/Website//channel/news-list-for-best-channel?amazing_comments=true" +
				"&eventid=592656649245ebc9d-a6af-41f8-8bd8-306d2b43e2b0" +
				"&cstart=0&infinite=true&searchentry=channel_navibar&refresh=1" +
				"&group_fromid=g181&collection_num=0&distribution=app.xiaomi.com" +
				"&version=020900&platform=1&ad_version=010948" +
				"&reqid=9suospd4_"+System.currentTimeMillis()+"_21671" +
				"&cv=4.6.6.0" +
				"&cend=30&appid=yidian" +
				"&fields=docid&fields=date" +
				"&fields=image&fields=image_urls&fields=like&fields=source&fields=title&fields=url&fields=comment_count&fields=up&fields=down" +
				"&net=wifi";
		Connection conn = null;
		Response response = null;
		String result = null;
		try {
			conn = Jsoup.connect(url);
			
			conn.method(Method.GET);
			conn.ignoreContentType(true);
			conn.header("Host", "www.yidianzixun.com");
			conn.header("Referer", "http://www.yidianzixun.com/");
			conn.header("Connection", "keep-alive");
			conn.cookie("JSESSIONID", "kt-fnz49vIQoSLFNLu3xMw");
			conn.userAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1");
			
			response = conn.execute();
			result = response.body();
			if (StringUtils.isNotBlank(result)) {
				extractAndSaveNewsInfo(result);
			}
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 从接口中解析所需的字段并保存入库
	 * @param result
	 */
	private static void extractAndSaveNewsInfo(String html) {
		if (StringUtils.isNotBlank(html)) {
			try {
				JSONObject json = JSONObject.parseObject(html);
				if (json != null && json.containsKey("result")) {
					JSONArray result = json.getJSONArray("result");
					int size = result == null ? 0 : result.size();
					for (int i = 0;i < size; i ++) {
						JSONObject item = result.getJSONObject(i);
						logger.info("*****************接口返回的原始内容-开始********************");
						logger.info(item);
						logger.info("*****************接口返回的原始内容-结束********************");
						String docid = item.getString("docid");
						String title = item.getString("title");
						String source = item.getString("source");
						String date = item.getString("date");
						String summary = item.getString("summary");
						String category = item.getString("category");
						String image = item.getString("image");
						String image_urls = item.getString("image_urls");
						String tag_name = item.getString("tag_name");
						String card_label = item.getString("card_label");
						logger.info("docid:" + docid + ", title:" + title);
						logger.info("source:" + source + ", date:" + date + ", category:" + category);
						logger.info("summary:" + summary);
						logger.info("image:" + image);
						logger.info("image_urls:" + image_urls);
						logger.info("---------------------------------------");
						logger.info("tag_name:" + tag_name + ", card_label:" + card_label);
						logger.info("---------------------------------------");
						String content = crawlAndExtractNewsDetailContent(docid);
						logger.info("content:" + content);
						json.put("originalContent", content);
						logger.info("***************************************");
						writeNewsInfoToKafka(KafkaTopics.toutiao_yidian_news_topic, yidianNewsKey,
								title, source, date, image, content, tag_name, card_label, docid);
					}
				}
			} catch(Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	/**
	 * 根据接口中的docid获取详情页中的正文信息
	 * @return
	 */
	public static String crawlAndExtractNewsDetailContent(String docid) {
		String content = null;
		if (StringUtils.isNotBlank(docid)) {
			Connection connection = null;
			Response response = null;
			String result = null;
			try {
				String url = "http://www.yidianzixun.com/article/" + docid;
				String contentContainer = "div.content-bd";//正文所在的html容器
				connection = Jsoup.connect(url);
				response = connection
						.method(Method.GET)
						.ignoreContentType(true)
						.execute();
				result = response.body();
				if (StringUtils.isNotBlank(result)) {
					Document doc = Jsoup.parse(result);
					Elements divs = doc.select(contentContainer);
					if (divs != null && divs.size() == 1) {
						Element divElement = divs.first();
						content = divElement.outerHtml();
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return content;
	}
	/**
	 * 保存文章信息
	 * @param json
	 */
	public static void saveNewsInfo(JSONObject json) {
		if (json != null) {
			//TODO save news detail info to db
			for (String  key : json.keySet()) {
				System.out.println("save record, key:" + key);
			}
		}
	}
}
