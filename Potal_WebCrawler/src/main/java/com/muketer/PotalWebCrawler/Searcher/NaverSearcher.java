package com.muketer.PotalWebCrawler.Searcher;

public class NaverSearcher extends CommonSearcher{
	
	public NaverSearcher(int limitPageNo, String[] searchKeywordsArray){
		super.domain = "https://search.naver.com";
		super.searchQuery = "/search.naver?where=webkr&query=";
		super.pageStartQuery = "&start=";
		super.query_totalSearchOutputNo = "span.title_num";
		super.query_findLinkPageUri = "li.sh_web_top dl dt a";
		super.increasingNo_makeSearchPage = 10;
		super.limitPageNo = limitPageNo;
		super.makeDocumentType_defaultAtThisPortal = "parseOpenStream";
		super.charSet_defaultAtThisPortal = "UTF-8";
		super.makeDocumentType_makeLinkPage = "jsoupConnect";
		super.charset_makeLinkPage = "";
		super.delimiter_linkCount_contentsCut = "°Ç";
		super.searchKeywordsArray = searchKeywordsArray;
	}
	
}
