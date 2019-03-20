package com.ssm.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ssm.model.AccessToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class WeiXinUtil {
    //从微信后台拿到APPID和APPSECRET 并封装为常量
    //本人账号：wxdfd601ee605f347c 测试账号：wxd92d6f9508262bba  人脉安全：wxafe859b1f85780a4 测试：wx123cec7360fa9e18
    public static String APPID = "";
    //本人账号：3bb02d939cb216a78f790a6e312fdaeb 测试账号：f4b65a4247e0ebf1a0253e5a32275c65 人脉安全：ae5584cf4ae9def902a42f414cc300ba 测试：9f12bd7dd167f4d3954b4259f91fee50
    public static String APPSECRET = "";
    //获取token路径
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";

    private static String mAccessToken="";
    private static int GetAccessToken_Time=0;
    //过期时间为7200秒
    private static int Expires_Period = 7200;



    /**
     * 读取properties文件值
     * */
    public static String getValue(String key){
        Properties p = new Properties();
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            HttpSession session = request.getSession();
            String path = session.getServletContext().getRealPath("/")+"WEB-INF/classes/";
            p.load(new FileInputStream(path + "url.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p.get(key).toString();
    }

    /**
     * 编写Get请求的方法。但没有参数传递的时候，可以使用Get请求
     *
     * @param url 需要请求的URL
     * @return 将请求URL后返回的数据，转为JSON格式，并return
     */
    public static JSONObject doGetStr(String url) throws ClientProtocolException, IOException {
        DefaultHttpClient client = new DefaultHttpClient();//获取DefaultHttpClient请求
        System.setProperty("jsse.enableSNIExtension", "false");
        HttpGet httpGet = new HttpGet(url);//HttpGet将使用Get方式发送请求URL
        JSONObject jsonObject = null;
        HttpResponse response = client.execute(httpGet);//使用HttpResponse接收client执行httpGet的结果
        HttpEntity entity = response.getEntity();//从response中获取结果，类型为HttpEntity
        if(entity != null){
            String result = EntityUtils.toString(entity,"UTF-8");//HttpEntity转为字符串类型
            jsonObject = JSON.parseObject(result);//字符串类型转为JSON类型
        }
        return jsonObject;
    }

    /**
     * 编写Post请求的方法。当我们需要参数传递的时候，可以使用Post请求
     *
     * @param url 需要请求的URL
     * @param outStr  需要传递的参数
     * @return 将请求URL后返回的数据，转为JSON格式，并return
     */
    public static JSONObject doPostStr(String url,String outStr) throws ClientProtocolException, IOException {
        DefaultHttpClient client = new DefaultHttpClient();//获取DefaultHttpClient请求
        HttpPost httpost = new HttpPost(url);//HttpPost将使用Get方式发送请求URL
        JSONObject jsonObject = null;
        httpost.setEntity(new StringEntity(outStr,"UTF-8"));//使用setEntity方法，将我们传进来的参数放入请求中
        HttpResponse response = client.execute(httpost);//使用HttpResponse接收client执行httpost的结果
        System.out.println(response.getEntity());
        String result = EntityUtils.toString(response.getEntity(),"UTF-8");//HttpEntity转为字符串类型
        jsonObject = JSONObject.parseObject(result);//字符串类型转为JSON类型
        return jsonObject;
    }

    /**
     * 获取AccessToken
     * @return 返回拿到的access_token及有效期
     */
    public static AccessToken getAccessToken() throws IOException{
        AccessToken token = new AccessToken();
        APPID = WeiXinUtil.getValue("APPID");
        APPSECRET = WeiXinUtil.getValue("APPSECRET");
        String url = ACCESS_TOKEN_URL.replace("APPID", APPID).replace("APPSECRET", APPSECRET);//将URL中的两个参数替换掉
        //如果为空，或者过期，需要重新获取
        if (StringUtils.isEmpty(mAccessToken) || HasExpired())
        {
            //获取
            JSONObject jsonObject = doGetStr(url);//使用刚刚写的doGet方法接收结果
            if(jsonObject!=null){ //如果返回不为空，将返回结果封装进AccessToken实体类
                token.setToken(jsonObject.getString("access_token"));//取出access_token
                token.setExpiresIn(jsonObject.getInteger("expires_in"));//取出access_token的有效期
                mAccessToken = jsonObject.getString("access_token");
                Date date = new Date();
                int timestamp = Integer.parseInt(String.valueOf(date.getTime()/1000));
                GetAccessToken_Time = timestamp;
            }
        }else {
            token.setToken(mAccessToken);
        }


        return token;
    }

    /**
     * 判断Access_token是否过期
     * @return
     */
    private static Boolean HasExpired()
    {
        if (GetAccessToken_Time != 0)
        {
            //过期时间，允许有一定的误差，一分钟。获取时间消耗
            Date date = new Date();//当前时间
            int timestamp = Integer.parseInt(String.valueOf(date.getTime()/1000));
            if((timestamp-GetAccessToken_Time)>Expires_Period){
                return true;
            }
        }
        return false;
    }

    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url;
            System.out.println(urlNameString);
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            System.setProperty("jsse.enableSNIExtension", "false");
            connection.setRequestProperty("jsse.enableSNIExtension","false");
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        System.out.println(result);
        return result;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            System.setProperty("jsse.enableSNIExtension", "false");
            conn.setRequestProperty("jsse.enableSNIExtension","false");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }


}
