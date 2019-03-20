<%--
  Created by IntelliJ IDEA.
  User: SeaRan
  Date: 2018/6/4
  Time: 17:12
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html lang="en">
<head>
    <%
        String path = request.getContextPath();
    %>
    <meta charset="UTF-8">
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <meta name="viewport" content="width=device-width,
                                     initial-scale=1.0,
                                     maximum-scale=1.0,
                                     user-scalable=no">
    <title>支付界面</title>
    <script type="text/javascript" src="<%=path%>/resources/js/jquery-3.1.0.min.js"></script>
</head>
<body>
　　<input type="button" value="pay" onclick="pay()"/>

<script>
    var prepay_id ;
    var sign ;
    var appId ;
    var timeStamp ;
    var nonceStr ;
    var packageStr ;
    var signType ;

    function pay(){
        var url = '<%=path%>/wechat/wechatpay/pay';
        $.ajax({
            type:"post",
            url:url,
            dataType:"json",
            data:{openId:'${openId}'},
            success:function(data) {
                if(data.result_code == 'SUCCESS'){
                    appId = data.appid;
                    sign = data.sign;
                    timeStamp = data.timeStamp;
                    nonceStr = data.nonce_str;
                    packageStr = data.packageStr;
                    signType = data.signType;
                    //调起微信支付控件
                    callpay();
                }else{
                    alert("统一下单失败");
                }
            }
        });
    }

    function onBridgeReady(){
        WeixinJSBridge.invoke(
            'getBrandWCPayRequest', {
                "appId":appId,     //公众号名称，由商户传入
                "paySign":sign,         //微信签名
                "timeStamp":timeStamp, //时间戳，自1970年以来的秒数
                "nonceStr":nonceStr , //随机串
                "package":packageStr,  //预支付交易会话标识
                "signType":signType     //微信签名方式
            },
            function(res){
                //alert(JSON.stringify(res));
                if(res.err_msg == "get_brand_wcpay_request:ok" ) {
                    //window.location.replace("index.html");
                    alert('支付成功');
                }else if(res.err_msg == "get_brand_wcpay_request:cancel"){
                    alert('支付取消');
                }else if(res.err_msg == "get_brand_wcpay_request:fail" ){
                    alert('支付失败');
                }
                //使用以上方式判断前端返回,微信团队郑重提示：res.err_msg将在用户支付成功后返回    ok，但并不保证它绝对可靠。
            }
        );
    }

    function callpay(){
        if (typeof WeixinJSBridge == "undefined"){
            if( document.addEventListener ){
                document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false);
            }else if (document.attachEvent){
                document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
                document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
            }
        }else{
            onBridgeReady();
        }
    }
</script>

</body>
</html>
