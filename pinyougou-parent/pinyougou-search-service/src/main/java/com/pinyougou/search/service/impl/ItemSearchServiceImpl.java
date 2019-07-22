package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;

import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;


@Service
public class ItemSearchServiceImpl implements ItemSearchService {
	@Autowired
	private SolrTemplate solrTemplate;
	@Override
	public Map search(Map searchMap) {
		Map map=new HashMap();
		//空格处理
		String keywords = (String) searchMap.get("keywords");
		searchMap.put("keywords",keywords.replace(" ",""));//关键字去掉
		
		//1.查询列表
		map.putAll(searchList(searchMap));//把括号里的map集合追加到调用者里
		//2.分组查询 商品分类列表
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);
		//3.查询品牌和规格列表
		String category=(String)searchMap.get("category");
		if(!"".equals(category)){//如果有分类名称
			map.putAll(searchBrandAndSpecList(category));
			}else{//如果没有分类名称，按照第一个查询
				if(categoryList.size()>0){
					map.putAll(searchBrandAndSpecList(categoryList.get(0)));
				}
			}
		return map;
	}



	//查询列表
	private Map searchList(Map searchMap) {
		Map map=new HashMap();
		//高亮选项初始化
		HighlightQuery query = new SimpleHighlightQuery();//构建高亮选项对象
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");//高亮域
		highlightOptions.setSimplePrefix("<em style='color:red'>");//前缀
		highlightOptions.setSimplePostfix("</em>");
		query.setHighlightOptions(highlightOptions);//为查询对象设置高亮选项

		//1.1关键字查询
		Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
		//1.2按照商品分类过滤
		if (!"".equals(searchMap.get("category"))) {//如果用户选择了分类
			FilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filtercriteria = new Criteria("item_category").is(searchMap.get("category"));
			filterQuery.addCriteria(filtercriteria);
			query.addFilterQuery(filterQuery);
		}
		
		//1.3按照商品品牌过滤
		if (!"".equals(searchMap.get("brand"))) {//如果用户选择了品牌
			FilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filtercriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			filterQuery.addCriteria(filtercriteria);
			query.addFilterQuery(filterQuery);
		}
		
		//1.4 过滤规格
		if(searchMap.get("spec")!=null){
			Map<String,String> specMap= (Map) searchMap.get("spec");
			for(String key:specMap.keySet() ){
				Criteria filterCriteria=new Criteria("item_spec_"+key).is( specMap.get(key) );
				FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery); 
			}
		}

		//1.5 价格过滤
			if(!"".equals(searchMap.get("price"))){
				String priceStr = (String) searchMap.get("price");//500-1000
				String[] price = priceStr.split("-");
				if(!price[0].equals("0")){//如果最低价格不等于0
					FilterQuery filterQuery = new SimpleFilterQuery();
					Criteria filtercriteria = new Criteria("item_price").greaterThanEqual(price[0]);
					filterQuery.addCriteria(filtercriteria);
					query.addFilterQuery(filterQuery);
				}
				if(!price[1].equals("*")){//如果最高价格不等于*
					FilterQuery filterQuery = new SimpleFilterQuery();
					Criteria filtercriteria = new Criteria("item_price").lessThanEqual(price[1]);
					filterQuery.addCriteria(filtercriteria);
					query.addFilterQuery(filterQuery);
				}
			}

			//1.6分页
		Integer pageNo = (Integer) searchMap.get("pageNo");//获取页码
		if (pageNo==null){
			pageNo=1;
		}
		Integer pageSize = (Integer) searchMap.get("pageSize");//获取页大小
		if (pageSize==null){
			pageSize=20;
		}

		query.setOffset((pageNo-1)*pageSize);//起始索引
		query.setRows(pageSize);

		//1.7按价格排序
		String sortValue= (String) searchMap.get("sort");//升序 ASC 降序 DESC
		String sortField=(String) searchMap.get("sortField");//排序字段

		if(sortValue!=null&&!sortValue.equals("")){
			if(sortValue.equals("ASC")){
				Sort sort=new Sort(Sort.Direction.ASC,"item_"+sortField);
				query.addSort(sort);
			}
			if(sortValue.equals("DESC")){
				Sort sort=new Sort(Sort.Direction.DESC,"item_"+sortField);
				query.addSort(sort);
			}
		}







		//***************** 获取高亮结果集****************
		//高亮页对象
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		//高亮入口集合(每条记录的高亮入口
		List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
		for(HighlightEntry<TbItem> entry:entryList) {
			//获取高亮列表(高亮域的个数
			List<Highlight> highlightList = entry.getHighlights();
		
			if (highlightList.size()>0 && highlightList.get(0).getSnipplets().size()>0) {
				TbItem item=entry.getEntity();
				item.setTitle(highlightList.get(0).getSnipplets().get(0));	
			}
		}
		map.put("rows",page.getContent());
		map.put("totalPages",page.getTotalPages());//总页数
		map.put("total",page.getTotalElements());//总记录数
		return map;
	}
	/**
	 * 分组查询(查询商品分类列表
	 * @param searchMap
	 * @return
	 */
	private List searchCategoryList(Map searchMap) {
		List<String> list = new ArrayList<>();
		Query query = new SimpleQuery("*:*");
		//关键字查询 where
		Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));//where ...
		query.addCriteria(criteria);
		
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");//group by...
		query.setGroupOptions(groupOptions);
		//获取分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
		//获取分组结果
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		//获取分组入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		//获取分组入口集合
		List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
		
		for (GroupEntry<TbItem> entry : entryList) {
			list.add(entry.getGroupValue());//将分组的结果添加到返回值中
		}
		return list;
	}
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询品牌和规格列表
	 * @param category 商品分类名称
	 */
	private Map searchBrandAndSpecList(String category) {
		Map map=new HashMap();
		//根据商品分类得到模板ID
		Long templateId = (Long)redisTemplate.boundHashOps("itemCat").get(category);
		if (templateId!=null) {
			//根据模板ID获取品牌列表
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
			map.put("brandList",brandList);
			//根据模板Id获取规格列表
			List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
			map.put("specList",specList);
		}
		return map;
	}

	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
	}

	@Override
	public void deleteByGoodsIds(List goodsIds) {
		Query query=new SimpleQuery("*:*");

		Criteria criteria=new Criteria("item_goodsid").in(goodsIds);
		query.addCriteria(criteria);

		solrTemplate.delete(query);
		solrTemplate.commit();
	}


}
