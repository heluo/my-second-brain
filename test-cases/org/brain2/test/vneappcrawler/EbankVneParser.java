package org.brain2.test.vneappcrawler;

import org.apache.http.protocol.HTTP;
import org.brain2.ws.core.utils.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

public class EbankVneParser extends MainParser{
	
	public Article parseHtmlToArticle(String theLink, String html,
			Article article, VnExpressDao _vnExpressDao) throws Exception {
		final Document doc = Jsoup.parse(html, HTTP.UTF_8);

		Elements contents = doc.select(".content");

		if (contents.size() > 0) {
			Element content = contents.get(0);

			Elements cpms_content = content.select("div[cpms_content=true]");

			if (cpms_content.size() > 0) {
				Element cpms = cpms_content.get(0);
				/**
				 * remove script links in content
				 */
				cpms.select("script").remove();
				cpms.select("#flashContent").remove();
				/**
				 * split related link from lead
				 */
				
				processLead(cpms,article,"h2.Lead");

				/**
				 * remove title and lead from content
				 */
				cpms.select(".Title").remove();
				cpms.select(".Lead").remove();

				/**
				 * Extract content
				 */
				
				/**
				 * remove Danh Gia IMGS
				 */
				
				Elements pointImg = cpms.select(".Point");
				if(pointImg.size()>0)
					pointImg.remove();
				
				/**
				 * Process page_2.asp and page_1.asp is a link in content
				 * Get Image from page_2.asp ,remove * Clip + video link
				 */
				Article exArticle = new Article();
				exArticle.setId(article.getId());
				processExtraPageLink(cpms,exArticle,".content","div[cpms_content=true]");
				
				/**
				 * Images
				 */
				
				extractIMG(cpms,article);
				article.addAll(exArticle.getRefObj());
				exArticle=null;
				/**
				 * Remove ContactUs voi link Tai Day
				 * a[href$=/ContactUs/?id=cuoi@vnexpress.net],
				 * /ContactUs/?id=cuoi@vnexpress.net
				 */
				processContact(cpms,"/contactus/?id=");
				/**
				 * Remove
				 * mailto:vitinh@vnexpress.net
				 */
				processContact(cpms,"mailto:");
				
				
				/**
				 * Detect "Theo dong su kien"
				 */
				processTDSK(cpms,article);
				
				
				cpms.select("a[class!=Normal]").remove();

				/**
				 * process page_1.asp
				 * Get thumbnail 
				 * TODO : case : page_2.asp luu thumnail
				 */
				getThumbnail(theLink,article,"div[cpms_content=true]",130,100);
				
				/**
				 * Remove all , just get <p>
				 */
				for (Element p : cpms.select("p")) {
					p.html(Jsoup.parse(p.html()).text());
				}
				Whitelist whiteList = new Whitelist();
				whiteList.addTags("p");
				String newContent = Jsoup.clean(cpms.html(), whiteList);
				article.setContent(newContent);
				
				/**
				 * Comment
				 */
				Elements boxItems = content.select(".bgComment");
				if(boxItems.size()>0)
					getComment(boxItems.get(0),article,_vnExpressDao,theLink);
				
			} else {
				Log.println("NO CMPS " + theLink);
			}
		}
		return article;
	}
}
