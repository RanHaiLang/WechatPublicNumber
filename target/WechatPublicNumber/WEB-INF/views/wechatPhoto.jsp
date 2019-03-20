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
<script src="http://res.wx.qq.com/open/js/jweixin-1.2.0.js" type="text/javascript"></script>

<%--百度地图没有密匙的可以去官网申请--%>
<%--<script type="text/javascript" src="http://api.map.baidu.com/api?v=2.0&ak=XhnyMmUYBCbTZUm7wex6eXhapjE34mi7"></script>
<script type="text/javascript" src="http://developer.baidu.com/map/jsdemo/demo/convertor.js"></script>
<script type="text/javascript" src="https://3gimg.qq.com/lightmap/components/geolocation/geolocation.min.js"></script>--%>
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
    <button onclick="takePicture()">拍照</button>
    <button onclick="uploadImg()">图片上传</button>
    <button onclick="getLocation()">获取地理位置</button>
    <button onclick="PreviewImage()">图片预览</button>
    <button onclick="StartRecord()">开始录音</button>
    <button onclick="StopRecord()">停止录音</button>
    <button onclick="PlayVoice()">播放录音</button>
    <button onclick="TranslateVoice()">语音识别</button>
    <input type="text" id="inputId"/>
    <img src="" id="imgid">
<script>

</script>

