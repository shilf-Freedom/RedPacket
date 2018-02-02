package cn.freedom.redpacket.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.freedom.redpacket.service.UserRedPacketService;

@Controller
@RequestMapping("/userRedPacket")
public class UserRedPacketController {
	
	@Autowired
	UserRedPacketService service = null;
	
	@RequestMapping("/grabRedPacket")
	@ResponseBody
	public Map<String, Object> grabRedPacket(Long redPacketId, Long userId) {
		int result = service.grabRedPacket(redPacketId, userId);
		Map<String, Object> retMap = new HashMap<>();
		boolean flag = result > 0;
		retMap.put("success", flag);
		retMap.put("message", flag ? "抢红包成功！" : "抢红包失败！");
		return retMap;
	}

}
