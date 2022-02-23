package br.com.beilke.api.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import br.com.beilke.api.model.Address;
import br.com.beilke.api.model.GeneralUser;


@Repository
public interface AddressRepository extends CrudRepository<Address, Long> {
	List<Address> findAllByUserDetails(GeneralUser userEntity);

	Address findByAddressId(String addressId);
}
