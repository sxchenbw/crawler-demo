package com.kpcoin.utils.mongo;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class NewsInfoMongoUtil {
	
	public static Logger logger = LoggerFactory.getLogger(NewsInfoMongoUtil.class);

	public static String kp_dbName = "kpcoin";
    public static String kp_newsInfoCollectionName = "crawl_news_info";

    public static String host;
    public static int port;

    public static MongoClient mongoClient = null;
    public static DBCollection newsInfoCollection = null;

    static {
        host = "bizapi.pezy.cn";
        port = 27017;
        mongoClient = new MongoClient(host, port);
        
        newsInfoCollection = getDBCollection(kp_dbName, kp_newsInfoCollectionName);
    }


    public static void main(String[] args) {
    	
    }
    
    /**
     * 返回指定数据库中的指定集合
     * @param dbName
     * @param collectionName
     * @return
     */
    public static MongoCollection<Document> getCollection(String dbName, String collectionName) {
        //实例化一个MongoDatabase
        MongoDatabase mongoDatabase = mongoClient.getDatabase(dbName);
        //获取数据库中的某个集合
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        return collection;
    }

    /**
     *
     * @param dbName
     * @param collectionName
     * @return
     */
    public static DBCollection getDBCollection(String dbName, String collectionName) {
        //实例化一个MongoDatabase
        DB mongoDatabase = mongoClient.getDB(dbName);
        //获取数据库中的某个集合
        DBCollection collection = mongoDatabase.getCollection(collectionName);
        return collection;
    }

    /**
     * 往指定db的指定collection插入一条数据
     * @param dbName
     * @param collectionName
     * @param _id
     * @param json
     */
    public static void save(String dbName, String collectionName, Object _id, JSONObject json) {
        if (StringUtils.isNoneBlank(dbName, collectionName)
                && json != null && !json.isEmpty()) {
            try {
                DBCollection collection = getDBCollection(dbName, collectionName);
                if (collection != null) {
                    BasicDBObject saveObj = new BasicDBObject();
                    if (_id != null) {
                        saveObj.put("_id", _id);
                    }
                    for (String key : json.keySet()) {
                        saveObj.put(key, json.get(key));
                    }
                    collection.save(saveObj);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
    
	
	/**
	 * 
	 * @param newsItem
	 * @return
	 */
	public static int saveNewsInfo(JSONObject newsItem) {
		int ret = -1;
		if (newsItem != null && !newsItem.isEmpty()) {
			try {
				BasicDBObject dbObject = new BasicDBObject();
				dbObject.put("_id", newsItem.getString("_id"));
				for (String key : newsItem.keySet()) {
					dbObject.put(key, newsItem.get(key));
				}
				ret = newsInfoCollection.save(dbObject).getN();
				logger.info("save newsItem info result:{}", ret);
			} catch(Exception e) {
				logger.error(e.getMessage());
			}
		}
		return ret;
	}
}
