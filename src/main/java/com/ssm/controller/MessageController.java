package com.ssm.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ssm.model.*;
import com.ssm.util.CheckUtil;
import com.ssm.util.WeiXinUtil;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ssm.controller.WeChatController.url;
import static com.ssm.controller.WeChatController.wechaturl;
import static com.ssm.util.WeiXinUtil.APPID;
import static com.ssm.util.WeiXinUtil.getAccessToken;

@RestController
@RequestMapping(value = "message")
public class MessageController {
    private static final String CREATE_MENU_URL = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";
    private static final String GET_MENU_URL = "https://api.weixin.qq.com/cgi-bin/menu/get?access_token=ACCESS_TOKEN";
    private static final String DEL_MENU_URL = "https://api.weixin.qq.com/cgi-bin/menu/delete?access_token=ACCESS_TOKEN";

    /**
     * 微信接入
     * @return
     * @throws IOException
     */
    @RequestMapping(value="/connect",method = {RequestMethod.GET, RequestMethod.POST})
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 将请求、响应的编码均设置为UTF-8（防止中文乱码）
        request.setCharacterEncoding("UTF-8");  //微信服务器POST消息时用的是UTF-8编码，在接收时也要用同样的编码，否则中文会乱码；
        response.setCharacterEncoding("UTF-8");

        boolean isGet = request.getMethod().toLowerCase().equals("get");

