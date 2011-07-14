<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="ja" xml:lang="ja">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <meta http-equiv="Content-Style-Type" content="text/css" />
  <meta http-equiv="Content-Script-Type" content="text/JavaScript" />
  <link href="${contextPath}/css/default.css" rel="stylesheet" type="text/css" media="screen,projection" charset="utf-8" />
  <link href="${contextPath}/css/vanilla-1.0.1.css" rel="stylesheet" type="text/css" media="screen,projection" charset="utf-8" />    
  <title>メタデータ</title>
</head>
<body>
<h2>メタデータ一覧</h2>
<table>
  <thead>
    <tr>
      <th>インスタンスID</th>
      <th>公開 DNS 名</th>
      <th>IP Address</th>
      <th>アベイラビリティゾーン</th>      
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><c:out value="${metadata.instanceId }"/></td>
      <td><c:out value="${metadata.publicHostName}"/></td>
      <td><c:out value="${metadata.publicIPv4}"/></td>
      <td>-</td>    
    </tr>
  </tbody>
</table>

</body>
</html>