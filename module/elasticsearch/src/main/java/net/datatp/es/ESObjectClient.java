package net.datatp.es;

import static org.elasticsearch.index.query.QueryBuilders.regexpQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.util.Map;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;

import net.datatp.util.dataformat.DataSerializer;
import net.datatp.util.io.IOUtil;

//TODO: reimplement with retry mechanism
public class ESObjectClient<T> {
  private ESClient esclient;
  private String   index;
  private Class<T> mappingType ;
  
  public ESObjectClient(ESClient client, String index, Class mappingType) {
    this.esclient = client;
    this.index = index;
    this.mappingType = mappingType;
  }

  public ESClient getESClient() { return this.esclient ; }
  
  public String getIndex() { return this.index; }

  public Class<T> getIDocumentType() { return this.mappingType; }
  
  public boolean isCreated() { return esclient.hasIndex(index) ; }

  public void createIndex() throws Exception {
    String settingUrl  = mappingType.getName().replace('.', '/') + ".setting.json";
    String mappingUrl  = mappingType.getName().replace('.', '/') + ".mapping.json";
    createIndexWithResourceConfig(settingUrl, mappingUrl);
  }
  
  public void createIndexWithResourceConfig(String settingUrl, String mappingUrl) throws Exception {
    String settingJson = IOUtil.getResourceAsString(settingUrl, "UTF-8");
    String mappingJson = IOUtil.getResourceAsString(mappingUrl, "UTF-8");
    createIndexWithJSONConfig(settingJson, mappingJson);
  }
  
  public void createIndexWithJSONConfig(String settings, String mapping) throws Exception {
    CreateIndexRequestBuilder req = esclient.client.admin().indices().prepareCreate(index);
    if (settings != null) {
      req.setSettings(settings);
    }
    if (mapping != null) {
      req.addMapping(mappingType.getSimpleName(), mapping);
    }
    CreateIndexResponse response = req.execute().actionGet();
  }

  public void updateSettings(String settings) throws Exception {
    esclient.updateSettings(index, settings);
  }

  public void updateMapping(String mapping) throws Exception {
    esclient.updateMapping(index, mappingType.getSimpleName(), mapping);
  }

  public void put(T idoc, String id) throws ElasticsearchException{
    BulkRequestBuilder bulkRequest = esclient.client.prepareBulk();
    byte[] data = DataSerializer.JSON.toBytes(idoc);
    bulkRequest.add(
      esclient.client.prepareIndex(index, mappingType.getSimpleName(), id).setSource(data)
    );
    BulkResponse bulkResponse = bulkRequest.execute().actionGet();
    if (bulkResponse.hasFailures()) {
      throw new ElasticsearchException("The operation has been failed!\n" + bulkResponse.buildFailureMessage());
    }
  }

  public void put(Map<String, T> records) throws ElasticsearchException {
    BulkRequestBuilder bulkRequest = esclient.client.prepareBulk();
    for (Map.Entry<String, T> entry : records.entrySet()) {
      T idoc = entry.getValue();
      byte[] data = DataSerializer.JSON.toBytes(idoc);
      bulkRequest.add(
        esclient.client.prepareIndex(index, mappingType.getSimpleName(), entry.getKey()).setSource(data)
      );
    }
    BulkResponse bulkResponse = bulkRequest.execute().actionGet();
    if (bulkResponse.hasFailures()) {
      throw new ElasticsearchException("The operation has been failed!!!");
    }
  }

  public T get(String id) throws ElasticsearchException {
    GetResponse response = esclient.client.prepareGet(index, mappingType.getSimpleName(), id).execute().actionGet();
    if (!response.isExists()) return null;
    return DataSerializer.JSON.fromBytes(response.getSourceAsBytes(), mappingType);
  }

  public boolean remove(String id) throws ElasticsearchException {
    DeleteResponse response =
        esclient.client.prepareDelete(index, mappingType.getSimpleName(), id).execute().actionGet();
    return response.getResult() == Result.DELETED;
  }
  
  public ESQueryExecutor getQueryExecutor() { return new ESQueryExecutor(index, esclient); }

  public SearchResponse search(QueryBuilder xqb) throws ElasticsearchException {
    return search(xqb, false, 0, 100);
  }

  public SearchResponse search(QueryBuilder xqb, int from, int to) throws ElasticsearchException {
    return search(xqb, false, from, to);
  }

  public SearchResponse searchTerm(String field, String exp, int from, int to) throws ElasticsearchException {
    return search(termQuery(field, exp), false, from, to);
  }
  
  public SearchResponse searchTermByRegex(String field, String exp, int from, int to) throws ElasticsearchException {
    return search(regexpQuery(field, exp), false, from, to);
  }

  public T getIDocument(SearchHit hit) throws ElasticsearchException {
    return DataSerializer.JSON.fromBytes(hit.source(), mappingType);
  }

  public SearchResponse search(QueryBuilder xqb, boolean explain, int from, int to) throws ElasticsearchException {
    return search(xqb, null, explain, from, to);
  }
  
  public SearchResponse search(QueryBuilder xqb, AbstractAggregationBuilder[] aggB, boolean explain, int from, int to) throws ElasticsearchException {
    SearchRequestBuilder searchReqBuilder = esclient.client.prepareSearch(index).setSearchType(SearchType.QUERY_THEN_FETCH);
    if(xqb == null) searchReqBuilder.setQuery(QueryBuilders.matchAllQuery());
    else searchReqBuilder.setQuery(xqb);
    
    if(aggB != null) {
      for(AbstractAggregationBuilder sel : aggB) {
        searchReqBuilder.addAggregation(sel);
      }
    }
    searchReqBuilder.setFrom(from).setSize(to).setExplain(explain);
    SearchResponse response = searchReqBuilder.execute().actionGet();
    return response;
  }

  public long count(QueryBuilder xqb) throws Exception {
    SearchResponse response = esclient.client.prepareSearch(index).setQuery(xqb).setSize(0).execute().actionGet();
    return response.getHits().getTotalHits();
  }
  
  public void close()  {
    esclient.close() ; 
  }
}