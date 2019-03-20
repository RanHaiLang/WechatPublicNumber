<%--
  Created by IntelliJ IDEA.
  User: SeaRan
  Date: 2018/4/17
  Time: 17:56
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <meta name="viewport" content="width=device-width,
                                     initial-scale=1.0,
                                     maximum-scale=1.0,
                                     user-scalable=no">
    <link rel="stylesheet" type="text/css" href="../../resources/weui/dist/style/weui.min.css" />
</head>
<body>
<div class="weui-msg">
    <div class="weui-icon-area"><i class="weui-icon-warn weui-icon_msg"></i></div>
    <div class="weui-msg__text-area">
        <h2 class="weui-msg__title">操作失败</h2>
        <p class="weui-msg__desc">${msg}</p>
    </div>
    <div class="weui-msg__opr-area">
        <p class="weui-btn-area">
            <a href="javascript:;" class="weui-btn weui-btn_primary" onclick="window.history.go(-1);">确定</a>
            <%--<a href="javascript:;" class="weui-btn weui-btn_default" onclick="javascript:window.close();window.opener.location.reload();" >取消</a>--%>
        </p>
    </div>
    <div class="weui-msg__extra-area">
        <%--<a href="">查看详情</a>--%>
    </div>
</div>
</body>
<script>
    function sure() {

    }
</script>
</html>
