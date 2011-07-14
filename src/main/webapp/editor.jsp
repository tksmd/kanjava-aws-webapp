<!DOCTYPE html>
<html lang="ja">
<head>
<title>Cloud Editor</title>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <meta http-equiv="Content-Style-Type" content="text/css" />
  <meta http-equiv="Content-Script-Type" content="text/JavaScript" />
  <link href="${contextPath}/css/default.css" rel="stylesheet" type="text/css" media="screen,projection" charset="utf-8" />
  <link href="${contextPath}/css/vanilla-1.0.1.css" rel="stylesheet" type="text/css" media="screen,projection" charset="utf-8" />    
  <link href="${contextPath}/css/editor.css" rel="stylesheet" type="text/css" media="screen,projection" charset="utf-8" />
  <link href="${contextPath}/css/humanity/jquery-ui-1.8.14.custom.css" rel="stylesheet" type="text/css" media="screen,projection" charset="utf-8" />    
  <title>関ジャバカンファレンス 2011 AWS ではじめる Programmable Cloud</title>
</head>
<body>

<h2>Cloud Editor</h2>
<div id="container">

<!-- 
<input type="button" id="connect" value="connect" />
 -->
  
<ul id="item">
<li><img id="item_tomcat" src="${contextPath }/img/tomcat.png" class="view" draggable="true" /></li>
<li><img id="item_glassfish" src="${contextPath }/img/glassfish.png" class="view" draggable="true" /></li>
<li><img id="item_ebs" src="${contextPath }/img/ebs.png" class="view" draggable="true"/></li>
<li><img id="item_elb" src="${contextPath }/img/elb.png" class="view" draggable="true" /></li>
</ul>
<canvas id="editor" width="800px" height="600px">
</canvas>
</div>

<div id="dialog"></div>

<script type="text/javascript" src="${contextPath }/js/jquery-1.6.1.js"></script>
<script type="text/javascript" src="${contextPath }/js/jquery-ui-1.8.14.custom.js"></script>
<script type="text/javascript" src="${contextPath }/js/underscore.js"></script>
<script type="text/javascript" src="${contextPath }/js/editor.js"></script>
<script type="text/javascript" src="${contextPath }/js/filter.js"></script>
<script type="text/javascript">

jQuery(function($){
  var basePath = "${contextPath}";	

  // register properties
  $.data($("#item_tomcat")[0],"props",{"key":"ec2","opts":{"type":"tomcat"}});
  $.data($("#item_glassfish")[0],"props",{"key":"ec2","opts":{"type":"glassfish"}});
  $.data($("#item_ebs")[0],"props",{"key":"ebs","opts":{}});
  $.data($("#item_elb")[0],"props",{"key":"elb","opts":{}});

  // create editor
  var editor = CloudEditor($("#editor")[0],basePath);
  editor.setDialog($("#dialog"));

  $("ul#item li img.view").bind("dragstart",function(evt){
    editor.dragItem(evt);
  });

  $("ul#item li").click(function(){
    $(this).siblings(".selected").removeClass("selected");
    $(this).toggleClass("selected");
  });

  $("input#connect").click(function(){
    editor.startConnect();
  });
  
});
</script>

</body>
</html>
