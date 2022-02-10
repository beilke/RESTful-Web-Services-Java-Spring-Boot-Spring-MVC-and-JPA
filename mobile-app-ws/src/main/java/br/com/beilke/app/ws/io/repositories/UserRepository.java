package br.com.beilke.app.ws.io.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import br.com.beilke.app.ws.io.entity.UserEntity;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long>
{
	UserEntity findUserByEmail(String email);

	UserEntity findUserByUserId(String id);

}
