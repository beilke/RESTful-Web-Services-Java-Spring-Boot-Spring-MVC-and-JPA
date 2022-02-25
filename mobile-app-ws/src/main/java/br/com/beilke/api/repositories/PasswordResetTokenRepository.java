package br.com.beilke.api.repositories;

import org.springframework.data.repository.CrudRepository;

import br.com.beilke.api.model.PasswordResetToken;

public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken, Long> {

	PasswordResetToken findByToken(String token);


}
