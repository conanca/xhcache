package com.dolplay.xhcache;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dolplay.xhcache.aspect.AdvancedCacheInterceptorTest;
import com.dolplay.xhcache.aspect.CacheInterceptorTest;
import com.dolplay.xhcache.aspect.ReturnEmptyTest;
import com.dolplay.xhcache.dao.AdvancedCacheDaoTest;
import com.dolplay.xhcache.dao.CacheDaoTest;
import com.dolplay.xhcache.lang.CStringsTest;

@RunWith(Suite.class)
@SuiteClasses({ CacheDaoTest.class, AdvancedCacheDaoTest.class,
		ReturnEmptyTest.class, CacheInterceptorTest.class,
		AdvancedCacheInterceptorTest.class, CStringsTest.class })
public class AllTests {

}
