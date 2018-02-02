package cn.freedom.redpacket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cn.freedom.redpacket.dao.RedPacketDao;
import cn.freedom.redpacket.dao.UserRedPacketDao;
import cn.freedom.redpacket.pojo.RedPacket;
import cn.freedom.redpacket.pojo.UserRedPacket;

@Service
public class UserRedPacketServiceImpl implements UserRedPacketService {
	
	@Autowired
	private UserRedPacketDao userRedPacketDao = null;
	
	@Autowired
	private RedPacketDao redPacketDao = null;
	
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

}
