package com.kpcoin.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by buwei on 2018/6/6.
 */
public class ImageUtil {

    public static Logger logger = LoggerFactory.getLogger(ImageUtil.class);

    /**
     *
     * @param imgUrl
     * @return
     */
    public static JSONObject calcImgSize(String imgUrl) {
        if (StringUtils.isNotBlank(imgUrl)) {
            try {
                URL url = new URL(imgUrl);
                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);
                BufferedImage image = ImageIO.read(connection.getInputStream());
                int srcWidth = image .getWidth();      // 源图宽度
                int srcHeight = image .getHeight();    // 源图高度

                logger.info("imgUrl:{}, imgWidth:{}, imgHeight:{}", imgUrl, srcWidth, srcHeight);

                JSONObject item = new JSONObject();
                item.put("url", imgUrl);
                item.put("width", srcWidth);
                item.put("height", srcHeight);
                return item;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String imgUrl = "http://i1.go2yd.com/image.php?url=0JCFGelzgz";
        JSONObject wh = calcImgSize(imgUrl);
        System.out.println(wh);
    }
}
