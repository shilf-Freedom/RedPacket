package cn.freedom.redpacket.service;

public interface UserRedPacketService {
	
	public int grabRedPacket(Long redPacketId, Long userId);
	
	public Long grabRedPacketByRedis(Long redPacketId, Long userId);

}
