package br.com.beilke.app.ws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.beilke.app.ws.security.AppPropertiers;

@SpringBootApplication
public class MobileAppWsApplication
{

	public static void main(String[] args)
	{
		SpringApplication.run(MobileAppWsApplication.class, args);
	}

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder()
	{
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SpringApplicationContext springApplicationContext()
	{
		return new SpringApplicationContext();
	}

	@Bean(name = "AppProperties")
	public AppPropertiers getAppProperties()
	{
		return new AppPropertiers();
	}
}
