package br.com.beilke.api.controller;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.beilke.api.exceptions.UserServiceException;
import br.com.beilke.api.model.request.PasswordReset;
import br.com.beilke.api.model.request.UserDetailsRequest;
import br.com.beilke.api.model.response.AddressesRest;
import br.com.beilke.api.model.response.ErrorMessages;
import br.com.beilke.api.model.response.OperationStatus;
import br.com.beilke.api.model.response.RequestOperationStatus;
import br.com.beilke.api.model.response.UserRest;
import br.com.beilke.api.service.AddressService;
import br.com.beilke.api.service.UserService;
import br.com.beilke.api.shared.dto.AddressDTO;
import br.com.beilke.api.shared.dto.UserDto;

@RestController
@RequestMapping("users") // http://localhost:8080/users
public class UserController {
	@Autowired
	UserService userService;

	@Autowired
	AddressService addressService;

	@GetMapping(path = "/{id}", consumes = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_XML_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	public UserRest getUser(@PathVariable String id) {

		UserDto userDto = userService.getUserByUserId(id);

		return new ModelMapper().map(userDto, UserRest.class);
	}

	@PostMapping(consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public UserRest createUser(@RequestBody UserDetailsRequest userDetails, HttpServletRequest request)
			throws UnsupportedEncodingException, MessagingException {
		if (userDetails.getFirstName().isEmpty())
			throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

		UserDto userDto = new ModelMapper().map(userDetails, UserDto.class);
		UserDto createdUser = userService.createUser(userDto);

		return new ModelMapper().map(createdUser, UserRest.class);
	}

	@PutMapping(path = "/{id}", consumes = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_XML_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	public UserRest updateUser(@PathVariable String id, @RequestBody UserDetailsRequest userDetails) {

		if (userDetails.getFirstName().isEmpty())
			throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

		UserDto userDto = new ModelMapper().map(userDetails, UserDto.class);
		UserDto updatedUser = userService.updateUser(id, userDto);

		return new ModelMapper().map(updatedUser, UserRest.class);
	}

	@DeleteMapping(path = "/{id}", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public OperationStatus deleteUser(@PathVariable String id) {
		OperationStatus returnValue = new OperationStatus();
		returnValue.setOperationName(RequestOperationName.DELETE.name());

		userService.deleteUser(id);

		returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		return returnValue;
	}

	// /users/?page=0&limit=50
	@GetMapping(produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public List<UserRest> getUser(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "limit", defaultValue = "25") int limit) {
		List<UserRest> returnValue = new ArrayList<>();

		List<UserDto> users = userService.getUsers(page, limit);

		for (UserDto userDto : users) {
			returnValue.add(new ModelMapper().map(userDto, UserRest.class));
		}

		return returnValue;
	}

	@GetMapping(path = "/{id}/addresses", consumes = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_XML_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	public CollectionModel<AddressesRest> getUserAddresses(@PathVariable String id) {
		List<AddressesRest> returnValue = new ArrayList<>();

		List<AddressDTO> addressesDTO = addressService.getAddressByUserId(id);

		if (addressesDTO != null && !addressesDTO.isEmpty()) {
			Type listType = new TypeToken<List<AddressesRest>>() {
			}.getType();
			returnValue = new ModelMapper().map(addressesDTO, listType);

			for (AddressesRest addressRest : returnValue) {
				Link selfLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(id).slash("addresses")
						.slash(addressRest.getAddressId()).withSelfRel();
				addressRest.add(selfLink);
			}
		}

		Link userLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(id).withRel("user");
		Link selfLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(id).slash("addresses").withSelfRel();

		return CollectionModel.of(returnValue, userLink, selfLink);
	}

	@GetMapping(path = "/{userId}/addresses/{addressId}", consumes = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_XML_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	public EntityModel<AddressesRest> getUserAddress(@PathVariable String userId, @PathVariable String addressId) {
		AddressDTO addressDTO = addressService.getAddress(addressId);

		AddressesRest returnValue = new ModelMapper().map(addressDTO, AddressesRest.class);

		// http://localhost:8080/users/<userid>
		Link userLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(userId).withRel("user");

		Link userAddressesLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(userId).slash("addresses")
				.withRel("addresses");
		Link selfLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(userId).slash("addresses").slash(addressId)
				.withSelfRel();

		return EntityModel.of(returnValue, Arrays.asList(userLink, userAddressesLink, selfLink));
	}

	// http://localhost:8080/uranus/users/verify?code=?
	@GetMapping(path = "/verify")
	public OperationStatus verifyUser(@Param("code") String code)
			throws UnsupportedEncodingException, MessagingException {
		OperationStatus returnValue = new OperationStatus();
		returnValue.setOperationName(RequestOperationName.EMAIL_VERIFICATION.name());
		if (userService.verify(code)) {
			returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		} else {
			returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
		}
		return returnValue;
	}

	// http://localhost:8080/uranus/users/password-reset-request/email=?
	@GetMapping(path = "/password-reset-request")
	public OperationStatus requestReset(@Param("email") String email) throws UnsupportedEncodingException, MessagingException {
		OperationStatus returnValue = new OperationStatus();

		boolean operationResult = userService.requestPasswordReset(email);

		returnValue.setOperationName(RequestOperationName.REQUEST_PASSWORD_RESET.name());
		returnValue.setOperationResult(RequestOperationStatus.ERROR.name());

		if (operationResult)
			returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());

		return returnValue;
	}

	//http://localhost:8080/uranus/users/password-reset
	@CrossOrigin(origins = "http://localhost:8080")
	@PostMapping(path = "/password-reset", consumes = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE })
	public OperationStatus resetPassword(@RequestBody PasswordReset passwordReset) throws UnsupportedEncodingException, MessagingException {
		OperationStatus returnValue = new OperationStatus();

		boolean operationResult = userService.resetPassword(passwordReset.getToken(), passwordReset.getPassword());

		returnValue.setOperationName(RequestOperationName.PASSWORD_RESET.name());
		returnValue.setOperationResult(RequestOperationStatus.ERROR.name());

		if (operationResult)
			returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());

		return returnValue;
	}

}
