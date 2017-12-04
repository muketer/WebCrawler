package com.muketer.PotalWebCrawler.ServiceClass;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import com.muketer.PotalWebCrawler.ServiceInterface.I_DocumentParser;

@Service
public class DocumentParser implements I_DocumentParser{
	private class InnerParser{
		protected int backCutting(String bodyContents, String backString, int frontCuttingPoint){
			 return bodyContents.indexOf(backString, frontCuttingPoint)+1;
		}
		
		protected String nextCutting(String bodyContents, String frontString, String backString, int frontCuttingPoint,
				int backCuttingPoint){
			int nextCuttingPoint = bodyContents.indexOf("<", backCuttingPoint);
			if(nextCuttingPoint<0)
				return bodyContents.substring(0, frontCuttingPoint)+bodyContents.substring(backCuttingPoint);
			return bodyContents.substring(0, frontCuttingPoint)+bodyContents.substring(nextCuttingPoint);
		}
		
		protected String removeText(String bodyContents, String frontString, String backString){
			int frontCuttingPoint = bodyContents.indexOf(frontString);
			if(frontCuttingPoint<0)
				return bodyContents;
			int	backCuttingPoint = backCutting(bodyContents, backString, frontCuttingPoint);
			return nextCutting(bodyContents, frontString, backString, frontCuttingPoint, backCuttingPoint);
		}
	}
	
	public DocumentParser(){}
	
	@Override
	public String documentParse(Document linkPage) throws IOException{		
		String bodyContents = pickHtmlBodyContents(linkPage);
		
		if(bodyContents==null)
			return null;
		
		bodyContents.trim();
		bodyContents = removeNedlessCode(bodyContents, "<!--", ">", new InnerParser());
		System.out.println("--- 코멘트 삭제 완료 ---");
		bodyContents = removeNedlessCode(bodyContents, "<script", "</script>", new InnerParser(){
			@Override
			public int backCutting(String bodyContents, String backString, int frontCuttingPoint){
				int backCuttingPoint = bodyContents.indexOf(backString, frontCuttingPoint);
				return bodyContents.indexOf(">", backCuttingPoint)+1;
			}
		});
		System.out.println("--- script 코드 삭제 완료 ---");
		bodyContents = removeNedlessCode(bodyContents, "<", ">", new InnerParser(){
			@Override
			protected String nextCutting(String bodyContents, String frontString, String backString, int frontCuttingPoint,
					int backCuttingPoint){
				return bodyContents.substring(0, frontCuttingPoint)+bodyContents.substring(backCuttingPoint);
			}
		});
		System.out.println("--- 나머지 태그 삭제 완료 ---");
		return bodyContents;
	}
	
	private String pickHtmlBodyContents(Document linkPage){
		String contents = linkPage.toString();
		int frontCuttingPoint = htmlBodyStartingTag_IndexNo(contents)+1;
		
		if(frontCuttingPoint<=0)
			return null;

		int backCuttingPoint = contents.indexOf("</body>");
		return contents.substring(frontCuttingPoint, backCuttingPoint);
	}
	
	private int htmlBodyStartingTag_IndexNo(String contents){
		int cuttingPoint = contents.indexOf("<body");
		
		if(cuttingPoint<0)
			return -1;
		
		return contents.indexOf(">", cuttingPoint);
	}
	
	private String removeNedlessCode(String bodyContents, String frontString, String backString,
			InnerParser innerParser){
		while(bodyContents.contains(frontString))
			bodyContents = innerParser.removeText(bodyContents, frontString, backString);
		return bodyContents.trim();
	}
}
