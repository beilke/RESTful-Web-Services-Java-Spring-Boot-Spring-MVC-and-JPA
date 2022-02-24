package br.com.beilke.api.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private JavaMailSender mailSender;

	@Override
	public UserDto createUser(UserDto user, String siteURL) throws UnsupportedEncodingException, MessagingException {
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

		sendVerificationEmail(userEntity, siteURL);

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
	public boolean requestPasswordReset(String email) {
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

		// TODO logic to send email

		return returnValue;
	}

	@Override
	public void sendVerificationEmail(GeneralUser user, String siteURL)
			throws UnsupportedEncodingException, MessagingException {
		String toAddress = user.getEmail();
		String fromAddress = "fernando.beilke@live.com";
		String senderName = "FBB";
		
		String subject = "Please verify your registration";
		
		String content = "Dear [[name]],<br>" 
				+ "Please click the link below to verify your registration:<br>"
				+ "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>" + "Thank you,<br>" + "FBB.";

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);

		content = content.replace("[[name]]", user.getFullName());
		String verifyURL = siteURL + "/users/verify?code=" + user.getEmailVerificationToken();

		content = content.replace("[[URL]]", verifyURL);

		helper.setText(content, true);

		mailSender.send(message);

	}

	@Override
	public void sendConfirmationEmail(GeneralUser user, String siteURL, boolean isVerified)
			throws UnsupportedEncodingException, MessagingException {
		String toAddress = user.getEmail();
		String fromAddress = "fernando.beilke@live.com";
		String senderName = "FBB";

		String subject = "Confirmation of your registration";

		String content = "Dear [[name]],<br>" 
				+ "Congratulations, your account has been verified.<br>"
				+ "Please click the link below to login:<br>"
				+ "<h3><a href=\"[[URL]]\" target=\"_self\">LOGIN</a></h3>" + "Thank you,<br>" + "FBB.";
		
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);
		
		content = content.replace("[[name]]", user.getFullName());
		String verifyURL = siteURL + "/users/login";

		content = content.replace("[[URL]]", verifyURL);
		
		helper.setText(content, true);

		mailSender.send(message);

	}

	@Override
	public boolean verify(String verificationCode, String siteURL)
			throws UnsupportedEncodingException, MessagingException {
		boolean isVerified = false;
		GeneralUser user = userRepository.findByEmailVerificationToken(verificationCode);

		if (user != null && !user.getEmailVerificationStatus()) {
			user.setEmailVerificationToken(null);
			user.setEmailVerificationStatus(true);
			userRepository.save(user);
			sendConfirmationEmail(user, siteURL, isVerified);
			isVerified = true;
		}

		return isVerified;
	}

}
