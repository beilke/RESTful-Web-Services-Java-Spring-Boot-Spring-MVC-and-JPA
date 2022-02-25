package br.com.beilke.api.security;

import br.com.beilke.api.SpringApplicationContext;

public class SecurityConstants
{
	public static final long EXPIRATION_TIME = 864000000; // 10 days
	public static final long PASSWORD_RESET_EXPIRATION_TIME = 3600000; // 1 hour
	public static final String LOGIN_URL = "/users/login";
	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String HEADER_STRING = "Authorization";

	public static final String EMAIL_FROMADDR = "fernando.beilke@live.com";
	public static final String EMAIL_SENDER = "FBB";

	public static final String SERVER_HOST_CONTEXT = "http://localhost:8088/uranus";

	public static final String EMAIL_CONFIRMATION_URL = SERVER_HOST_CONTEXT + LOGIN_URL;
	public static final String EMAIL_CONFIRMATION_BODY = "Dear [[name]],<br>"
			+ "Congratulations, your account has been verified.<br>"
			+ "Please click the link below to login:<br>"
			+ "<h3><a href=\"[[URL]]\" target=\"_self\">LOGIN</a></h3>" + "Thank you,<br>" + EMAIL_SENDER;
	public static final String EMAIL_CONFIRMATION_SUBJECT = "Confirmation of your registration";

	public static final String EMAIL_VERIFICATION_URL = SERVER_HOST_CONTEXT + "/users/verify?code=";
	public static final String EMAIL_VERIFICATION_BODY = "Dear [[name]],<br>"
			+ "Please click the link below to verify your registration:<br>"
			+ "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>" + "Thank you,<br>" + EMAIL_SENDER;
	public static final String EMAIL_VERIFICATION_SUBJECT = "Please verify your registration";

	public static final String EMAIL_PASSWORD_RESET_URL = "http://localhost:8080/userService/password-reset.html?token=";
	public static final String EMAIL_PASSWORD_RESET_BODY  = "Dear [[name]],<br>"
			+ "Someone has requested to reset your password within our project.<br>"
			+ "If it were not you, please ignore it, otherwise please click on the link below to set a new password:<br>"
			+ "<h3><a href=\"[[URL]]\" target=\"_self\">Click this link to Reset Password</a></h3>" + "Thank you,<br>" + EMAIL_SENDER;
	public static final String EMAIL_PASSWORD_RESET_SUBJECT  = "Password reset request";



	public static final String[] URLS_TO_ALLOW_WITHOUT_AUTH = { "/v2/api-docs", "/configuration/**", "/swagger-ui.html",
			"/swagger*/**", "/webjars/**", "/h2-console/**", "/actuator/**", "/users/login/**", "/users/password-reset-request",
			"/users/verify/**", "/users/password-reset-request/**", "/users/password-reset/**" };

	private SecurityConstants()
	{
	} // Prevents instantiation

	public static String getTokenSecret()
	{
		AppProperties appProperties = (AppProperties) SpringApplicationContext.getBean("AppProperties");
		return appProperties.getTokenSecret();
	}

}
