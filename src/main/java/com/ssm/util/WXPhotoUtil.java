package com.ssm.util;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.ssm.model.WXjsTicket;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class WXPhotoUtil {
    private static final Logger logger = LoggerFactory.getLogger(WXPhotoUtil.class);
    //获取jsapi_ticket的url
    private static final String JSAPIURL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=ACCESS_TOKEN&type=jsapi";

    private static String wxjsTicket="";
    private static int wxjsTicket_Time=0;
    //过期时间为7200秒
    private static int Expires_Period = 7200;


    public static WXjsTicket getWXjsTicket(String accessToken) throws IOException {
        WXjsTicket wXjsTicket = null;
        String requestUrl= JSAPIURL.replace("ACCESS_TOKEN", accessToken);

        wXjsTicket = new WXjsTicket();
        if(StringUtils.isEmpty(wxjsTicket)||HasExpired()){
            // 发起GET请求获取凭证
            JSONObject jsonObject = WeiXinUtil.doGetStr(requestUrl);
            if (null != jsonObject) {
                try {

                    wXjsTicket.setJsTicket(jsonObject.getString("ticket"));
                    wXjsTicket.setJsTicketExpiresIn(jsonObject.getInteger("expires_in"));
                } catch (JSONException e) {
                    wXjsTicket = null;
                    // 获取wXjsTicket失败
                    logger.error("获取wXjsTicket失败 errcode:{} errmsg:{}", jsonObject.getInteger("errcode"), jsonObject.getString("errmsg"));
                }
            }else {
                wXjsTicket.setJsTicket(wxjsTicket);
            }
        }

        return wXjsTicket;
    }

    /**
     * @desc ： 4.1 生成签名的函数
     * @param nonceStr 随机串，自己定义
     * @param timeStamp 生成签名用的时间戳
     * @param url 需要进行免登鉴权的页面地址，也就是执行dd.config的页面地址
     * @return
     * @throws Exception String
     */

    public static String getSign(String jsTicket, String nonceStr, String timeStamp, String url) throws Exception {
        String plainTex = "jsapi_ticket=" + jsTicket + "&noncestr=" + nonceStr + "&timestamp=" + timeStamp + "&url=" + url;
        System.out.println(plainTex);
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(plainTex.getBytes("UTF-8"));
            return byteToHex(crypt.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new Exception(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * @desc ：4.2 将bytes类型的数据转化为16进制类型
     *
     * @param hash
     * @return
     *   String
     */
    private static String byteToHex(byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", new Object[] { Byte.valueOf(b) });
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }


    /**
     * 判断wxjsTicket是否过期
     * @return
     */
    private static Boolean HasExpired()
    {
        if (wxjsTicket_Time != 0)
        {
            //过期时间，允许有一定的误差，一分钟。获取时间消耗
            Date date = new Date();//当前时间
            int timestamp = Integer.parseInt(String.valueOf(date.getTime()/1000));
            if((timestamp-wxjsTicket_Time)>Expires_Period){
                return true;
            }
        }
        return false;
    }
}
