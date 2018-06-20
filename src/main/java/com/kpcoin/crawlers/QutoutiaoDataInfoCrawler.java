package com.kpcoin.crawlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.kpcoin.kafka.KafkaTopics;
import com.kpcoin.utils.DateUtil;
import com.kpcoin.utils.ImageUtil;
import com.kpcoin.utils.Md5Util;
import com.kpcoin.utils.mongo.NewsInfoMongoUtil;
import com.kpcoin.utils.proxy.ProxyPool;

/**
 * 趣头条内容抓取
 * @author chenbw
 *
 */
public class QutoutiaoDataInfoCrawler extends BaseCrawler {

	public static Logger logger = LoggerFactory.getLogger(QutoutiaoDataInfoCrawler.class);
	
	public static final String charset = "utf-8";
	public static final String sourceFrom = "qtt";
	
	public static String contentListUrl = "https://api.1sapp.com/content/getListV2?qdata=NjE3OTQ1M0M1NDBBMUM5NTlERjYzMDQwNDk4MzcyMEIuY0dGeVlXMGZNamxrTVRFM01UZ3RaVGRqWkMwME9XVmtMVGd6TWpjdE1ERTNaalJqTm1VNVpUSTJIblpsY25OcGIyNGZNUjV3YkdGMFptOXliUjloYm1SeWIybGsuabHZtkKHsYMKmR7Z0vrcFP43oYn4PpzwRJV7eaJhdRsbOFmNhRGQMbdeQT%2FmmXeXxXC0SAvpW4zUBsZzFHqM3uzEOEouxHLssKo4sUCuK5Bb0fHrS7s%2BbS42xazMW%2FLYZis9MI0N86z1VfoVTG41hoDb7Kp6T%2BEY0wmgKmarBOVQI4X9lYPzlEffoStanF7Omj%2BGpBvaCgkmHAUprZksMlTFVB%2FFETeyFCxCHMh3MO7hU4SvqF%2B2OUIFM5bUKIzRfNfEcBH1cJTZNl31Q6xpWTn2XjnQ6Q6QRFJinw6tXaqqQePRFLJT%2B%2BKn4SWCmwpdoMZbCDH5v0H5TBsSi0gAsr1dekRga8dpyQbYnuHFmmzRtwKyYHZUUTADQIv3fT1z15wEdLv3vK%2FnLaakL%2FmRUwPvprH0u7B2aaIOey4tgKiGm4WO78IewT1rT78oEwJAtg%2FXaxjl8OrwHoGLnu5tZ9bZrVtw9D%2FsHRoanLCjV6lNT%2BwfGtnSeU8NrbRO1p8kM9DzTvyD7n3NenE7VWJPpjBwo6GvoiJ6sMd4gaH7MKYeTw9KW8JGyk4gURc7IMuLO8aR9a%2F2sskBrE%2B7fVucCt%2Bp1XBf7cZxWdAjGgDJSYZHfqd5pyiUKSb1N65aEd%2BUH15lhewYcWtxQ13Gmm4Qj6jW8YTYszUMoESbRSMwS4RY";
	
	//抓取过的url
	public static Set<String> urlSet = Sets.newHashSet();
	public static Set<String> idSet = Sets.newHashSet();
	
	public static Random random = new Random();
	
	public static AtomicLong counter = new AtomicLong(0);
	
	
	public static void main(String[] args) {
		doCrawl();
	}
	
	public static void doCrawl() {
		/**/
		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(8);
		scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				try {
					TimeUnit.SECONDS.sleep(random.nextInt(3));
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
				scheduledCrawlNewsInfo();
			}
		}, 0, 10, TimeUnit.SECONDS);
		
