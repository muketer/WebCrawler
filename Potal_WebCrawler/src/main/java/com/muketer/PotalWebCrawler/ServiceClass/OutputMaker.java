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
		double[] performanceScores = null;
		double totalScoreSize = 0;
		double totalScore = 0;
		for(int count = 0;count<totalSearchOutputPageCount;count++){
			linkPages = searchOutput_linkPages.get(count+1);
			performanceScores = checkLinkPage(linkPages);
			totalScore += sumPerformanceScores(performanceScores);
			totalScoreSize += checkTotalScoreSize(performanceScores);
			System.out.println("�˻� ��� �� ������ ���� ���� �Ϸ�");
		}
		return computeFinalScore(totalScoreSize, totalScore, performanceScores);
	}
	

	private double computeFinalScore(double totalScoreSize, double totalScore, double[] performanceScores){
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
	}
	
	private double[] checkLinkPage(List<Document> linkPages)
			throws IOException{
		double[] performanceScores = new double[linkPages.size()];
		int scoreArrayNo = 0;
		long documentTextSize = 0;
		
		for(Document linkPage : linkPages){
			if(linkPage==null){
				performanceScores[scoreArrayNo] = 0;
				scoreArrayNo++;
				continue;
			}
			documentTextSize = linkPage.text().length();
			scoreLinkPage(documentTextSize, linkPage, performanceScores, scoreArrayNo);
			scoreArrayNo++;
		}
		return performanceScores;
	}
	
	private void scoreLinkPage(long documentTextSize, Document linkPage, double[] performanceScores,
			int scoreArrayNo) throws IOException{
		I_DocumentParser parser = new DocumentParser();		
		String bodyContents = parser.documentParse(linkPage);
		I_PerformanceChecker checker = new PerformanceChecker(searchLinkOutputPageNo, searchRunTime,
				searchKeywordsArray, bodyContents, "\n", ".", documentTextSize);
		performanceScores[scoreArrayNo] = checker.scoreCheck();
	}
}
