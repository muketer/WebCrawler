package com.muketer.PotalWebCrawler.ServiceClass;

import java.io.IOException;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.muketer.PotalWebCrawler.ServiceInterface.I_PerformanceChecker;

@Service
public class PerformanceChecker implements I_PerformanceChecker{
	/**
	 * 검색 키워드 매칭률 산정 기준
	 * 
	 * 1. 각 검색 키워드 당 개별 매칭 개수
	 * 
	 * 2. 한 문장 안에서의 복수 키워드 매칭 횟수
	 *    (문장 단위 파싱 방법 연구 필요 - ex) .text() 말고, 시작 태그 '>'와 끝 태그 '<' 사이의 텍스트 뽑고 '.'로 파싱하는 방법) 
	 * ex) 키워드 : 사과, 과일
	 *     문장 : '사과는 몸에 좋은 과일이다.', '사과를 매일 한 개씩 먹으면 건강에 좋다.', '과일 섭취는 건강 관리에 중요하다.'
	 *           => 복수 키워드 매칭 횟수 : 1
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
				System.out.println("각 링크 페이지별 키워드 토탈 매칭 수 : "+searchKeywordMatchingNo);
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
			System.out.println("각 링크 페이지 별 키워드 개별 매칭 수 : "+matchingNos[matchingNoArrayNo]);
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
				System.out.println("PerformanceChecker - countMatchingKeyword / index 아웃 바운드");
				break;
			}
		}
		return matchingNo;
	}
}
