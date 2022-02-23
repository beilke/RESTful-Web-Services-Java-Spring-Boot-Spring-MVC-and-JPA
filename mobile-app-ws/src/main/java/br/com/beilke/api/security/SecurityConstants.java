package br.com.beilke.api.security;

import br.com.beilke.api.SpringApplicationContext;

public class SecurityConstants
{
	public static final long EXPIRATION_TIME = 864000000; // 10 days
	public static final String SIGN_UP_URL = "/users";
	public static final String LOGIN_URL = "/users/login";
	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String HEADER_STRING = "Authorization";
	// public static final String TOKEN_SECRET = "34hj3k29skkx";
	public static final String[] URLS_TO_ALLOW_WITHOUT_AUTH = { "/v2/api-docs", "/configuration/**", "/swagger-ui.html",
			"/swagger*/**", "/webjars/**", "/h2-console/**", "/actuator/**", "/users/login/**" };

	private SecurityConstants()
	{
	} // Prevents instantiation

	public static String getTokenSecret()
	{
		AppPropertiers appProperties = (AppPropertiers) SpringApplicationContext.getBean("AppProperties");
		return appProperties.getTokenSecret();
	}

}
