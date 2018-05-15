package com.kpcoin.crawlers;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kpcoin.kafka.KafkaTopics;
import com.kpcoin.utils.DateUtil;
import com.kpcoin.utils.HttpClientUtil;

public class ToutiaoDataInfoCrawler extends BaseCrawler {
	
	public static final Logger logger = Logger.getLogger(ToutiaoDataInfoCrawler.class);
	
	public static String as = "A1451ACF91272DA";
	public static String cp = "5AF167522DEA1E1";
	
	public static String serviceUrl = "https://m.toutiao.com/list/?tag=#tag#&ac=wap&count=20&format=json_raw&as=#as#&cp=#cp#&min_behot_time=0&i=";
	
	public static String cookie = "";

	public static final String encoder = "utf-8";
	public static final String dateFmt = "yyyy-MM-dd HH:mm:ss";
	public static final long cxJudgeTime = 86400 * 5 * 1000;//抓取时间与推出时间相差5天的默认进入长效待审库
	
	
	public static final String fixedParam = "&as=A1451ACF91272DA&cp=5AF167522DEA1E1";
	public static Set<String> crawledUrlSet = Sets.newHashSet();
	
	public static final List<String> newsTagList = Lists.newArrayList();
	public static final Map<String, String> tagMap = Maps.newHashMap();
	
	static {
		//newsTagList与标签对应
		newsTagList.add("__all__");//全部
		newsTagList.add("news_hot");//热点
		newsTagList.add("news_society");//社会
		newsTagList.add("news_entertainment");//娱乐
		newsTagList.add("news_tech");//教育
		newsTagList.add("news_sports");//体育
		newsTagList.add("news_car");//汽车
		newsTagList.add("news_finance");//财经
		newsTagList.add("news_military");//军事
		newsTagList.add("news_world");//国际
		newsTagList.add("news_travel");//旅行
		newsTagList.add("news_discovery");//探索
		newsTagList.add("news_baby");//育儿
		newsTagList.add("news_regimen");//养生
		newsTagList.add("news_story");//故事
		newsTagList.add("news_essay");//美文
		newsTagList.add("news_game");//游戏
		newsTagList.add("news_history");//历史
		newsTagList.add("news_food");//美食
		
		//tagMap可自定义映射
		tagMap.put("__all__", "全部");
		tagMap.put("news_hot", "热点");
		tagMap.put("news_society", "社会");
		tagMap.put("news_entertainment", "娱乐");
		tagMap.put("news_tech", "教育");
		tagMap.put("news_sports", "体育");
		tagMap.put("news_car", "汽车");
		tagMap.put("news_finance", "财经");
		tagMap.put("news_military", "军事");
		tagMap.put("news_world", "国际");
		tagMap.put("news_travel", "旅行");
		tagMap.put("news_discovery", "探索");
		tagMap.put("news_baby", "育儿");
		tagMap.put("news_regimen", "养生");
		tagMap.put("news_story", "故事");
		tagMap.put("news_essay", "美文");
		tagMap.put("news_game", "游戏");
		tagMap.put("news_history", "历史");
		tagMap.put("news_food", "美食");
		
	}
	
	public static void main(String[] args) {
		doCrawl();
	}
	
	/**
	 * 
	 */
	public static void doCrawl() {
//		System.setProperty("http.maxRedirects", "50");
//		System.getProperties().setProperty("proxySet", "true");
//		// 如果不设置，只要代理IP和代理端口正确,此项不设置也可以
//		String ip = "125.118.75.233";
//		System.getProperties().setProperty("http.proxyHost", ip);
//		System.getProperties().setProperty("http.proxyPort", "6666");
		//获取cookie信息
		resetCookie();
		//m.toutiao端数据接口
		for (String tag : tagMap.keySet()) {
			crawlChannelNewsList(tag);
		}
	}
	
