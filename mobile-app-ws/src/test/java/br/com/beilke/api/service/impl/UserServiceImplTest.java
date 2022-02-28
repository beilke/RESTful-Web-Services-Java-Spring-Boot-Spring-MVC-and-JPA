package br.com.beilke.api.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.beilke.api.exceptions.UserServiceException;
import br.com.beilke.api.model.GeneralUser;
import br.com.beilke.api.repositories.UserRepository;
import br.com.beilke.api.service.MailService;
import br.com.beilke.api.shared.Utils;
import br.com.beilke.api.shared.dto.AddressDTO;
import br.com.beilke.api.shared.dto.UserDto;

class UserServiceImplTest {

	@InjectMocks
	UserServiceImpl userService;

	@Mock
	UserRepository userRepository;

	@Mock
	Utils utils;

	@Mock
	MailService mailService;

	GeneralUser userEntity;

	@Mock
	BCryptPasswordEncoder bCryptPasswordEncoder;

	String userId = "Wolfgang";
	String encryptedPassword = "1241sagt";

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);

		userEntity = new GeneralUser();
		userEntity.setId(1L);
		userEntity.setFirstName("Wolfgang");
		userEntity.setUserId(userId);
		userEntity.setEncryptedPassword(encryptedPassword);
		userEntity.setEmail("howard@hotmail.com");
		userEntity.setEmailVerificationToken("fsjdhfjkh");
	}

	@Test
	final void testGetUser() {

		when(userRepository.findByEmail(anyString())).thenReturn(userEntity);

		UserDto userDto = userService.getUser("test@test.com");

		assertNotNull(userDto);
		assertEquals("Wolfgang", userDto.getFirstName());

	}

	@Test
	final void testGetUser_UsernameNotFoundException() {
		when(userRepository.findByEmail(anyString())).thenReturn(null);

		assertThrows(UserServiceException.class,
				()-> {
					userService.getUser("test@test.com");
				}
				);

	}

	@Test
	final void testCreateUser() throws UnsupportedEncodingException, MessagingException {
		when(userRepository.findByEmail(anyString())).thenReturn(null);
		when(utils.generateRandomString(anyInt())).thenReturn("dksjkldjskld");
		when(bCryptPasswordEncoder.encode(anyString())).thenReturn(encryptedPassword);
		when(userRepository.save(ArgumentMatchers.any(GeneralUser.class))).thenReturn(userEntity);

		AddressDTO addressDto = new AddressDTO();
		addressDto.setType("shipping");

		List<AddressDTO> addresses = new ArrayList<>();
		addresses.add(addressDto);

		UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);
		userDto.setAddresses(addresses);

		UserDto storedUserDetails =	userService.createUser(userDto);
		assertNotNull(storedUserDetails);
		assertEquals(userEntity.getFirstName(), storedUserDetails.getFirstName());
	}

}
