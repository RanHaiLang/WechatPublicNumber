<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%--
  Created by IntelliJ IDEA.
  User: SeaRan
  Date: 2018/4/24
  Time: 17:00
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
    <%
        String path = request.getContextPath();
    %>
    <link href="<%=path%>/resources/wechatcss/item.css?e106c93fc570c75dd3ac" rel="stylesheet"></head>
    <link rel="stylesheet" href="<%=path%>/resources/bootstrap/css/bootstrap.css">
    <script type="text/javascript" src="<%=path%>/resources/js/jquery-3.1.0.min.js"></script>
    <script type="text/javascript" src="<%=path%>/resources/js/intense.min.js"></script>
    <title>隐患详情</title>
    <style>
        .title{
            width:40px;
            height:auto;
        }
    </style>
</head>
<body>
    <div class="yinhuan-list">
        <div class="item">
            <span class="name">单号:</span>
            <span class="value">${rmevent.code}</span>
        </div>
        <div class="item">
            <span class="name">隐患事件:</span>
            <span class="value">${rmevent.EVT_WORKADDRESS}</span>
        </div>
        <div class="item">
            <span class="name">提报单位:</span>
            <span class="value">${rmevent.OBJ_DESC}</span>
        </div>
        <div class="item">
            <span class="name">隐患提报时间:</span><%--${rmevent.EVT_DATE}--%>
            <span class="value">${rmevent.EVT_DATE}</span>
        </div>
        <div class="item">
            <span class="name">隐患发现人:</span>
            <span class="value">${rmevent.USER_DESC}</span>
        </div>
        <div class="item">
            <span class="name">提报部门:</span>
            <span class="value">${rmevent.EVT_MRCDESC}</span>
        </div>
        <div class="item">
            <span class="name">隐患分类:</span>
            <span class="value">${rmevent.EVT_UDFCHAR07DESC}</span>
        </div>
        <div class="item">
            <span class="name">隐患地点:</span>
            <span class="value">${rmevent.EVT_UDFCHAR02}</span>
        </div>
        <div class="item">
            <span class="name">隐患描述:</span>
            <span class="value">${rmevent.EVT_WORKADDRESS}</span>
        </div>
    </div>
    <div class="img-content">
        <div class="content-inner">
            <c:forEach items="${rmevent.R5rmdocuments}" var="document">
                <c:choose>
                        <c:when test="${fn:substringAfter(document.docnewname,'.')=='rmvb'||fn:substringAfter(document.docnewname,'.')=='rm'
                        ||fn:substringAfter(document.docnewname,'.')=='flv'||fn:substringAfter(document.docnewname,'.')=='mp4'
                        ||fn:substringAfter(document.docnewname,'.')=='avi'||fn:substringAfter(document.docnewname,'.')=='mov'
                        ||fn:substringAfter(document.docnewname,'.')=='RMVB'||fn:substringAfter(document.docnewname,'.')=='RM'
                        ||fn:substringAfter(document.docnewname,'.')=='FLV'||fn:substringAfter(document.docnewname,'.')=='MP4'
                        ||fn:substringAfter(document.docnewname,'.')=='AVI'||fn:substringAfter(document.docnewname,'.')=='MOV'}">
                            <div class="video_div">
                                <div class="img-wrap">
                                    <a href="#" style="position: relative">
                                        <div class="trueoffClass playTip glyphicon glyphicon-play" style="width: 50px;
                                        height: 50px;
                                        z-index: 90;
                                        border: 2px solid #ffff;
                                        border-radius: 50%;
                                        top: 50%;
                                        left: 50%;
                                        position: absolute;
                                        margin-top: -25px;
                                        margin-left: -25px;
                                        line-height: 46px;
                                        color: white;
                                        font-size: 30px;"></div>
                                        <div class="videoClass" style="width: 100%;height: 90px;background-color: black;z-index: 0">
                                            <video id="videoId" style="width: 100%;height: 90px;" preload="auto" x5-video-player-fullscreen="false">
                                                <source src="${imgurl}stream/video/${document.docnewname}">
                                            </video>
                                        </div>
                                    </a>
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="img-wrap" style="z-index: 100">
                                <a href="#">
                                    <img class="demo-image" src="${imgurl}stream/images/${document.docnewname}"/>
                                </a>
                            </div>
                        </c:otherwise>
                </c:choose>
            </c:forEach>
        </div>
    </div>
    <div class="time-wrap">
        <div class="time-title">
            提报过程
        </div>
        <div class="time-content">
            <c:forEach items="${details.Addetails}" var="detail" varStatus="idxStatus">
                <div class="time-item active">
                    <div class="left">
                        <div class="line"></div>
                        <div class="icon">
                            <c:choose>
                                <c:when test="${idxStatus.index==0}">
                                    <img src="<%=path%>/resources/wechatcss/images/3.png">
                                </c:when>
                                <c:otherwise>
                                    <img src="<%=path%>/resources/wechatcss/images/2.png">
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    <div class="right">
                        <div>${detail.add_created}</div>
                        <div class="name">${detail.usr_desc}</div>
                        <div>整改部门处理：${detail.ugr_desc}</div>
                        <p>部门负责人审核：${detail.add_text}</p>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>

    <script type="text/javascript" src="<%=path%>/resources/wechatjs/index.js"></script>
</body>
<script type="text/javascript">
    var elements = document.querySelectorAll( '.demo-image' );
    Intense( elements );
    window.onresize = function(){
        $('#videoId').style.width = window.innerWidth + "px";
        $('#videoId').style.height = window.innerHeight + "px";
    }
    $(".img-wrap").click(function(){
        $(this).find("video")[0].play();
    })

</script>
</html>
