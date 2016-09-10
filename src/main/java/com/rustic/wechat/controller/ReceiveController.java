package com.rustic.wechat.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;
import com.rustic.wechat.util.Constants;

import weixin.popular.bean.message.EventMessage;
import weixin.popular.bean.xmlmessage.XMLMessage;
import weixin.popular.bean.xmlmessage.XMLNewsMessage;
import weixin.popular.bean.xmlmessage.XMLNewsMessage.Article;
import weixin.popular.bean.xmlmessage.XMLTextMessage;
import weixin.popular.client.HttpClientFactory;
import weixin.popular.util.SignatureUtil;
import weixin.popular.util.XMLConverUtil;

/**
 * 微信订阅号消息接受类
 * @author zhangchao
 */
@Controller
@RequestMapping("receive")
public class ReceiveController {
	
	private static Logger logger = Logger.getLogger(ReceiveController.class);
	
//	@Autowired(required=true)
//	private BaseTokenDao baseTokenDao;
//
//	public void setBaseTokenDao(BaseTokenDao baseTokenDao) {
//		this.baseTokenDao = baseTokenDao;
//	}
	
	/**
	 * 用于监听微信后台返回的事件信息
	 */
	@RequestMapping(value="/listen")
	public void listener(HttpServletRequest request ,HttpServletResponse response) {
		try {
			ServletInputStream inputStream = request.getInputStream();
			ServletOutputStream outputStream = response.getOutputStream();
			//获取接口调用token
//			BaseToken baseToken = this.baseTokenDao.findUndeletedToken();
			//获取微信校验参数
			String signature = request.getParameter("signature");
			String timestamp = request.getParameter("timestamp");
			String nonce = request.getParameter("nonce");
			String echostr = request.getParameter("echostr");
			//首次校验服务器
			if (null != echostr) {
				this.outputStreamWrite(outputStream, echostr);
				return;
			}
			//验证请求签名
			if (!SignatureUtil.generateEventMessageSignature(Constants.WX_BY_GZ_SERVER_TOKEN, timestamp, nonce).equals(signature)) {
				this.outputStreamWrite(outputStream, "The request signature is invalid");
				System.out.println("The request signature is invalid");
	            return;
			}
			if (null != inputStream) {
				EventMessage eventMessage = XMLConverUtil.convertToObject(EventMessage.class,inputStream);
				if (null == eventMessage) {
					this.outputStreamWrite(outputStream, "xml convert error!");
					System.out.println("xml convert error!");
		            return;
				}
				//创建回复
				XMLMessage xmlMessage = null;
				if ("event".equals(eventMessage.getMsgType())) {
					if (!"LOCATION".equals(eventMessage.getEvent()) && !"unsubscribe".equals(eventMessage.getEvent())) {
						logger.info("user's event-->" + eventMessage.getEvent());
						if ("subscribe".equals(eventMessage.getEvent())) {
							xmlMessage = new XMLTextMessage(
									eventMessage.getFromUserName(), 
									eventMessage.getToUserName(), 
									"盖茨比深切地体会到财富是怎样令青春和神秘永远常驻，体会到一身身华服如何让人保持青春靓丽，体会到黛西像白银一样闪亮耀眼，在穷人激烈的生存斗争之上，安然而高傲地活着…\n"
									+ "--From 《了不起的盖茨比》");
						} else {
							//未知事件
							xmlMessage = new XMLTextMessage(
									eventMessage.getFromUserName(), 
									eventMessage.getToUserName(), 
									"盖茨比深切地体会到财富是怎样令青春和神秘永远常驻，体会到一身身华服如何让人保持青春靓丽，体会到黛西像白银一样闪亮耀眼，在穷人激烈的生存斗争之上，安然而高傲地活着…\n"
									+ "--From 《了不起的盖茨比》");
						}
					}
				} else if ("text".equals(eventMessage.getMsgType())) {
					logger.info("user's content-->" + eventMessage.getContent());
					if (null == eventMessage.getContent()) {
						xmlMessage = new XMLTextMessage(
								eventMessage.getFromUserName(), 
								eventMessage.getToUserName(), 
								"这是我的<a href='http://blog.rustic.pub'>小站</a>，欢迎交流~\n"
								+ "你是我的一束光");
					} else {
						if (eventMessage.getContent().contains("张超")) {
							xmlMessage = new XMLTextMessage(
									eventMessage.getFromUserName(), 
									eventMessage.getToUserName(), 
									"你知道我的名字？报上你的大名，我会为您准备一份很寒酸的小礼物/微笑");
						} else if (eventMessage.getContent().contains("绿水")) {
							Article article = new Article();
							article.setTitle("我爱绿水");
							article.setDescription("我给自己取了一个这样的字符ID（Rustic），欢迎交流～");
							article.setPicurl("https://mmbiz.qlogo.cn/mmbiz/fSZWEN0brSqGHZia080ugaRhVNTM6GyTgwbiaeqrOoWISzVLgGht70dliaGYNZ3Kzo7OYAia5moMJZassUwt0GBbjA/0?wx_fmt=jpeg");
							article.setUrl("http://mp.weixin.qq.com/s?__biz=MzI2NjM4MDg2Mw==&mid=2247483651&idx=1&sn=48d1eca9c65105d4e458dc3fc1f1df9e&scene=0#wechat_redirect");
							List<Article> articles = new ArrayList<Article>();
							articles.add(article);
							xmlMessage = new XMLNewsMessage(
									eventMessage.getFromUserName(),
									eventMessage.getToUserName(),
									articles);
						} else if (eventMessage.getContent().contains("交流")) {
							xmlMessage = new XMLTextMessage(
									eventMessage.getFromUserName(), 
									eventMessage.getToUserName(), 
									"这是我的<a href='http://blog.rustic.pub'>小站</a>，欢迎交流~\n"
									+ "你是我的一束光");
						} else {
							String resChat = this.chatBot(eventMessage.getContent());
							xmlMessage = new XMLTextMessage(
									eventMessage.getFromUserName(),
									eventMessage.getToUserName(),
									null == resChat ? 
									"这是我的<a href='http://blog.rustic.pub'>小站</a>，欢迎交流~\n"
									+ "你是我的一束光" : resChat);
						}
					}
				}
				logger.info("wechat's response-->" + JSONObject.toJSONString(xmlMessage));
				xmlMessage.outputStreamWrite(outputStream);
			}
			this.outputStreamWrite(outputStream,"");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 微信客户端数据流输出
	 * @return
	 */
	private boolean outputStreamWrite(OutputStream outputStream,String text) {
		try {
			outputStream.write(text.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 获取聊天机器人的响应回复
	 * 关于聊天机器人的具体实现可以参考
	 * http://blog.just4fun.site/create-wechat-bot.html
	 * @return
	 */
	private String chatBot(String chatStr) {
		logger.info("per replace chatStr-->:" + chatStr);
		//将双引号替换成单引号
		chatStr = chatStr.replace("\"", "'");
		HttpUriRequest httpUriRequest = RequestBuilder.get()
				.setUri("http://127.0.0.1:8000/get_response?user_input=\"" + chatStr + "\"")
				.build();
		logger.info("request url -->:" + httpUriRequest.getURI());
		CloseableHttpClient httpClient = HttpClientFactory.createHttpClient(100, 10, 5000, 2);
		try {
			CloseableHttpResponse response = httpClient.execute(httpUriRequest);
			int responseCode = response.getStatusLine().getStatusCode();
			logger.info("chatBot response's code -->:" + responseCode);
			if (200 != responseCode) {
				return null;
			}
			//获取响应实体
			HttpEntity entity = response.getEntity();
			if (null == entity) {
				return null;
			}
			JSONObject obj = JSONObject.parseObject(EntityUtils.toString(entity));
			if (null == obj) {
				return null;
			}
			httpClient.close();
			return obj.getString("response");
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] arg) {
		HttpUriRequest httpUriRequest = RequestBuilder.get()
				.setUri("http://54.149.48.112:8000/get_response?user_input=复杂优于晦涩")
				.build();
		CloseableHttpClient httpClient = HttpClientFactory.createHttpClient(100, 10, 5000, 2);
		try {
			CloseableHttpResponse response = httpClient.execute(httpUriRequest);
			int responseCode = response.getStatusLine().getStatusCode();
			System.out.println("response code -->:" + responseCode);
			if (200 == responseCode) {
				HttpEntity entity = response.getEntity();
				if (null != entity) {
					String content = EntityUtils.toString(entity);
					System.out.println(content);
					JSONObject obj = JSONObject.parseObject(content);
					String resStr = obj.getString("response");
					System.out.println(resStr);
					httpClient.close();
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
