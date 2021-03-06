define([
  'jquery', 
  'underscore', 
  'backbone',
  'ui/UIContent',
  'ui/UIBorderLayout'
], function($, _, Backbone, UIContent, UIBorderLayout) {
  var UIBorderLayoutDemo = UIBorderLayout.extend({
    label: 'BorderLayout Demo',

    config: {
    },
    
    onInit: function(options) {
      var northConfig = { height: "75px" };
      this.setUI('north', new UIContent({ content: 'North Panel Content'}), northConfig);

      var shouthConfig = { height: "auto" };
      this.setUI('shouth', new UIContent({ content: 'Shouth Panel Content'}), shouthConfig);

      var westConfig = { width: "250px"};
      this.setUI('west', new UIContent({ content: 'West Panel Content'}), westConfig);

      var eastConfig = { width: "150px"};
      this.setUI('east', new UIContent({ content: 'East Panel Content'}), eastConfig);

      var centerConfig = {};
      this.setUI('center', new UIContent({ content: 'Center Panel Content'}), centerConfig);
    }
  });

  return new UIBorderLayoutDemo() ;
});
