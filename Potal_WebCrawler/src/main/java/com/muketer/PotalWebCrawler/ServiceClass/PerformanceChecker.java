package com.muketer.PotalWebCrawler.ServiceClass;

import java.io.IOException;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.muketer.PotalWebCrawler.ServiceInterface.I_PerformanceChecker;

@Service
public class PerformanceChecker implements I_PerformanceChecker{
	
	/**
	 * �˻� Ű���� ��Ī�� ���� ����
	 * 
	 * 1. �� �˻� Ű���� �� ���� ��Ī ����
	 * 
	 * 2. �� ���� �ȿ����� ���� Ű���� ��Ī Ƚ��
	 *    (���� ���� �Ľ� ��� ���� �ʿ� - ex) .text() ����, ���� �±� '>'�� �� �±� '<' ������ �ؽ�Ʈ �̰� '.'�� �Ľ��ϴ� ���) 
	 * ex) Ű���� : ���, ����
	 *     ���� : '����� ���� ���� �����̴�.', '����� ���� �� ���� ������ �ǰ��� ����.', '���� ����� �ǰ� ������ �߿��ϴ�.'
	 *           => ���� Ű���� ��Ī Ƚ�� : 1
	 * 
	 * +a
	 * 
	 */
	
	/*
	 * ���⼭�� ���������� int[0]�� �ش� �������� ���� ��Ī ���, int[1]�� �ش� �������� ��Ż ��Ī ��� ��Ƽ� ����
	 * 
	 * OutputMaker���� ȣ���ؼ� �� �� �迭 �� ���� ���ؼ� ��� ���ų� ������ �޼ҵ� ��
	 * ���� ���� ����� �޼ҵ� ����� OutputMaker���� ȣ���ؼ� ��� 
	 */
	
	private long documentTextSize;
	private String[] searchKeywordsArray, splitedBodyContents;
	private String bodyContents, firstSplitDelimiter, secondSplitDelimiter;
	
	public PerformanceChecker(){}
	
	public PerformanceChecker(String[] searchKeywordsArray,
			String bodyContents, String firstSplitDelimiter, String secondSplitDelimiter, long documentTextSize){
		this.searchKeywordsArray = searchKeywordsArray;
		this.bodyContents = bodyContents;
		this.firstSplitDelimiter = firstSplitDelimiter;
		this.secondSplitDelimiter = secondSplitDelimiter;
		this.documentTextSize = documentTextSize;
	}
	
	@Override
	public void scoreCheck(double[] keywordMatchingScoresArray) throws IOException {
		int[] eachMatchingNos = countEachMatchingKeywords();
		int totalMatchingNo = countTotalMatchingKeywords(/*count, ScoreArrayNo*/);
		
		keywordMatchingScoresArray[0] += scoreEachMatchingKeywords(eachMatchingNos);	
		keywordMatchingScoresArray[1] += scoreTotalMatchingKeywords(totalMatchingNo);
	}
	
	@Override
	// ������ 0�� �κ��� ������ ���⼭ ���� �� ��
	public double computeFinalScore(double searchRunTime, long searchLinkOutputPageNo,
			double[] keywordMatchingScoresArray){
		double totalMatchingScoreWeight = 0.55, eachMatchingScoreWeight = 0.25,
				searchRunTimeWeight = 0.1, searchLinkOutputPageNoWeight = 0.1,
				subWeight1 = 1000, subWeight2 = 0.001;
		
		// �׽�Ʈ
		System.out.println("PerformanceChecker - computeFinalScore / totalMatchingScore : "+keywordMatchingScoresArray[1]);
		System.out.println("PerformanceChecker - computeFinalScore / eachMatchingScore : "+keywordMatchingScoresArray[0]);
		System.out.println("PerformanceChecker - computeFinalScore / searchRunTime : "+searchRunTime);
		System.out.println("PerformanceChecker - computeFinalScore / searchLinkOutputPageNo : "+searchLinkOutputPageNo);
		
		return (keywordMatchingScoresArray[1]*totalMatchingScoreWeight+
				keywordMatchingScoresArray[0]*eachMatchingScoreWeight+
				1/searchRunTime*searchRunTimeWeight)*subWeight1+
				(double)searchLinkOutputPageNo*searchLinkOutputPageNoWeight*subWeight2;
	}
	
