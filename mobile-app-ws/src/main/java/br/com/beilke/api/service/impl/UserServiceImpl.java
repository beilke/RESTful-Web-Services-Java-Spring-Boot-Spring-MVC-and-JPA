package br.com.beilke.api.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.beilke.api.exceptions.UserServiceException;
import br.com.beilke.api.model.GeneralUser;
import br.com.beilke.api.model.response.ErrorMessages;
import br.com.beilke.api.repositories.UserRepository;
import br.com.beilke.api.service.UserService;
import br.com.beilke.api.shared.Utils;
import br.com.beilke.api.shared.dto.AddressDTO;
import br.com.beilke.api.shared.dto.UserDto;

@Service
public class UserServiceImpl implements UserService
{
	@Autowired
	UserRepository userRepository;

	@Autowired
	Utils utils;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Override
	public UserDto createUser(UserDto user)
	{
		if (userRepository.findUserByEmail(user.getEmail()) != null)
			throw new UserServiceException(ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage());

		for(int i=0;i<user.getAddresses().size();i++)
		{
			AddressDTO address = user.getAddresses().get(i);
			address.setUserDetails(user);
			address.setAddressId(utils.generateRandomString(30));
			user.getAddresses().set(i, address);
		}

		GeneralUser userEntity = new ModelMapper().map(user, GeneralUser.class);

		String publicUserId = utils.generateRandomString(30);
		userEntity.setUserId(publicUserId);
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));

		GeneralUser storedUserDetails = userRepository.save(userEntity);

		return new ModelMapper().map(storedUserDetails, UserDto.class);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
	{
		GeneralUser userEntity = userRepository.findUserByEmail(username);

		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), new ArrayList<>());
	}

	@Override
	public UserDto getUser(String email)
	{
		GeneralUser userEntity = userRepository.findUserByEmail(email);

		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		return new ModelMapper().map(userEntity, UserDto.class);
	}

	@Override
	public UserDto getUserByUserId(String id)
	{
		GeneralUser userEntity = userRepository.findUserByUserId(id);

		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		return new ModelMapper().map(userEntity, UserDto.class);
	}

	@Override
	public UserDto updateUser(String id, UserDto user)
	{
		GeneralUser userEntity = userRepository.findUserByUserId(id);

		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		userEntity.setFirstName(user.getFirstName());
		userEntity.setLastName(user.getLastName());

		GeneralUser storedUserDetails = userRepository.save(userEntity);

		return new ModelMapper().map(storedUserDetails, UserDto.class);
	}

	@Override
	public void deleteUser(String id)
	{
		GeneralUser userEntity = userRepository.findUserByUserId(id);

		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		userRepository.delete(userEntity);
	}

	@Override
	public List<UserDto> getUsers(int page, int limit)
	{
		List<UserDto> returnValue = new ArrayList<>();

		Pageable pageableRequest = PageRequest.of(page, limit);

		Page<GeneralUser> usersPage = userRepository.findAll(pageableRequest);
		List<GeneralUser> users = usersPage.getContent();

		for (GeneralUser userEntity : users)
		{
			returnValue.add(new ModelMapper().map(userEntity, UserDto.class));
		}

		return returnValue;
	}

}
