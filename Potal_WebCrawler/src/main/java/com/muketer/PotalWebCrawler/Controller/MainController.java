package com.muketer.PotalWebCrawler.Controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.muketer.PotalWebCrawler.DTO.SearchInformationDTO;
import com.muketer.PotalWebCrawler.Searcher.CommonSearcher;
import com.muketer.PotalWebCrawler.Searcher.DaumSearcher;
import com.muketer.PotalWebCrawler.Searcher.GoogleSearcher;
import com.muketer.PotalWebCrawler.Searcher.NaverSearcher;
import com.muketer.PotalWebCrawler.ServiceClass.DocumentParser;
import com.muketer.PotalWebCrawler.ServiceClass.OutputMaker;
import com.muketer.PotalWebCrawler.ServiceClass.SearchKeywordHandler;
import com.muketer.PotalWebCrawler.ServiceInterface.I_DocumentParser;
import com.muketer.PotalWebCrawler.ServiceInterface.I_OutputMaker;
import com.muketer.PotalWebCrawler.ServiceInterface.I_PerformanceChecker;
import com.muketer.PotalWebCrawler.ServiceInterface.I_SearchKeywordHandler;
import com.muketer.PotalWebCrawler.ServiceInterface.I_TemporaryDAOUser;

@Controller
public class MainController {
	
	private static final Logger logger = LoggerFactory.getLogger(MainController.class);
	
	@Autowired
	private I_SearchKeywordHandler handler;
	@Autowired
	private I_OutputMaker maker;
	@Autowired
	private I_DocumentParser parser;
	@Autowired
	private I_PerformanceChecker checker;
	@Autowired
	private I_TemporaryDAOUser daoUser;
	
	private String[] searchKeywordsArray;
	private int limitPageNo = 20;
	
	@RequestMapping("/")
	public String home(){
		return "home";
	}
	
	@RequestMapping("/googleSearch")
	public String googleSearch(SearchInformationDTO dto, Model model) throws IOException{
		
		I_SearchKeywordHandler handler = new SearchKeywordHandler(dto.getSearchKeywords());
		searchKeywordsArray = handler.searchKeywordsHandling();
		
		CommonSearcher googleSearcher = new GoogleSearcher(searchKeywordsArray);
		double searchRunTime = googleSearcher.checkSearchRunTime();
		
		// �׽�Ʈ
		logger.info("googleSearch / searchRunTime : "+searchRunTime);
		
		long searchLinkOutputPageNo = googleSearcher.countTotalLinkOuputPage();
		Map<Integer, List<Document>> searchOutput_linkPages = googleSearcher.parse();
		limitPageNo = googleSearcher.countLimitLinkOutputPage(searchOutput_linkPages);
		
		I_OutputMaker maker = new OutputMaker(searchKeywordsArray, searchOutput_linkPages,
				searchRunTime, searchLinkOutputPageNo);
		double googleScore = maker.checkLinkPages();
		
		// �׽�Ʈ
		logger.info("googleSearch / googleScore : "+googleScore);
		
		model.addAttribute("searchKeywords", dto.getSearchKeywords());
		model.addAttribute("age", dto.getAge());
		model.addAttribute("sex", dto.getSex());
		model.addAttribute("googleScore", googleScore);
		
		return "home";
	}
	
	@RequestMapping("/naverSearch")
	public String naverSearch(SearchInformationDTO dto, Model model) throws IOException{
		
		I_SearchKeywordHandler handler = new SearchKeywordHandler(dto.getSearchKeywords());
		searchKeywordsArray = handler.searchKeywordsHandling();
		
		CommonSearcher naverSearcher = new NaverSearcher(limitPageNo, searchKeywordsArray);
		double searchRunTime = naverSearcher.checkSearchRunTime();
		
		// �׽�Ʈ
		logger.info("naverSearch / searchRunTime : "+searchRunTime);
		
		long searchLinkOutputPageNo = naverSearcher.countTotalLinkOuputPage();
		
		// �׽�Ʈ
		logger.info("naverSearch / searchLinkOutputPageNo : "+searchLinkOutputPageNo);
		
		Map<Integer, List<Document>> searchOutput_linkPages = naverSearcher.parse();
		
		I_OutputMaker maker = new OutputMaker(searchKeywordsArray, searchOutput_linkPages,
				searchRunTime, searchLinkOutputPageNo);
		double naverScore = maker.checkLinkPages();
		
		model.addAttribute("searchKeywords", dto.getSearchKeywords());
		model.addAttribute("age", dto.getAge());
		model.addAttribute("sex", dto.getSex());
		model.addAttribute("googleScore", dto.getGoogleScore());
		model.addAttribute("naverScore", naverScore);
		
		return "home";
	}
	
	@RequestMapping("/daumSearch")
	public String daumSearch(SearchInformationDTO dto, Model model) throws IOException{
		
		I_SearchKeywordHandler handler = new SearchKeywordHandler(dto.getSearchKeywords());
		searchKeywordsArray = handler.searchKeywordsHandling();
		
		CommonSearcher daumSearcher = new DaumSearcher(limitPageNo, searchKeywordsArray);
		double searchRunTime = daumSearcher.checkSearchRunTime();
		
		// �׽�Ʈ
		logger.info("daumSearch / searchRunTime : "+searchRunTime);
		
		long searchLinkOutputPageNo = daumSearcher.countTotalLinkOuputPage();
		Map<Integer, List<Document>> searchOutput_linkPages = daumSearcher.parse();
		
		I_OutputMaker maker = new OutputMaker(searchKeywordsArray, searchOutput_linkPages,
				searchRunTime, searchLinkOutputPageNo);
		double daumScore = maker.checkLinkPages();
		
		model.addAttribute("searchKeywords", dto.getSearchKeywords());
		model.addAttribute("age", dto.getAge());
		model.addAttribute("sex", dto.getSex());
		model.addAttribute("googleScore", dto.getGoogleScore());
		model.addAttribute("naverScore", dto.getNaverScore());
		model.addAttribute("daumScore", daumScore);
		
		/*dto.setDaumScore(daumScore);
		daoUser.dataInsert(dto);*/
		
		return "home";
	}	
	
}
