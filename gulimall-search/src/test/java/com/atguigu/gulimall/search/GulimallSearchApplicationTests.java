package com.atguigu.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

	@Autowired
	private RestHighLevelClient client;


	/**
	 * 测试给es索引数据
	 */
	@Test
	public void searchData() throws IOException {

		// 创建检索请求
		SearchRequest searchRequest = new SearchRequest();
		// 指定索引
		searchRequest.indices("bank");
		// 构造查询条件
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

//		searchSourceBuilder.query();
//		searchSourceBuilder.from();
//		searchSourceBuilder.size();
//		searchSourceBuilder.aggregation();

		searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));

		TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
		searchSourceBuilder.aggregation(ageAgg);

		AvgAggregationBuilder avgAgg = AggregationBuilders.avg("avgAgg").field("balance");
		searchSourceBuilder.aggregation(avgAgg);

		System.out.println("检索条件：" +searchSourceBuilder.toString());

		searchRequest.source(searchSourceBuilder);

		// 执行检索
		SearchResponse searchResponse = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

		// 分析结果
		System.out.println(searchResponse.toString());

//		Map map = JSON.parseObject(searchRequest.toString(), Map.class);

		SearchHits hits = searchResponse.getHits();

		SearchHit[] searchHits = hits.getHits();

		for (SearchHit searchHit : searchHits) {

			String asString = searchHit.getSourceAsString();
			Accout accout = JSON.parseObject(asString, Accout.class);
			System.out.println("当前检索到的数据信息：" + accout);
		}

		// 获取聚合的分析信息
		Aggregations aggregations = searchResponse.getAggregations();

		Terms terms = aggregations.get("ageAgg");

		for (Terms.Bucket bucket : terms.getBuckets()) {

			String keyAsString = bucket.getKeyAsString();
			System.out.println("年龄" + keyAsString);
		}

		Avg avg = aggregations.get("avgAgg");
		System.out.println("平均薪资" + avg.getValue());

//		for (Aggregation aggregation : aggregations.asList()) {
//			System.out.println("当前聚合的名字：" + aggregation.getName());
//
//		}



	}

	@Data
	@ToString
	static class Accout {

		private int account_number;
		private int balance;
		private String firstname;
		private String lastname;
		private int age;
		private String gender;
		private String address;
		private String employer;
		private String email;
		private String city;
		private String state;


	}

	/**
	 * 测试给es索引数据
	 */
	@Test
	public void indexData() throws IOException {

		IndexRequest indexRequest = new IndexRequest("users");
		indexRequest.id("1");
//		indexRequest.source("username", "lubancantfly","age",18,"gender","M");

		User user = new User();
		user.setUsername("鲁班不会飞");
		user.setGender("M");
		user.setAge(18);
		String json = JSON.toJSONString(user);

		indexRequest.source(json, XContentType.JSON);

		// 执行操作
		IndexResponse index = client.index(indexRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);


		System.out.println(index);
	}

	@Data
	class User{
		private String username;
		private String gender;
		private Integer age;
	}

	@Test
	public void contextLoads() {

		System.out.println(client);
	}



}
