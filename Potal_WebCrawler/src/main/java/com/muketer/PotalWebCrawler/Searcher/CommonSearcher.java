package com.muketer.PotalWebCrawler.Searcher;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CommonSearcher {
	
	// ------------------------------- 멤버 필드 ---------------------------------
	
	// 아래 필드들(searchKeywordsArray까지) 모두  각 포털별 searcher들 생성자에서 super. 으로 대입 
	// enum 사용해서 정리할 수 있을지도 한 번 생각해보자
	protected String domain;
	protected String searchQuery;
	protected String pageStartQuery;
	protected String query_totalSearchOutputNo;
	protected String query_findLinkPageUri;
	protected int increasingNo_makeSearchPage;
	protected int limitPageNo;
	protected String makeDocumentType_defaultAtThisPortal;
	protected String charSet_defaultAtThisPortal;	
	protected String makeDocumentType_makeLinkPage;
	protected String charset_makeLinkPage;
	protected String delimiter_linkCount_contentsCut;
	protected String[] searchKeywordsArray;
	
	protected Document page_countTotalSearchOutput;
	
	
	// ------------------------------- 기타 기능들 --------------------------------
	
	private String makeSearchQuery(){
		String completedSearchQuery = searchQuery;
		for(int roomNo = 0 ; roomNo<searchKeywordsArray.length ; roomNo++){
			completedSearchQuery += searchKeywordsArray[roomNo];
			if(roomNo!=searchKeywordsArray.length-1)
				completedSearchQuery += "+";
		}
		return completedSearchQuery;
	}
	
	private String makeSearchQuery(String startPageSentence){
		return makeSearchQuery()+startPageSentence;
	}
	
	public double checkSearchRunTime(){
		String completedSearchQuery = makeSearchQuery();
		double startTime = System.currentTimeMillis();
		page_countTotalSearchOutput = makeDocument(makeDocumentType_defaultAtThisPortal,
				charSet_defaultAtThisPortal, domain+completedSearchQuery);
		double endTime = System.currentTimeMillis();
		return (endTime-startTime)/1000;
	}
	
	public long countTotalLinkOuputPage(){
		String linkCount = page_countTotalSearchOutput.select(query_totalSearchOutputNo).text();
		return linkCount_contentsCut(linkCount);
	}
	
	private long linkCount_contentsCut(String linkCount){
		int firstCuttingPoint = linkCount.lastIndexOf(" ")+1;
		int secondCuttingPoint = linkCount.lastIndexOf(delimiter_linkCount_contentsCut);
		linkCount = linkCount.substring(firstCuttingPoint, secondCuttingPoint);
		linkCount = linkCount.replace(",", "");
		return Long.parseLong(linkCount);
	}
	
	public int countLimitLinkOutputPage(Map<Integer, List<Document>> searchOutput_linkPages){
		int linkOutputPageNo = 0;
		Set<Integer> keySet = searchOutput_linkPages.keySet();
		Iterator<Integer> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			int key = iterator.next();
			List<Document> values = searchOutput_linkPages.get(key);
			for(int i=0; i<values.size();i++)
				linkOutputPageNo++;
		}
		return linkOutputPageNo;
	}
	
	// ------------------------------------------------------------------------------
	
	
	// ------------------------------- 검색 결과 저장 관련 --------------------------------
	
	public Map<Integer, List<Document>> parse(){
		String completedSearchQuery = makeSearchQuery(pageStartQuery);
		List<Document> searchOutput_pageDocs = makeDocumentList_searchPage(completedSearchQuery);
		Map<Integer, List<Document>> searchOutput_linkPages = makeDocumentList_linkPage(searchOutput_pageDocs);
		return searchOutput_linkPages;
	}
	
	protected List<Document> makeDocumentList_searchPage(String completedSearchQuery){
		List<Document> docs = new ArrayList<Document>();
		String searchUri = domain+completedSearchQuery;
		int pageCountInt = 1;
		limitPageNo /= 10;
		String pageCountString = "";
		Document doc = null;
		
		for(int i = 0 ; i < limitPageNo ; pageCountInt += increasingNo_makeSearchPage){
			pageCountString = String.valueOf(pageCountInt);
			doc = makeDocument(makeDocumentType_defaultAtThisPortal,
					charSet_defaultAtThisPortal, searchUri+pageCountString);
			docs.add(doc);
			i++;
		}
		return docs;
	}
	
	private Map<Integer, List<Document>> makeDocumentList_linkPage(List<Document> searchOutput_pageDocs) {
		Map<Integer, List<Document>> searchOutput_linkPages = new HashMap<Integer, List<Document>>();
		Elements searchOutput_linkTitleElements = null;
		int pageNo = 1;
		
		for(Document doc : searchOutput_pageDocs){
			// 이 처리에 대응하는 "검색 결과가 없습니다" 등의 메시지 보여주는 기능도 필요
			searchOutput_linkTitleElements = doc.select(query_findLinkPageUri);
			if(searchOutput_linkTitleElements==null)
				break;
			else{
				putDocumentList(pageNo, searchOutput_linkTitleElements, searchOutput_linkPages);
			}
			pageNo++;
		}
		return searchOutput_linkPages;
	}
	
	private void putDocumentList(int pageNo, Elements searchOutput_linkTitleElements,
			Map<Integer, List<Document>> searchOutput_linkPages) {
		List<Document> page_linkPages = new ArrayList<Document>();
		Document doc = null;
		
		for(Element searchOutput_linkTitleElement : searchOutput_linkTitleElements){
			makeLinkPage(searchOutput_linkTitleElement, page_linkPages);
		}
		searchOutput_linkPages.put(pageNo, page_linkPages);
	}
	
	protected void makeLinkPage(Element searchOutput_linkTitleElement, List<Document> page_linkPages){
		String searchOutput_linkUri = searchOutput_linkTitleElement.attr("href");
		Document doc = makeDocument(makeDocumentType_makeLinkPage, charset_makeLinkPage, searchOutput_linkUri);
		page_linkPages.add(doc);
	}
	
	// --------------------------------------------------------------------------------
	
	// ------------------------------- Document 생성 관련 --------------------------------
	
	protected Document makeDocument(String type, String charset, String searchUri){
		switch(type){
		case "jsoupConnect" : return jsoupConnect(searchUri);
		case "parseOpenStream" : return parseOpenStream(searchUri, charset);
		case "httpGet" : return httpGet(searchUri);
		default : return jsoupConnect(searchUri);
		}
	}
	
	private Document jsoupConnect(String searchUri){
		try{
			return Jsoup.connect(searchUri).userAgent("Mozilla/5.0").timeout(5000).get();
		}catch(IOException e){
			System.out.println("makeDocument_jsoupConnect / Document 생성 실패 - IOException");
			return null;
		}
	}
	
	private Document parseOpenStream(String searchUri, String charset){
		try{
			return Jsoup.parse(new URL(searchUri).openStream(), charset, searchUri);
		}catch(SSLHandshakeException e){
			System.out.println("makeDocument_parseOpenStream / Document 생성 실패 - SSLHandshakeException");
			return null;
		}catch(MalformedURLException e){
			System.out.println("makeDocument_parseOpenStream / Document 생성 실패 - MalformedURLException");
			return null;
		}catch(IOException e){
			System.out.println("makeDocument_parseOpenStream / Document 생성 실패 - IOException");
			return null;
		}
	}
	
	private Document httpGet(String searchUri){
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(searchUri);
		return executeResponse(httpClient, httpGet);
	}
	
	private Document executeResponse(HttpClient httpClient, HttpGet httpGet) {
		String res;
		try {
			res = httpClient.execute(httpGet, new BasicResponseHandler() {
				@Override
				public String handleResponse(HttpResponse response) throws HttpResponseException, IOException {
					return new String(super.handleResponse(response).getBytes("UTF-8"), "UTF-8");
				}
			});
		} catch (ClientProtocolException e) {
			System.out.println("makeDocument_httpGet / Document 생성 실패 - ClientProtocolException");
			return null;
		} catch (IOException e) {
			System.out.println("makeDocument_httpGet / Document 생성 실패 - IOException");
			return null;
		}
		// Jsoup.parse(res)가 에러 날 경우에 대한 대비 필요할 수도 있음
		return Jsoup.parse(res);
	}
}