        if(isGet){
            String signature = request.getParameter("signature");
            String timestamp = request.getParameter("timestamp");
            String nonce = request.getParameter("nonce");
            String echostr = request.getParameter("echostr");


            PrintWriter out = response.getWriter();

            if(CheckUtil.checkSignature(signature, timestamp, nonce)){
                //如果校验成功，将得到的随机字符串原路返回
                out.print(echostr);
            }
        }else {
            doPost(request,response);
        }
        AccessToken accessToken = WeiXinUtil.getAccessToken();
        String menu ="";
        //deteleMenu(accessToken.getToken());
        menu = getMenu(accessToken.getToken());
        System.out.println(menu);
        if(StringUtils.isEmpty(menu)){
            menu = JSONObject.toJSON(initMenu()).toString();
            int result = createMenu(accessToken.getToken(), menu);
            if(result==0){
                System.out.println("菜单创建成功！");
                menu = getMenu(accessToken.getToken());
                System.out.println(menu);
            }else{
                System.out.println("菜单创建失败");
            }
        }
    }

    //获取菜单
    public static String getMenu(String token) throws ClientProtocolException, IOException{
        String url = GET_MENU_URL.replace("ACCESS_TOKEN", token);
        String result = "";
        JSONObject jsonObject = doGetStr(url);
        if(jsonObject!=null){
            result = jsonObject.getString("menu");
        }
        return result;
    }
    //创建菜单
    public static int createMenu(String token,String menu) throws ClientProtocolException, IOException {
        int result = 0;
        String url = CREATE_MENU_URL.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = doPostStr(url, menu);

        if(jsonObject != null){
            result = jsonObject.getInteger("errcode");
        }

        return result;
    }
    //删除菜单
    public  static int deteleMenu(String token) throws IOException {
        String url = DEL_MENU_URL.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = doGetStr(url);
        int result = 1;
        if(jsonObject !=null){
            result = jsonObject.getInteger("errcode");
        }
        return result;
    }
    //自定义菜单组装
    public static Menu initMenu() throws UnsupportedEncodingException {

        wechaturl = WeiXinUtil.getValue("wechaturl");//+"WechatPublicNumber/";
        //wechaturl = WeiXinUtil.getValue("wechaturl");
        APPID = WeiXinUtil.getValue("APPID");
        Menu menu = new Menu();


        ViewButton viewButton1_1 = new ViewButton();
        viewButton1_1.setType("view");
        viewButton1_1.setName("用户绑定");
        String bt1uri = URLEncoder.encode(wechaturl+"UserBinding.jsp","UTF-8");
        viewButton1_1.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid="+APPID+"&redirect_uri="+bt1uri+"&response_type=code&scope=snsapi_base&state=STATE#wechat_redirect");


       /* ViewButton viewButton3_1 = new ViewButton();
        viewButton3_1.setType("view");
        viewButton3_1.setName("解除绑定");
        String redirect_uri = URLEncoder.encode(wechaturl+"UserUnbind.jsp", "UTF-8");
        viewButton3_1.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid="+APPID+"&redirect_uri="+redirect_uri+"&response_type=code&scope=snsapi_base&state=STATE#wechat_redirect");
        */

        ViewButton viewButton3_1 = new ViewButton();
        viewButton3_1.setType("view");
        viewButton3_1.setName("微信支付");
        String redirect_uri = URLEncoder.encode(wechaturl+"wechat/wechatpay/redirectpay", "UTF-8");
        viewButton3_1.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid="+APPID+"&redirect_uri="+redirect_uri+"&response_type=code&scope=snsapi_base&state=STATE#wechat_redirect");

        ViewButton viewButton4_1 = new ViewButton();
        viewButton4_1.setType("view");
        viewButton4_1.setName("拍照");
        String redirect4_uri = URLEncoder.encode(wechaturl+"wechat/wechatphoto/redirectphoto", "UTF-8");
        viewButton4_1.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid="+APPID+"&redirect_uri="+redirect4_uri+"&response_type=code&scope=snsapi_base&state=STATE#wechat_redirect");

        menu.setButton(new Button[]{viewButton1_1,viewButton3_1,viewButton4_1});

        return menu;

    }

    //消息的接收与响应
    @RequestMapping(value = "dopost")
    public static void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        String str = null;
        try {

            //将request请求，传到Message工具类的转换方法中，返回接收到的Map对象

            Map<String, String> map = xmlToMap(request);

            //从集合中，获取XML各个节点的内容

            String ToUserName = map.get("ToUserName");

            String FromUserName = map.get("FromUserName");

            String CreateTime = map.get("CreateTime");

            String MsgType = map.get("MsgType");

            String Content = map.get("Content");

            String MsgId = map.get("MsgId");

            //System.out.println(FromUserName+":"+Content);

            if (MsgType.equals("text")) {//判断消息类型是否是文本消息(text)
                boolean result=Content.matches("[0-9]+");
                if(result){
                    url = WeiXinUtil.getValue("url")+"auditing/";
                    System.out.println("url:"+url);
                    //获取工单信息
                    String data = WeiXinUtil.sendGet(url+"events/eventDetails?code="+Content,"");
                    JSONObject jsonObject = JSONObject.parseObject(data);
                    if(jsonObject!=null){
                        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");//这个是你要转成后的时间的格式
                        String sd = sdf.format(new Date(Long.parseLong(String.valueOf(jsonObject.getString("EVT_DATE")))));   // 时间戳转换成时间
                        WeChatController.sendTextMessageToUser(FromUserName,Content,jsonObject.getString("EVT_WORKADDRESS")==null?"":jsonObject.getString("EVT_WORKADDRESS"),sd,"0");
                    }else {
                        TestMessage message = new TestMessage();

                        message.setFromUserName(ToUserName);//原来【接收消息用户】变为回复时【发送消息用户】
                        message.setToUserName(FromUserName);
                        message.setMsgType("text");
                        message.setCreateTime(new Date().getTime());//创建当前时间为消息时间
                        message.setContent("工单号不存在");

                        str = objectToXml(message); //调用Message工具类，将对象转为XML字符串

                        out.print(str); //返回转换后的XML字符串
                        out.close();
                    }

                }else {
                    TestMessage message = new TestMessage();

                    message.setFromUserName(ToUserName);//原来【接收消息用户】变为回复时【发送消息用户】

                    message.setToUserName(FromUserName);

                    message.setMsgType("text");

                    message.setCreateTime(new Date().getTime());//创建当前时间为消息时间

                    message.setContent("请输入正确的工单号");

                    str = objectToXml(message); //调用Message工具类，将对象转为XML字符串

                    out.print(str); //返回转换后的XML字符串

                    out.close();
                }




            }

        } catch (DocumentException e) {

            e.printStackTrace();

        }

    }

    /**

     * 新建方法，将接收到的XML格式，转化为Map对象

     * @param request 将request对象，通过参数传入

     * @return 返回转换后的Map对象

     */

    public static Map<String, String> xmlToMap(HttpServletRequest request) throws IOException, DocumentException {

        Map<String, String> map = new HashMap<String, String>();

        //从dom4j的jar包中，拿到SAXReader对象。

        SAXReader reader = new SAXReader();



        InputStream is = request.getInputStream();//从request中，获取输入流

        Document doc =  reader.read(is);//从reader对象中,读取输入流

        Element root = doc.getRootElement();//获取XML文档的根元素

        List<Element> list = root.elements();//获得根元素下的所有子节点

        for (Element e : list) {

            map.put(e.getName(), e.getText());//遍历list对象，并将结果保存到集合中

        }
        is.close();
        return map;

    }

    /**
     * 将文本消息对象转化成XML格式
     *
     * @param message 文本消息对象
     * @return 返回转换后的XML格式
     */

    public static String objectToXml(TestMessage message) {

        XStream xs = new XStream();

        //由于转换后xml根节点默认为class类，需转化为<xml>

        xs.alias("xml", message.getClass());

        return xs.toXML(message);

    }

    /**
     * 编写Get请求的方法。但没有参数传递的时候，可以使用Get请求
     *
     * @param url 需要请求的URL
     * @return 将请求URL后返回的数据，转为JSON格式，并return
     */
    public static JSONObject doGetStr(String url) throws ClientProtocolException, IOException {
        DefaultHttpClient client = new DefaultHttpClient();//获取DefaultHttpClient请求
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
        //System.out.println(response.getEntity());
        String result = EntityUtils.toString(response.getEntity(),"UTF-8");//HttpEntity转为字符串类型
        jsonObject = JSONObject.parseObject(result);//字符串类型转为JSON类型
        return jsonObject;
    }
}
