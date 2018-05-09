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
		} else {
			crawlToutiaoYidianNews();
		}
	}
	/**
	 * 
	 */
	public static void crawlToutiaoYidianNews() {
		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);
		scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				ToutiaoDataInfoCrawler.doCrawl();
				YidianClientNewsCrawler.doCrawl();
			}
		}, 0, 60, TimeUnit.SECONDS);//每60秒钟抓取一次
	} 
}
