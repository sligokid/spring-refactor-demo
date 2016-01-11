package com.magoo.total;

import com.magoo.core.model.entity.Quote;

/**
 * Quote Total calculation strategy.
 * 
 */
public interface TotalCalculator {

	void summarize(Quote quote);

}