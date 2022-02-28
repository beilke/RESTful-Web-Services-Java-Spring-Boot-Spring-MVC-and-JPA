package br.com.beilke.api.service;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;

import br.com.beilke.api.model.GeneralUser;

public interface MailService {

	void send(GeneralUser user, String content, String subject, String verifyURL)
			throws MessagingException, UnsupportedEncodingException;

}
