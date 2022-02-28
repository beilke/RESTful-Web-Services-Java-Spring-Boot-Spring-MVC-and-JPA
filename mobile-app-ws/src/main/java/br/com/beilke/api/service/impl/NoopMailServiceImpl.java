package br.com.beilke.api.service.impl;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import br.com.beilke.api.model.GeneralUser;
import br.com.beilke.api.service.MailService;

@Service
@Profile("!mail") // By adding the exclamation mark, this implementation will be used when the
					// mail profile isn't active
public class NoopMailServiceImpl implements MailService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void send(GeneralUser user, String content, String subject, String verifyURL)
			throws MessagingException, UnsupportedEncodingException {
		logger.debug("Dummy implementation, no e-mail is being sent");

	}

}
