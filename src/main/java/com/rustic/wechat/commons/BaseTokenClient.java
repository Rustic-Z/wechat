package com.rustic.wechat.commons;

import java.util.Date;

import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.rustic.wechat.dao.BaseTokenDao;
import com.rustic.wechat.entity.BaseToken;
import com.rustic.wechat.util.Constants;

import weixin.popular.api.TokenAPI;
import weixin.popular.bean.token.Token;

/**
 * 定时更新baseToken
 * @author zhangchao
 *
 */
public class BaseTokenClient {
	
	private static Logger logger = Logger.getLogger(BaseTokenClient.class);
	
	@Autowired(required=true)
	private BaseTokenDao baseTokenDao;

	public void setBaseTokenDao(BaseTokenDao baseTokenDao) {
		this.baseTokenDao = baseTokenDao;
	}

	/**
	 * 获取微信公众接口基础调用token
	 */
	@Transactional
	public void updateBaseToken() {
		logger.info("更新token定时器开始---------------------------------------------");
		Token tokenObj = TokenAPI.token(Constants.WX_GZ_APP_ID, Constants.WX_GZ_APP_SECRET);
		logger.info("getToken errorCode-->" + tokenObj.getErrcode() + ", errorMsg-->" + tokenObj.getErrmsg() + ", token-->" + tokenObj.getAccess_token());
		if (null != tokenObj && null != tokenObj.getAccess_token()) {
			BaseToken token = this.baseTokenDao.findUndeletedToken();
			if (null == token) {
				token = new BaseToken(tokenObj.getAccess_token());
			} else {
				token.setToken(tokenObj.getAccess_token());
				token.setUpdateTime(new Date());
			}
			logger.info("token ---> " + tokenObj.getAccess_token() + "---------------------------------------------");
			this.baseTokenDao.saveOrUpdate(token);
			logger.info("更新token定时器结束---------------------------------------------");
		}
	}

}
