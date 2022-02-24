package br.com.beilke.api.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import br.com.beilke.api.model.GeneralUser;

@Repository
public interface UserRepository extends PagingAndSortingRepository<GeneralUser, Long>
{
	GeneralUser findByEmail(String email);

	GeneralUser findByUserId(String id);
	
	@Query("SELECT u FROM users u WHERE u.emailVerificationToken = ?1")
	GeneralUser findByEmailVerificationToken(String token);

}
