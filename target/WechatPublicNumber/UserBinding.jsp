<%@ page language="java" pageEncoding="utf-8"%>
<html>
<head>
    <meta charset="UTF-8">
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <meta name="viewport" content="width=device-width,
                                     initial-scale=1.0,
                                     maximum-scale=1.0,
                                     user-scalable=no">

    <%
        String path = request.getContextPath();
        String code = request.getParameter("code");
    %>

    <title>用户绑定</title>
    <script type="text/javascript" src="<%=path%>/resources/js/jquery-3.1.0.min.js"></script>
    <link href="<%=path%>/resources/css/index.css?e106c93fc570c75dd3ac" rel="stylesheet"></head>
    <script type="text/javascript" src="<%=path%>/resources/js/index.js"></script>
</head>
<body>

    <form action="<%=request.getContextPath()%>/wechat/wechat/banding" method="post" id="userbindingform" role="form">
        <input type="hidden" name="code" value="<%=code%>">
        <div class="input-content">
            <div class="input-item">
                <div class="item-inner">
                    <div class="input-label">
                        隐患账号
                    </div>
                    <input type="text" name="username">
                </div>
            </div>
            <div class="input-item">
                <div class="item-inner">
                    <div class="input-label">
                        密码
                    </div>
                    <input type="password" name="password">
                </div>
            </div>
        </div>
        <div class="button-wrap">
            <button type="submit">绑定</button>
        </div>
        <div style="height: 30px;font-weight: 400;font-size: initial;margin-left: 5%;">
            <p>友情提示：</p>
        </div>
        <div style="font-weight: 400;font-size: initial;margin-left: 5%;">
            <p>1、输入安全隐患APP登录用户</p>
            <p>2、输入安全隐患APP登录密码</p>
            <p>3、一个微信号只能绑定一个安全隐患APP用户</p>
            <p>4、绑定后可以使用微信提醒待办</p>
        </div>
    </form>
</body>
<script>

</script>
</html>