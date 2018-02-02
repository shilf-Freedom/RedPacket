package cn.freedom.redpacket.service;

import cn.freedom.redpacket.pojo.RedPacket;

public interface RedPacketService {
	
	public RedPacket getRedPacket(Long id);
	
	public int decreaseRedPacket(Long id);
	
	public RedPacket getRedPacketForUpdate(Long id);
	
}
