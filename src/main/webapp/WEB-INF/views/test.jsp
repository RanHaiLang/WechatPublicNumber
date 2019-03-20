<%--
  Created by IntelliJ IDEA.
  User: SeaRan
  Date: 2018/6/7
  Time: 16:40
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String path = request.getContextPath();
%>
<script type="text/javascript" src="<%=path%>/resources/js/jquery-3.1.0.min.js"></script>

<html>
<head>
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <meta name="viewport" content="width=device-width,
                                     initial-scale=1.0,
                                     maximum-scale=1.0,
                                     user-scalable=no">
    <title>拍照上传</title>
</head>
<body>
    <form id="fileform" method="post" enctype="multipart/form-data" action="<%=path%>/wechat/wechatphoto/uploadimg">
        <input type="file" name="myfile">
        <input type="submit"  value="上传"/>
    </form>
</body>

<script>
    function up() {
        var formdata = $("#fileform").serialize();
        alert(formdata)
        $.ajax({
            url:'<%=path%>/wechat/wechatphoto/uploadimg',
            type:'POST',
            data:formdata,
            success:function (res) {

            }
        })
    }
</script>
</html>
