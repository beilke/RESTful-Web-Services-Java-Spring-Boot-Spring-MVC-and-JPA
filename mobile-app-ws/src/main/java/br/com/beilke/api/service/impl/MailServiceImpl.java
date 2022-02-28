package br.com.beilke.api.service.impl;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import br.com.beilke.api.model.GeneralUser;
import br.com.beilke.api.security.SecurityConstants;
import br.com.beilke.api.service.MailService;

@Service
@Profile("mail") // Only create this bean when the 'mail' profile is used
public class MailServiceImpl implements MailService {

	@Autowired
	private JavaMailSender mailSender;

	@Override
	public void send(GeneralUser user, String content, String subject, String verifyURL)
			throws MessagingException, UnsupportedEncodingException {
		String toAddress = user.getEmail();

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(SecurityConstants.EMAIL_FROMADDR, SecurityConstants.EMAIL_SENDER);
		helper.setTo(toAddress);
		helper.setSubject(subject);

		content = content.replace("[[name]]", user.getFullName());
		content = content.replace("[[URL]]", verifyURL);

		helper.setText(content, true);

		mailSender.send(message);

	}

}
