package com.payment.exception;

public class InvalidPaymentException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidPaymentException(String msg){
		super(msg);
	}
}
