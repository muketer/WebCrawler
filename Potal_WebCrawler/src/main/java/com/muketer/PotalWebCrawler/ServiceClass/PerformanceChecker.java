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
	
	private long searchLinkOutputPageNo, documentTextSize;
	private double searchRunTime, eachMatchingScore, totalMatchingScore, finalScore;
	private String[] searchKeywordsArray, splitedBodyContents;
	private String bodyContents, firstSplitDelimiter, secondSplitDelimiter;
	
	public PerformanceChecker(){
		
	}
	
	public PerformanceChecker(long searchLinkOutputPageNo, double searchRunTime, String[] searchKeywordsArray,
			String bodyContents, String firstSplitDelimiter, String secondSplitDelimiter, long documentTextSize){
		this.searchLinkOutputPageNo = searchLinkOutputPageNo;
		this.searchRunTime = searchRunTime;
		this.searchKeywordsArray = searchKeywordsArray;
		this.bodyContents = bodyContents;
		this.firstSplitDelimiter = firstSplitDelimiter;
		this.secondSplitDelimiter = secondSplitDelimiter;
		this.documentTextSize = documentTextSize;
	}
	
	@Override
	public double scoreCheck() throws IOException {
		int[] eachMatchingNos = countEachMatchingKeywords();
		int totalMatchingNo = countTotalMatchingKeywords(/*count, ScoreArrayNo*/);
		eachMatchingScore = scoreEachMatchingKeywords(eachMatchingNos);	
		totalMatchingScore = scoreTotalMatchingKeywords(totalMatchingNo);
		computeFinalScore();
		
		return finalScore;
	}
	
	private void computeFinalScore(){
		double totalMatchingScoreWeight = 0.55, eachMatchingScoreWeight = 0.25,
				searchRunTimeWeight = 0.1, searchLinkOutputPageNoWeight = 0.1,
				subWeight1 = 1000, subWeight2 = 0.001;
		finalScore = (totalMatchingScore*totalMatchingScoreWeight+
				eachMatchingScore*eachMatchingScoreWeight+
				1/searchRunTime*searchRunTimeWeight)*subWeight1+
				(double)searchLinkOutputPageNo*searchLinkOutputPageNoWeight*subWeight2;
	}
	
	private double scoreTotalMatchingKeywords(int totalMatchingNo){
		int emptyRoomNo = checkEmptyRoom();
		int totalRoomNo = splitedBodyContents.length;
		totalRoomNo -= emptyRoomNo;
		if(totalMatchingNo==0||totalRoomNo==0)
			return 0;
		return (double)totalMatchingNo/totalRoomNo;
	}
	
	private double scoreEachMatchingKeywords(int[] eachMatchingNos){
		double[] eachMatchingScores = new double[eachMatchingNos.length];
		double eachMatchingScore = 0;
		int roomNo = 0;
		for(int matchingNo : eachMatchingNos){
			eachMatchingScores[roomNo] = (double)matchingNo/documentTextSize;
		}
		for(double score : eachMatchingScores)
			eachMatchingScore += score;
		return eachMatchingScore /= eachMatchingScores.length;
	}
	
	private int countTotalMatchingKeywords(){
		int matchingNos = 0;
		bodyContentsTransform();
		for(String content : splitedBodyContents){
			int searchKeywordMatchingNo = countTotalMatching(content);
			if(searchKeywordMatchingNo!=0)
				System.out.println("�� ��ũ �������� Ű���� ��Ż ��Ī �� : "+searchKeywordMatchingNo);
			if(searchKeywordMatchingNo==searchKeywordsArray.length)
				matchingNos++;
		}
		return matchingNos;
	}
	
	private void bodyContentsTransform() {
		splitedBodyContents = bodyContents.split(firstSplitDelimiter);
		bodyContents = String.join(secondSplitDelimiter, splitedBodyContents);
		splitedBodyContents = bodyContents.split(Pattern.quote(secondSplitDelimiter));
	}
	
	private int checkEmptyRoom(){
		int emptyRoomNo=0;
		for(String content : splitedBodyContents){
			if(content.isEmpty())
				emptyRoomNo++;
		}
		return emptyRoomNo;
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
			System.out.println("�� ��ũ ������ �� Ű���� ���� ��Ī �� : "+matchingNos[matchingNoArrayNo]);
			matchingNoArrayNo++;
		}
		return matchingNos;
	}
	
	private int countEachMatchingKeyword(String searchKeyword){
		int matchingNo = 0;
		int searchPoint = 0;
		while(bodyContents.indexOf(searchKeyword)>=0){
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
