define([
  'rest/Server'
], function(Server) {
  var server = new Server("http://localhost:8080/crawler");

  function SiteRest() {
    this.getSiteConfigs = function() {
      return server.restGET("/site/configs");
    };
  };

  function SchedulerRest(server) {
    this.server = server;
    
    this.getURLCommitInfos = function(max) {
      if(max === undefined) max = 100;
      return this.server.restGET("/scheduler/report/url-commit", {max: max});
    };

    this.getURLScheduleInfos = function(max) {
      if(max === undefined) max = 100;
      return this.server.restGET("/scheduler/report/url-schedule", {max: max});
    };
  };

  function FetcherRest(server) {
    this.server = server;
  };

  var Rest = {
    site:       new SiteRest(server),
    scheduler:  new SchedulerRest(server),
    fetcher:    new FetcherRest(server),
  }
  return Rest ;
});