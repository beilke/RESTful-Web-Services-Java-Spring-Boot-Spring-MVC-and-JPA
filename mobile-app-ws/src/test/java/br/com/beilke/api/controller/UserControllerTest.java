package br.com.beilke.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.com.beilke.api.model.response.UserRest;
import br.com.beilke.api.service.impl.UserServiceImpl;
import br.com.beilke.api.shared.dto.AddressDTO;
import br.com.beilke.api.shared.dto.UserDto;

class UserControllerTest {

	@InjectMocks
	UserController userController;

	@Mock
	UserServiceImpl userService;

	UserDto userDto;

	final String USER_ID = "hgh346hch&%uds";

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);

		userDto = new UserDto();
		userDto.setFirstName("George");
		userDto.setLastName("Bigs");
		userDto.setPassword("1234abcde");
		userDto.setEmail("test@email.com");
		userDto.setEmailVerificationStatus(Boolean.FALSE);
		userDto.setEmailVerificationToken(null);
		userDto.setUserId(USER_ID);
		userDto.setAddresses(getAddressesDto());
		userDto.setEncryptedPassword("dgfggd432");
	}

	@Test
	final void testGetUser() {
		when(userService.getUserByUserId(anyString())).thenReturn(userDto);

		UserRest userRest = userController.getUser(USER_ID);

		assertNotNull(userRest);
		assertEquals(USER_ID, userRest.getUserId());
		assertEquals(userDto.getFirstName(), userRest.getFirstName());
		assertEquals(userDto.getLastName(), userRest.getLastName());
		assertTrue(userDto.getAddresses().size() == userRest.getAddresses().size());
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

}
