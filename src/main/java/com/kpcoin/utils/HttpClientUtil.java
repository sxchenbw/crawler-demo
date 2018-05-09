package com.kpcoin.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HttpClientUtil {
	
	private static final int DEFAULT_TIMEOUT = 300000;
	private static final Logger log = LogManager.getLogger(HttpClientUtil.class);
	private static final String DEFAULT_CHARSET = "UTF-8";
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object executeHttpRequest(String url, Map<String, String> keyValueMap) {
        HttpClient client = createHttpClient();
        PostMethod postMethod = new PostMethod(url);
        try {
            if (keyValueMap != null) {
                Iterator it = keyValueMap.entrySet().iterator();
                NameValuePair[] parameters = new NameValuePair[keyValueMap.size()];
                int c = 0;
                while (it.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
                    NameValuePair nvp = new NameValuePair();
                    nvp.setName(entry.getKey());
                    nvp.setValue(entry.getValue());
                    parameters[c] = nvp;
                    c++;
                }
                postMethod.addParameters(parameters);
            }
            log.debug("query uri ===============" + postMethod.getURI());
            postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
            postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "gbk");
            postMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, DEFAULT_TIMEOUT);
            int statusCode = client.executeMethod(postMethod);
            if (statusCode != HttpStatus.SC_OK) {
                log.info("request '" + url + "' failed,the status is not 200,status:" + statusCode);
                return "";
            }
            byte[] responseBody = postMethod.getResponseBody();
            return responseBody;
        } catch (Exception e) {
            log.error("executeHttpRequest异常，url:" + url, e);
        } finally {
            postMethod.releaseConnection();
        }
        return null;
    }


    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static String executeHttpRequestString(String url, Map<String, String> keyValueMap) {
        HttpClient client = createHttpClient();
        PostMethod postMethod = new PostMethod(url);
        try {
            if (keyValueMap != null) {
                Iterator it = keyValueMap.entrySet().iterator();
                NameValuePair[] parameters = new NameValuePair[keyValueMap.size()];
                int c = 0;
                while (it.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
                    NameValuePair nvp = new NameValuePair();
                    nvp.setName(entry.getKey());
                    nvp.setValue(entry.getValue());
                    parameters[c] = nvp;
                    c++;
                }
                postMethod.addParameters(parameters);
            }
            log.debug("query uri ===============" + postMethod.getURI());
            postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
            postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "gbk");
            postMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, DEFAULT_TIMEOUT);
            
            int statusCode = client.executeMethod(postMethod);
            System.out.println(postMethod.getResponseBodyAsString());
            if (statusCode != HttpStatus.SC_OK) {
                log.info("request '" + url + "' failed,the status is not 200,status:" + statusCode);
                return "";
            }
            String responseBody = postMethod.getResponseBodyAsString();
            return responseBody;
        } catch (Exception e) {
            log.error("executeHttpRequestString异常，url:" + url, e);
        } finally {
            postMethod.releaseConnection();
        }
        return null;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static String executeHttpRequestString(String url, Map<String, String> keyValueMap,String encoder) {
        HttpClient client = createHttpClient();
        PostMethod postMethod = new PostMethod(url);
        try {
            if (keyValueMap != null) {
                Iterator it = keyValueMap.entrySet().iterator();
                NameValuePair[] parameters = new NameValuePair[keyValueMap.size()];
                int c = 0;
                while (it.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
                    NameValuePair nvp = new NameValuePair();
                    nvp.setName(entry.getKey());
                    nvp.setValue(entry.getValue());
                    parameters[c] = nvp;
                    c++;
                }
                postMethod.addParameters(parameters);
            }
            log.debug("query uri ===============" + postMethod.getURI());
            postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
            postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, encoder);
            postMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, DEFAULT_TIMEOUT);
            int statusCode = client.executeMethod(postMethod);
            if (statusCode != HttpStatus.SC_OK) {
            	log.info("request '" + url + "' failed,the status is not 200,status:" + statusCode);
            	return "";
            }
            InputStream inputStream = postMethod.getResponseBodyAsStream();   
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, encoder));   
            StringBuffer stringBuffer = new StringBuffer();   
            String str= "";   
            while((str = br.readLine()) != null){
                stringBuffer .append(str );   
            }   
