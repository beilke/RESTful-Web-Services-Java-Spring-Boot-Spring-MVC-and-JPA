package br.com.beilke.api.security;

import br.com.beilke.api.SpringApplicationContext;

public class SecurityConstants
{
	public static final long EXPIRATION_TIME = 864000000; // 10 days
	public static final long PASSWORD_RESET_EXPIRATION_TIME = 3600000; // 1 hour
	public static final String LOGIN_URL = "/users/login";
	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String HEADER_STRING = "Authorization";
	
	public static final String[] URLS_TO_ALLOW_WITHOUT_AUTH = { "/v2/api-docs", "/configuration/**", "/swagger-ui.html",
			"/swagger*/**", "/webjars/**", "/h2-console/**", "/actuator/**", "/users/login/**", "/users/password-reset-request", "/users/verify/**" };

	private SecurityConstants()
	{
	} // Prevents instantiation

	public static String getTokenSecret()
	{
		AppProperties appProperties = (AppProperties) SpringApplicationContext.getBean("AppProperties");
		return appProperties.getTokenSecret();
	}

}
