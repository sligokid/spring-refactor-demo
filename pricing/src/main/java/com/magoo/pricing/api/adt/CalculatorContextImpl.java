package com.magoo.pricing.api.adt;

import java.math.BigDecimal;
import java.util.Map;

import com.magoo.core.model.entity.CountryClusterVersion;
import com.magoo.core.model.entity.ProductLine;
import com.magoo.core.model.entity.Quote;

/**
 * A context implementation that encapsulates data structures required by a calculator.
 * 
 * @author mcgowaki
 * @version $Id: CalculatorContextImpl.java 3273 2016-01-08 11:16:44Z kieran.mcgowan $
 * @since 9 Apr 2014
 */
public class CalculatorContextImpl implements CalculatorContext {

	private final Quote							quote;
	private final CountryClusterVersion			countryClusterVersion;
	private final Map<ProductLine, BigDecimal>	aohByProductLine;

	public CalculatorContextImpl(Quote quote, CountryClusterVersion countryClusterVersion, Map<ProductLine, BigDecimal> aohByProductLine) {
		this.quote = quote;
		this.countryClusterVersion = countryClusterVersion;
		this.aohByProductLine = aohByProductLine;
	}

	@Override
	public Quote getQuote() {
		return quote;
	}

	@Override
	public CountryClusterVersion getCountryClusterVersion() {
		return countryClusterVersion;
	}

	@Override
	public BigDecimal getAohByProductLine(ProductLine productLine) {
		return aohByProductLine.get(productLine);
	}

}
