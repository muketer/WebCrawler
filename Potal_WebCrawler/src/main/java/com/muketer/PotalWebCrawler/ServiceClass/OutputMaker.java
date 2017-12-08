package com.muketer.PotalWebCrawler.ServiceClass;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import com.muketer.PotalWebCrawler.ServiceInterface.I_DocumentParser;
import com.muketer.PotalWebCrawler.ServiceInterface.I_OutputMaker;
import com.muketer.PotalWebCrawler.ServiceInterface.I_PerformanceChecker;

@Service
public class OutputMaker implements I_OutputMaker{
	/*
	 * 해당 검색 키워드에 대한 검색 결과가 없을 때, 혹은 특정 검색 결과 링크 페이지에서 검색 키워드와 매칭되는 것이 없는 등의 케이스 같이
	 * 점수가 0점일 때에 대한 대처가 없는 상황임
	 * 즉, 현재 상황은 점수가 0점인 것에 대해 특정 document 생성 실패해서 그 자리의 document가 없는 상황만을 가정한 것
	 */
	
	/*
	 * 이 클래스에 있는 점수 체크 관련 메소드들 다 PerformanceChecker로 옮기기,
	 * 여기서는 그 메소드들 호출해서 사용만 하기
	 */
	
	
	private String[] searchKeywordsArray;
	private Map<Integer, List<Document>> searchOutput_linkPages;
	private double searchRunTime;
	private long searchLinkOutputPageNo;
	
	public OutputMaker(){}
	
	public OutputMaker(String[] searchKeywordsArray, Map<Integer, List<Document>> searchOutput_linkPages,
			double searchRunTime, long searchLinkOutputPageNo){
		this.searchKeywordsArray = searchKeywordsArray;
		this.searchOutput_linkPages = searchOutput_linkPages;
		this.searchRunTime = searchRunTime;
		this.searchLinkOutputPageNo = searchLinkOutputPageNo;
	}
	
	@Override
	public double checkLinkPages() throws IOException {
		int totalSearchOutputPageCount = searchOutput_linkPages.size();
		List<Document> linkPages = null;
		double[] keywordMatchingScoresArray = {0, 0};
		/*double totalScoreSize = 0;
		double totalScore = 0;*/
		
		int linkPagesNo = 0;
		for(int count = 0;count<totalSearchOutputPageCount;count++){
			linkPages = searchOutput_linkPages.get(count+1);
			linkPagesNo += checkLinkPage(linkPages, keywordMatchingScoresArray);
			/*totalScore += sumPerformanceScores(performanceScores);
			totalScoreSize += checkTotalScoreSize(performanceScores);*/
			System.out.println("=============================== 검색 결과 한 페이지 점수 측정 완료");
		}
		
		keywordMatchingScoresArray[0] /= linkPagesNo;
		keywordMatchingScoresArray[1] /= linkPagesNo;
		
		return computeFinalScore(keywordMatchingScoresArray);
	}
	
	private double computeFinalScore(double[] keywordMatchingScoresArray) {
		I_PerformanceChecker checker = new PerformanceChecker();
		return checker.computeFinalScore(searchRunTime, searchLinkOutputPageNo, keywordMatchingScoresArray);
	}
	

	/*private double computeFinalScore(double totalScoreSize, double totalScore){
		return totalScore/totalScoreSize;
	}
	
	private double checkTotalScoreSize(double[] performanceScores){
		double emptyRoomNo = 0;
		for(double performanceScore : performanceScores){
			if(performanceScore==0)
				emptyRoomNo++;
		}
		return ((double)performanceScores.length - emptyRoomNo);
	}
	
	private double sumPerformanceScores(double[] performanceScores){
		double totalScore = 0;
		for(double performanceScore : performanceScores)				
			totalScore += performanceScore;
		return totalScore;
	}*/
	
	private int checkLinkPage(List<Document> linkPages, double[] keywordMatchingScoresArray)
			throws IOException{
		long documentTextSize = 0;
		for(Document linkPage : linkPages){
			if(linkPage==null){
				continue;
			}
			documentTextSize = linkPage.text().length();
			scoreLinkPage(documentTextSize, linkPage, keywordMatchingScoresArray /*, performanceScores, scoreArrayNo*/);
		}
		return linkPages.size();
	}
	
	private void scoreLinkPage(long documentTextSize, Document linkPage, double[] keywordMatchingScoresArray /*, double[] performanceScores,
			int scoreArrayNo*/) throws IOException{
		I_DocumentParser parser = new DocumentParser();
		String bodyContents = parser.documentParse(linkPage);
		
		// 테스트
		System.out.println("------------------------------------------------------- 링크 페이지 하나 파싱 완료");
		
		I_PerformanceChecker checker = new PerformanceChecker(searchKeywordsArray, bodyContents,
				"\n", ".", documentTextSize);
		
		checker.scoreCheck(keywordMatchingScoresArray);
		
		// 테스트
		System.out.println("------------------------------------------------------- 링크 페이지 하나 점수 체크 완료");
		
	}
}
