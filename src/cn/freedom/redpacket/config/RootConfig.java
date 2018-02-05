package cn.freedom.redpacket.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import redis.clients.jedis.JedisPoolConfig;

@Configuration
@ComponentScan(value="cn.freedom.*", includeFilters= {@Filter(type = FilterType.ANNOTATION, value = {Service.class})})
@EnableTransactionManagement
public class RootConfig implements TransactionManagementConfigurer {

	private DataSource dataSource = null;
	
	// 配置数据库连接池
	@Bean(name="dataSource")
	public DataSource initDataSource() {
		if(dataSource == null) {
			Properties properties =  new Properties();
			properties.setProperty("driverClassName", "com.mysql.jdbc.Driver");
			properties.setProperty("url", "jdbc:mysql://localhost:3306/ssm?useUnicode=true&characterEncoding=utf-8");
			properties.setProperty("username", "root");
			properties.setProperty("password", "shilongfei");
			properties.setProperty("maxIdle", "20");
			properties.setProperty("maxWait", "30000");
			
			try {
				dataSource = BasicDataSourceFactory.createDataSource(properties);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return dataSource;
	}
	
	// 配置sqlSessionFactory
	@Bean(name="sqlSessionFactory")
	public SqlSessionFactoryBean initSqlSessionFactory() {
		SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
		Resource resource = new ClassPathResource("/cn/freedom/redpacket/config/mybatis-config.xml");
		
		sqlSessionFactory.setDataSource(initDataSource());
		sqlSessionFactory.setConfigLocation(resource);
		
		return sqlSessionFactory;
	}
	
	// 设置通过自动扫描，找到Mapper接口
	@Bean
	public MapperScannerConfigurer initMapperScannerConfigurer() {
		MapperScannerConfigurer msc = new MapperScannerConfigurer();
		
		msc.setSqlSessionFactoryBeanName("sqlSessionFactory");
		msc.setBasePackage("cn.freedom.*");
		msc.setAnnotationClass(Repository.class);
		
		return msc;
	}

	// 注册注解事务
	@Override
	@Bean(name="annotationDrivenTransactionManager")
	public PlatformTransactionManager annotationDrivenTransactionManager() {
		DataSourceTransactionManager manager = new DataSourceTransactionManager();
		manager.setDataSource(initDataSource());
		
		return manager;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean(name="redisTemplate")
	public RedisTemplate initRedisTemplate() {
		JedisPoolConfig config = new JedisPoolConfig();
		// 最大连接数
		config.setMaxTotal(100);
		// 最大空闲数
		config.setMaxIdle(50);
		// 最大等待毫秒
		config.setMaxWaitMillis(20000);
		
		// 创建Jedis工厂
		JedisConnectionFactory factory = new JedisConnectionFactory(config);
		// 设置地址
		factory.setHostName("localhost");
		// 设置端口
		factory.setPort(6379);
		// 调用后初始化方法，没有此方法将抛出异常
		factory.afterPropertiesSet();
		
		// 自定义Redis序列化器
		RedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();
		RedisSerializer stringRedisSerializer = new StringRedisSerializer();
		
		// 定义RedisTemplate，设置连接工厂
		RedisTemplate template = new RedisTemplate<>();
		template.setConnectionFactory(factory);
		// 设置序列化器
		template.setDefaultSerializer(stringRedisSerializer);
		template.setKeySerializer(stringRedisSerializer);
		template.setValueSerializer(stringRedisSerializer);
		template.setHashKeySerializer(stringRedisSerializer);
		template.setHashValueSerializer(stringRedisSerializer);
		
		return template;		
	}
	
}
