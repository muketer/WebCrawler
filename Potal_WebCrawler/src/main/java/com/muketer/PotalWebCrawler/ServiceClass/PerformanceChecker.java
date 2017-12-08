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
	
	/*
	 * 여기서는 최종적으로 int[0]에 해당 페이지의 개별 매칭 평균, int[1]에 해당 페이지의 토탈 매칭 평균 담아서 리턴
	 * 
	 * OutputMaker에서 호출해서 쓸 위 배열 속 값들 더해서 평균 내거나 연산할 메소드 및
	 * 최종 점수 계산할 메소드 만들고 OutputMaker에서 호출해서 계산 
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
	// 점수가 0인 부분이 있으면 여기서 에러 날 것
	public double computeFinalScore(double searchRunTime, long searchLinkOutputPageNo,
			double[] keywordMatchingScoresArray){
		double totalMatchingScoreWeight = 0.55, eachMatchingScoreWeight = 0.25,
				searchRunTimeWeight = 0.1, searchLinkOutputPageNoWeight = 0.1,
				subWeight1 = 1000, subWeight2 = 0.001;
		
		// 테스트
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
			
/*			// 테스트
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
			
/*			// 테스트
			System.out.println("PerformanceChecker - countEachMatchingKeywords / eachMatchingNo : "+matchingNos[matchingNoArrayNo]);*/
			
			matchingNoArrayNo++;
		}
		
		// 테스트
		System.out.println("--- 검색 키워드 전체에 대한 개별 매칭 점수 체크 완료");
		
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
				System.out.println("PerformanceChecker - countMatchingKeyword / index 아웃 바운드");
				break;
			}
		}
		return matchingNo;
	}
}
