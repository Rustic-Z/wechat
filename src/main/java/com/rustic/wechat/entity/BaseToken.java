package com.rustic.wechat.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import weixin.popular.bean.token.Token;

import com.rustic.wechat.util.Constants;

@Entity
@Table(name = "base_token", catalog = "wechat")
public class BaseToken {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Integer id;
	
	/**
	 * 微信公众平台接口调用token
	 */
	@Column(name = "token")
	private String token;
	
	/**
	 * 最后更新时间
	 */
	@Column(name = "update_time")
	private Date updateTime;
	
	@Column(name = "deleted")
	private Integer deleted;
	
	public BaseToken() {
		super();
	}
	
	public BaseToken(String token) {
		super();
		this.token = token;
		this.updateTime = new Date();
		this.deleted = Constants.DELETED_NO;
	}
	
	public BaseToken(Token token) {
		super();
		this.token = token.getAccess_token();
		this.updateTime = new Date();
		this.deleted = Constants.DELETED_NO;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Integer getDeleted() {
		return deleted;
	}

	public void setDeleted(Integer deleted) {
		this.deleted = deleted;
	}

}
