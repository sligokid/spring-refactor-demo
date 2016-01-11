package com.magoo.pricing.component;

import java.util.List;

import com.magoo.core.model.entity.QuoteBaseTotalComponent;
import com.magoo.pricing.api.adt.CalculatorContext;

/**
 * Quote total component pricing strategy.
 * 
 * @author mcgowaki
 * @since 16 May 2014
 * @version $Id: TotalComponentCalculator.java 2995 2015-11-16 15:01:49Z kieran.mcgowan $ 
 */
public interface TotalComponentCalculator {

	/**
	 * Gets the _new_ total components created by the implementation.
	 * 
	 */
	List<QuoteBaseTotalComponent> getNewTotalComponents(CalculatorContext calculatorContext);

}
