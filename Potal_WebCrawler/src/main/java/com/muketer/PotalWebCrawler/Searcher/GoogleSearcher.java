package com.muketer.PotalWebCrawler.Searcher;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleSearcher extends CommonSearcher{
	
	private static final Logger logger = LoggerFactory.getLogger(GoogleSearcher.class);
	
	public GoogleSearcher(String[] searchKeywordsArray){
		super.domain = "https://www.google.com";
		super.searchQuery = "/search?q=";
		super.pageStartQuery = "&start=";
		super.query_totalSearchOutputNo = "div.sd";
		super.query_findLinkPageUri = "h3.r a";
		super.makeDocumentType_defaultAtThisPortal = "httpGet";
		super.charSet_defaultAtThisPortal = "";
		super.makeDocumentType_makeLinkPage = "jsoupConnect";
		super.charset_makeLinkPage = "";
		super.delimiter_linkCount_contentsCut = "��";
		super.searchKeywordsArray = searchKeywordsArray;
	}
	
	@Override
	protected List<Document> makeDocumentList_searchPage(String completedSearchQuery){
		List<Document> docs = new ArrayList<Document>();
		String searchUri = super.domain+completedSearchQuery;
		int pageCountInt = 0;
		String pageCountString = "";
		Document doc = null;
		
		// �׽�Ʈ
		System.out.println("=============================== searchPage Document List ���� ����");
		
		while(true){
			pageCountString = String.valueOf(pageCountInt);
			doc = super.makeDocument(super.makeDocumentType_defaultAtThisPortal,
					super.charSet_defaultAtThisPortal, searchUri+pageCountString);
			if(judgeBreak_searchOutput_lastPage(doc)){
				if(pageCountInt!=0){
					docs.add(doc);
					break;
				}
			}
			docs.add(doc);
			pageCountInt += 10;
		}
		
		// �׽�Ʈ
		System.out.println("=============================== searchPage Document List ���� �Ϸ�");
		
		return docs;
	}
	
	private boolean judgeBreak_searchOutput_lastPage(Document doc){
		int judgeSize = 2;
		Elements elements = doc.select("td.b a.fl");
		if(elements.size()<judgeSize)
			return true;
		else
			return false;
	}
	
	@Override
	protected void makeLinkPage(Element searchOutput_linkTitleElement, List<Document> page_linkPages){
		String searchOutput_linkUri = searchOutput_linkTitleElement.attr("href");
		
		// �׽�Ʈ
		logger.info("makeDocumentList - makeLinkPage / searchOutput_linkUri : "+searchOutput_linkUri);
		System.out.println("-------------------------------");
		
		if(searchOutput_linkUri.startsWith("/search?") || searchOutput_linkUri.startsWith("http")) {
			
			// �׽�Ʈ
			logger.info("makeDocumentList - makeLinkPage / searchOutput_linkUri �� /search? �� http ����");
			System.out.println("-------------------------------");
			
			return;
		}
		searchOutput_linkUri = uriCut(searchOutput_linkUri);
		Document doc = super.makeDocument(super.makeDocumentType_makeLinkPage,
				super.charset_makeLinkPage, searchOutput_linkUri);
		page_linkPages.add(doc);
	}
	
	private String uriCut(String modifiedUri){
		int cuttingPoint = modifiedUri.indexOf("=")+1;
		int endPoint = modifiedUri.indexOf("&");
		modifiedUri = modifiedUri.substring(cuttingPoint, endPoint);
		return modifiedUri;
	}
	
	
}
