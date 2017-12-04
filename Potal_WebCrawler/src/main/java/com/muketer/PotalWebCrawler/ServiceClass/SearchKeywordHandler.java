package com.muketer.PotalWebCrawler.ServiceClass;

import org.springframework.stereotype.Service;

import com.muketer.PotalWebCrawler.ServiceInterface.I_SearchKeywordHandler;

@Service
public class SearchKeywordHandler implements I_SearchKeywordHandler{
	private class ElementsRepository{
		int cuttingPoint;
		String searchKeyword;
	}
	
	private String searchKeywords;
	
	public SearchKeywordHandler(){}
	
	public SearchKeywordHandler(String searchKeywords){
		this.searchKeywords = searchKeywords;
	}
	
	@Override
	public String[] searchKeywordsHandling(){
		ElementsRepository repository = new ElementsRepository();
		return searchKeywordsRefining(repository, searchKeywords);
	}
	
	private String[] searchKeywordsRefining(ElementsRepository repository, String str){
		int count = countSearchKeywords(repository, str);
		String[] searchKeywordsArray = new String[count];
		putSearchKeywordsArray(repository, str, searchKeywordsArray);
		return searchKeywordsArray;
	}
	
	private int countSearchKeywords(ElementsRepository repository, String str){
		int count = 0;
		str = str.trim();
		
		while(true){
			repository.cuttingPoint = str.indexOf(" ");
			if(!(str.isEmpty())&&repository.cuttingPoint<0){
				count++;
				break;
			}
			repository.searchKeyword = str.substring(0, repository.cuttingPoint).trim();
			count++;
			str = str.substring(repository.cuttingPoint+1, str.length()).trim();
			if(str.indexOf(" ")<0){
				count++;
				break;
			}
		}
		return count;
	}
	
	private void putSearchKeywordsArray(ElementsRepository repository, String str, String[] searchKeywordsArray){
		int arrayNo = 0;
		str = str.trim();
		while(true){
			repository.cuttingPoint = str.indexOf(" ");
			if(!(str.isEmpty())&&repository.cuttingPoint<0){
				searchKeywordsArray[arrayNo] = str;
				break;
			}
			repository.searchKeyword = str.substring(0, repository.cuttingPoint).trim();
			searchKeywordsArray[arrayNo] = repository.searchKeyword;
			str = str.substring(repository.cuttingPoint+1, str.length()).trim();
			if(str.indexOf(" ")<0){
				arrayNo++;
				searchKeywordsArray[arrayNo] = str;
				break;
			}
			arrayNo++;
		}
	}
}
