package cn.freedom.redpacket.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
@ComponentScan(value="cn.freedom.*", includeFilters = {@Filter(type=FilterType.ANNOTATION, value = Controller.class)})
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {
	
	// 配置视图渲染器
	@Bean(name = "viewResolver")
	public ViewResolver initViewResolver() {
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
		viewResolver.setPrefix("/WEB-INF/view/");
		viewResolver.setSuffix(".jsp");
		return viewResolver;
	}
	
	// 初始化RequestMappingHandlerAdapter，加载http的json转化器
	@Bean(name="requestMappingHandlerAdapter")
	public HandlerAdapter initRequestMappingHandlerAdapter() {
		// 创建RequestMappingHandlerAdapter适配器
		RequestMappingHandlerAdapter rmha = new RequestMappingHandlerAdapter();
		
		// 创建http json转化器
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		
		// MappingJackson2HttpMessageConverter接收json类型消息的转化
		MediaType mediaType = MediaType.APPLICATION_JSON_UTF8;
		
		List<MediaType> mediaTypes = new ArrayList<MediaType>();
		mediaTypes.add(mediaType);
		
		// 加入转化器的支持类型
		converter.setSupportedMediaTypes(mediaTypes);
		
		// 往适配器中加入json转化器
		rmha.getMessageConverters().add(converter);
		
		return rmha;
	}
	
}
