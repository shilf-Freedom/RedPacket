package cn.freedom.redpacket.service;

public interface RedisRedPacketService {
	
	public void saveUserRedPacketByRedis(Long redPacketId, Double unitAmount);

}