//		while (true) {
//			try {
//				TimeUnit.SECONDS.sleep(random.nextInt(3));
//			} catch (InterruptedException e) {
//				logger.error(e.getMessage(), e);
//			}
//			scheduledCrawlNewsInfo();
//		}
	}

	/**
	 * 
	 */
	public static void scheduledCrawlNewsInfo() {
		String html = null;
		try {
			JSONObject proxyInfo = null;//ProxyPool.choiceOneExpireProxy();
			logger.info("proxyInfo:{}", proxyInfo);
			html = crawlPageSourceWithJavaURL(contentListUrl, proxyInfo);//			html = crawlPageSourceWithJsoup(contentListUrl);
			if (StringUtils.isNotBlank(html)) {
				extractNewsListInfo(html);
			}
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	/**
	 * 从列表接口中抽取符合条件的新闻项信息
	 * @param listData
	 * @return
	 */
	public static List<JSONObject> extractNewsListInfo(String listData) {
		List<JSONObject> list = Lists.newArrayList();
		if (StringUtils.isNotBlank(listData)) {
			try {
				String dataKey = "data", typeKey = "type";
				JSONObject jsonObject = JSONObject.parseObject(listData);
				if (jsonObject != null && jsonObject.containsKey(dataKey)) {
					JSONObject dataJson = jsonObject.getJSONObject(dataKey);//取外层data json object
					if (dataJson != null && dataJson.containsKey(dataKey)) {;
						JSONArray dataArray = dataJson.getJSONArray(dataKey);//取内层data json array
						if (dataArray != null) {
							for (int i = 0; i < dataArray.size(); i++) {
								JSONObject item = dataArray.getJSONObject(i);
								if ("ad".equalsIgnoreCase(item.getString(typeKey))) {
									logger.info("======================================广告信息，跳过。。。");
									continue;
								}
								//非广告数据，则获取如下字段：
								//获取标题（title）、媒体名称（source_name）、发布时间（publish_time）、tags（tag）、封面图（cover）、
								//文章id（id）、内容页链接（url）、分享页链接（share_url）、摘要（introduction）、类型（type）
								//获取到数据保存到mongo库
								String id = item.getString("id");
								if (idSet.contains(id)) {
									continue;
								}
								String title = item.getString("title");
								String source = item.getString("source_name");
								long publishTimeTs = item.getLongValue("publish_time");
								String publish_time = DateUtil.getDateStrByLong(publishTimeTs, "yyyy-MM-dd HH:mm:ss");
								String url = item.getString("url");
								String shareUrl = item.getString("share_url");
								String introduction = item.getString("introduction");
								JSONArray tags = item.getJSONArray("tag");
								JSONArray cover = item.getJSONArray("cover");
								String thumbnail = cover != null && cover.size() > 0 ? cover.getString(0) : "";
								String type = item.getString("type");
								String content = crawlNewsDetail(shareUrl);
								logger.info("*******************************************************");
								logger.info("id:{}", id);
								logger.info("title:{}", title);
								logger.info("source:{}", source);
								logger.info("publish_time:{}", publish_time);
								logger.info("thumbnail:{}", thumbnail);
								logger.info("url:{}", url);
								logger.info("shareUrl:{}", shareUrl);
								logger.info("introduction:{}", introduction);
								logger.info("tags:{}", tags);
								logger.info("cover:{}", cover);
								logger.info("type:{}", type);
//								logger.info("content:{}", content);
								idSet.add(id);
								if (StringUtils.isNotBlank(content)) {
									JSONArray thumbnailImgs = processCoverToImgArray(cover);
									JSONObject newsItem = new JSONObject();
									String _id = Md5Util.MD5Encode(sourceFrom + "_" + id);
									newsItem.put("_id", _id);
									newsItem.put("sourceFrom", sourceFrom);
									newsItem.put("id", id);
									newsItem.put("title", title);
									newsItem.put("source", source);
									newsItem.put("publish_time", publish_time);
									newsItem.put("thumbnail", thumbnail);
									newsItem.put("url", url);
									newsItem.put("shareUrl", shareUrl);
									newsItem.put("introduction", introduction);
									newsItem.put("tags", tags);
									newsItem.put("cover", cover);
									newsItem.put("thumbnailImgs", thumbnailImgs);
									newsItem.put("type", type);
									newsItem.put("content", content);
									newsItem.put("writeTime", System.currentTimeMillis());
									
									NewsInfoMongoUtil.saveNewsInfo(newsItem);
									logger.info("now the counter is ========================>{}", counter.incrementAndGet());
									/**/
									writeNewsInfoToKafka(KafkaTopics.toutiao_yidian_news_topic, qutoutiaoNewsKey,
											title, 
											source, 
											publish_time, 
											thumbnail, 
											content, 
											item.getString("tag"), 
											item.getString("tag"), 
											url,
											thumbnailImgs);
											
											
								}
							}
						}
					}
				}
			} catch(Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return list;
	}
	
	/**
	 * 封面图转成统一的格式
	 * @param cover
	 * @return
	 */
	public static JSONArray processCoverToImgArray(JSONArray cover) {
		JSONArray imgs = new JSONArray();
		if (cover != null) {
			for (int i = 0;i < cover.size();i ++) {
				String url = cover.getString(i);
				if (StringUtils.isNotBlank(url)) {
					JSONObject imgItem = new JSONObject();
					JSONObject imgJson = ImageUtil.calcImgSize(url);
					imgItem.put("url", url);
					if (imgJson != null) {
						imgItem.put("width", imgJson.getIntValue("width"));
						imgItem.put("height", imgJson.getIntValue("height"));
					} else {
						imgItem.put("width", 640);
						imgItem.put("height", 360);
					}
					
					imgs.add(imgItem);
				}
			}
		}
		return imgs;
	}
	
	/**
	 * 抓取趣头条文章说详情页内容
	 * @param url
	 * @return
	 */
	public static String crawlNewsDetail(String url) {
		String result = "";
		if (StringUtils.isNotBlank(url) && !urlSet.contains(url)) {//url为空，且没有抓取过详情页
			try {
				JSONObject proxyInfo = null;//ProxyPool.choiceOneProxy();
				String pageSource = crawlPageSourceWithJsoup(url, proxyInfo);
				if (StringUtils.isNotBlank(pageSource)) {
					String contentSelector = "div.article div.content";
					Document doc = Jsoup.parse(pageSource);
					Elements contentElements = doc.select(contentSelector);
					if (contentElements != null) {
						Element contentEle = contentElements.first();
						if (contentEle != null) {
							Elements imgs = contentEle.select("img");
							for (Element img : imgs) {
								if (StringUtils.isBlank(img.attr("src")) && StringUtils.isNotBlank(img.attr("data-src"))) {
									img.attr("src", img.attr("data-src"));
								}
							}
							result = contentEle.outerHtml();
						}
					}
				}
			} catch(Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return result;
	}
	
	/**
	 * 获取url返回的页面内容
	 * @param url
	 * @return
	 */
	public static String crawlPageSourceWithJsoup(String url, JSONObject proxyInfo) {
		logger.info("crawlPageSource::url={}", url);
		String pageSource = "";
		if (StringUtils.isNotBlank(url)) {
			Connection connection = null;
			Response response = null;
			try {
				connection = Jsoup.connect(url).ignoreContentType(true);
				if (proxyInfo != null) {
					String ip = proxyInfo.getString("ip");
					int port = proxyInfo.getIntValue("port");
					connection.proxy(ip, port);
				}
				response = connection.execute();
				if (response != null) {
					pageSource = response.body();
				}
			} catch(Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return pageSource;
	}
	/**
	 * 
	 * @param pageUrl
	 * @return
	 */
	public static String crawlPageSourceWithJavaURL(String pageUrl, JSONObject proxyInfo) {
		logger.info("crawlPageSource::url={}", pageUrl);
		String pageSource = "";
		
		URL url = null;
        URLConnection con = null;
        BufferedReader reader = null;
        StringBuffer buffer = null;
		
		if (StringUtils.isNotBlank(pageUrl)) {
            buffer = new StringBuffer();
            HttpURLConnection http = null;
            try {
            	url = new URL(pageUrl);
            	if (proxyInfo != null) {//使用代理抓取指定网站内容
            		String ip = proxyInfo.getString("ip");
            		int port = proxyInfo.getIntValue("port");
            		
            		InetSocketAddress addr = new InetSocketAddress(ip, port);  
            		Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);

            		con = url.openConnection(proxy);
            	} else {
            		con = url.openConnection();
            	}
                http = (HttpURLConnection) con;
                http.setConnectTimeout(1000 * 3);
                http.setReadTimeout(1000 * 10);
                http.connect();

                if (HttpURLConnection.HTTP_OK == http.getResponseCode()) {
                    reader = new BufferedReader(new InputStreamReader(http.getInputStream(), charset));
                    String line = null;

                    while (null != (line = reader.readLine())) {
                        buffer.append(line);
                    }
                }
                pageSource = buffer.toString();

            } catch (Exception e) {
                logger.error("error,url=" + url, e);
            } finally {
                if (http != null) {
                    http.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        reader = null;
                    }
                }
                con = null;
            }
		}
		return pageSource;
	}
	/**
	 * 保存数据到数据库
	 * @param newsItem
	 * @return
	 */
	public static int saveToMongoDb(JSONObject newsItem) {
		int ret = -1;
		if (newsItem != null) {
			try {
				
			} catch(Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return ret;
	}
}
