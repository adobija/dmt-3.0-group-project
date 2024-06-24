package com.dmt.bankingapp.configMvc;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

	public void addViewControllers(ViewControllerRegistry registry) {
		//index site
		registry.addViewController("/").setViewName("indexTemplates/welcome");
		registry.addViewController("/homepage").setViewName("indexTemplates/homepage");
		//registry.addViewController("/welcome").setViewName("welcome");

		//register
		registry.addViewController("/signup").setViewName("registerTemplates/signup");
		//login
		registry.addViewController("/login").setViewName("loginTemplates/login");



		registry.addViewController("/hello").setViewName("hello");
		registry.addViewController("/admin").setViewName("admin");
		


	}

}