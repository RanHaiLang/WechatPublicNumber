package com.ssm.controller;

import com.alibaba.fastjson.JSONObject;
import com.ssm.model.AccessToken;
import com.ssm.model.WeixinOauth2Token;
import com.ssm.util.WeiXinUtil;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.ssm.util.WeiXinUtil.APPID;
import static com.ssm.util.WeiXinUtil.APPSECRET;
import static com.ssm.util.WeiXinUtil.doGetStr;

@RestController
@RequestMapping(value = "wechat")
public class WeChatController {
    //https://eam.saas.rminfo.net/auditing
    public static String url="";
    public static String wechaturl="";
    public static String modelId="";

    //获取用户id
    public static WeixinOauth2Token getUser(String code) throws IOException {
        System.out.println("code:"+code);
        WeixinOauth2Token weixinOauth2Token = new WeixinOauth2Token();
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+APPID+"&secret="+APPSECRET+"&code="+code+"&grant_type=authorization_code";
        JSONObject jsonObject = doGetStr(url);
        if(jsonObject!=null){
            System.out.println(jsonObject);
            /*String access_token = jsonObject.getString("access_token");//网页授权接口调用凭证,注意：此access_token与基础支持的access_token不同
            String expires_in = jsonObject.getString("expires_in");//access_token接口调用凭证超时时间，单位（秒）
            String refresh_token = jsonObject.getString("refresh_token");//用户刷新access_token*/
            String openid = jsonObject.getString("openid");//用户唯一标识，请注意，在未关注公众号时，用户访问公众号的网页，也会产生一个用户和公众号唯一的OpenID
            //String scope = jsonObject.getString("scope");//用户授权的作用域，使用逗号（,）分隔
            if(openid!=null){
                weixinOauth2Token.setOpenId(openid);
            }
        }
        return weixinOauth2Token;
    }

    //微信小程序使用，获取用户id
    @RequestMapping(value = "openId")
    @ResponseBody
    public String getUsers(String code,String appId,String appsecret) throws IOException {
        System.out.println("code:"+code);
        WeixinOauth2Token weixinOauth2Token = new WeixinOauth2Token();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid="+appId+"&secret="+appsecret+"&grant_type=authorization_code&js_code="+code;
        JSONObject jsonObject = doGetStr(url);
        if(jsonObject!=null){
            System.out.println(jsonObject);
            String openid = jsonObject.getString("openid");//用户唯一标识，请注意，在未关注公众号时，用户访问公众号的网页，也会产生一个用户和公众号唯一的OpenID
            if(openid!=null){
                weixinOauth2Token.setOpenId(openid);
            }
        }
        return weixinOauth2Token.getOpenId();
    }


    /* * 发送信息
     * @param content 文本内容
     * @param toUser 微信用户
     * @return
     */
    @RequestMapping(value = "/sendmessage",method = RequestMethod.POST)
    public static JSONObject sendTextMessageToUser(String openid,String evt_code,String desc,String datetime,String key) throws IOException {
        wechaturl = WeiXinUtil.getValue("wechaturl")+"WechatPublicNumber/";
        modelId = WeiXinUtil.getValue("modelId");
        if(!key.equals("0")){
            desc=new String(desc.getBytes("ISO-8859-1"),"UTF-8");
        }
        String json = "{\n" +
                "           \"touser\":\""+openid+"\",\n" +
                "           \"template_id\":\""+modelId+"\",\n" +
                "           \"url\":\""+wechaturl+"wechat/wechat/redirectEvents?code="+evt_code+"\",  \n" +
                "           \"data\":{\n" +
                "                   \"first\": {\n" +
                "                       \"value\":\"安全隐患\",\n" +
                "                       \"color\":\"#173177\"\n" +
                "                   },\n" +
                "                   \"keyword1\":{\n" +
                "                       \"value\":\""+evt_code+"\",\n" +
                "                       \"color\":\"#173177\"\n" +
                "                   },\n" +
                "                   \"keyword2\": {\n" +
                "                       \"value\":\""+desc+"\",\n" +
                "                       \"color\":\"#173177\"\n" +
                "                   },\n" +
                "                   \"keyword3\": {\n" +
                "                       \"value\":\""+datetime+"\",\n" +
                "                       \"color\":\"#173177\"\n" +
                "                   },\n" +
                "                   \"remark\":{\n" +
                "                       \"value\":\"请尽快处理！\",\n" +
                "                       \"color\":\"#173177\"\n" +
                "                   }\n" +
                "           }\n" +
                "       }";
        //获取access_token
        AccessToken accessToken = WeiXinUtil.getAccessToken();
        //发送消息路径
        String action = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token="+accessToken.getToken();
        System.out.println("json:"+json);

        //connectWeiXinInterface(action,json);
        JSONObject jsonObject = WeiXinUtil.doPostStr(action,json);

        return jsonObject;
    }

