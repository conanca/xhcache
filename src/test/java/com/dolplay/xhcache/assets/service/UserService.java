package com.dolplay.xhcache.assets.service;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dolplay.xhcache.annotation.Cache;
import com.dolplay.xhcache.annotation.CacheKeySuffix;
import com.dolplay.xhcache.assets.CacheKeyPrefix;
import com.dolplay.xhcache.assets.domain.User;
import com.dolplay.xhcache.assets.lang.Lang;
import com.dolplay.xhcache.assets.lang.Pager;

public class UserService {

	/**
	 * 查询参考用户
	 * 
	 * @return
	 */
	@Cache(cacheKeyPrefix = CacheKeyPrefix.TEST_CACHE_REFERENCEUSER)
	public User viewReferenceUser() {
		User u = new User();
		u.setId(1L);
		u.setName("jack");
		u.setGender("male");
		u.setBirthday(new Date(System.currentTimeMillis()));
		u.setDescription("test...");
		return u;
	}

	/**
	 * 查询参考用户的名称
	 * 
	 * @return
	 */
	@Cache(cacheKeyPrefix = CacheKeyPrefix.TEST_CACHE_REFERENCEUSERNAME)
	public String viewReferenceUserName() {
		return "jack";
	}

	/**
	 * 查询参考用户的名称
	 * 
	 * @return
	 */
	@Cache(cacheKeyPrefix = CacheKeyPrefix.TEST_CACHE_REFERENCEUSERID)
	public long viewReferenceUserId() {
		return 1L;
	}

	/**
	 * 根据id查询用户
	 * 
	 * @param id
	 * @return
	 */
	@Cache(cacheKeyPrefix = CacheKeyPrefix.TEST_CACHE_USER)
	public User view(@CacheKeySuffix int id) {
		User u = new User();
		u.setId((long) id);
		u.setName("jack");
		u.setGender("male");
		u.setBirthday(new Date(System.currentTimeMillis()));
		u.setDescription("test...");
		return u;
	}

	/**
	 * 分页查询全部用户列表
	 * 
	 * @param pager
	 * @return
	 */
	@Cache(cacheKeyPrefix = CacheKeyPrefix.TEST_CACHE_ALLUSERS_INPAGE)
	public List<User> listInPage(@CacheKeySuffix Pager pager) {
		User u = new User();
		u.setId((long) 3);
		u.setName("jhon");
		u.setGender("male");
		u.setBirthday(new Date(System.currentTimeMillis()));
		u.setDescription("test11...");
		User u1 = new User();
		u1.setId((long) 4);
		u1.setName("tom");
		u1.setGender("male");
		u1.setBirthday(new Date(System.currentTimeMillis()));
		u1.setDescription("test112...");
		return Lang.list(u, u1);
	}

	/**
	 * 统计用户数
	 * 
	 * @return
	 */
	@Cache(cacheKeyPrefix = CacheKeyPrefix.TEST_CACHE_COUNTUSER, cacheTimeout = 10)
	public int countUser() {
		return 10;
	}

	/**
	 * 查询全部用户id列表
	 * 
	 * @return
	 */
	@Cache(cacheKeyPrefix = CacheKeyPrefix.TEST_CACHE_USERIDS)
	public Set<Integer> userIds() {
		return Lang.set(1, 2, 6, 8, 9, 16);
	}

	/**
	 * 查询全部用户id列表
	 * 
	 * @return
	 */
	@Cache(cacheKeyPrefix = CacheKeyPrefix.TEST_CACHE_TOP5)
	public List<Integer> userTop5() {
		return Lang.list(7, 1, 3, 5, 9);
	}

}
