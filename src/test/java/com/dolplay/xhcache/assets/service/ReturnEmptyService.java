package com.dolplay.xhcache.assets.service;

import java.util.ArrayList;
import java.util.List;

import com.dolplay.xhcache.annotation.Cache;
import com.dolplay.xhcache.assets.domain.User;
import com.dolplay.xhcache.type.CacheType;

public class ReturnEmptyService {

	@Cache(cacheKeyPrefix = "xobj", cacheTimeout = -1)
	public User viewXobj() {
		System.out.println("try to fetch date to get a object...");
		System.out.println("ops,no date!");
		return null;
	}

	@Cache(cacheKeyPrefix = "xinteger", cacheTimeout = -1)
	public Integer viewXinteger() {
		System.out.println("try to fetch date to get a object...");
		System.out.println("ops,no date!");
		return null;
	}

	@Cache(cacheKeyPrefix = "xlist", cacheType = CacheType.zset, cacheTimeout = -1)
	public List<String> xlist() {
		System.out.println("try to fetch date to get a list...");
		System.out.println("ops,no date!");
		return new ArrayList<String>();
	}

	@Cache(cacheKeyPrefix = "xlist2", cacheType = CacheType.zset, cacheTimeout = -1)
	public List<String> xlist2() {
		System.out.println("(***)try to fetch date to get a list...");
		System.out.println("ops,no date!");
		return null;
	}
}