    /**
     * 绑定
     * */
    @RequestMapping(value = "/banding",method ={RequestMethod.POST,RequestMethod.GET} )
    @ResponseBody
    public ModelAndView bangding(String username, String password, String code,String openId) throws IOException {
        Map<String, Object> map = new HashedMap();
        ModelAndView modelAndView = new ModelAndView();
        JSONObject jsonObject = null;
        String data = "";

        url = WeiXinUtil.getValue("url")+"auditing/";
        //判断用户是否存在
        data = WeiXinUtil.sendGet(url+"wechatApp/getuser?username="+username+"&password="+password,"");
        jsonObject = JSONObject.parseObject(data);
        if(jsonObject.getString("result").equals("1")){//用户不存在
            modelAndView.addObject("msg",jsonObject.getString("message"));
            System.out.println(jsonObject.getString("message"));
            modelAndView.setViewName("/weui_err_msg");
        }
        if(jsonObject.getString("result").equals("2")){
            modelAndView.addObject("msg",jsonObject.getString("message"));
            modelAndView.setViewName("/weui_err_msg");
        }
        if(jsonObject.getString("result").equals("0")) {
            String openid = "";
            //获取微信用户id
            if(openId!=null&&openId!=""){
                openid= openId;
            }else {
                WeixinOauth2Token weixinOauth2Token = getUser(code);
                openid=weixinOauth2Token.getOpenId();
            }
            System.out.println("id:"+openid);
            if(openid!=null){
                //判断用户是否绑定
                data = WeiXinUtil.sendGet(url+"wechatApp/WhetherBanding?username="+username+"&openid="+openid,"");
                jsonObject = JSONObject.parseObject(data);
                if(jsonObject.getString("result").equals("1")){//该用户已绑定
                    modelAndView.addObject("msg",jsonObject.getString("message"));
                    System.out.println(jsonObject.getString("message"));
                    modelAndView.setViewName("/weui_err_msg");
                }else {
                    //绑定微信号与app用户
                    data = WeiXinUtil.sendPost(url+"wechatApp/wechatBanding","username="+username+"&openid="+openid);
                    jsonObject = JSONObject.parseObject(data);
                    if(jsonObject.getString("result").equals("1")){//该用户已绑定
                        modelAndView.addObject("msg",jsonObject.getString("message"));
                        System.out.println(jsonObject.getString("message"));
                        modelAndView.setViewName("/weui_err_msg");
                    }else {
                        modelAndView.addObject("msg",jsonObject.getString("message"));
                        System.out.println(jsonObject.getString("message"));
                        modelAndView.setViewName("/weui_success_msg");
                    }
                }
            }else {
                modelAndView.addObject("msg","授权失败请重新登录授权绑定");
                modelAndView.setViewName("/weui_err_msg");
            }



        }
        System.out.println(modelAndView);
        return modelAndView;
    }

