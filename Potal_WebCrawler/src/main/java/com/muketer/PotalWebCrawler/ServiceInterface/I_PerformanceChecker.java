package com.muketer.PotalWebCrawler.ServiceInterface;

import java.io.IOException;

public interface I_PerformanceChecker {

	public void scoreCheck(double[] keywordMatchingScoresArray) throws IOException;
	public double computeFinalScore(double searchRunTime, long searchLinkOutputPageNo,
			double[] keywordMatchingScoresArray);
}
