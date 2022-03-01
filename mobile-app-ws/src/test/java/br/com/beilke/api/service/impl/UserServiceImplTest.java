package br.com.beilke.api.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.beilke.api.exceptions.UserServiceException;
import br.com.beilke.api.model.Address;
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

	@Mock
	BCryptPasswordEncoder bCryptPasswordEncoder;

	GeneralUser userEntity;

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);

		userEntity = new GeneralUser();
		userEntity.setId(1L);
		userEntity.setFirstName("Wolfgang");
		userEntity.setLastName("Mozart");
		userEntity.setUserId("wmozart");
		userEntity.setEncryptedPassword("1241sagt");
		userEntity.setEmail("wolfgang@hotmail.com");
		userEntity.setEmailVerificationToken("fsjdhfjkh");
		userEntity.setAddresses(getAddressesEntity());
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
	final void testCreateUser_UsernameNotFoundException() {
		when(userRepository.findByEmail(anyString())).thenReturn(userEntity);

		UserDto userDto = new UserDto();
		userDto.setAddresses(getAddressesDto());
		userDto.setFirstName("George");
		userDto.setLastName("Bigs");
		userDto.setPassword("1234abcde");
		userDto.setEmail("test@email.com");

		assertThrows(UserServiceException.class,
				()-> {
					userService.createUser(userDto);
				}
				);

	}

	@Test
	final void testCreateUser() throws UnsupportedEncodingException, MessagingException {
		when(userRepository.findByEmail(anyString())).thenReturn(null);
		when(utils.generateRandomString(anyInt())).thenReturn("dksjkldjskld");
		when(bCryptPasswordEncoder.encode(anyString())).thenReturn("1241sagt");
		when(userRepository.save(ArgumentMatchers.any(GeneralUser.class))).thenReturn(userEntity);
		Mockito.doNothing().when(mailService).send(userEntity, null, null, null);

		UserDto userDto = new UserDto();
		userDto.setAddresses(getAddressesDto());
		userDto.setFirstName("George");
		userDto.setLastName("Bigs");
		userDto.setPassword("1234abcde");
		userDto.setEmail("test@email.com");
		UserDto storedUserDetails =	userService.createUser(userDto);

		assertNotNull(storedUserDetails);
		assertEquals(userEntity.getFirstName(), storedUserDetails.getFirstName());
		assertEquals(userEntity.getLastName(), storedUserDetails.getLastName());
		assertNotNull(storedUserDetails.getUserId());
		assertEquals(storedUserDetails.getAddresses().size(), userEntity.getAddresses().size());
		verify(utils,times(3)).generateRandomString(30);
		verify(bCryptPasswordEncoder, times(1)).encode("1234abcde");
		verify(userRepository, times(1)).save(ArgumentMatchers.any(GeneralUser.class));
	}

	private List<AddressDTO> getAddressesDto() {
		AddressDTO shippingAddressDto = new AddressDTO();
		shippingAddressDto.setType("shipping");
		shippingAddressDto.setCity("Mannheim");
		shippingAddressDto.setCountry("Deutschland");
		shippingAddressDto.setPostalCode("68169");
		shippingAddressDto.setStreetName("Straßename 123");

		AddressDTO billingAddressDto = new AddressDTO();
		billingAddressDto.setType("billing");
		billingAddressDto.setCity("Heidelberg");
		billingAddressDto.setCountry("Deutschland");
		billingAddressDto.setPostalCode("68123");
		billingAddressDto.setStreetName("Straßename 456");


		List<AddressDTO> addresses = new ArrayList<>();
		addresses.add(shippingAddressDto);
		addresses.add(billingAddressDto);
		return addresses;
	}

	private List<Address> getAddressesEntity() {
		List<AddressDTO> addresses = getAddressesDto();

		Type listType = new TypeToken<List<Address>>() {}.getType();
		return new ModelMapper().map(addresses, listType);
	}

}
