define([
  'jquery',
  'underscore', 
  'backbone',
  'text!plugins/uidemo/UIBody.jtpl'
], function($, _, Backbone, Template) {
  var UIBody = Backbone.View.extend({
    el: $("#UIBody"),
    
    initialize: function () {
      _.bindAll(this, 'render') ;
    },
    
    _template: _.template(Template),

    render: function() {
      var params = { } ;
      $(this.el).html(this._template(params));
    },

    onActivate: function(evt) {
      require(['plugins/uidemo/UITableDemo'], function(UIDemoComponent) { 
        $('#UIDemoWS').empty();
        $('#UIDemoWS').unbind();
        UIDemoComponent.setElement($('#UIDemoWS')).render();
      }) ;
    },

    events: {
      'click .onSelectUIComponent': 'onSelectUIComponent'
    },
    
    onSelectUIComponent: function(evt) {
      var name = $(evt.target).closest('.onSelectUIComponent').attr('name') ;
      console.log('on select: ' + name) ;

      require(['plugins/uidemo/' + name], function(UIDemoComponent) { 
        $('#UIDemoWS').empty();
        $('#UIDemoWS').unbind();
        UIDemoComponent.setElement($('#UIDemoWS')).render();
      }) ;
    }
  });
  
  return new UIBody() ;
});
