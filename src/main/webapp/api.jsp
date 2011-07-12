<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="ja" xml:lang="ja">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <meta http-equiv="Content-Style-Type" content="text/css" />
  <meta http-equiv="Content-Script-Type" content="text/JavaScript" />
  <link href="${contextPath}/css/default.css" rel="stylesheet" type="text/css" media="screen,projection" charset="utf-8" />
  <title>関ジャバカンファレンス 2011 AWS ではじめる Programmable Cloud</title>
</head>
<body>
<h2>関ジャバカンファレンス 2011 AWS ではじめる Programmable Cloud</h2>

<h3>インスタンス</h3>
<div id="ec2">
</div>
<input id="ec2runbtn" type="button" value="run!"></input>

<h3>EBS</h3>
<div id="ebs">
</div>


<script type="text/javascript" src="${contextPath }/js/jquery-1.6.1.js"></script>
<script type="text/javascript" src="${contextPath }/js/underscore.js"></script>
<script type="text/javascript">
var basePath = "${contextPath}";
jQuery(function($){
	$("#ec2runbtn").click(function(){
		$.ajax(basePath + "/api/ec2/run/tomcat",{
			dataType : "json",
			success : function(data){
				console.log(data);
			}
		})
	});
	
});
</script>
</body>
</html>
