package cn.freedom.redpacket.config;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration.Dynamic;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/*
 * 任何扩展了AbstractAnnotationConfigDispatcherServletInitializer的类
 * 都会自动配置DispatcherServlet和Spring应用上下文,
 * 实际上，AbstractAnnotationConfigDispatcherServletInitializer
 * 会同时创建DispatcherServlet和ContextLoaderListener,
 * getServletConfigClasses()方法返回的带有@Configuration注解的类会用来定义DispatcherServlet应用上下文中的bean,
 * getRootConfigClasses()方法返回的带有@Configuration注解的类会用来定义ContextLoaderListener应用上下文中的bean。
 * 
 */
public class MyWebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	// Spring IoC容器配置
	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class<?>[] {RootConfig.class};
	}

	// DispatcherServlet的URI映射关系
	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class<?>[] {WebConfig.class};
	}

	// DispatcherServlet拦截器内容
	@Override
	protected String[] getServletMappings() {
		return new String[] {"/"};
	}
	
	@Override
	protected void customizeRegistration(Dynamic registration) {
		// 配置上传文件路径
		String filePath = "";
		// 单个文件最大5M
		Long singleMax = (long) (5*Math.pow(2, 20));
		// 总文件大小10M
		Long totalMax = (long) (10*Math.pow(2, 20));
		// 设置上传文件配置
		registration.setMultipartConfig(new MultipartConfigElement(filePath, singleMax, totalMax, 0));
	}

}
