package com.atguigu.gulimall.search.web;

import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description TODO
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/14 09:22
 * @Version 1.0
 **/
@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request) {

        String queryString = request.getQueryString();
        param.set_queryString(queryString);

        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result", result);
        return "list";
    }


}
