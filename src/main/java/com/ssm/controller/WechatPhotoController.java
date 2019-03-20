package com.ssm.controller;

import com.alibaba.fastjson.JSONObject;
import com.ssm.model.AccessToken;
import com.ssm.model.WeixinOauth2Token;
import com.ssm.util.WXPayUtil;
import com.ssm.util.WXPhotoUtil;
import com.ssm.util.WeiXinUtil;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.ssm.controller.WeChatController.getUser;

@RestController
@RequestMapping(value = "wechatphoto")
public class WechatPhotoController {

    /**
     * 获取权限验证配置参数
     * @param code
     * @param request
     * @return
     */
    @RequestMapping(value = "redirectphoto")
    public ModelAndView getParameter(String code, HttpServletRequest request){
        ModelAndView modelAndView = new ModelAndView("wechatPhoto");
        //获取微信用户id
        try {

            //1.准备好参与签名的字段
            //1.1 url
            /*
             *以http://localhost/test.do?a=b&c=d为例
             *request.getRequestURL的结果是http://localhost/test.do
             *request.getQueryString的返回值是a=b&c=d
             */
            String urlString = request.getRequestURL().toString();
            String queryString = request.getQueryString();
            String queryStringEncode = null;
            String url;
            if (queryString != null) {
                queryStringEncode = URLDecoder.decode(queryString);
                url = urlString + "?" + queryStringEncode;
            } else {
                url = urlString;
            }

            String signedUrl = url;
            String timestamp = WXPayUtil.getTimeStamp();//时间戳
            String nonce_str = WXPayUtil.getNonceStr();//随机字符串


            String jsapi_ticket = WXPhotoUtil.getWXjsTicket(WeiXinUtil.getAccessToken().getToken()).getJsTicket();

            String sign = WXPhotoUtil.getSign(jsapi_ticket,nonce_str,timestamp,signedUrl);


            modelAndView.addObject("appId",WeiXinUtil.getValue("APPID"));
            modelAndView.addObject("timestamp",timestamp);
            modelAndView.addObject("noncestr",nonce_str);
            modelAndView.addObject("signature",sign);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return modelAndView;
    }

    /**
     * 图片上传
     */
    @RequestMapping(value = "uploadimg")
    public Map upload(String serverId,HttpServletRequest request) throws IOException {
        Map map = new HashMap();

        saveImageToDisk(serverId,request);

        uploadImg(serverId);
        return map;
    }


    /**
     * 保存图片至微信开发者服务器
     * @param mediaId
     * @return 文件名
     */
    public String saveImageToDisk(String mediaId,HttpServletRequest request) throws IOException {
        String filename = "";
        InputStream inputStream = getMedia(mediaId);
        System.out.println(inputStream);

        byte[] data = new byte[1024];
        int len = 0;
        FileOutputStream fileOutputStream = null;
        try {
            //服务器存图路径D:/soft/server_https/RMInfoApplicationServer81/webapps/vehicleupload/
            String path = "D:/soft/server_https/RMInfoApplicationServer81/webapps/vehicleupload/";
            filename = System.currentTimeMillis() + WXPayUtil.getNonceStr() + ".jpg";
            fileOutputStream = new FileOutputStream(path + filename);
            while ((len = inputStream.read(data)) != -1) {
                fileOutputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return filename;
    }

    /**
     * 获取临时素材
     */
    private InputStream getMedia(String mediaId) throws IOException {
        String url = "https://api.weixin.qq.com/cgi-bin/media/get";
        AccessToken access_token = WeiXinUtil.getAccessToken();
        String params = "access_token=" + access_token.getToken() + "&media_id=" + mediaId;
        InputStream is = null;
        try {
            String urlNameString = url + "?" + params;
            URL urlGet = new URL(urlNameString);
            System.out.println("\nurlget:"+urlGet);
            HttpURLConnection http = (HttpURLConnection) urlGet.openConnection();
            http.setRequestMethod("GET"); // 必须是get方式请求
            http.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            http.setDoOutput(true);
            http.setDoInput(true);
            http.connect();
            // 获取文件转化为byte流
            is = http.getInputStream();
            System.out.println("is:"+is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return is;
    }


    /**
     * HttpClient发送InputStream对象
     * 上传图片
     * @param mediaId
     * @throws IOException
     */
    public void uploadImg(String mediaId) throws IOException {
        InputStream ins = getMedia(mediaId);

        CloseableHttpClient client = HttpClients.createDefault();

        HttpPost post= new HttpPost("http://weboa.rminfo.net:8169/auditing/events/FileUplod");

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addBinaryBody("myfiles",ins, ContentType.create("multipart/form-data"),".jpg");
        //构建请求参数普通表单项
        StringBody stringBody = new StringBody("12",ContentType.MULTIPART_FORM_DATA);
        builder.addPart("id",stringBody);
        HttpEntity entity = builder.build();
        post.setEntity(entity);

        //发送请求
        HttpResponse response = client.execute(post);
        entity = response.getEntity();
        if (entity != null) {
            ins = entity.getContent();
            //转换为字节输入流
            BufferedReader br = new BufferedReader(new InputStreamReader(ins, Consts.UTF_8));
            String body = null;
            while ((body = br.readLine()) != null) {
                System.out.println(body);
            }
        }

    }

}