    /**
     * 工单详情页跳转
     */
    @RequestMapping(value = "redirectEvents",method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView redirectEvent(String code){
        ModelAndView modelAndView = new ModelAndView("wechatevent");
        url = WeiXinUtil.getValue("url")+"auditing/";
        String data = WeiXinUtil.sendGet(url+"events/eventDetails?code="+code,"");
        JSONObject jsonObject = JSONObject.parseObject(data);
        if(jsonObject==null){
            modelAndView.setViewName("non_existent");
        }else {
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//这个是你要转成后的时间的格式
            String sd = sdf.format(new Date(Long.parseLong(String.valueOf(jsonObject.getString("EVT_DATE")))));   // 时间戳转换成时间
            jsonObject.put("EVT_DATE",sd);

            String data1 = WeiXinUtil.sendGet(url+"process/getProcess?code="+code,"");
            JSONObject jsonObject1 = JSONObject.parseObject(data1);

            //System.out.println(jsonObject);
            modelAndView.addObject("rmevent",jsonObject);
            modelAndView.addObject("details",jsonObject1);

            String imgurl = WeiXinUtil.getValue("url");
            modelAndView.addObject("imgurl",imgurl);
        }


        return modelAndView;
    }

    /**
     * 页面跳转
     * @throws IOException
     */
    @RequestMapping(value = "/redirectsuccessUrl")
    public ModelAndView redirectUrl(String msg){
        ModelAndView modelAndView = new ModelAndView("weui_success_msg");
        modelAndView.addObject("msg",msg);
        return modelAndView;
    }

    @RequestMapping(value = "/redirecterrUrl")
    public ModelAndView redirecterrUrl(String msg){
        ModelAndView modelAndView = new ModelAndView("weui_err_msg");
        modelAndView.addObject("msg",msg);
        return modelAndView;
    }

    /**
     * 解除绑定
     * @param username
     * @param password
     * @param code
     * @return
     */
    @RequestMapping(value = "unband",method = RequestMethod.POST)
    public ModelAndView UnBand(String username,String password,String code) throws IOException {
        ModelAndView modelAndView = new ModelAndView();
        JSONObject jsonObject = null;
        String data = "";

        url = WeiXinUtil.getValue("url")+"auditing/";
        //判断用户是否存在
        data = WeiXinUtil.sendGet(url+"wechatApp/getuser?username="+username+"&password="+password,"");
        jsonObject = JSONObject.parseObject(data);
        if(jsonObject.getString("result").equals("1")){//用户不存在
            modelAndView.addObject("msg",jsonObject.getString("message"));
            System.out.println(jsonObject.getString("message"));
            modelAndView.setViewName("/weui_err_msg");
        }
        if(jsonObject.getString("result").equals("2")){
            modelAndView.addObject("msg",jsonObject.getString("message"));
            modelAndView.setViewName("/weui_err_msg");
        }
        if(jsonObject.getString("result").equals("0")) {
            //获取微信用户id
            WeixinOauth2Token weixinOauth2Token = getUser(code);
            if (weixinOauth2Token.getOpenId() != null) {
                //判断app用户是否与微信id绑定
                data = WeiXinUtil.sendGet(url + "wechatApp/ornottobind?username=" + username + "&openid=" + weixinOauth2Token.getOpenId(), "");
                jsonObject = JSONObject.parseObject(data);
                if (jsonObject.getString("result").equals("1")) {//该app用户未于微信id绑定
                    modelAndView.addObject("msg", jsonObject.getString("message"));
                    System.out.println(jsonObject.getString("message"));
                    modelAndView.setViewName("/weui_err_msg");
                } else {
                    //解除绑定微信号与app用户
                    data = WeiXinUtil.sendPost(url + "wechatApp/unbind", "username=" + username + "&openid=" + weixinOauth2Token.getOpenId());
                    jsonObject = JSONObject.parseObject(data);
                    if (jsonObject.getString("result").equals("1")) {//解除绑定失败
                        modelAndView.addObject("msg", jsonObject.getString("message"));
                        System.out.println(jsonObject.getString("message"));
                        modelAndView.setViewName("/weui_err_msg");
                    } else {
                        modelAndView.addObject("msg", jsonObject.getString("message"));
                        System.out.println(jsonObject.getString("message"));
                        modelAndView.setViewName("/weui_success_msg");
                    }
                }
            } else {
                modelAndView.addObject("msg", "授权失败请重新登录授权解除绑定");
                modelAndView.setViewName("/weui_err_msg");
            }
        }
        return modelAndView;
    }


    public static void main_(String[] args) throws IOException {

        Map<String,String> map = new HashMap<String,String>();
        map.put("username","admin");
        map.put("password","123456");

        //sendTextMessageToUser("ohjpM1WmTSgWS9nlWOQ3d1dbrGLM","10069");
        //判断用户是否存在
        //String data = WeiXinUtil.sendGet(url+"wechatApp/getuser?username=admin&password=123456","");
        //判断用户是否绑定
        //String httpOrgCreateTestRtn = WeiXinUtil.sendGet(url+"wechatApp/WhetherBanding?username=admin&openid=123456","");
        //绑定用户
        /*String httpOrgCreateTest = WeiXinUtil.sendPost(url+"wechatApp/wechatBanding","username=admin&openid=123456");
        System.out.println(httpOrgCreateTest);*/

        //sendTextMessageToUser("ohjpM1Vxnz1CBBVtRX7Nh5XxvE6g");


        Date date1 = new Date();//1525936699 1525936714
        String timestamp = String.valueOf(date1.getTime()/1000);
        System.out.println(timestamp);

        Date date2 = new Date();
        String timestamp2 = String.valueOf(date2.getTime()/1000);
        System.out.println(timestamp2);

        System.out.println(Integer.parseInt(timestamp2)-Integer.parseInt(timestamp));


        System.out.println(WeiXinUtil.getValue("wechaturl")+"WechatPublicNumber/");

    }
}
