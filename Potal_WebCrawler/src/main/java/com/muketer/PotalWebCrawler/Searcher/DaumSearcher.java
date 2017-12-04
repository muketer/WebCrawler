package com.muketer.PotalWebCrawler.Searcher;

public class DaumSearcher extends CommonSearcher{
	
	public DaumSearcher(int limitPageNo, String[] searchKeywordsArray){
		super.domain = "https://search.daum.net";
		super.searchQuery = "/search?w=web&q=";
		super.pageStartQuery = "&p=";
		super.query_totalSearchOutputNo = "span.txt_info";
		super.query_findLinkPageUri = "div.coll_cont div.mg_cont div.wrap_cont div.cont_inner a.f_link_b";
		super.increasingNo_makeSearchPage = 1;
		super.limitPageNo = limitPageNo;
		super.makeDocumentType_defaultAtThisPortal = "httpGet";
		super.charSet_defaultAtThisPortal = "";	
		super.makeDocumentType_makeLinkPage = "jsoupConnect";
		super.charset_makeLinkPage = "";
		super.delimiter_linkCount_contentsCut = "°Ç";
		super.searchKeywordsArray = searchKeywordsArray;
	}
	
}
