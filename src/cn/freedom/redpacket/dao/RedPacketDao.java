package cn.freedom.redpacket.dao;

import org.springframework.stereotype.Repository;

import cn.freedom.redpacket.pojo.RedPacket;

@Repository
public interface RedPacketDao {
	
	public RedPacket getRedPacket(Long id);
	
	public int decreaseRedPacket(Long id);
	
	public RedPacket getRedPacketForUpdate(Long id);
	
	public int decreaseRedPacketForVersion(Long id, Integer version);
	
}
