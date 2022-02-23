package br.com.beilke.api.controller;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
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
		ModelMapper modelMapper = new ModelMapper();

		UserDto userDto = userService.getUserByUserId(id);

		return modelMapper.map(userDto, UserRest.class);
	}

	@PostMapping(consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public UserRest createUser(@RequestBody UserDetailsRequest userDetails) {
		if (userDetails.getFirstName().isEmpty())
			throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

		ModelMapper modelMapper = new ModelMapper();

		UserDto userDto = modelMapper.map(userDetails, UserDto.class);
		UserDto createdUser = userService.createUser(userDto);

		return modelMapper.map(createdUser, UserRest.class);
	}

	@PutMapping(path = "/{id}", consumes = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_XML_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	public UserRest updateUser(@PathVariable String id, @RequestBody UserDetailsRequest userDetails) {
		UserRest returnValue = new UserRest();

		if (userDetails.getFirstName().isEmpty())
			throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

		UserDto userDto = new UserDto();
		BeanUtils.copyProperties(userDetails, userDto);

		UserDto updatedUser = userService.updateUser(id, userDto);
		BeanUtils.copyProperties(updatedUser, returnValue);

		return returnValue;
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
			UserRest userModel = new UserRest();
			BeanUtils.copyProperties(userDto, userModel);
			returnValue.add(userModel);
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
				Link selfLink = WebMvcLinkBuilder
						.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
						.getUserAddress(id, addressRest.getAddressId()))
						.withSelfRel();
				addressRest.add(selfLink);
			}
		}

		Link userLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(id).withRel("user");
		Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getUserAddresses(id))
				.withSelfRel();
		return CollectionModel.of(returnValue, userLink, selfLink);
	}

	@GetMapping(path = "/{userId}/addresses/{addressId}", consumes = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_XML_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	public EntityModel<AddressesRest> getUserAddress(@PathVariable String userId, @PathVariable String addressId) {
		AddressDTO addressDTO = addressService.getAddress(addressId);

		ModelMapper modelMapper = new ModelMapper();
		AddressesRest returnValue = modelMapper.map(addressDTO, AddressesRest.class);

		// http://localhost:8080/users/<userid>
		Link userLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(userId).withRel("user");
		Link userAddressesLink = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getUserAddresses(userId)).withRel("addresses");
		Link selfLink = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getUserAddress(userId, addressId))
				.withSelfRel();

		return EntityModel.of(returnValue, Arrays.asList(userLink, userAddressesLink, selfLink));
	}

}
