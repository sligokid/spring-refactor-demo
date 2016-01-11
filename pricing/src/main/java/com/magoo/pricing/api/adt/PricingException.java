package com.magoo.pricing.api.adt;

/**
 * A custom Runtime Exception thrown for pricing related exceptions.
 *
 * @author mcgowaki
 * @since 16 May 2014
 * @version $Id: PricingException.java 2995 2015-11-16 15:01:49Z kieran.mcgowan $
 */
public class PricingException extends RuntimeException {

	private static final long	serialVersionUID	= 6372120069723374284L;

	/**
	 * Instantiates a new pricing exception.
	 *
	 * @param cause the cause
	 */
	public PricingException(String cause) {
		super(cause);
	}

	/**
	 * Instantiates a new pricing exception.
	 *
	 * @param cause the cause
	 */
	public PricingException(Throwable cause) {
		super(cause);
	}
}
