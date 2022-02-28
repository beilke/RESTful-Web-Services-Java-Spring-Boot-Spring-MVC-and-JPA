package br.com.beilke.api.service;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.security.core.userdetails.UserDetailsService;

import br.com.beilke.api.shared.dto.UserDto;

public interface UserService extends UserDetailsService
{
	UserDto createUser(UserDto user) throws UnsupportedEncodingException, MessagingException;

	UserDto getUser(String email);

	UserDto getUserByUserId(String id);

	UserDto updateUser(String id, UserDto userDto);

	void deleteUser(String id);

	List<UserDto> getUsers(int page, int limit);

	boolean requestPasswordReset(String email) throws UnsupportedEncodingException, MessagingException;

	boolean verify(String verificationCode) throws UnsupportedEncodingException, MessagingException;

	boolean resetPassword(String token, String password);
}
