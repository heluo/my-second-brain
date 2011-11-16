package org.brain2.test.vneappcrawler;

import static org.junit.Assert.assertEquals;

import org.apache.http.protocol.HTTP;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

public class SeaGameVneParserTest {
	String domain = "http://seagames.vnexpress.net";
	@Test
	public void testExtractIMG(){
		String theLink1 = "/tin/hinh-anh-dep/2011/11/da-tiec-am-thanh-anh-sang-o-le-khai-mac-sea-games-26/";
		String html = HttpClientUtil.executeGet(domain+theLink1);
		Document doc = Jsoup.parse(html, HTTP.UTF_8);
		
		Elements contents = doc.select(".ctdt");

		if (contents.size() > 0) {
			Element content = contents.get(0);
			Article article =new Article();
			article.setId("1");
			article.setSharedURL(theLink1);
			content = SeagameVneParser.removeElement(content);
			SeagameVneParser.extractIMG(content, article);
			for(ReferenceObject obj : article.getRefObj()){
				System.out.println("IMG URL :"+obj.getUrl());
			}
			assertEquals(14, article.getRefObj().size());
		}
	}
//	@Test 
//	public void testProcessLead(){
//		String theLink1 = "/tin/mon-khac/2011/11/doan-viet-nam-gianh-hc-vang-thu-26/";
//		String html = HttpClientUtil.executeGet(domain+theLink1);
//		Document doc = Jsoup.parse(html, HTTP.UTF_8);
//		
//		
//		Elements contents = doc.select(".ctdt");
//		Article article =new Article();
//		article.setId("2");
//		article.setSharedURL(theLink1);
//		if (contents.size() > 0) {
//			Element content = contents.get(0);
//			SeagameVneParser.processLead(content, article);
//			long i=0;
//			for(ReferenceObject obj: article.getRefObj()){
//				if(obj.getType().index()==3){
//					i++;
//					System.out.println("RELATED URL :"+i+"-"+obj.getUrl());
//				}
//			}
//			
//			System.out.println("Abstract :"+article.getAbstractS());
//			assertEquals(2, i);
//		}
//			
//	}
//	@Test
//	public void testGetThumbnail(){
//		String theLink = "/tin/mon-khac/2011/11/doan-viet-nam-gianh-hc-vang-thu-26/";
//		Article article =new Article();
//		article.setId("2");
//		article.setSharedURL(theLink);
//		SeagameVneParser.getThumbnail(domain+theLink,article,".ctdt",130);
//		System.out.println("THUMBNIAL MD5 :"+ article.getThumbnailMD5());
//		assertEquals("/Files/Subject/3b/bb/c3/ac/s-td1.jpg", article.getThumbnailURL());
//		
//	}
//	@Test 
//	public void testGetComment(){
//		/**
//		 * EX :/tin/u23-viet-nam/2011/11/u23-viet-nam-va-co-hoi-mo-toang-cua-vao-ban-ket
//		 */
//		String theLink = "/tin/u23-viet-nam/2011/11/u23-viet-nam-va-co-hoi-mo-toang-cua-vao-ban-ket";
//		String html = HttpClientUtil.executeGet(domain+theLink);
//		Article article =new Article();
//		article.setId("2");
//		article.setSharedURL(theLink);
//		VnExpressDao _vnExpressDao;
//		try {
//			_vnExpressDao = VnExpressDao.getInstance();
//			Document doc = Jsoup.parse(html, HTTP.UTF_8);
//			
//			Elements contents = doc.select(".ctdt");
//
//			if (contents.size() > 0) {
//				Element content = contents.get(0);
//				Elements commentws = content.select(".commentw");
//				if(commentws.size()>0){
//					SeagameVneParser.getComment(commentws.get(0), article, _vnExpressDao, domain+theLink);
//					System.out.println("COMMET SIZE :"+article.getComments().size());
//					for(Comment c: article.getComments()){
//						System.out.println("Tile:"+c.getTitle());
//						System.out.println("Content:"+c.getContent());
//						System.out.println("Author:"+c.getFullname());
//					}
//					assertEquals(8, article.getComments().size());
//				}
//			}
//			_vnExpressDao.closeConnection();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
//	@Test
//	public void testProcessExtraPageLink(){
//		String theLink = "/tin/tieu-diem-trong-ngay/2011/10/giai-ma-vu-khi-bi-mat-cua-phan-thi-ha-thanh/";
//		String html = HttpClientUtil.executeGet(domain+theLink);
//		Document doc = Jsoup.parse(html, HTTP.UTF_8);
//		
//		Elements contents = doc.select(".ctdt");
//
//		if (contents.size() > 0) {
//			Element content = contents.get(0);
//			Article exArticle = new Article();
//			exArticle.setId("1");
//			SeagameVneParser.processExtraPageLink(content,exArticle,".bodypage",".ctdt");
//			for(ReferenceObject obj : exArticle.getRefObj()){
//				System.out.println("URL:"+obj.getUrl());
//			}
//			assertEquals(0, exArticle.getRefObj().size());
//		}
//	}
//	@Test
//	public void testParseHtmlToArticle(){
//		String theLink="/tin/goc-chuyen-gia/2011/11/hlv-falko-goetz-hang-cong-u23-vn-co-van-de";
//		String html = HttpClientUtil.executeGet(domain+theLink);
//		VnExpressDao _vnExpressDao;
//		try {
//			_vnExpressDao = VnExpressDao.getInstance();
//			Article article =new Article();
//			article.setId("1");
//			article.setSharedURL(theLink);
//			SeagameVneParser.parseHtmlToArticle(domain+theLink, html, article, _vnExpressDao);
//			_vnExpressDao.saveArticle(article);
//			System.out.println("Article Content : "+article.getContent());
//			_vnExpressDao.closeConnection();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

}