<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<script src="https://code.jquery.com/jquery-3.2.0.js"></script>
<script type="text/javascript">
$(document).ready(function(){
	var max = 3000;
	for(var i = 0; i < max; i++){
		$.post({
			url:'${ pageContext.request.contextPath }/userRedPacket/grabRedPacketByRedis?redPacketId=1&userId='+i,
			success:function(result){}
		});
	}
});
</script>
</head>
<body>

</body>
</html>