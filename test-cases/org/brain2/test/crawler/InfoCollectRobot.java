package org.brain2.test.crawler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.http.protocol.HTTP;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.brain2.ws.core.search.MetaDataUtil;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.l3s.boilerpipe.extractors.DefaultExtractor;

public class InfoCollectRobot {

	private final String root;
	private String seedUrl;
	
	private final Map<String, Boolean> vertices;
	private final Map<String, Set<String>> clustersByURI;
	private Queue<String> urlQueue;
	private BufferedWriter crawlStatistics = null;

	private final IndexWriter indexWriter;

	private static final String DISALLOW = "Disallow:";
	private int maxNumberUrls;
	private String urlRuleShouldNotMatch = "";
	private String urlRuleShouldMatch = "";
	private String mainContentNodeId = "";
	private String mainNavigationNodeId = "";
	private volatile boolean forceExit = false;

	public InfoCollectRobot(String root, int maxNumberUrls) {
		this.root = root;
		this.maxNumberUrls = maxNumberUrls;
		vertices = new HashMap<String, Boolean>(this.maxNumberUrls);
		clustersByURI = new HashMap<String, Set<String>>(100);
		clustersByURI.put("unclassified", new HashSet<String>());
		try {
			indexWriter = MetaDataUtil.getIndexWriter();
			crawlStatistics = new BufferedWriter(new FileWriter("crawlStatistics.txt"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
	
	private void closeOutputStream()  {
		try {
			crawlStatistics.append("\n ### urlQueue.size()= "+ urlQueue.size() + " ### \n");
			crawlStatistics.flush();
			crawlStatistics.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * Visit a link in Info Graph
	 * 
	 * @param link
	 * @return String html
	 */
	protected String visitingLink(String link) {
		vertices.put(link, true);
		return HttpClientUtil.executeGet(link);
	}

	/**
	 * Seed all href in page
	 * 
	 * @param theLink
	 */
	protected void seedLinks(final String theLink) {
		final String html = visitingLink(theLink);
		// System.out.println(html);

		System.out.println();
		final Document doc = Jsoup.parse(html, HTTP.UTF_8);
		final String baseURL = "http://" + this.root;
		doc.setBaseUri(baseURL);

		final Elements linkNodes;
		if (mainNavigationNodeId.isEmpty()) {
			linkNodes = doc.select("a[href]");
		} else {
			linkNodes = doc.select(mainNavigationNodeId).select("a[href]");
		}

		for (Element linkNode : linkNodes) {
			String href = linkNode.attr("href");
			if (isValidLink(href)) {
				if (!href.startsWith("http")) {
					href = baseURL + href;
				}
				System.out.print("Found link: " + href);
				if (shouldAddToQueue(href)) {					
					if (!vertices.containsKey(href)) {
						vertices.put(href, false);
						urlQueue.add(href);
						System.out.println(" , PUSH to queue ");
						try {
							crawlStatistics.append(href+"\n");
							crawlStatistics.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						System.out.println(" , SKIP");
					}
				} else {
					System.out.println(" not matched rules");
				}
			}
		}

		Runnable thread = new Runnable() {

			@Override
			public void run() {
				System.out.println("\n ==> theLink: " + theLink);
				
				String theGroup = theLink.replace(seedUrl, "").split("/")[0];
				System.out.println(" ==> theGroup: " + theGroup);
				Set<String> theSet = clustersByURI.get(theGroup);
				if(theSet != null){
					theSet.add(theLink);
					System.out.print(" with size: " + theSet.size() + " \n\n ");
				}

				Elements metas = doc.select("meta");
				String descriptionTxt = "";
				String keywordsTxt = "";
				String robotsTxt = "";

				for (Element meta : metas) {
					String metaName = meta.attr("name");
					String metaContent = meta.attr("content");
					// System.out.println("meta name: " + metaName);
					if (metaName.equals("description")) {
						descriptionTxt = metaContent;
						System.out.println("descriptionTxt: " + descriptionTxt);
					} else if (metaName.equals("keywords")) {
						keywordsTxt = metaContent;
						// System.out.println("descriptionTxt: " + keywordsTxt);
					} else if (metaName.equals("robots")) {
						robotsTxt = metaContent;
						// System.out.println("robotsTxt: " + robotsTxt);
					}
				}

				Elements title = doc.select("title");
				String titleTxt = "";
				if (title.size() > 0) {
					titleTxt = title.get(0).text();
					System.out.println("titleTxt: " + titleTxt);
				}

				try {
					System.out.println(" BEGIN #####################");
					Elements contentNode;
					if (mainContentNodeId.isEmpty()) {
						contentNode = doc.select("body");						
					} else {						
						contentNode = doc.select(mainContentNodeId);
					}
					
					//System.out.println(" HTML: "+contentNode.html());
					
					//get images in content
					Elements imgs = contentNode.select("img[src]");
					for (Element img : imgs) {
						String src = img.attr("src");
						//FIXME
						if(src.startsWith("/Files/Subject/")){
							System.out.println(" $$$ img[src] = " + baseURL + src);
						}
					}

					String content = DefaultExtractor.INSTANCE.getText(contentNode.html());
					System.out.println(content);
//					org.apache.lucene.document.Document newDoc = MetaDataUtil
//							.createDocumentForLink(theLink, titleTxt,
//									descriptionTxt, keywordsTxt, content);
//					indexWriter.addDocument(newDoc);
//					indexWriter.commit();
					System.out.println(" END #####################");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				forceExit = true;
				
			}
		};
		new Thread(thread).start();
	}

	protected boolean shouldAddToQueue(String link) {
		boolean rs = false;
		try {
			URL url = new URL(link);
			rs = url.getHost().contains(root);
			if( ! urlRuleShouldNotMatch.isEmpty() ){
				rs = rs && !(link.matches(urlRuleShouldNotMatch));
				//rs = rs && ! link.contains(urlRuleShouldNotMatch);
			}
			if( ! urlRuleShouldMatch.isEmpty() ){
				rs = rs && (link.matches(urlRuleShouldMatch));
				//rs = rs && link.contains(urlRuleShouldMatch);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}

	protected boolean isValidLink(String s) {
		if (s.matches("javascript:.*|mailto:.*|#.*")) {
			return false;
		}
		return true;
	}
	
	

	public void startCrawling(String seedUrl) throws InterruptedException,
			CorruptIndexException, IOException {
		urlQueue = new LinkedList<String>();
		this.seedUrl = seedUrl;
		this.seedLinks(seedUrl);
		while (!urlQueue.isEmpty() && urlQueue.size() < this.maxNumberUrls) {
			System.out.println("## urlQueue size : " + urlQueue.size());
			String link = urlQueue.remove();
			System.out.println("enqueue a link: " + link);
			if (!this.vertices.get(link)) {
				this.seedLinks(link);
				Thread.sleep(450);
			} else {
				System.out.println("## visited, skip : " + link);
			}
			if(forceExit){
				break;
			}
		}
		indexWriter.optimize();
		closeOutputStream();
	}

	public int getMaxNumberUrls() {
		return maxNumberUrls;
	}

	public void setMaxNumberUrls(int maxNumberUrls) {
		this.maxNumberUrls = maxNumberUrls;
	}

	public String getUrlRuleShouldNotMatch() {
		return urlRuleShouldNotMatch;
	}

	public void setUrlRuleShouldNotMatch(String urlRuleShouldNotMatch) {
		this.urlRuleShouldNotMatch = urlRuleShouldNotMatch;
	}

	public String getUrlRuleShouldMatch() {
		return urlRuleShouldMatch;
	}

	public void setUrlRuleShouldMatch(String urlRuleShouldMatch) {
		this.urlRuleShouldMatch = urlRuleShouldMatch;
	}

	public String getMainContentNodeId() {
		return mainContentNodeId;
	}

	public void setMainContentNodeId(String mainContentNodeId) {
		this.mainContentNodeId = mainContentNodeId;
	}

	public String getMainNavigationNodeId() {
		return mainNavigationNodeId;
	}

	public void setMainNavigationNodeId(String mainNavigationNodeId) {
		this.mainNavigationNodeId = mainNavigationNodeId;
	}

	public String getRoot() {
		return root;
	}		

	public Map<String, Set<String>> getClustersByURI() {
		return clustersByURI;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		long start = System.nanoTime();
		
		test_tantrieuf31_blogspot_com();

		long end = System.nanoTime();
		long miliseconds = (end - start) / 10000000;
		System.out.println(" \n === Test done === in miliseconds:" + miliseconds);
	}

	protected static void test_tantrieuf31_blogspot_com() throws Exception {
		int maxQueueSize = 10000;
		String domain = "tantrieuf31.blogspot.com";
		InfoCollectRobot robot = new InfoCollectRobot(domain, maxQueueSize);
		robot.setMainNavigationNodeId("#BlogArchive1_ArchiveList");
		robot.setUrlRuleShouldMatch(".*.html");
		robot.setUrlRuleShouldNotMatch(".*_archive.html");
		robot.startCrawling("http://tantrieuf31.blogspot.com/");
	}

	protected static void test_vnexpress_net() throws Exception {
		int maxQueueSize = 1000;
		String domain = "vnexpress.net";
		InfoCollectRobot robot = new InfoCollectRobot(domain, maxQueueSize);
		
		robot.setMainContentNodeId("#content .content-center");
		//robot.setUrlRuleShouldMatch("http://vnexpress.net/*");
		robot.getClustersByURI().put("san-pham-moi", new HashSet<String>());
		robot.getClustersByURI().put("kinh-nghiem", new HashSet<String>());
		robot.getClustersByURI().put("giai-tri", new HashSet<String>());
		robot.getClustersByURI().put("hoi-dap", new HashSet<String>());		
		
		robot.startCrawling("http://vnexpress.net/");
	}
	
	protected static void test_asmarterplanet_com() throws Exception {
		int maxQueueSize = 10000;
		String domain = "asmarterplanet.com";
		InfoCollectRobot robot = new InfoCollectRobot(domain, maxQueueSize);
		robot.setMainContentNodeId("#content");
		robot.setUrlRuleShouldMatch("http://asmarterplanet.com/blog/2011/10/.*");
		robot.startCrawling("http://asmarterplanet.com/blog/2011/10/smarter-silhouettes-a-curriculum-of-analytics.html");
	}
	
	protected static void test_nhipsongso_tuoitre_vn() throws Exception {
		int maxQueueSize = 10000;
		String domain = "nhipsongso.tuoitre.vn";
		InfoCollectRobot robot = new InfoCollectRobot(domain, maxQueueSize);				
		robot.startCrawling("http://nhipsongso.tuoitre.vn/");
	}
	
	protected static void test_video() throws Exception {
		int maxQueueSize = 10000;
		String domain = "vnexpress.net";
		InfoCollectRobot robot = new InfoCollectRobot(domain, maxQueueSize);		
		robot.setUrlRuleShouldMatch("http://vnexpress.net/video/.*");
		robot.startCrawling("http://vnexpress.net/video/the-gioi-tuong-lai-trong-tri-tuong-tuong-cua-microsoft/8/59647/");
	}
	
	protected static void test_infoq_com() throws Exception {
		int maxQueueSize = 10000;
		String domain = "infoq.com";
		InfoCollectRobot robot = new InfoCollectRobot(domain, maxQueueSize);		
		robot.setUrlRuleShouldNotMatch("http://infoq.com/cn/.* & http://infoq.com/.*jsessionid=.*");		
		robot.startCrawling("http://www.infoq.com");
	}
	
	protected static void test_24h_com() throws Exception {
		int maxQueueSize = 10000;
		String domain = "24h.com.vn";
		InfoCollectRobot robot = new InfoCollectRobot(domain, maxQueueSize);		
		robot.setUrlRuleShouldMatch(".*/video-phim-dac-sac/.*");		
		robot.startCrawling("http://www.24h.com.vn/");
		
		boolean test = "http://game.24h.com.vn/tinh-diem/dao-vang-ii-c145g624b16.html".matches("http://hcm.24h.com.vn/video-phim-dac-sac/.*");
		System.out.println("test match: "+test);
		
		boolean test2 = "http://hcm.24h.com.vn/video-phim-dac-sac/video-phim-mr-bean-quay-o-thu-vien-c458v413458.js".matches("http://hcm.24h.com.vn/video-phim-dac-sac/.*.js");
		System.out.println("test2 match: "+test2);
		
	}
	
	
	
	
	

}
