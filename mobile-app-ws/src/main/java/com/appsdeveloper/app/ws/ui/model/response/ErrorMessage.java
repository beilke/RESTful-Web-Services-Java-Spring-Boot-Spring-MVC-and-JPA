package com.appsdeveloper.app.ws.ui.model.response;

import java.util.Date;

public class ErrorMessage
{
	private Date timestamp;
	private String message;
	private Exception exception;

	public ErrorMessage()
	{
	}

	public ErrorMessage(Date timestamp, String message)
	{
		this.timestamp = timestamp;
		this.message = message;
	}
	
	public ErrorMessage(Date timestamp, String message, Exception exception)
	{
		this.timestamp = timestamp;
		this.message = message;
		this.exception = exception;
	}

	public Date getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(Date timestamp)
	{
		this.timestamp = timestamp;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public Exception getException()
	{
		return exception;
	}

	public void setException(Exception exception)
	{
		this.exception = exception;
	}
	
	

}
