package cn.freedom.redpacket.dao;

import org.springframework.stereotype.Repository;

import cn.freedom.redpacket.pojo.UserRedPacket;

@Repository
public interface UserRedPacketDao {
	
	public int grabRedPacket(UserRedPacket userRedPacket);

}
