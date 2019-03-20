package com.ssm.controller;

import com.ssm.model.*;
import com.ssm.util.WXPayUtil;
import com.ssm.util.WeiXinUtil;
import com.ssm.util.WxSign;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.ssm.controller.WeChatController.getUser;

/**
 * 微信支付controller
 * 1.用户发起微信支付，初始化数据、调用统一下单接口。生成JSAPI页面调用的支付参数并签名（paySign,prepay_id,nonceStr,timestamp）
 * 2.js如果返回Ok，提示支付成功，实际支付结果已收到通知为主。
 * 3.在微信支付结果通知中，获取微信提供的最终用户支付结果信息，支付结果等信息更新用户支付记录中
 * 4.根据微信支付结果通知中的微信订单号调用查询接口，如果查询是已经支付成功，则发送支付成功模板信息给客户
 */
@RestController
@RequestMapping(value = "wechatpay")
public class WechatPayController{
    private static Logger log = LoggerFactory.getLogger(WechatPayController.class);

    @RequestMapping(value = "redirectpay")
    public ModelAndView Pay(String code){
        ModelAndView modelAndView = new ModelAndView("wechatPay");
        //获取微信用户id
        try {
            WeixinOauth2Token weixinOauth2Token = getUser(code);
            modelAndView.addObject("openId",weixinOauth2Token.getOpenId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return modelAndView;
    }

    /**
 　　* 点击支付 统一下单,获得预付id(prepay_id)
 　　* @param request
 　　* @param response
 　　* @return
 　　 */
    @ResponseBody
    @RequestMapping(value = "pay")
    public WxPaySendData prePay(HttpServletRequest request,HttpServletResponse response,String openId){
        WxPaySendData result = new WxPaySendData();
        try {
        //商户订单号
        String out_trade_no = WXPayUtil.getOut_trade_no();
        //产品价格,单位：分
        Integer total_fee = 1;
        //客户端ip
        String ip = request.getRemoteAddr();
        //支付成功后回调的url地址
        String notify_url = "http://"+WeiXinUtil.getValue("wechaturl")+"wechat/wechatpay/callback";
        //统一下单
        String strResult = WXPayUtil.unifiedorder("testPay", out_trade_no, total_fee, ip, notify_url,openId);
        //解析xml
        XStream stream = new XStream(new DomDriver());
        stream.alias("xml", WxPaySendData.class);
        WxPaySendData wxReturnData = (WxPaySendData)stream.fromXML(strResult);

        //两者都为SUCCESS才能获取prepay_id
        if( wxReturnData.getResult_code().equals("SUCCESS") && wxReturnData.getReturn_code().equals("SUCCESS") ){
                //业务逻辑，写入订单日志(你自己的业务) .....
                String timeStamp = WXPayUtil.getTimeStamp();//时间戳
                String nonce_str = WXPayUtil.getNonceStr();//随机字符串
                //注：上面这两个参数，一定要拿出来作为后续的value，不能每步都创建新的时间戳跟随机字符串，不然H5调支付API，会报签名参数错误
                result.setResult_code(wxReturnData.getResult_code());
                result.setAppid(WeiXinUtil.getValue("APPID"));
                result.setTimeStamp(timeStamp);
                result.setNonce_str(nonce_str);
                result.setPackageStr("prepay_id="+wxReturnData.getPrepay_id());
                result.setSignType("MD5");

                //WXPayUtil.unifiedorder(.....) 下单操作中，也有签名操作，那个只针对统一下单，要区别于下面的paySign
                //第二次签名,将微信返回的数据再进行签名
                SortedMap<Object,Object> signMap = new TreeMap<Object,Object>();
                signMap.put("appId", WeiXinUtil.getValue("APPID"));
                signMap.put("timeStamp", timeStamp);
                signMap.put("nonceStr", nonce_str);
                signMap.put("package", "prepay_id="+wxReturnData.getPrepay_id());  //注：看清楚，值为：prepay_id=xxx,别直接放成了wxReturnData.getPrepay_id()
                signMap.put("signType", "MD5");
                String paySign = WxSign.createSign(signMap,  WeiXinUtil.getValue("Key"));//支付签名

                result.setSign(paySign);
            }else{
                result.setResult_code("fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }



    /**
     * 支付回调接口
     * @param request
     * @return
     */
    @RequestMapping("/callback")
    public void callBack(HttpServletRequest request, HttpServletResponse response){
        response.setContentType("text/xml;charset=UTF-8");
        try {
            InputStream is = request.getInputStream();
            String result = IOUtils.toString(is, "UTF-8");
            if("".equals(result)){
                response.getWriter().write("<xm><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[参数错误！]]></return_msg></xml>");
                return ;
            }
            //解析xml
            XStream stream = new XStream(new DomDriver());
            stream.alias("xml", WxPaySendData.class);
            WxPaySendData wxPaySendData = (WxPaySendData)stream.fromXML(result);
            System.out.println(wxPaySendData.toString());

            String appid = wxPaySendData.getAppid();
            String mch_id =wxPaySendData.getMch_id();
            String nonce_str = wxPaySendData.getNonce_str();
            String out_trade_no = wxPaySendData.getOut_trade_no();
            String total_fee = wxPaySendData.getTotal_fee();
            //double money = DBUtil.getDBDouble(DBUtil.getDBInt(wxPaySendData.getTotal_fee())/100.0);
            String trade_type = wxPaySendData.getTrade_type();
            String openid =wxPaySendData.getOpenid();
            String return_code = wxPaySendData.getReturn_code();
            String result_code = wxPaySendData.getResult_code();
            String bank_type = wxPaySendData.getBank_type();
            Integer cash_fee = wxPaySendData.getCash_fee();
            String fee_type = wxPaySendData.getFee_type();
            String is_subscribe = wxPaySendData.getIs_subscribe();
            String time_end = wxPaySendData.getTime_end();
            String transaction_id = wxPaySendData.getTransaction_id();
            String sign = wxPaySendData.getSign();

            //签名验证
            SortedMap<Object,Object> parameters = new TreeMap<Object,Object>();
            parameters.put("appid",appid);
            parameters.put("mch_id",mch_id);
            parameters.put("nonce_str",nonce_str);
            parameters.put("out_trade_no",out_trade_no);
            parameters.put("total_fee",total_fee);
            parameters.put("trade_type",trade_type);
            parameters.put("openid",openid);
            parameters.put("return_code",return_code);
            parameters.put("result_code",result_code);
            parameters.put("bank_type",bank_type);
            parameters.put("cash_fee",cash_fee);
            parameters.put("fee_type",fee_type);
            parameters.put("is_subscribe",is_subscribe);
            parameters.put("time_end",time_end);
            parameters.put("transaction_id",transaction_id);
            //以下4个参数针对优惠券(鼓励金之类的)这个坑真的弄了好久
            parameters.put("coupon_count",wxPaySendData.getCoupon_count());
            parameters.put("coupon_fee",wxPaySendData.getCoupon_fee());
            parameters.put("coupon_id_0",wxPaySendData.getCoupon_id_0());
            parameters.put("coupon_fee_0",wxPaySendData.getCoupon_fee_0());

            String sign2 = WxSign.createSign(parameters,WeiXinUtil.getValue("Key"));

            if(sign.equals(sign2)){//校验签名，两者需要一致，防止别人绕过支付操作，不付钱直接调用你的业务，不然，哈哈，你老板会很开心的 233333.。。。
                if(return_code.equals("SUCCESS") && result_code.equals("SUCCESS")){
                    //业务逻辑(先判断数据库中订单号是否存在，并且订单状态为未支付状态)
                    //do something ...

                    //request.setAttribute("out_trade_no", out_trade_no);
                    //通知微信.异步确认成功.必写.不然会一直通知后台.八次之后就认为交易失败了.
                    response.getWriter().write("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
                }else{
                    response.getWriter().write("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[交易失败]]></return_msg></xml>");
                }
            }else{
                //通知微信.异步确认成功.必写.不然会一直通知后台.八次之后就认为交易失败了.
                response.getWriter().write("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[签名校验失败]]></return_msg></xml>");
            }
            response.getWriter().flush();
            response.getWriter().close();
            return ;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
