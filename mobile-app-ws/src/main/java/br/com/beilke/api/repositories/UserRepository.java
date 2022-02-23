package br.com.beilke.api.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import br.com.beilke.api.model.GeneralUser;

@Repository
public interface UserRepository extends PagingAndSortingRepository<GeneralUser, Long>
{
	GeneralUser findUserByEmail(String email);

	GeneralUser findUserByUserId(String id);

}
