package com.magoo.pricing.api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.magoo.core.model.entity.CountryClusterVersion;
import com.magoo.core.model.entity.ProductLine;
import com.magoo.core.model.entity.Quote;
import com.magoo.core.model.entity.QuoteAssignmentComponent;
import com.magoo.core.model.entity.QuoteBaseTotalComponent;
import com.magoo.core.model.entity.QuoteTotal;
import com.magoo.core.service.CountryClusterVersionService;
import com.magoo.core.service.RegionRateService;
import com.magoo.pricing.api.adt.CalculatorContext;
import com.magoo.pricing.api.adt.CalculatorContextImpl;
import com.magoo.pricing.component.TotalComponentCalculator;
import com.magoo.pricing.total.assignment.AssignmentCalculator;
import com.magoo.total.TotalCalculator;

/**
 * A pricing service implementation that calculates quote pricing components and subsequent summaries.
 * 
 * Each TotalComponentCalculator returns a list of _new_ quote total components.
 * 
 * Each TotalCalculator summarizes high level quote totals.
 * 
 * @author mcgowaki
 * @version $Id: PricingServiceImpl.java 3273 2016-01-08 11:16:44Z kieran.mcgowan $
 * @since 9 Apr 2014
 * 
 */
@Service
class PricingServiceImpl implements PricingService {

	private static final Logger				LOG	= LoggerFactory.getLogger(PricingServiceImpl.class);

	@Autowired
	private CountryClusterVersionService	countryClusterVersionService;

	@Autowired
	private RegionRateService				regionRateService;

	@Autowired
	@Qualifier("callhandling")
	private TotalComponentCalculator		callHandlingCalculatorService;
	@Autowired
	@Qualifier("saphana")
	private TotalComponentCalculator		sapHanaCalculator;
	@Autowired
	@Qualifier("deliverable")
	private TotalComponentCalculator		deliverableCalculator;
	@Autowired
	@Qualifier("dcdm")
	private TotalComponentCalculator		dcdmCalculator;
	@Autowired
	@Qualifier("dcia")
	private TotalComponentCalculator		dciaCalculatorService;
	@Autowired
	@Qualifier("osscalculator")
	private TotalComponentCalculator		ossCalculator;

	@Autowired
	@Qualifier("gross")
	private TotalCalculator					grossTotalCalculator;
	@Autowired
	@Qualifier("channel")
	private TotalCalculator					channelCalculatorService;
	@Autowired
	@Qualifier("net")
	private TotalCalculator					netTotalCalculator;
	@Autowired
	@Qualifier("assignment")
	private AssignmentCalculator			assignmentCalculator;

	private List<TotalComponentCalculator>	totalComponentCalculators;

	private List<TotalCalculator>			totalCalculators;

	@PostConstruct
	private void loadCalculators() {
		totalComponentCalculators = new ArrayList<>();
		totalComponentCalculators.add(callHandlingCalculatorService);
		totalComponentCalculators.add(sapHanaCalculator);
		totalComponentCalculators.add(deliverableCalculator);
		totalComponentCalculators.add(dcdmCalculator);
		totalComponentCalculators.add(dciaCalculatorService);
		totalComponentCalculators.add(ossCalculator);

		// Note: order is important here, ie gross, discount, net, assignment
		totalCalculators = new ArrayList<>();
		totalCalculators.add(grossTotalCalculator);
		totalCalculators.add(channelCalculatorService);
		totalCalculators.add(netTotalCalculator);
		totalCalculators.add(assignmentCalculator);
	}

	@Override
	public void recalculateTotals(Quote quote) {
		if (quote.getQuoteDevices().isEmpty()) {
			return;
		}
		CountryClusterVersion countryClusterVersion = countryClusterVersionService.loadCountryClusterVersion(quote);
		recalculateTotals(quote, countryClusterVersion);
	}

	void recalculateTotals(Quote quote, CountryClusterVersion countryClusterVersion) {
		// Granular Totals
		List<QuoteBaseTotalComponent> newTotalComponents = recalculateTotalComponents(quote, countryClusterVersion);

		// Replace
		replaceTotalComponents(quote, newTotalComponents);

		// Gross / Discount / Net / Assignment
		recalculateNetPrices(quote);
	}

