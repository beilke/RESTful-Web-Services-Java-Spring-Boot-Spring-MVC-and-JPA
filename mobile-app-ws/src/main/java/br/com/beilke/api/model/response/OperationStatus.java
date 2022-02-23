package br.com.beilke.api.model.response;

public class OperationStatus
{
	private String operationResult;
	private String operationName;

	public String getOperationResult()
	{
		return operationResult;
	}

	public void setOperationResult(String operationResult)
	{
		this.operationResult = operationResult;
	}

	public String getOperationName()
	{
		return operationName;
	}

	public void setOperationName(String operationName)
	{
		this.operationName = operationName;
	}

}
