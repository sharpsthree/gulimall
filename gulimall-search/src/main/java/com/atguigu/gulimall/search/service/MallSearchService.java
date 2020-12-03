package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;

public interface MallSearchService {

    /**
     * 去es检索需要的信息
     * @param param 检索的所有参数
     * @return 返回检索的结果,页面需要显示的所以数据
     */
    SearchResult search(SearchParam param);
}
