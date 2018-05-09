/**
 * @(#)Md5Util.java, Aug 29, 2007.
 *
 */

package com.kpcoin.utils;

import java.security.MessageDigest;

public class Md5Util {

    /*
     * public static int getLocation(String[] fields, String field) { if (fields ==
     * null || fields.length == 0) { return -1; } for (int i = 0; i <
     * fields.length; i++) { //System.out.println(fields[i]); if
     * (fields[i].indexOf(field) != -1) { //System.out.println("i:" + i); return
     * i; } } return -1; }
     */

    private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

    public static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    /**
     * MD5Encode
     * @param origin
     * @return MD5 code
     */
    public static String MD5Encode(String origin) {
        String resultString = null;

        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            resultString = byteArrayToHexString(md.digest(resultString
                    .getBytes("utf-8")));
        } catch (Exception ex) {

        }
        return resultString;
    }

    public static void main(String[] args) {
//		String s_str = title + split + source + split + url;
//		String s_str = "860955021909167";
//		String d_str = MD5Encode(s_str);
//		System.out.println(d_str);
//		System.out.println(d_str.length());
//    	String aid = "9839891";
//    	String cdnUrl = getArticleCdnUrl(aid);
//    	System.out.println("cdnUrl:" + cdnUrl);
    	
    	String appkey = "clientpaybiz";
        String secret = "^ifeng@)!&biz_clientpay$";
        String orderNo = "20170512170501";
        String payeeAccount = "13520986585";
        String amount = "0.2";
		String s_str = appkey + orderNo + payeeAccount + amount + secret;
		System.out.println(s_str);
		String d_str = MD5Encode("ifengnews_14170825142454567937");
		System.out.println(d_str);
		System.out.println(MD5Encode(MD5Encode("chenbw")));
	}
    public static final String cdnMd5Key = "~!@#$%^&*()_+{}|\":>?<";
    /**
     * 自媒体文章生成cdnurl
     * @param aid
     * @return
     */
    public static String getArticleCdnUrl(String aid) {
    	String dir1,dir2,dir3;
    	String md5Str = Md5Util.MD5Encode(aid + cdnMd5Key);
    	System.out.println(md5Str);
    	dir1 = md5Str.substring(0, 3);
    	dir2 = md5Str.substring(3, 6);
    	dir3 = md5Str.substring(6, 9);
    	String cdnUrl = "http://cdn.iclient.ifeng.com/res/article/" + dir1 + "/" + dir2 + "/" + dir3 + "/" + aid + ".html";
    	return cdnUrl;

    }

}