//            System.out.println("ResponseBody:\n" + stringBuffer.toString());
            
//            String responseBody = postMethod.getResponseBodyAsString();
            String responseBody = stringBuffer.toString();
            return responseBody;
        } catch (Exception e) {
            log.error("executeHttpRequestString异常，url:" + url);
            log.error("params:" + keyValueMap);
            log.error("ERRORMSG:" + e.getMessage());
        } finally {
            postMethod.releaseConnection();
        }
        return null;
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static String executeHttpRequestStringWithRedirect(String url, Map<String, String> keyValueMap,String encoder) {
    	HttpClient client = createHttpClient();
    	PostMethod postMethod = new PostMethod(url);
    	try {
    		if (keyValueMap != null) {
    			Iterator it = keyValueMap.entrySet().iterator();
    			NameValuePair[] parameters = new NameValuePair[keyValueMap.size()];
    			int c = 0;
    			while (it.hasNext()) {
    				Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
    				NameValuePair nvp = new NameValuePair();
    				nvp.setName(entry.getKey());
    				nvp.setValue(entry.getValue());
    				parameters[c] = nvp;
    				c++;
    			}
    			postMethod.addParameters(parameters);
    		}
    		log.debug("query uri ===============" + postMethod.getURI());
    		postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
    		postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, encoder);
    		postMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, DEFAULT_TIMEOUT);
    		int statusCode = client.executeMethod(postMethod);
    		if (statusCode != HttpStatus.SC_OK) {
    			log.info("request '" + url + "' failed,the status is not 200,status:" + statusCode);
    			if (statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
    				Header locationHeader = postMethod.getResponseHeader("Location");
    				return locationHeader == null ? "" : locationHeader.getValue();
    			} else {
    				return "";
    			}
    		}
    		InputStream inputStream = postMethod.getResponseBodyAsStream();   
    		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, encoder));   
    		StringBuffer stringBuffer = new StringBuffer();   
    		String str= "";   
    		while((str = br.readLine()) != null){
    			stringBuffer .append(str );   
    		}   
//            System.out.println("ResponseBody:\n" + stringBuffer.toString());
    		
//            String responseBody = postMethod.getResponseBodyAsString();
    		String responseBody = stringBuffer.toString();
    		return responseBody;
    	} catch (Exception e) {
    		log.error("executeHttpRequestString异常，url:" + url);
    		log.error("params:" + keyValueMap);
    		log.error("ERRORMSG:" + e.getMessage());
    	} finally {
    		postMethod.releaseConnection();
    	}
    	return null;
    }
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static String executeHttpRequestStringWithIp(String url, Map<String, String> keyValueMap,String encoder, String ip) {
    	HttpClient client = createHttpClient();
    	PostMethod postMethod = new PostMethod(url);
    	try {
    		if (keyValueMap != null) {
    			Iterator it = keyValueMap.entrySet().iterator();
    			NameValuePair[] parameters = new NameValuePair[keyValueMap.size()];
    			int c = 0;
    			while (it.hasNext()) {
    				Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
    				NameValuePair nvp = new NameValuePair();
    				nvp.setName(entry.getKey());
    				nvp.setValue(entry.getValue());
    				parameters[c] = nvp;
    				c++;
    			}
    			postMethod.addParameters(parameters);
    		}
    		log.debug("query uri ===============" + postMethod.getURI());
    		postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
    		postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, encoder);
    		postMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, DEFAULT_TIMEOUT);
    		if (StringUtils.isNotBlank(ip)) {
    			postMethod.setRequestHeader("X-Forwarded-For", ip);
    		}
    		int statusCode = client.executeMethod(postMethod);
    		if (statusCode != HttpStatus.SC_OK) {
    			log.info("request '" + url + "' failed,the status is not 200,status:" + statusCode);
    			return "";
    		}
    		InputStream inputStream = postMethod.getResponseBodyAsStream();   
    		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, encoder));   
    		StringBuffer stringBuffer = new StringBuffer();   
    		String str= "";   
    		while((str = br.readLine()) != null){   
    			stringBuffer .append(str );   
    		}   
//            System.out.println("ResponseBody:\n" + stringBuffer.toString());
    		
