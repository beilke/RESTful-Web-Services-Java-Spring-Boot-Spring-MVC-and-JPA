package br.com.beilke.api.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.beilke.api.exceptions.UserServiceException;
import br.com.beilke.api.model.GeneralUser;
import br.com.beilke.api.model.PasswordResetToken;
import br.com.beilke.api.model.response.ErrorMessages;
import br.com.beilke.api.repositories.PasswordResetTokenRepository;
import br.com.beilke.api.repositories.UserRepository;
import br.com.beilke.api.security.SecurityConstants;
import br.com.beilke.api.service.MailService;
import br.com.beilke.api.service.UserService;
import br.com.beilke.api.shared.Utils;
import br.com.beilke.api.shared.dto.AddressDTO;
import br.com.beilke.api.shared.dto.UserDto;

@Service
public class UserServiceImpl implements UserService {
	@Autowired
	UserRepository userRepository;

	@Autowired
	PasswordResetTokenRepository passwordResetTokenRepository;

	@Autowired
	Utils utils;

	@Autowired
	MailService mailService;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Override
	public UserDto createUser(UserDto user) throws UnsupportedEncodingException, MessagingException {
		if (userRepository.findByEmail(user.getEmail()) != null)
			throw new UserServiceException(ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage());

		if (user.getAddresses() != null) {
			for (int i = 0; i < user.getAddresses().size(); i++) {
				AddressDTO address = user.getAddresses().get(i);
				address.setUserDetails(user);
				address.setAddressId(utils.generateRandomString(30));
				user.getAddresses().set(i, address);
			}
		}
		GeneralUser userEntity = new ModelMapper().map(user, GeneralUser.class);

		String publicUserId = utils.generateRandomString(30);
		userEntity.setUserId(publicUserId);
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		userEntity.setEmailVerificationToken(utils.generateRandomString(64));
		userEntity.setEmailVerificationStatus(false);

		GeneralUser storedUserDetails = userRepository.save(userEntity);

		mailService.send(userEntity, SecurityConstants.EMAIL_VERIFICATION_BODY,
				SecurityConstants.EMAIL_VERIFICATION_SUBJECT,
				SecurityConstants.EMAIL_VERIFICATION_URL + user.getEmailVerificationToken());


		return new ModelMapper().map(storedUserDetails, UserDto.class);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		GeneralUser userEntity = userRepository.findByEmail(username);

		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), new ArrayList<>());
	}

	@Override
	public UserDto getUser(String email) {
		GeneralUser userEntity = userRepository.findByEmail(email);

		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		return new ModelMapper().map(userEntity, UserDto.class);
	}

	@Override
	public UserDto getUserByUserId(String id) {
		GeneralUser userEntity = userRepository.findByUserId(id);

		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		return new ModelMapper().map(userEntity, UserDto.class);
	}

	@Override
	public UserDto updateUser(String id, UserDto user) {
		GeneralUser userEntity = userRepository.findByUserId(id);

		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		userEntity.setFirstName(user.getFirstName());
		userEntity.setLastName(user.getLastName());

		GeneralUser storedUserDetails = userRepository.save(userEntity);

		return new ModelMapper().map(storedUserDetails, UserDto.class);
	}

	@Override
	public void deleteUser(String id) {
		GeneralUser userEntity = userRepository.findByUserId(id);

		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		userRepository.delete(userEntity);
	}

	@Override
	public List<UserDto> getUsers(int page, int limit) {
		List<UserDto> returnValue = new ArrayList<>();

		Pageable pageableRequest = PageRequest.of(page, limit);

		Page<GeneralUser> usersPage = userRepository.findAll(pageableRequest);
		List<GeneralUser> users = usersPage.getContent();

		for (GeneralUser userEntity : users) {
			returnValue.add(new ModelMapper().map(userEntity, UserDto.class));
		}

		return returnValue;
	}

	@Override
	public boolean requestPasswordReset(String email) throws UnsupportedEncodingException, MessagingException {
		boolean returnValue = true;

		GeneralUser userEntity = userRepository.findByEmail(email);

		if (userEntity == null) {
			return false;
//			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
		}

		String token = utils.generatePasswordResetToken(userEntity.getUserId());

		PasswordResetToken passwordResetToken = new PasswordResetToken();
		passwordResetToken.setToken(token);
		passwordResetToken.setUserDetails(userEntity);
		passwordResetTokenRepository.save(passwordResetToken);

		mailService.send(userEntity, SecurityConstants.EMAIL_CONFIRMATION_BODY,
				SecurityConstants.EMAIL_CONFIRMATION_SUBJECT, SecurityConstants.EMAIL_CONFIRMATION_URL + token);


		return returnValue;
	}



	@Override
	public boolean verify(String verificationCode)
			throws UnsupportedEncodingException, MessagingException {
		boolean isVerified = false;
		GeneralUser user = userRepository.findByEmailVerificationToken(verificationCode);

		if (user != null && !user.getEmailVerificationStatus()) {
			user.setEmailVerificationToken(null);
			user.setEmailVerificationStatus(true);
			userRepository.save(user);
			mailService.send(user, SecurityConstants.EMAIL_CONFIRMATION_BODY,
					SecurityConstants.EMAIL_CONFIRMATION_SUBJECT, SecurityConstants.EMAIL_CONFIRMATION_URL);

			isVerified = true;
		}

		return isVerified;
	}

	@Override
	public boolean resetPassword(String token, String password) {
		boolean returnValue = false;

		if(Utils.hasTokenExpired(token))
			return returnValue;

		PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token);

		if(passwordResetToken == null) {
			return returnValue;
		}

		// Prepare de password
		String encodedPassword = bCryptPasswordEncoder.encode(password);

		// Update user password
		GeneralUser userEntity = passwordResetToken.getUserDetails();
		userEntity.setEncryptedPassword(encodedPassword);
		GeneralUser storedUserDetails = userRepository.save(userEntity);

		// Verify if password was saved
		if(storedUserDetails != null && storedUserDetails.getEncryptedPassword().equalsIgnoreCase(encodedPassword))
			returnValue = true;

		// Remove password reset token from database
		passwordResetTokenRepository.delete(passwordResetToken);

		return returnValue;
	}

}