	private List<QuoteBaseTotalComponent> recalculateTotalComponents(Quote quote, CountryClusterVersion countryClusterVersion) {
		quote.setCountryClusterVersionId(countryClusterVersion.getId());
		Map<ProductLine, BigDecimal> aohByProductLine = regionRateService.loadAohCostPercentByProductLine(quote);
		CalculatorContext calculatorContext = new CalculatorContextImpl(quote, countryClusterVersion, aohByProductLine);

		List<QuoteBaseTotalComponent> newTotalComponents = new ArrayList<>();

		// Calculate
		LOG.info("---- Components ------------------------------------------------------------------------------");
		for (TotalComponentCalculator calculator : totalComponentCalculators) {
			LOG.info("[Q{}] BGN: Calculating total components via {}", quote.getId(), calculator);
			newTotalComponents.addAll(calculator.getNewTotalComponents(calculatorContext));
			LOG.info("[Q{}] END: Calculating total components via {}", quote.getId(), calculator);
		}
		LOG.info("---- Components ------------------------------------------------------------------------------");
		return newTotalComponents;
	}

	/**
	 * Replace the existing quote total components in the db with new the ones returned from the calculators.
	 * 
	 * Reuse the existing component where possible to avoid exhausting the id sequence.
	 */
	private void replaceTotalComponents(Quote quote, List<QuoteBaseTotalComponent> newTotalComponents) {
		LOG.info("[Q{}] BGN: Replacing QuoteTotal Components for quote {}", quote.getId(), quote);
		List<QuoteBaseTotalComponent> oldQuoteTotalComponents = quote.getQuoteTotalComponents();

		Map<QuoteBaseTotalComponent, QuoteBaseTotalComponent> existingTotalComponentFinder = new HashMap<>();
		for (QuoteBaseTotalComponent oldQuoteTotalComponent : oldQuoteTotalComponents) {
			existingTotalComponentFinder.put(oldQuoteTotalComponent, oldQuoteTotalComponent);
		}

		List<QuoteBaseTotalComponent> mergedTotalComponents = new ArrayList<>();

		for (QuoteBaseTotalComponent newTotalComponent : newTotalComponents) {
			QuoteBaseTotalComponent existing = existingTotalComponentFinder.get(newTotalComponent);
			if (existing != null) {
				existing.updateFrom(newTotalComponent);
				mergedTotalComponents.add(existing);
				LOG.debug("[Q{}] Replacing {} ", quote.getId(), existing);
			} else {
				mergedTotalComponents.add(newTotalComponent);
				LOG.debug("[Q{}] Adding {} ", quote.getId(), newTotalComponent);
			}
		}
		oldQuoteTotalComponents.clear();
		oldQuoteTotalComponents.addAll(mergedTotalComponents);
		LOG.info("[Q{}] END: Replacing QuoteTotal Components for quote {}", quote.getId(), quote);
	}

	// Recalculating Gross / Discount / Net / Assignment
	@Override
	public void recalculateNetPrices(Quote quote) {
		quote.setQuoteTotal(createIfAbsentQuoteTotal(quote.getQuoteTotal()));
		LOG.info("---- Totals ------------------------------------------------------------------------------");
		for (TotalCalculator calculator : totalCalculators) {
			LOG.info("[Q{}] BGN: Summarizing total via {}", quote.getId(), calculator);
			calculator.summarize(quote);
			LOG.info("[Q{}] END: Summarizing total via {}", quote.getId(), calculator);
		}
		LOG.info("---- Totals ------------------------------------------------------------------------------");
	}

	QuoteTotal createIfAbsentQuoteTotal(QuoteTotal quoteTotal) {
		return quoteTotal == null ? new QuoteTotal() : quoteTotal;
	}

	@Override
	public void recalculateAssignmentTotal(Quote quote, List<QuoteAssignmentComponent> allQpcTotals) {
		assignmentCalculator.summarize(quote, allQpcTotals);
	}
}
