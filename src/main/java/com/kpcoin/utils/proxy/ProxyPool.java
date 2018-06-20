package com.kpcoin.utils.proxy;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.kpcoin.utils.DateUtil;

/**
 * 代理池
 * 代理来源：
 * 选择一：极光爬虫代理http://h.jiguangip.com/
 * @author chenbw
 *
 */
public class ProxyPool {
	
	public static Logger logger = LoggerFactory.getLogger(ProxyPool.class);
	
	public static List<String> proxyList = Lists.newArrayList();
	
	public static List<JSONObject> expireProxyList = Lists.newArrayList();
	
	public static Random random = new Random();
	
	public static volatile boolean hasBalance = true;//是否还有余额
	
	static {
		proxyList.add("125.122.144.78:4562");
		proxyList.add("111.72.107.122:4597");
		proxyList.add("118.252.64.69:4568");
		proxyList.add("122.138.29.199:4545");
		proxyList.add("122.232.109.149:4531");
		proxyList.add("123.152.67.203:4528");
		proxyList.add("111.76.143.86:4563");
		proxyList.add("59.63.15.18:4523");
		proxyList.add("117.94.68.149:4555");
		proxyList.add("115.58.72.208:4531");
		proxyList.add("42.242.38.248:4562");
		proxyList.add("182.243.33.182:4531");
		proxyList.add("42.243.3.211:4525");
		proxyList.add("123.189.129.95:4596");
		proxyList.add("182.243.33.157:4531");
		proxyList.add("58.22.212.155:4593");
		proxyList.add("180.103.11.35:4555");
		proxyList.add("218.72.2.124:4562");
		proxyList.add("182.105.200.249:4526");
		proxyList.add("116.248.178.141:4537");
		proxyList.add("42.242.125.160:4516");
		proxyList.add("114.230.147.146:4571");
		proxyList.add("101.27.23.98:4562");
		proxyList.add("222.219.157.105:4569");
		proxyList.add("114.234.163.204:4569");
		proxyList.add("183.166.215.13:4507");
		proxyList.add("175.151.102.140:4596");
		proxyList.add("36.34.27.50:4546");
		proxyList.add("123.156.186.110:4581");
		proxyList.add("171.122.255.198:4552");
		proxyList.add("182.105.201.78:4526");
		proxyList.add("49.68.105.95:4569");
		proxyList.add("123.156.178.196:4581");
		proxyList.add("117.67.130.240:4595");
		proxyList.add("119.118.99.153:4529");
		proxyList.add("117.90.137.26:4580");
		proxyList.add("36.26.251.101:4569");
		proxyList.add("183.141.205.133:4531");
		proxyList.add("59.62.166.140:4503");
		proxyList.add("112.113.156.244:4542");
		proxyList.add("175.44.155.150:4593");
		proxyList.add("115.212.36.64:4524");
		proxyList.add("220.186.176.52:4518");
		proxyList.add("115.151.141.251:4563");
		proxyList.add("175.150.4.182:4567");
		proxyList.add("117.95.7.13:4529");
		proxyList.add("182.37.102.214:4572");
		proxyList.add("36.57.212.119:4554");
		proxyList.add("117.94.71.28:4513");
		proxyList.add("180.118.135.67:4580");
	}
	
	public static void main(String[] args) {
		regainProxyList();
	}
	

	
	/**
	 * 
	 * @return
	 */
	public static JSONObject choiceOneProxy() {
		JSONObject proxy = null;
		if (proxyList != null && proxyList.size() > 0) {
			int size = proxyList.size();
			int index = random.nextInt(size);
			String ipInfo = proxyList.get(index);
			if (StringUtils.isNotBlank(ipInfo)) {
				ipInfo = ipInfo.trim();
				proxy = new JSONObject();
				proxy.put("ip", ipInfo.split(":")[0]);
				proxy.put("port", Integer.valueOf(ipInfo.split(":")[1]));
				proxy.put("index", index);//用于失效时删除用
			}
		}
		return proxy;
	}
	
