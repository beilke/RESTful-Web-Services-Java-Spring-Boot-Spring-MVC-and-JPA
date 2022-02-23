package br.com.beilke.api.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.beilke.api.model.Address;
import br.com.beilke.api.model.GeneralUser;
import br.com.beilke.api.repositories.AddressRepository;
import br.com.beilke.api.repositories.UserRepository;
import br.com.beilke.api.service.AddressService;
import br.com.beilke.api.shared.dto.AddressDTO;

@Service
public class AddressServiceImpl implements AddressService{

	@Autowired
	UserRepository userRepository;

	@Autowired
	AddressRepository addressRepository;

	@Override
	public List<AddressDTO> getAddressByUserId(String id) {
		List<AddressDTO> returnValue = new ArrayList<>();

		ModelMapper modelMapper = new ModelMapper();

		GeneralUser userEntity = userRepository.findUserByUserId(id);

		if (userEntity == null)
			return returnValue;

		Iterable<Address> addresses = addressRepository.findAllByUserDetails(userEntity);

		for (Address address : addresses)
		{
			returnValue.add(modelMapper.map(address, AddressDTO.class));
		}

		return returnValue;
	}

	@Override
	public AddressDTO getAddress(String addressId) {
		AddressDTO returnValue = null;
		Address address  = addressRepository.findByAddressId(addressId);

		if (address != null)
			returnValue = new ModelMapper().map(address, AddressDTO.class);

		return returnValue;

	}

}
