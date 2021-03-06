define([
  'jquery', 
  'underscore', 
  'backbone',
  'ui/UITabbedPane',
  'ui/UIContent'
], function($, _, Backbone, UITabbedPane, UIContent) {
  var UITabbedPaneDemo = UITabbedPane.extend({
    label: 'Tabbed Pane Demo',

    config: {
      style: "ui-round-tabs",
      tabs: [
        { 
          label: "Tab 1",  name: "tab1",
          onSelect: function(thisUI, tabConfig) {
            var uiTab1 = new UIContent( { content: "Tab 1" }) ;
            thisUI.setSelectedTabUIComponent(tabConfig.name, uiTab1) ;
          }
        },
        { 
          label: "Tab 2",  name: "tab2", closable: true,
          onSelect: function(thisUI, tabConfig) {
            var uiTab2 = new UIContent( { content: "Tab 2" }) ;
            thisUI.setSelectedTabUIComponent(tabConfig.name, uiTab2) ;
          }
        }
      ]
    },

  });
  return new UITabbedPaneDemo({}) ;
});
