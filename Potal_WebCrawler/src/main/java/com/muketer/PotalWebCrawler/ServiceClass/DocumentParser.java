package com.muketer.PotalWebCrawler.ServiceClass;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.muketer.PotalWebCrawler.Searcher.CommonSearcher;
import com.muketer.PotalWebCrawler.ServiceInterface.I_DocumentParser;

@Service
public class DocumentParser implements I_DocumentParser{
	
	private static final Logger logger = LoggerFactory.getLogger(DocumentParser.class);
	
	public DocumentParser(){}
	
	@Override
	public String documentParse(Document linkPage) throws IOException{		
		String bodyContents = pickHtmlBodyContents(linkPage);
		
		if(bodyContents==null) {
			
			// �׽�Ʈ
			logger.info("documentParse / bodyContents==null");
			
			return null;
		}
		
		bodyContents.trim();
		bodyContents = removeNedlessCode(bodyContents, "<!--", ">", new InnerParser());
		
		// �׽�Ʈ
		System.out.println("--- �ڸ�Ʈ ���� �Ϸ�");
		
		bodyContents = removeNedlessCode(bodyContents, "<script", "</script>", new InnerParser(){
			@Override
			public int backCutting(String bodyContents, String backString, int frontCuttingPoint){
				int backCuttingPoint = bodyContents.indexOf(backString, frontCuttingPoint);
				return bodyContents.indexOf(">", backCuttingPoint)+1;
			}
		});
		
		// �׽�Ʈ
		System.out.println("--- script �ڵ� ���� �Ϸ�");
		
		bodyContents = removeNedlessCode(bodyContents, "<", ">", new InnerParser(){
			@Override
			protected String nextCutting(String bodyContents, String frontString, String backString, int frontCuttingPoint,
					int backCuttingPoint){
				return bodyContents.substring(0, frontCuttingPoint)+bodyContents.substring(backCuttingPoint);
			}
		});
		
		// �׽�Ʈ
		System.out.println("--- ������ �±� ���� �Ϸ�");
		
		return bodyContents;
	}
	
	private String pickHtmlBodyContents(Document linkPage){
		String contents = linkPage.toString();
		int frontCuttingPoint = htmlBodyStartingTag_IndexNo(contents)+1;
		
		if(frontCuttingPoint<=0) {
		
			// �׽�Ʈ
			logger.info("pickHtmlBodyContents / frontCuttingPoint<=0");
			
			return null;
		}

		int backCuttingPoint = contents.indexOf("</body>");
		return contents.substring(frontCuttingPoint, backCuttingPoint);
	}
	
	private int htmlBodyStartingTag_IndexNo(String contents){
		int cuttingPoint = contents.indexOf("<body");
		
		if(cuttingPoint<0) {
			
			// �׽�Ʈ
			logger.info("htmlBodyStartingTag_IndexNo / cuttingPoint<0");
			
			return -1;
		}
		
		return contents.indexOf(">", cuttingPoint);
	}
	
	private String removeNedlessCode(String bodyContents, String frontString, String backString,
			InnerParser innerParser){
		while(bodyContents.contains(frontString))
			bodyContents = innerParser.removeText(bodyContents, frontString, backString);
		return bodyContents.trim();
	}
	
	private class InnerParser{
		protected int backCutting(String bodyContents, String backString, int frontCuttingPoint){
			
			// �׽�Ʈ
			logger.info("backCutting");
			
			 return bodyContents.indexOf(backString, frontCuttingPoint)+1;
		}
		
		protected String nextCutting(String bodyContents, String frontString, String backString, int frontCuttingPoint,
				int backCuttingPoint){
			
			// �׽�Ʈ
			logger.info("nextCutting");
			
			int nextCuttingPoint = bodyContents.indexOf("<", backCuttingPoint);
			if(nextCuttingPoint<0)
				return bodyContents.substring(0, frontCuttingPoint)+bodyContents.substring(backCuttingPoint);
			return bodyContents.substring(0, frontCuttingPoint)+bodyContents.substring(nextCuttingPoint);
		}
		
		protected String removeText(String bodyContents, String frontString, String backString){
			
			// �׽�Ʈ
			logger.info("removeText");
			
			int frontCuttingPoint = bodyContents.indexOf(frontString);
			if(frontCuttingPoint<0)
				return bodyContents;
			int	backCuttingPoint = backCutting(bodyContents, backString, frontCuttingPoint);
			return nextCutting(bodyContents, frontString, backString, frontCuttingPoint, backCuttingPoint);
		}
	}
	
}
