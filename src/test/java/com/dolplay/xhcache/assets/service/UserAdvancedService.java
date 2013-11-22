package com.dolplay.xhcache.assets.service;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import com.dolplay.xhcache.annotation.Cache;
import com.dolplay.xhcache.annotation.CacheKeySuffix;
import com.dolplay.xhcache.assets.CacheKeyPrefix;
import com.dolplay.xhcache.assets.domain.User;
import com.dolplay.xhcache.assets.lang.Lang;
import com.dolplay.xhcache.type.CacheType;

public class UserAdvancedService {

	/**
	 * 根据指定性别查询用户id列表 缓存类型为有序集
	 * 
	 * @param gender
	 * @return
	 */
	@Cache(cacheKeyPrefix = CacheKeyPrefix.TEST_CACHE_ALLUSERS_IDLIST, cacheType = CacheType.zset)
	public List<Integer> listIdByGender(@CacheKeySuffix String gender) {
		return Lang.list(11, 13, 22, 25, 26, 41);
	}

	/**
	 * 根据指定性别查询用户列表 缓存类型为有序集
	 * 
	 * @param gender
	 * @return
	 */
	@Cache(cacheKeyPrefix = CacheKeyPrefix.TEST_CACHE_ALLUSERS_LIST, cacheType = CacheType.zset)
	public List<User> listByGender(@CacheKeySuffix String gender) {
		User u = new User();
		u.setId(1L);
		u.setName("jack");
		u.setGender(gender);
		u.setBirthday(new Date(System.currentTimeMillis()));
		u.setDescription("test...");
		User u2 = new User();
		u2.setId(1L);
		u2.setName("tom");
		u2.setGender(gender);
		u2.setBirthday(new Date(System.currentTimeMillis()));
		u2.setDescription("test11...");
		return Lang.list(u, u2);
	}

	/**
	 * 查询08年以后出生的用户name列表
	 * 
	 * @return
	 * @throws ParseException
	 */
	@Cache(cacheKeyPrefix = CacheKeyPrefix.TEST_CACHE_NEWUSERS_NAMELIST, cacheType = CacheType.zset, reverse = true)
	public List<String> listNewUsers() throws ParseException {
		return Lang.list("kate", "jo", "tom", "john");
	}
}
