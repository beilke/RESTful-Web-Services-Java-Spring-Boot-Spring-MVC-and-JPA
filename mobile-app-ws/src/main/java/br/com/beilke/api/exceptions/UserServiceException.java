package br.com.beilke.api.exceptions;

public class UserServiceException extends RuntimeException
{
	private static final long serialVersionUID = -7330730869672004324L;

	public UserServiceException(String message)
	{
		super(message);
	}

}
