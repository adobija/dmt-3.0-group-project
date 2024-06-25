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
		//register
		registry.addViewController("/signup").setViewName("registerTemplates/signup");
		//login
		registry.addViewController("/login").setViewName("loginTemplates/login");
		//other
		registry.addViewController("/hello").setViewName("indexTemplates/hello");
		registry.addViewController("/admin").setViewName("admin");
		registry.addViewController("/about").setViewName("other/about");
		//client
		registry.addViewController("/changeName").setViewName("clientTemplates/editNameForm");
		registry.addViewController("/changePassword").setViewName("clientTemplates/editPasswordForm");
		registry.addViewController("/changeAdminPermission").setViewName("clientTemplates/editAdminForm");
		registry.addViewController("/byClientIdForm").setViewName("clientTemplates/byClientIdForm");
		//transaction
		registry.addViewController("/outgoingTransactions").setViewName("transactionTemplates/outgoing");
		registry.addViewController("/incomingTransactions").setViewName("transactionTemplates/incoming");
		registry.addViewController("/getAll").setViewName("transactionTemplates/getAll");
		registry.addViewController("/accNumber").setViewName("transactionTemplates/accNumber");
		//installment
		registry.addViewController("/myAll").setViewName("installmentTemplates/myAll");
		registry.addViewController("/next").setViewName("installmentTemplates/next");
		registry.addViewController("/given").setViewName("installmentTemplates/given");
		registry.addViewController("/loan").setViewName("installmentTemplates/loan");
		registry.addViewController("/all").setViewName("installmentTemplates/all");
	}
}