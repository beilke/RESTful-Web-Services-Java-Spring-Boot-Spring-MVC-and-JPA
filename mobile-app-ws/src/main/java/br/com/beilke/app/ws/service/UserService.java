package br.com.beilke.app.ws.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import br.com.beilke.app.ws.shared.dto.UserDto;

public interface UserService extends UserDetailsService
{
	UserDto createUser(UserDto user);

	UserDto getUser(String email);

	UserDto getUserByUserId(String id);

	UserDto updateUser(String id, UserDto userDto);

	UserDto deleteUser(String id, UserDto userDto);

}