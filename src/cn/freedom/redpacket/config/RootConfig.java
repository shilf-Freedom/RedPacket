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
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

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
	
}
