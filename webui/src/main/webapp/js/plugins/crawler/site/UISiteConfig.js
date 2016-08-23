define([
  'jquery', 
  'underscore', 
  'backbone',
  'ui/UIBreadcumbs',
  'ui/UIBean',
  'ui/UICollapsible',
  'plugins/crawler/site/UISiteAnalyzer'
], function($, _, Backbone, UIBreadcumbs, UIBean, UICollabsible, UISiteAnalyzer) {

  var UIURLPattern = UIBean.extend({
    label: "URL Pattern",
    config: {
      type: 'array',
      beans: {
        urlPattern: {
          label: 'URL Pattern',
          fields: [
            {
              field:  "type", label: "Type",
              select: {
                getOptions: function(field, bean) {
                  var options = [
                    { label: 'ignore',  value: 'ignore' },
                    { label: 'list',    value: 'list' },
                    { label: 'detail',  value: 'detail' },
                    { label: 'unkwnon', value: 'unknown' },
                  ];
                  return options ;
                }
              }
            },
            { field: "pattern",  label: "Pattern", multiple: true }
          ]
        }
      }
    }
  });

  var UISiteConfigGeneric = UIBean.extend({
    label: "Site Config Generic",
    config: {
      beans: {
        siteConfig: {
          name: 'siteConfig', label: 'Site Config',
          fields: [
            { field: "group",   label: "Group", required: true  },
            { field: "hostname",   label: "Hostname", required: true },
            { field: "status",   label: "Status" },
            { field: "injectUrl",   label: "Inject URL", multiple: true },
            { 
              field: "crawlSubDomain",   label: "Crawl Subdomain",
              select: {
                getOptions: function(field, bean) {
                  var options = [
                    { label: 'True', value: true },
                    { label: 'False', value: false }
                  ];
                  return options ;
                }
              }
            },
            { field: "crawlDeep",   label: "Crawl Deep" },
            { field: "maxConnection",   label: "Max Connection" },
            { field: "language",   label: "Language" },
            { field: "description",   label: "Description", textarea: {} }
          ],
          edit: {
            disable: false , 
            actions: [ ],
          },
          view: {
            actions: [ ]
          }
        }
      }
    }
  });

  var UISiteConfigCollabsible = UICollabsible.extend({
    label: "Site Config", 
    config: {
      actions: [
        {
          action: "save", label: "Save",
          onClick: function(thisUI) { 
            var siteConfig = uiSiteConfig.siteConfig;
          }
        },
        {
          action: "analyzer", label: "Analyzer",
          onClick: function(thisUI) { 
            var uiSiteConfig = thisUI.getAncestorOfType("UISiteConfig") ;
            var siteConfig = uiSiteConfig.siteConfig;
            uiSiteConfig.push(new UISiteAnalyzer({ siteConfig: siteConfig }));
          }
        },
        { 
          action: "back", label: "Back",
          onClick: function(thisUI) {
          }
        }
      ]
    },

    onInit: function(options) {
      this.siteConfig = options.siteConfig;

      var uiSiteConfigGeneric = new UISiteConfigGeneric();
      uiSiteConfigGeneric.bind('siteConfig', this.siteConfig, true) ;
      uiSiteConfigGeneric.getBeanState('siteConfig').editMode = true ;

      var uiURLPattern = new UIURLPattern() ;
      if(this.siteConfig.urlPatterns == null) this.siteConfig.urlPatterns = [];
      uiURLPattern.bindArray('urlPattern', this.siteConfig.urlPatterns) ;

      this.add(uiSiteConfigGeneric);
      this.add(uiURLPattern);
    }
  }) ;

  var UISiteConfig = UIBreadcumbs.extend({
    type:  "UISiteConfig",

    onInit: function(options) {
      var uiSiteConfigCollabsible = new UISiteConfigCollabsible({siteConfig: options.siteConfig}) ;
      this.push(uiSiteConfigCollabsible);
    }
  });

  return UISiteConfig ;
});
