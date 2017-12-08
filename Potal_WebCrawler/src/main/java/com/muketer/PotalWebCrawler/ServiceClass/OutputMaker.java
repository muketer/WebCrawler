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
	 * �ش� �˻� Ű���忡 ���� �˻� ����� ���� ��, Ȥ�� Ư�� �˻� ��� ��ũ ���������� �˻� Ű����� ��Ī�Ǵ� ���� ���� ���� ���̽� ����
	 * ������ 0���� ���� ���� ��ó�� ���� ��Ȳ��
	 * ��, ���� ��Ȳ�� ������ 0���� �Ϳ� ���� Ư�� document ���� �����ؼ� �� �ڸ��� document�� ���� ��Ȳ���� ������ ��
	 */
	
	/*
	 * �� Ŭ������ �ִ� ���� üũ ���� �޼ҵ�� �� PerformanceChecker�� �ű��,
	 * ���⼭�� �� �޼ҵ�� ȣ���ؼ� ��븸 �ϱ�
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
			System.out.println("=============================== �˻� ��� �� ������ ���� ���� �Ϸ�");
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
		
		// �׽�Ʈ
		System.out.println("------------------------------------------------------- ��ũ ������ �ϳ� �Ľ� �Ϸ�");
		
		I_PerformanceChecker checker = new PerformanceChecker(searchKeywordsArray, bodyContents,
				"\n", ".", documentTextSize);
		
		checker.scoreCheck(keywordMatchingScoresArray);
		
		// �׽�Ʈ
		System.out.println("------------------------------------------------------- ��ũ ������ �ϳ� ���� üũ �Ϸ�");
		
	}
}
