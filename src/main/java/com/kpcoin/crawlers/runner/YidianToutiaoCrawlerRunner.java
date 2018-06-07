package com.kpcoin.crawlers.runner;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.kpcoin.crawlers.ToutiaoDataInfoCrawler;
import com.kpcoin.crawlers.YidianClientNewsCrawler;

public class YidianToutiaoCrawlerRunner {

	public static Logger logger = Logger.getLogger(YidianToutiaoCrawlerRunner.class);
	
	public static void main(String[] args) {
		if (args != null && args.length > 0) {
			if ("toutiao".equals(args[0])) {
				ToutiaoDataInfoCrawler.doCrawl();
			} else if ("yidian".equals(args[0])) {
				YidianClientNewsCrawler.doCrawl();
			} else {
				crawlToutiaoYidianNews();
			}
			for (String arg : args) {
				if ("writeToKafka=true".equalsIgnoreCase(arg)) {
					System.getProperties().setProperty("writeToKafka", "yes");
					break;
				}
			}
		} else {
			crawlToutiaoYidianNews();
		}
	}
	/**
	 * 
	 */
	public static void crawlToutiaoYidianNews() {
		ScheduledExecutorService ydScheduledExecutorService = Executors.newScheduledThreadPool(4);
		ydScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				YidianClientNewsCrawler.doCrawl();
			}
		}, 0, 60, TimeUnit.SECONDS);//每60秒钟抓取一次
		ScheduledExecutorService ttScheduledExecutorService = Executors.newScheduledThreadPool(4);
		ttScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				ToutiaoDataInfoCrawler.doCrawl();
			}
		}, 0, 15, TimeUnit.MINUTES);
	} 
}