	/**
	 * 重新获取cookie信息
	 */
	private static void resetCookie() {
		String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1";
		String url = "https://m.toutiao.com/?channel=__all__#channel=__all__";
		Connection connection = null;
		Response response = null;
		try {
			connection = Jsoup.connect(url);
			response = connection
					.method(Method.GET)
					.ignoreContentType(true)
					.userAgent(userAgent)
					.execute();
			Map<String, String> respHeaders = response.headers();
			StringBuffer buff = new StringBuffer("W2atIF=1; _ga=GA1.2.85492167.1524722357; _gid=GA1.2.283134492.1524722357; _ba=BA0.2-20171206-51225-9dxazuMFtOFrwZrIsWE4");
			for (String key : respHeaders.keySet()) {
				if (key.toLowerCase().indexOf("cookie") > 0) {
					String cookieValue = respHeaders.get(key);
					if (StringUtils.isNotBlank(cookieValue)) {
						int webidIndex = cookieValue.indexOf("tt_webid=");
						int csrftokenIndex = cookieValue.indexOf("csrftoken=");
						if (webidIndex > -1) {
							buff.append(cookieValue.substring(webidIndex, cookieValue.indexOf(";", webidIndex) + 1));
						}
						if (csrftokenIndex > -1) {
							buff.append(cookieValue.substring(csrftokenIndex, cookieValue.indexOf(";", csrftokenIndex) + 1));
						}
					}
				}
			}
//			buff.append("__tasessionId=" + genTasessionId(9)).append("; ");
//			cookie = buff.toString();
			cookie = "UM_distinctid=1633f27bd713ad-0aa8e298da27f8-3c3c5905-100200-1633f27bd724ea; tt_webid=6553146256926377486; csrftoken=665bdfbfaba6b194cb271143dbfe796a; W2atIF=1; _ga=GA1.2.208499608.1525773011; _gid=GA1.2.1628626342.1525773011; _ba=BA0.2-20180508-51225-sKfF7iOkd5txoZXVrvkO";
			logger.info("cookie==>" + cookie);
			
			
			
		} catch(Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	/**
	 * 抓取频道内容列表
	 * @param channel
	 */
	public static void crawlChannelNewsList(String channel) {
		if (StringUtils.isBlank(channel)) {
			logger.warn("channel is blank!");
			return ;
		}
		
		String crawlUrl = "https://m.toutiao.com/list/?tag=${channel}&ac=wap&count=20&format=json_raw" + fixedParam;
		try {
			String fileName = "/data02/crawler/biz_toutiao_yidian_crawlers/tt_tookie.txt";
			File file = new File(fileName);
			if (file.exists()) {
				@SuppressWarnings("deprecation")
				String cookieInfo = FileUtils.readFileToString(file);
				if (StringUtils.isNotBlank(cookieInfo)) {
					cookie = cookieInfo;
				}
			}
			crawlUrl = crawlUrl.replace("${channel}", channel);
			String html = HttpClientUtil.executeRequestWithJsoupGetWithCookie(crawlUrl, null, encoder, cookie);
			if (StringUtils.isNotBlank(html)) {
				JSONObject respJsonObject = JSONObject.parseObject(html);
				if (respJsonObject != null && respJsonObject.containsKey("data")) {
					JSONArray data = respJsonObject.getJSONArray("data");
					if (data != null && data.size() > 0) {
						for (int i = 0;i < data.size(); i ++) {
							JSONObject item = data.getJSONObject(i);
							String title = item.getString("title");
							String url = item.getString("url");
							if (item.containsKey("ad_label") || item.containsKey("video_id") || !url.contains("toutiao.com")) {
								continue;
							}
							if (crawledUrlSet.contains(url)) {
								logger.info("url:" + url + " has crawled, repeated!");
								continue;
							}
							logger.info("==============>title:" + title + ", url:" + url + " match the crawrlTime - pubTime >= " + cxJudgeTime + " condition!");
							JSONObject json = crawlNewsDetail(url);
							if (json != null) {
								String thumbnail = null;
								if (item.containsKey("image_list")) {
									JSONArray image_list = item.getJSONArray("image_list");
									if (image_list != null && image_list.size() > 0) {
										thumbnail = image_list.getJSONObject(0).getString("url");
									}
								} else if (item.containsKey("image_url")) {
									thumbnail =  item.getString("image_url");
								}
								json.put("channel", tagMap.get(channel));
								json.put("thumbnail", thumbnail);
								logger.info("**************************************************************");
								logger.info("*******************title:" + json.getString("title"));
								logger.info("*******************subName:" + json.getString("subName"));
								logger.info("*******************pubtime:" + json.getString("pubtime"));
								logger.info("*******************crawlTime:" + json.getString("crawlTime"));
								logger.info("*******************channel:" + json.getString("channel"));
								logger.info("**************************************************************");
								writeNewsInfoToKafka(KafkaTopics.toutiao_yidian_news_topic, toutiaoNewsKey,
										title, 
										json.getString("subName"), 
										json.getString("pubtime"), 
										thumbnail, 
										json.getString("content"), 
										json.getString("channel"), 
										json.getString("channel"), 
										json.getString("url"));
								
							}
						}
					}
				}
			}
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	/**
	 * https://www.toutiao.com/a6498420555169923598/
	 * @param detailUrl
	 * @return
	 */
	public static JSONObject crawlNewsDetail(String detailUrl) {
		JSONObject json = null;
		if (StringUtils.isNotBlank(detailUrl)) {
			try {
				String html = HttpClientUtil.executeRequestWithJsoupGetWithCookie(detailUrl, null, encoder, cookie);
				if (StringUtils.isNotBlank(html)) {
					json = extractNewsInfoFromResponse(html, detailUrl);
				}
			} catch(Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return json;
	}
	
	/**
	 * 从返回的详情接口获取
	 * @param responseHtml
	 * @return
	 */
	public static JSONObject extractNewsInfoFromResponse(String responseHtml, String url) {
		JSONObject json = null;
		if (StringUtils.isNotBlank(responseHtml)) {
			String title = null, subName = null, pubtime = null, content = null, itemId = null, groupId = null;
			try {
				int articleInfoIndex = responseHtml.indexOf("articleInfo: {");
				int commentInfoIndex = responseHtml.indexOf("commentInfo: {");
				if (articleInfoIndex > 0 && commentInfoIndex > articleInfoIndex + "articleInfo: {".length()) {
					String articleInfo = responseHtml.substring(articleInfoIndex + "articleInfo: {".length() - 1, commentInfoIndex);
					articleInfo = articleInfo.trim();
					if (articleInfo.endsWith(",")) {
						articleInfo = articleInfo.substring(0, articleInfo.length() - 1);
					}
					JSONObject articleInfoJson = JSONObject.parseObject(articleInfo);
					title = articleInfoJson.getString("title");
					content = articleInfoJson.getString("content");
					if (StringUtils.isNotBlank(content)) {
						content = StringEscapeUtils.unescapeHtml4(content);
					}
					JSONObject subInfo = articleInfoJson.getJSONObject("subInfo");
					if (subInfo == null || subInfo.isEmpty()) {
						return null;
					}
					json = new JSONObject();
					pubtime = subInfo.getString("time");
					subName = subInfo.getString("source");
					
					JSONObject tagInfo = articleInfoJson.getJSONObject("tagInfo");
					if (tagInfo != null && tagInfo.containsKey("tags")) {
						JSONArray tags = tagInfo.getJSONArray("tags");
						int size = tags == null ? 0 : tags.size();
						List<String> tagsList = Lists.newArrayList();
						for (int i = 0;i < size; i ++) {
							JSONObject tagItem = tags.getJSONObject(i);
							tagsList.add(tagItem.getString("name"));
						}
						json.put("tags", StringUtils.join(tagsList, " "));
					}
					
					itemId = articleInfoJson.getString("itemId");
					groupId = articleInfoJson.getString("groupId");
					
					
					json.put("title", title);
					json.put("content", content);
					json.put("itemId", itemId);
					json.put("groupId", groupId);
					json.put("pubtime", pubtime);
					json.put("crawlTime", DateUtil.getCurrentDateTimeStr(dateFmt));
					json.put("crawlDuration", System.currentTimeMillis() - DateUtil.getDateFromStr(pubtime, dateFmt).getTime());
					json.put("subName", subName);
					json.put("url", url);
				}
			} catch(Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return json;
	}
	
	public static Random random = new Random();
	
	public static String genTasessionId(int e) {
		StringBuffer buff = new StringBuffer("");
		for(String t="";t.length()<e; ) {
			t+= randomString();
			buff.append(t);
		}
		return buff.substring(0, 9) + System.currentTimeMillis();
	}
	
	public static String randomString() {
		return RandomStringUtils.random(2, true, true);
	}
	
	/**
	 * 
	 * @param time
	 */
	public static void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
