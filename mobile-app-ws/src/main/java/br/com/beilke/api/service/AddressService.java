package br.com.beilke.api.service;

import java.util.List;

import br.com.beilke.api.shared.dto.AddressDTO;

public interface AddressService {

	List<AddressDTO> getAddressByUserId(String id);

	AddressDTO getAddress(String addressId);

}
