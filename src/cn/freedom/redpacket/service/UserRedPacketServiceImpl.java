package cn.freedom.redpacket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cn.freedom.redpacket.dao.RedPacketDao;
import cn.freedom.redpacket.dao.UserRedPacketDao;
import cn.freedom.redpacket.pojo.RedPacket;
import cn.freedom.redpacket.pojo.UserRedPacket;
import redis.clients.jedis.Jedis;

@Service
public class UserRedPacketServiceImpl implements UserRedPacketService {
	
	@Autowired
	private UserRedPacketDao userRedPacketDao = null;
	
	@Autowired
	private RedPacketDao redPacketDao = null;
	
	@Autowired
	private RedisTemplate redisTemplate = null;
	
	@Autowired
	private RedisRedPacketService redisRedPacketService = null;
	
	private static String SCRIPT = "local listkey = 'red_packet_list_'..KEYS[1]\r\n" + 
			"local redPacket = 'red_packet_'..KEYS[1]\r\n" + 
			"local stock = tonumber(redis.call('hget', redPacket, 'stock'))\r\n" + 
			"if stock <= 0 then return 0 end\r\n" + 
			"stock = stock - 1\r\n" + 
			"redis.call('hset', redPacket, 'stock', tostring(stock))\r\n" + 
			"redis.call('rpush', listkey, ARGV[1])\r\n" + 
			"if stock == 0 then return 2 end\r\n" + 
			"return 1 \n";
	String sha1 = null;
	
	private int result = 0;

	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW, isolation=Isolation.READ_COMMITTED)
	public int grabRedPacket(Long redPacketId, Long userId) {
		// 不做任何处理，查询数据库，会出现超发或少发
		RedPacket redPacket = redPacketDao.getRedPacket(redPacketId);
		// 乐观锁：在SQL中使用for update来锁定行，避免出现问题，但性能下降
//		RedPacket redPacket = redPacketDao.getRedPacketForUpdate(redPacketId);
		
		if(redPacket.getStock() > 0) {
			// 不进行任何处理
//			redPacketDao.decreaseRedPacket(redPacketId);
			// 乐观锁，采用CAS原理
			int flag = redPacketDao.decreaseRedPacketForVersion(redPacketId, redPacket.getVersion());
			
			if(flag != 0) {
				UserRedPacket userRedPacket = new UserRedPacket();
				userRedPacket.setRedPacketId(redPacketId);
				userRedPacket.setUserId(userId);
				userRedPacket.setAmount(redPacket.getUnitAmount());
				String msg = "用户 " + userId + " 抢了用户 " + redPacket.getUserId() + 
						" 的 " + redPacket.getUnitAmount() + " 的红包";
				userRedPacket.setNote(msg);
				result = userRedPacketDao.grabRedPacket(userRedPacket);
			}
		}
		
		return result;
	}

	@Override
	public Long grabRedPacketByRedis(Long redPacketId, Long userId) {
		Long result = null;
		
		// 构造userId和time参数字符串，将在保存UserRedPackst时用到
		String args = userId + "-" + System.currentTimeMillis();
		
		// 获取底层Redis操作对象
		Jedis jedis = (Jedis) redisTemplate.getConnectionFactory().getConnection().getNativeConnection();
		
		try {
			// 如果sha1为空，加载脚本，返回一个sha1编码
			if (sha1 == null) {
				sha1 = jedis.scriptLoad(SCRIPT);
			}
			
			// 执行脚本返回结果
			result = (Long) jedis.evalsha(sha1, 1, redPacketId + "", args);
			
			// 返回2时为最后一个红包，将抢红包信息异步保存到数据库中
			if (result == 2) {
				// 获取单个小红包金额
				String unitAmountStr = jedis.hget("red_packet_" + redPacketId, "unit_amount");
				Double unitAmount = Double.parseDouble(unitAmountStr);
				System.out.println("currentThreadName:" + Thread.currentThread().getName());
				
				// 保存数据到数据库
				redisRedPacketService.saveUserRedPacketByRedis(redPacketId, unitAmount);
			} 
		} finally {
			if(jedis != null && jedis.isConnected()) {
				jedis.close();
			}
		}
		
		return result;
	}

}