	private double scoreEachMatchingKeywords(int[] eachMatchingNos){
		double eachMatchingScore = 0;
		for(int matchingNo : eachMatchingNos) {
			if(matchingNo==0||documentTextSize==0)
				eachMatchingScore += 0;
			eachMatchingScore += (double)matchingNo/documentTextSize;
		}
		if(eachMatchingScore==0||documentTextSize==0)
			return 0;
		return eachMatchingScore /= eachMatchingNos.length;
	}
	
	private double scoreTotalMatchingKeywords(int totalMatchingNo){
		int emptyRoomNo = checkEmptyRoom();
		int totalRoomNo = splitedBodyContents.length;
		totalRoomNo -= emptyRoomNo;
		if(totalMatchingNo==0||totalRoomNo==0)
			return 0;
		return (double)totalMatchingNo/totalRoomNo;
	}
	
	private int checkEmptyRoom(){
		int emptyRoomNo=0;
		for(String content : splitedBodyContents){
			if(content.isEmpty())
				emptyRoomNo++;
		}
		return emptyRoomNo;
	}
	
	private int countTotalMatchingKeywords(){
		int matchingNo = 0;
		bodyContentsTransform();
		for(String content : splitedBodyContents){
			int searchKeywordMatchingNo = countTotalMatching(content);
			if(searchKeywordMatchingNo==searchKeywordsArray.length)				
				matchingNo++;
			
/*			// �׽�Ʈ
			System.out.println("PerformanceChecker - countTotalMatchingKeywords / totalMatchingNo : "+matchingNo);*/
			
		}
		return matchingNo;
	}
	
	private void bodyContentsTransform() {
		splitedBodyContents = bodyContents.split(firstSplitDelimiter);
		bodyContents = String.join(secondSplitDelimiter, splitedBodyContents);
		splitedBodyContents = bodyContents.split(Pattern.quote(secondSplitDelimiter));
	}
	
	private int countTotalMatching(String content){
		int searchKeywordMatchingNo = 0;
		for(String searchKeyword : searchKeywordsArray){
			if(content.contains(searchKeyword))
				searchKeywordMatchingNo++;
		}
		return searchKeywordMatchingNo;
	}
	
	private int[] countEachMatchingKeywords(){
		int[] matchingNos = new int[searchKeywordsArray.length];
		int matchingNoArrayNo = 0;
		for(String searchKeyword : searchKeywordsArray){
			matchingNos[matchingNoArrayNo] = countEachMatchingKeyword(searchKeyword);
			
/*			// �׽�Ʈ
			System.out.println("PerformanceChecker - countEachMatchingKeywords / eachMatchingNo : "+matchingNos[matchingNoArrayNo]);*/
			
			matchingNoArrayNo++;
		}
		
		// �׽�Ʈ
		System.out.println("--- �˻� Ű���� ��ü�� ���� ���� ��Ī ���� üũ �Ϸ�");
		
		return matchingNos;
	}
	
	private int countEachMatchingKeyword(String searchKeyword){
		int matchingNo = 0;
		int searchPoint = 0;
		while(bodyContents.indexOf(searchKeyword, searchPoint)>=0){
			try{
				searchPoint = bodyContents.indexOf(searchKeyword, searchPoint)+1;
				if(searchPoint<=0)
					break;
				matchingNo++;
			}catch(StringIndexOutOfBoundsException e){
				System.out.println("PerformanceChecker - countMatchingKeyword / index �ƿ� �ٿ��");
				break;
			}
		}
		return matchingNo;
	}
}
