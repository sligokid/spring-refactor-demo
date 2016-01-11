package com.magoo.pricing.api.adt;

import java.math.BigDecimal;

import com.magoo.core.model.entity.CountryClusterVersion;
import com.magoo.core.model.entity.ProductLine;
import com.magoo.core.model.entity.Quote;

/**
 * A context that encapsulates data structures required by a calculator.
 * 
 * @author mcgowaki
 * @version $Id: CalculatorContext.java 3273 2016-01-08 11:16:44Z kieran.mcgowan $
 * @since 9 Apr 2014
 */
public interface CalculatorContext {

	Quote getQuote();

	CountryClusterVersion getCountryClusterVersion();

	BigDecimal getAohByProductLine(ProductLine productLine);

}