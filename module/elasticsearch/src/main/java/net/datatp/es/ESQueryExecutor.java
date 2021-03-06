package net.datatp.es;

import java.io.IOException;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;

public class ESQueryExecutor {
  private String               index;
  private ESClient             esclient;
  private SearchRequestBuilder searchReqBuilder;
  
  public ESQueryExecutor(String index, ESClient esclient) {
    this.index = index;
    this.esclient = esclient;
    searchReqBuilder = esclient.client.prepareSearch(index).setSearchType(SearchType.QUERY_THEN_FETCH);
    setQueryBuilder(QueryBuilders.matchAllQuery());
    setFrom(0).setPageSize(100);
  }
  
  public ESQueryExecutor setExplain(boolean b) {
    searchReqBuilder.setExplain(b);
    return this;
  }
  
  public ESQueryExecutor setFrom(int from) {
    searchReqBuilder.setFrom(from);
    return this;
  }
  
  public ESQueryExecutor setPageSize(int pageSize) {
    searchReqBuilder.setSize(pageSize);
    return this;
  }
  
  public ESQueryExecutor setQueryBuilder(QueryBuilder xqb) {
    searchReqBuilder.setQuery(xqb);
    return this;
  }
  
  public ESQueryExecutor setAggregations(AbstractAggregationBuilder ... agg) {
    for(AbstractAggregationBuilder sel : agg) {
      searchReqBuilder.addAggregation(sel);
    }
    return this;
  }

  public ESQueryExecutor matchAll() {
    searchReqBuilder.setQuery(QueryBuilders.matchAllQuery());
    return this;
  }
  
  public ESQueryExecutor matchTerm(String field, String term) throws ElasticsearchException {
    searchReqBuilder.setQuery(QueryBuilders.termQuery(field, term));
    return this;
  }
  
  public ESQueryExecutor matchTerms(String[] field, String terms) throws ElasticsearchException {
    MultiMatchQueryBuilder multiMatch = QueryBuilders.multiMatchQuery(terms, field);
    searchReqBuilder.setQuery(multiMatch);
    return this;
  }
  
  public ESQueryExecutor matchTermByRegex(String field, String exp) throws ElasticsearchException {
    searchReqBuilder.setQuery(QueryBuilders.regexpQuery(field, exp));
    return this;
  }
  
  public ESQueryExecutor query(String query) throws ElasticsearchException {
    searchReqBuilder.setQuery(QueryBuilders.queryStringQuery(query));
    return this;
  }
  
  public SearchResponse execute() throws ElasticsearchException {
    return searchReqBuilder.execute().actionGet();
  }
  
  public String executeAndReturnAsJson() throws ElasticsearchException, IOException {
    SearchResponse response = searchReqBuilder.execute().actionGet();
    XContentBuilder builder = XContentFactory.jsonBuilder();
    builder.prettyPrint();
    builder.startObject();
    response.toXContent(builder, ToXContent.EMPTY_PARAMS);
    builder.endObject();
    return builder.string();
  }
  
  public long countByMatchTerm(String field, String term) throws Exception {
    TermQueryBuilder termQuery = QueryBuilders.termQuery(field, term);
    SearchResponse response = 
      esclient.client.prepareSearch(index).setQuery(termQuery).setSize(0).execute().actionGet();
    return response.getHits().getTotalHits();
  }
}
