package com.magoo.pricing;

import java.math.BigDecimal;
import java.util.Date;

import com.magoo.core.model.entity.Quote;
import com.magoo.core.model.entity.QuoteBaseTotalComponent;
import com.magoo.core.model.enums.ChargeGroup;
import com.magoo.core.util.Constants;
import com.magoo.core.util.DateDurationUtils;
import com.magoo.core.util.NumberUtils;

/**
 * Base methods used in calculators.
 * 
 * @author mcgowaki
 * @version $Id: BaseCalculator.java 3273 2016-01-08 11:16:44Z kieran.mcgowan $
 * @since 9 Apr 2014 
 */
public abstract class BaseCalculator {

	/**
	 * Calculate  units from the price.
	 * 
	 * The formula is {@code sap units = gross price / sap unit price}
	 */
	public BigDecimal calculateUnitsFromPrice(BigDecimal grossPrice, ChargeGroup chargeGroup, BigDecimal sapUnitPrice) {
		if (ChargeGroup.CREDIT.equals(chargeGroup)) {
			return BigDecimal.ZERO;
		}
		return NumberUtils.divide(grossPrice, sapUnitPrice);
	}

	protected void applyYearFactor(QuoteBaseTotalComponent component, BigDecimal yearRatio) {
		BigDecimal deliveryUnits = NumberUtils.multiply(true, component.getDeliveryUnits(), yearRatio);
		BigDecimal sapUnits = NumberUtils.multiply(true, component.getSapUnits(), yearRatio);
		BigDecimal cost = NumberUtils.multiply(component.getCost(), yearRatio);
		BigDecimal grossPrice = NumberUtils.multiply(component.getGrossPrice(), yearRatio);
		BigDecimal netPrice = grossPrice;
		setComponentValues(component, deliveryUnits, sapUnits, cost, grossPrice, netPrice);
	}

	public void setComponentValues(QuoteBaseTotalComponent component, BigDecimal deliveryUnits, BigDecimal sapUnits, BigDecimal cost,
			BigDecimal grossPrice, BigDecimal netPrice) {
		component.setDeliveryUnits(NumberUtils.setScale(true, deliveryUnits));
		component.setSapUnits(NumberUtils.setScale(true, sapUnits));
		component.setCost(NumberUtils.setScale(cost));
		component.setGrossPrice(NumberUtils.setScale(grossPrice));
		component.setNetPrice(NumberUtils.setScale(netPrice));
	}

	/**
	 * The number of months a included for a given year of the contract.
	 */
	public int getMonthsIncludedForYear(Quote quote, Integer year, Date itemStartDate, Date itemEndDate) {
		Date yearStart = quote.getYearStartDateByType(year);
		Date yearEnd = DateDurationUtils.addMonthsPreviousDay(yearStart, Constants.MONTHS_PER_YEAR);
		yearStart = (quote.isTrueUp()) ? DateDurationUtils.getLatest(yearStart, quote.getStartDate()) : yearStart;
		return DateDurationUtils.rangesOverlapInMonths(yearStart, yearEnd, itemStartDate, itemEndDate);
	}

}