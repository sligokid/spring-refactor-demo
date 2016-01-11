package com.magoo.pricing.api;

import java.util.List;

import com.magoo.core.model.entity.Quote;
import com.magoo.core.model.entity.QuoteAssignmentComponent;
import com.magoo.core.model.entity.QuoteBaseTotalComponent;
import com.magoo.core.model.entity.QuoteTotal;

/**
 * A pricing service to calculate pricing.
 * 
 * @author mcgowaki
 * @version $Id: PricingService.java 3273 2016-01-08 11:16:44Z kieran.mcgowan $
 * @since 9 Apr 2014
 */
public interface PricingService {

	/**
	 * Recalculate the pricing components gross and net totals on the {@link QuoteBaseTotalComponent}s and the {@link QuoteTotal}.
	 * 
	 * Process all individual calculators creating the required low level total components for each calculator. 
	 * Replace the existing {@link QuoteBaseTotalComponent}s before calculating the new summarized gross and net prices and attaching to the quote. 
	 * 
	 * Builds only the components, not the trees.
	 */
	void recalculateTotals(Quote quote);

	/**
	 * Recalculate the existing pricing components net totals on the {@link QuoteBaseTotalComponent}s and the {@link QuoteTotal}.
	 * 
	 * This is required on the Pricing screen to allow the discount to adjust net prices, without a full recalculation of all gross prices.
	 * 
	 * Calculates all the pricing fields net totals only, not the trees.
	 */
	void recalculateNetPrices(Quote quote);

	/**
	 * Recalculate the add-on price breakdown components & totals.
	 */
	void recalculateAssignmentTotal(Quote quote, List<QuoteAssignmentComponent> allAssignments);
}
