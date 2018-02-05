package cn.freedom.redpacket.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import cn.freedom.redpacket.pojo.UserRedPacket;

@Service
public class RedisRedPacketServiceImpl implements RedisRedPacketService {
	
	private static final String PREFIX = "red_packet_list_";
	
	// 每次取出1000条，防止一次太多消耗内存
	private static final int TIME_SIZE = 1000;
	
	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisTemplate redisTemplate = null;
	
	@Autowired
	private DataSource dataSource = null;

	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	// 开启新线程运行
	@Async
	public void saveUserRedPacketByRedis(Long redPacketId, Double unitAmount) {
		System.out.println("开始保存数据");
		
		Long start = System.currentTimeMillis();
		
		BoundListOperations ops = redisTemplate.boundListOps(PREFIX + redPacketId);
		Long size = ops.size();
		Long times = size % TIME_SIZE == 0 ? size/TIME_SIZE : size/TIME_SIZE+1;
		int count = 0;
		List<UserRedPacket> userRedPackets = new ArrayList<>(TIME_SIZE);
		
		for(int i = 0; i < times; i++) {
			// 一次最多取出TIME_SIZE条UserRedPacket信息
			List userList = null;
			
			if(i == 0) {
				userList = ops.range(i * TIME_SIZE, (i + 1) * TIME_SIZE);
			}else {
				userList = ops.range(i * TIME_SIZE + 1, (i + 1) * TIME_SIZE);
			}
			
//			if(i == 0) {
//				userList = ops.range(0, (i + 1) * TIME_SIZE - 1);
//			}else {
//				userList = ops.range(i * TIME_SIZE, (i + 1) * TIME_SIZE - 1);
//			}
			
			// 清空userRedPackets
			userRedPackets.clear();
			
			// 循环获取参数列表userRedPackets
			for(int j = 0; j < userList.size(); j++) {
				// 获取userId和time，构造note
				String argsStr = userList.get(j).toString();
				String args[] = argsStr.split("-");
				String userIdStr = args[0];
				String timeStr = args[1];
				Long userId = Long.parseLong(userIdStr);
				Long time = Long.parseLong(timeStr);
				String note = "用户 " + userId + " 抢了用户 " + userId + 
						" 的 " + unitAmount + " 的红包";
				
				// 构造UserRedPacket
				UserRedPacket userRedPacket = new UserRedPacket();
				userRedPacket.setRedPacketId(redPacketId);
				userRedPacket.setUserId(userId);
				userRedPacket.setAmount(unitAmount);
				userRedPacket.setGrabTime(new Timestamp(time));
				userRedPacket.setNote(note);
				
				// 添加userRedPacket
				userRedPackets.add(userRedPacket);
			}
			
			// 插入抢红包信息
			count +=executeBatch(userRedPackets);
		}
		
		// 删除redis列表
		redisTemplate.delete(PREFIX + redPacketId);
		
		Long end = System.currentTimeMillis();
		System.out.println("耗时：" + (end - start) + "  保存记录：" + count);
	}

	private int executeBatch(List<UserRedPacket> userRedPackets) {

		Connection connection = null;
		Statement statement = null;
		int count[] = null;
		
		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);
			statement = connection.createStatement();
			
			for(UserRedPacket u : userRedPackets) {
				// 设置简单时间格式
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
				// 构造SQL语句
				String sql1 = "update T_RED_PACKET set stock = stock - 1 where id = " + u.getRedPacketId();
				String sql2 = "insert T_USER_RED_PACKET(red_packet_id, user_id, amount, grab_time, note) "
						+ "values(" + u.getRedPacketId() + ", " + u.getUserId() + ", " + u.getAmount() + 
						", '" + dateFormat.format(u.getGrabTime()) + "', '" + u.getNote() +  "')";
				
				// 添加到批处理
				statement.addBatch(sql1);
				statement.addBatch(sql2);
			}
			
			// 执行批量
			count = statement.executeBatch();
			
			// 提交事务
			connection.commit();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			try {
				if(connection != null && !connection.isClosed()) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return count.length/2;
	}

}
