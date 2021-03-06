define([
  'jquery', 'underscore', 'backbone',
  "ui/bean/widget",
  'util/util',
  "ui/bean/UIBeanEditor",
], function($, _, Backbone, widget, util, UIBeanEditor) {
  var UIBeanTmpl = `
    <%var width = config.width ? config.width : "100%"; %>

    <div class="ui-beans" style="width: <%=width%>">
      <%if(config.header) {%>
        <div class="header">
          <span><%=config.header%></span>
          <div class="box-display-ib toggle-mode"><span/></div>
        </div>
      <%}%>

      <div class="ui-bean">
        <%for(var name in beanInfo.fields) { %>
        <%  var field = beanInfo.fields[name]; %>
            <div class="field">
              <div class="field-label"><%=field.label%></div>
              <div class="field-value" field="<%=name%>"><span/></div>
            </div>
        <%}%>
      <div>
      <div class="actions"></div>
    </div>
  `;

  var UIBean = UIBeanEditor.extend({
    initialize: function (options) {
      var defaultConfig = {} ;
      if(this.config) $.extend(true, defaultConfig, this.config);
      this.config = defaultConfig;

      $.extend(this.events, this.UIBeanEditorEvents);
      if(this.onInit) this.onInit(options);
    },

    init: function(bInfo, beanState) { 
      this.beanInfo = bInfo;
      this.state    = beanState;
      return this;
    },

    configure: function(newConfig) { 
      $.extend(true, this.config, newConfig); 
      return this;
    },

    getBean: function() { return this.state.getBean(); },

    getBeanState: function() { return this.state; },
    
    set: function(bInfo, bean) { 
      this.beanInfo = bInfo;
      this.state    = this.__createBeanState(bInfo, bean);
      this.state.editeMode = false;
      return this;
    },

    setEditMode: function(bool) {
      this.state.editMode = bool;
      return this;
    },

    onViewMode: function() { this.__setViewMode($(this.el).find(".ui-bean")); },

    onEditMode: function() { this.__setEditMode($(this.el).find(".ui-bean")); },

    _template: _.template(UIBeanTmpl),

    render: function() {
      var params = { config: this.config, beanInfo: this.beanInfo };
      $(this.el).html(this._template(params));
      if(this.state.editMode) {
        this.onEditMode();
      } else {
        this.onViewMode();
      }

      var actionsBlk = $(this.el).find(".actions");
      widget.actions(actionsBlk, this.config.actions);

      var uiToggleMode = $(this.el).find(".ui-beans").find(".toggle-mode");
      widget.toggle(uiToggleMode);
    },
    

    events: {
      //Handle by UIBean
      'click      .actions .onAction' : 'onAction',
      'click      .toggle-mode .onToggleMode' : 'onToggleMode',
    },


    __getBean: function(fv) { return this.state.getBean(); },

    __getBeanState: function(fv) { return this.state; },

    __getBeanInfo: function() { return this.beanInfo; },

    onAction: function(evt) {
      var name = $(evt.target).attr('name');
      var action = this.config.actions[name];
      action.onClick(this);
    },

    onToggleMode: function(evt) {
      if(this.state.editMode)  this.onViewMode();
      else this.onEditMode();
    },
  });

  return UIBean ;
});