	/**
	 * 
	 * @return
	 */
	public static JSONObject choiceOneExpireProxy() {
		JSONObject proxy = null;
		if (expireProxyList.size() == 0) {//可用代理为空，从指定接口重新获取一批
			regainExpireProxyList();
		}
		if (expireProxyList != null && expireProxyList.size() > 0) {
			try {
				while (proxy == null) {
					if (expireProxyList.size() == 0) {//全部已过期，重新获取一批代理
						if (hasBalance) {//有余额的情况下再获取
							regainExpireProxyList();
						} else {
							logger.warn("余额已不足！无法重新获取代理信息，请及时充值！");
							break;
						}
					}
					int size = expireProxyList.size();
					int index = random.nextInt(size);
					JSONObject jsonObject = expireProxyList.get(index);
					if (jsonObject != null) {
						String expireTimeStr = jsonObject.getString("expire_time");
						Date expireTime = DateUtil.getDateFromStr(expireTimeStr, "yyyy-MM-dd HH:mm:ss");
						Date now = new Date();
						if (expireTime.before(now)) {//已过期，则删除
							expireProxyList.remove(index);
						} else {
							proxy = new JSONObject();
							proxy.put("ip", jsonObject.getString("ip"));
							proxy.put("port", jsonObject.getInteger("port"));
							proxy.put("expireTime", expireTimeStr);
							proxy.put("index", index);//用于失效时删除用
						}
					}
				}
			} catch(Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return proxy;
	}
	
	public static synchronized void removeProxy(int i) {
		if (proxyList != null && proxyList.size() > i) {
			try {
				proxyList.remove(i);
			} catch(Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	/**
	 * regainProxyList
	 * 重新从第三方接口获取指定数量的ip
	 */
	public static synchronized void regainProxyList() {
		String url = "http://d.jghttp.golangapi.com/getip?num=50&type=1&pro=&city=0&yys=0&port=1&pack=446&ts=1&ys=0&cs=0&lb=1&sb=0&pb=4&mr=0&regions=";
		Connection conn = null;
		Response response = null;
		try {
			conn = Jsoup.connect(url).ignoreContentType(true);
			response = conn.execute();
			String respCon = response.body();
			if (StringUtils.isNotBlank(respCon)) {
				String[] ips = respCon.split("\\s+");
				if (ips.length > 1) {
					proxyList.clear();
					for (String ipInfo : ips) {
						proxyList.add(ipInfo);
						System.out.println("proxyList.add(\"" + ipInfo + "\");");
					}
				}
			}
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 重新获取一批带有过期时间信息的代理
	 */
	public static synchronized void regainExpireProxyList() {
		if (hasBalance) {
			//test env
//			String url = "http://d.jghttp.golangapi.com/getip?num=50&type=2&pro=&city=0&yys=0&port=1&pack=446&ts=1&ys=0&cs=0&lb=1&sb=0&pb=4&mr=0&regions=";
			//prod env
			String url = "http://d.jghttp.golangapi.com/getip?num=50&type=2&pro=&city=0&yys=0&port=1&pack=454&ts=1&ys=0&cs=0&lb=1&sb=0&pb=4&mr=0&regions=";
			Connection conn = null;
			Response response = null;
			try {
				conn = Jsoup.connect(url).ignoreContentType(true);
				response = conn.execute();
				String respCon = response.body();
				if (StringUtils.isNotBlank(respCon)) {
					JSONObject result = JSONObject.parseObject(respCon);
					if (result != null && result.containsKey("data")) {
						JSONArray data = result.getJSONArray("data");
						if (data != null && data.size() > 0) {
							expireProxyList.clear();
							for (int i = 0;i < data.size(); i++) {
								JSONObject item = data.getJSONObject(i);
								expireProxyList.add(item);
							}
							logger.info("账户余额：{}", data.size());
						} else {
							hasBalance = false;
							logger.warn("账户余额不足！请及时充值");
						}
					}
				}
			} catch(Exception e) {
				logger.error(e.getMessage(), e);
			}
		} else {
			logger.warn("账户余额不足！");
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public static boolean isEmpty() {
		return proxyList == null || proxyList.size() == 0;
	}
}