</body>
<script>
    $(function(){
        //注入权限验证配置
        wx.config({
            debug: false, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
            appId: '${appId}', // 必填，公众号的唯一标识
            timestamp: '${timestamp}', // 必填，生成签名的时间戳
            nonceStr: '${noncestr}', // 必填，生成签名的随机串
            signature: '${signature}',// 必填，签名，见附录1
            jsApiList: ['chooseImage', 'uploadImage','downloadImage',
                        'getLocalImgData','openLocation','getLocation',
                        'checkJsApi','previewImage','startRecord',
                        'stopRecord','playVoice','translateVoice',
                        'onMenuShareQQ'] // 必填，需要使用的JS接口列表，所有JS接口列表见附录2
        });
        wx.checkJsApi({
            jsApiList: ['getLocation'], // 需要检测的JS接口列表，所有JS接口列表见附录2,
            success: function(res) {
                if (res.checkResult.getLocation == false) {
                    alert('你的微信版本太低，不支持微信JS接口，请升级到最新的微信版本！');
                    return;
                }
            }
        });

        wx.ready(function(){
            // config信息验证后会执行ready方法，所有接口调用都必须在config接口获得结果之后，config是一个客户端的异步操作，所以如果需要在页面加载时就调用相关接口，则须把相关接口放在ready函数中调用来确保正确执行。对于用户触发时才调用的接口，则可以直接调用，不需要放在ready函数中。
            wx.onMenuShareQQ({
                title: '第七篇 ：微信公众平台开发实战Java版之如何获取微信用户基本信息', // 分享标题
                desc: '第七篇 ：微信公众平台开发实战Java版之如何获取微信用户基本信息', // 分享描述
                link: 'http://a0922c7f.ngrok.io/index.jsp', // 分享链接
                imgUrl: 'http://images.cnblogs.com/cnblogs_com/liuhongfeng/737147/o_1442809977405.jpg', // 分享图标
                success: function () {
                    // 用户确认分享后执行的回调函数
                    alert("确认分享");
                },
                cancel: function () {
                    // 用户取消分享后执行的回调函数
                }
            });
        });
        wx.error(function(res){
            // config信息验证失败会执行error函数，如签名过期导致验证失败，具体错误信息可以打开config的debug模式查看，也可以在返回的res参数中查看，对于SPA可以在这里更新签名。
            alert("验证出错");
        });
    });

    var images = {
        localId : [],
        serverId : []
    };
    //拍照
    function takePicture(){
        wx.chooseImage({
            count: 1, // 默认9
            sizeType: ['original', 'compressed'], // 可以指定是原图还是压缩图，默认二者都有
            sourceType: ['album', 'camera'], // 可以指定来源是相册还是相机，默认二者都有
            success: function (res) {
                var localIds = res.localIds; // 返回选定照片的本地ID列表，localId可以作为img标签的src属性显示图片
                $('#imgid').attr("src",localIds);
                $('#inputId').val(localIds);
                images.localId = localIds;
            }
        });
    }

    var localData;
    //上传图片
    function uploadImg() {
        if (images.localId.length == 0) {
            alert('请先使用 chooseImage 接口选择图片');
            return;
        }
        var i = 0, length = images.localId.length;
        images.serverId = [];

        function upload() {
            wx.uploadImage({
                localId : images.localId[i],
                success : function(res) {
                    i++;
                    alert('已上传：' + i + '/' + length);
                    images.serverId.push(res.serverId);

                    wx.downloadImage({
                        serverId: res.serverId, // 需要下载的图片的服务器端ID，由uploadImage接口获得
                        isShowProgressTips: 1, // 默认为1，显示进度提示
                        success: function (res) {
                            var localId = res.localId; // 返回图片下载后的本地ID
                            alert("localId:"+localId);
                        }
                    });
                    //将serverId上传至服务器
                    $.ajax({
                        type : "POST",
                        url : "<%=path%>/wechat/wechatphoto/uploadimg",
                        data : {
                            serverId :res.serverId
                        },
                        dataType : "text",
                        success : function(data) {
                            alert(data);
                        }
                    });
                    if (i < length) {
                        upload();
                    }
                },
                fail : function(res) {
                    alert(JSON.stringify(res));
                }
            });
        }
        upload();
    };

    var latitude;
    var longitude;
    var speed;
    var accuracy;
    //获取地理位置
    function getLocation(){
        wx.getLocation({
            type:'gcj02',// 默认为wgs84的gps坐标，如果要返回直接给openLocation用的火星坐标，可传入'gcj02'
            success : function(res) {
                /*latitude = res.latitude; // 纬度，浮点数，范围为90 ~ -90
                longitude = res.longitude; // 经度，浮点数，范围为180 ~ -180。
                speed = res.speed; // 速度，以米/每秒计
                accuracy = res.accuracy; // 位置精度
                alert(latitude);
                alert(accuracy);*/

                //getAddressInfo2(res.longitude,res.latitude);
                //使用微信内置地图查看位置接口
                wx.openLocation({
                    latitude : res.latitude, // 纬度，浮点数，范围为90 ~ -90
                    longitude : res.longitude, // 经度，浮点数，范围为180 ~ -180。
                    name : $("#nowAdd").val(), // 位置名
                    address : '', // 地址详情说明
                    scale : 28, // 地图缩放级别,整形值,范围从1~28。默认为最大
                    infoUrl : 'http://www.gongjuji.net' // 在查看位置界面底部显示的超链接,可点击跳转（测试好像不可用）
                });
            },
            cancel : function(res) {
                alert('未能获取地理位置');
            }
        });
    }

    //图片预览
    function PreviewImage() {
        wx.previewImage({
            current: 'http://pic19.nipic.com/20120223/3693935_220314428359_2.jpg', // 当前显示图片的http链接
            urls: ['http://pic19.nipic.com/20120223/3693935_220314428359_2.jpg','https://goss1.vcg.com/creative/vcg/800/version23/VCG41455360875.jpg'] // 需要预览的图片http链接列表
        });
    }
    //开始录音
    function StartRecord() {
        wx.startRecord();
        wx.onVoiceRecordEnd({
            // 录音时间超过一分钟没有停止的时候会执行 complete 回调
            complete: function (res) {
                recordLocalId = res.localId;
            }
        });
    }


    var recordLocalId;
    //停止录音
    function StopRecord() {
        wx.stopRecord({
            success: function (res) {
                recordLocalId = res.localId;
            }
        });
    }

    //播放语音
    function PlayVoice() {
        wx.playVoice({
            localId: recordLocalId // 需要播放的音频的本地ID，由stopRecord接口获得
        });
    }

    //识别音频
    function TranslateVoice() {
        wx.translateVoice({
            localId: recordLocalId, // 需要识别的音频的本地Id，由录音相关接口获得
            isShowProgressTips: 1, // 默认为1，显示进度提示
            success: function (res) {
                alert(res.translateResult); // 语音识别的结果
            }
        });
    }


    //调用百度地图API，经纬度转化成实际地址
    function getAddressInfo2(lon,lat) {
        var myGeo = new BMap.Geocoder();
        var pt = new BMap.Point(lon,lat);
        alert(pt);
         translateCallback2 = function (point){
            myGeo.getLocation(point, function(rs) {
                var addComp = rs.addressComponents;
                var addr = addComp.province+ addComp.city + addComp.district+ addComp.street+ addComp.streetNumber;
                $("#nowAdd").val(addr);
            });
        };
        setTimeout(function(){
            BMap.Convertor.translate(pt,2,translateCallback2);     //火星经纬度转成百度坐标
        }, 100);
    }

    

</script>
</html>
