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
	
	/*
	 * 运行前在redis中加入red_packet_1键值对
	 * hset red_packet_1 stock 2000
	 * hset red_packet_1 unit_amount 10.00
	 * hget red_packet_1 stock
	 */
	@RequestMapping("/grabRedPacketByRedis")
	@ResponseBody
	public Map<String, Object> grabRedPacketByRedis(Long redPacketId, Long userId) {
		Long result = service.grabRedPacketByRedis(redPacketId, userId);
		Map<String, Object> retMap = new HashMap<>();
		boolean flag = result > 0;
		retMap.put("success", flag);
		retMap.put("message", flag ? "抢红包成功！" : "抢红包失败！");
		return retMap;
	}

}