//            String responseBody = postMethod.getResponseBodyAsString();
    		String responseBody = stringBuffer.toString();
    		return responseBody;
    	} catch (Exception e) {
    		log.error("executeHttpRequestString异常，url:" + url);
    		log.error("ERRORMSG:" + e.getMessage());
    	} finally {
    		postMethod.releaseConnection();
    	}
    	return null;
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static String executeHttpRequestString(String url, Map<String, String> keyValueMap,String encoder, int timeout) {
    	HttpClient client = createHttpClient();
    	PostMethod postMethod = new PostMethod(url);
    	try {
    		if (keyValueMap != null) {
    			Iterator it = keyValueMap.entrySet().iterator();
    			NameValuePair[] parameters = new NameValuePair[keyValueMap.size()];
    			int c = 0;
    			while (it.hasNext()) {
    				Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
    				NameValuePair nvp = new NameValuePair();
    				nvp.setName(entry.getKey());
    				nvp.setValue(entry.getValue());
    				parameters[c] = nvp;
    				c++;
    			}
    			postMethod.addParameters(parameters);
    		}
    		log.debug("query uri ===============" + postMethod.getURI());
    		postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
    		postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, encoder);
    		postMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, timeout);
    		int statusCode = client.executeMethod(postMethod);
    		if (statusCode != HttpStatus.SC_OK) {
    			log.info("request '" + url + "' failed,the status is not 200,status:" + statusCode);
    			return "";
    		}
    		String responseBody = postMethod.getResponseBodyAsString();
    		return responseBody;
    	} catch (Exception e) {
    		log.error("executeHttpRequestString异常，url:" + url);
    		log.error("ERRORMSG:" + e.getMessage());
    		log.error("CAUSE:" + e.getCause());
    	} finally {
    		postMethod.releaseConnection();
    	}
    	return null;
    }



    /**
     *
     * @param url
     * @param keyValueMap
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static String executeHttpRequestByGetType(String url, Map<String, String> keyValueMap, String charset) {
        HttpClient client = createHttpClient();
        StringBuffer sb = new StringBuffer(url);
        GetMethod getMethod = null;
        try {
            if (keyValueMap != null) {
                Iterator it = keyValueMap.entrySet().iterator();
                if (keyValueMap.size() > 0) {
                    sb.append("?");
                    while (it.hasNext()) {
                        Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
                        sb.append(entry.getKey() + "=" + entry.getValue() + "&");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                }

            }
            getMethod = new GetMethod(sb.toString());
            log.info("query uri ===============" + getMethod.getURI());
            getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
            getMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, StringUtils.isNoneBlank(charset) ? charset : DEFAULT_CHARSET);
            getMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, DEFAULT_TIMEOUT);
            getMethod.addRequestHeader("Host", "d1.weather.com.cn");
            getMethod.addRequestHeader("Referer", "http://www.weather.com.cn/live/");
            getMethod.addRequestHeader("Cookie", "vjuids=502d4d4e3.1547b1075c6.0.1b8720f3; pgv_pvi=5970710528; BIGipServerd1src_pool=3625155696.20480.0000; __asc=62c55358154a9a15228a3caf3ba; __auc=b397e8741547b721c9968090f68; f_city=%E5%8C%97%E4%BA%AC%7C101010100%7C; Hm_lvt_080dabacb001ad3dc8b9b9049b36d43b=1462875468,1462875509,1462935949,1463134327; Hm_lpvt_080dabacb001ad3dc8b9b9049b36d43b=1463135682; vjlast=1462353557.1463134327.11");
            int statusCode = client.executeMethod(getMethod);
            if (statusCode != HttpStatus.SC_OK) {
                log.info("request '" + url + "' failed,the status is not 200,status:" + statusCode);
                return "";
            }
            String responseBody = getMethod.getResponseBodyAsString();
            return responseBody;
        } catch (Exception e) {
            log.error("executeHttpRequestByGetType异常，url:" + url, e);
        } finally {
            if (getMethod != null) {
            	getMethod.releaseConnection();
            }
        }
        return null;
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static String executeHttpRequestByGetType(String url, Map<String, String> keyValueMap, String charset, int timeout) {
    	HttpClient client = createHttpClient();
    	StringBuffer sb = new StringBuffer(url);
    	GetMethod getMethod = null;
    	try {
    		if (keyValueMap != null) {
    			Iterator it = keyValueMap.entrySet().iterator();
    			if (keyValueMap.size() > 0) {
    				sb.append("?");
    				while (it.hasNext()) {
    					Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
    					sb.append(entry.getKey() + "=" + entry.getValue() + "&");
    				}
    				sb.deleteCharAt(sb.length() - 1);
    			}
    			
    		}
    		getMethod = new GetMethod(sb.toString());
    		log.info("query uri ===============" + getMethod.getURI());
    		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
    		getMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, StringUtils.isNoneBlank(charset) ? charset : DEFAULT_CHARSET);
    		getMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, timeout);
    		int statusCode = client.executeMethod(getMethod);
    		if (statusCode != HttpStatus.SC_OK) {
    			log.info("request '" + url + "' failed,the status is not 200,status:" + statusCode);
    			return "";
    		}
    		String responseBody = getMethod.getResponseBodyAsString();
    		return responseBody;
    	} catch (Exception e) {
    		log.error("executeHttpRequestByGetType异常，url:" + url + "[messgae:"+e.getMessage()+"]");
    	} finally {
    		if (getMethod != null) {
    			getMethod.releaseConnection();
    		}
    	}
    	return null;
    }

    /**
     *
     * @param surl
     * @param keyValueMap
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static String executeHttpRequestUrl(String surl, Map<String, String> keyValueMap) {
        String charset = "gbk";
        StringBuffer sb = new StringBuffer(surl);
        StringBuffer buffer = null;
        try {
            if (keyValueMap != null) {
                Iterator it = keyValueMap.entrySet().iterator();
                if (keyValueMap.size() > 0) {
                    sb.append("?");
                    while (it.hasNext()) {
                        Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
                        sb.append(entry.getKey() + "=" + entry.getValue() + "&");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                }

            }
            log.info("hotel search url:" + sb.toString());
            URL url = new URL(sb.toString());
            URLConnection con = null;
            BufferedReader reader = null;
            buffer = null;
            if (url != null) {
                buffer = new StringBuffer();
                HttpURLConnection http = null;
                try {
                    con = url.openConnection();
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

                } catch (Exception e) {
                    log.error("error,url=" + url.toString(), e);
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
        } catch (MalformedURLException e) {
            log.error("error,url=" + sb.toString(), e);
        }

        return buffer != null ? buffer.toString().trim() : "".intern();
    }
    
    public static void main(String[] args) throws IOException {
    	String cookie = "_uab_collina=150685781639882198034084; acw_tc=AQAAALsXBFOJRQsAAhMz0swyW0t+Ct+o; gr_user_id=909ce1cd-6cb2-4511-bff3-6332150b54d2; acw_sc=59d0d2ec6f828ac691a092b3220f52265336b081; Hm_lvt_aba7c981bd757402fc23fa14ae4c7708=1506856798; Hm_lpvt_aba7c981bd757402fc23fa14ae4c7708=1506857981; Hm_lvt_2d02af1a29752dc3c0b9e49f97d041ef=1506856798; Hm_lpvt_2d02af1a29752dc3c0b9e49f97d041ef=1506857981; _ga=GA1.2.121729812.1506856798; _gid=GA1.2.88964236.1506856798; u_asec=090%23qCQXE4XPXOlXuTi0XXXXXuS8vzgEjU0Jzl4C3efQ%2B9kODzxbhn5AAwdvWXQXi679XvS5XvXQ0ZsNLdXiXXdjY60JvoxllTQXxTYEWCKigBwSfrlgp8EWl4ycW%2BLeBSJWrHCCIHN3SCwuTrJlpLcBlsDe3kgNmE2fyzJ7%2F%2BIKgBNW3zl9p8MIrvK7WHhlgZDplb%2FEIBKNgvxNpbw1%2Fd26ydP7p1LSlLcSlHYESTQXUOjl%2FCRvwuKim6vvwqavKM%2BPsiavwqijRLjlQqavwuqBm6vvwqavx82pXvXPceEUFWGNEcsTXvXumEXBQ2qtCXQXiJW3YNAXfX%3D%3D; gr_session_id_ad678802b3096ab4=826dbe54-17db-4b8c-bae4-2fb7c950d454; gr_cs1_826dbe54-17db-4b8c-bae4-2fb7c950d454=user_id%3A15068567974730006328859773432116";
    	int start = 1, max = 10;
		for (int i = start; i <= max; i++) {
			String url = "http://www.happyjuzi.com/star-ku-0-0-0-0-0-0-0/p" + i + ".html";
	    	String content = executeRequestWithJsoupGetWithCookie(url, null, "utf-8", cookie);
			System.out.println(content);
	    	Document doc = Jsoup.parse(content);
			Elements eles = doc.select("a.name_hotstar");
			if (eles.size() > 0) {
				for (Element imgEle : eles) {
					System.out.println(i + "==>" + imgEle.text());
				}
			}
		}
    	
	}
    
    public static HttpClient createHttpClient() {
		return new HttpClient(new HttpClientParams(),new SimpleHttpConnectionManager(true));
	}
    
    public static String executeRequestWithJsoupGet(String url, Map<String, String> keyValueMap,String encoder) {
		String result = null;
		Response response = null;
		int count = 1;
		String[] userAgentArr = new String[]{
				"Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1",
				"Mozilla/5.0 (iPad; CPU OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1",
				"Mozilla/5.0 (iPad; CPU OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1",
				"Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1",
				"Mozilla/5.0 (Linux; Android 5.1.1; Nexus 6 Build/LYZ28E) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Mobile Safari/537.36",
				"Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Mobile Safari/537.36",
				"Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Mobile Safari/537.36",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:52.0) Gecko/20100102 Firefox/52.0"
		};
		int len = userAgentArr.length;
		Random random = new Random();
		String userAgent = userAgentArr[random.nextInt(len)];
//		String userAgent = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Mobile Safari/537.36";
		while (count <= 3) {
			try {
				Connection connection = Jsoup.connect(url)
						.ignoreContentType(true)
						.userAgent(userAgent)
//						.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0")
//						.userAgent("Mozilla/5.0 (iPad; CPU OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1")
						.header("Host", "m.toutiao.com")
						.header("Referer", "https://servicewechat.com/wx5cae48102dc09d5e/3/page-frame.html")
						;
				if (keyValueMap != null) {
					response = connection.data(keyValueMap).timeout(DEFAULT_TIMEOUT).method(Method.GET).execute();
				} else {
					response = connection.timeout(DEFAULT_TIMEOUT).execute();
				}
				if (response != null) {
					result = response.body();
					break;
				}
			} catch(Exception e) {
				log.error("executeRequestWithJsoupGet异常，url:" + url, e);
				result = "error";
				count++;
			}
		}
		return result;
	}
    public static String executeRequestWithJsoupGetWithCookie(String url, Map<String, String> keyValueMap,String encoder, String cookie) {
    	String result = null;
    	Response response = null;
    	int count = 1;
    	while (count <= 3) {
    		try {
    			Connection connection = Jsoup.connect(url).ignoreContentType(true);
    			connection.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
    			connection.header("Accept-Encoding", "gzip, deflate");
    			connection.header("Accept-Language", "zh-CN,zh;q=0.8");
    			connection.header("Cache-Control", "max-age=0");
    			connection.header("Connection", "keep-alive");
    			connection.header("Host", "www.yidianzixun.com");
    			connection.header("Referer", "http://www.yidianzixun.com/");
    			connection.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
    			if (StringUtils.isNotBlank(cookie)) {
    				connection.header("Cookie", cookie);
    			}
    			if (keyValueMap != null) {
    				response = connection.data(keyValueMap).timeout(DEFAULT_TIMEOUT).method(Method.GET).execute();
    			} else {
    				response = connection.timeout(DEFAULT_TIMEOUT).method(Method.GET).execute();
    			}
    			if (response != null) {
    				result = response.body();
    				break;
    			}
    			System.out.println(response);
    		} catch(Exception e) {
    			log.error("executeRequestWithJsoupGet异常，url:" + url, e);
    			result = "error";
    			count++;
    		}
    	}
    	return result;
    }
    
    public static String executeRequestWithJsoupPostWithIp(String url, Map<String, String> keyValueMap,String encoder, String ip) {
		String result = null;
		Response response = null;
		try {
			//获取请求连接
			Connection con = Jsoup.connect(url);
			if (StringUtils.isNotBlank(ip)) {
				con.header("X-Forwarded-For", ip);
			}
			//遍历生成参数
			if(keyValueMap != null){
				for (Entry<String, String> entry : keyValueMap.entrySet()) {     
					//添加参数
					con.data(entry.getKey(), entry.getValue());
				} 
			}
			response = con.ignoreContentType(true).method(Method.POST).timeout(DEFAULT_TIMEOUT).execute();
			if (response != null) {
				result = response.body();
			}
		} catch(Exception e) {
			log.error("executeHttpRequest异常，url:" + url, e);
		}
		return result;
	}
	public static String executeRequestWithJsoupPost(String url, Map<String, String> keyValueMap,String encoder) {
		String result = null;
		Response response = null;
		try {
			//获取请求连接
	        Connection con = Jsoup.connect(url);
	        //遍历生成参数
	        if(keyValueMap != null){
	            for (Entry<String, String> entry : keyValueMap.entrySet()) {     
	               //添加参数
	                con.data(entry.getKey(), StringUtils.isNotBlank(entry.getValue()) ? entry.getValue() : "");
	               } 
	        }
			response = con.ignoreContentType(true).method(Method.POST).execute();
			if (response != null) {
				result = response.body();
			}
		} catch(Exception e) {
			log.error("executeHttpRequest异常，url:" + url, e);
		}
		return result;
	}
	public static String executeRequestWithJsoupPostNoResponse(String url, Map<String, String> keyValueMap,String encoder) {
		String result = null;
		try {
			//获取请求连接
			Connection con = Jsoup.connect(url);
			//遍历生成参数
			if(keyValueMap != null){
				for (Entry<String, String> entry : keyValueMap.entrySet()) {     
					//添加参数
					con.data(entry.getKey(), StringUtils.isNotBlank(entry.getValue()) ? entry.getValue() : "");
				} 
			}
			con.ignoreContentType(true).method(Method.POST).execute();
			result = "success";
		} catch(Exception e) {
			log.error("executeHttpRequest异常，url:" + url, e);
			result = "error";
		}
		return result;
	}
	public static String executeRequestWithJsoupPost(String url, Map<String, String> keyValueMap,String encoder, int timeout) {
		String result = null;
		Response response = null;
		try {
			//获取请求连接
	        Connection con = Jsoup.connect(url);
	        //遍历生成参数
	        if(keyValueMap != null){
	            for (Entry<String, String> entry : keyValueMap.entrySet()) {     
	               //添加参数
	                con.data(entry.getKey(), entry.getValue());
	               } 
	        }
			response = con.ignoreContentType(true).timeout(timeout).method(Method.POST).execute();
			if (response != null) {
				result = response.body();
			}
		} catch(Exception e) {
			log.error("executeHttpRequest异常，url:" + url, e);
		}
		return result;
	}
	@SuppressWarnings("deprecation")
	public static String sendPost(String url, String data, String encoder) {
		HttpClient client = createHttpClient();
        PostMethod postMethod = new PostMethod(url);
        try {
            log.debug("query uri ===============" + postMethod.getURI());
            StringRequestEntity postingString = new StringRequestEntity(data);// json传递  
//            postMethod.setRequestHeader("Content-type", "text/html");
            postMethod.setRequestHeader("Content-type", "application/json");
            postMethod.setRequestEntity(postingString);  
            postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
            postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, encoder);
            postMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, DEFAULT_TIMEOUT);
            int statusCode = client.executeMethod(postMethod);
            if (statusCode != HttpStatus.SC_OK) {
                log.info("request '" + url + "' failed,the status is not 200,status:" + statusCode);
                return "";
            }
            InputStream inputStream = postMethod.getResponseBodyAsStream();   
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, encoder));   
            StringBuffer stringBuffer = new StringBuffer();   
            String str= "";   
            while((str = br.readLine()) != null){   
                stringBuffer .append(str );   
            }   
//            System.out.println("ResponseBody:\n" + stringBuffer.toString());
            
//            String responseBody = postMethod.getResponseBodyAsString();
            String responseBody = stringBuffer.toString();
            return responseBody;
        } catch (Exception e) {
            log.error("executeHttpRequestString异常，url:" + url);
            log.error("ERRORMSG:" + e.getMessage());
        } finally {
            postMethod.releaseConnection();
        }
        return null;
	}
}
