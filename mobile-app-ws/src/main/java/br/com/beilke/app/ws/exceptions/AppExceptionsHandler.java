package br.com.beilke.app.ws.exceptions;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import br.com.beilke.app.ws.ui.model.response.ErrorMessage;

@ControllerAdvice
public class AppExceptionsHandler
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AppExceptionsHandler.class);

	@ExceptionHandler(value = { UserServiceException.class })
	public ResponseEntity<Object> handleUserServiceException(UserServiceException ex, WebRequest request)
	{
		String message = ex.getMessage();

		LOGGER.error("Business Exception: "+ex.getMessage(), ex);

		ErrorMessage errorMessage = new ErrorMessage(new Date(), message, ex);

		return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(value = { Exception.class })
	public ResponseEntity<Object> handleOtherExceptions(Exception ex, WebRequest request)
	{
		String message = ex.getMessage();

		LOGGER.error("Application Exception: "+message, ex);

		ErrorMessage errorMessage = new ErrorMessage(new Date(), ex.getMessage(), ex);

		return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
