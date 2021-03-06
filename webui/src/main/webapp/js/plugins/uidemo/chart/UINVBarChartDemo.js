define([
  'jquery', 'underscore', 'backbone',
  'ui/nvd3'
], function($, _, Backbone, nvd3) {
  var UINVBarChartDemo = nvd3.UIBarChart.extend({
    label: 'NV Bar Chart Demo',
    
    config: {
      xAxis: {
        label: { title: "X Axis Demo" },
        tickFormat: "integer"
      },

      yAxis: {
        label: { title: "Y Axis Demo" },
        tickFormat: "number",
      }
    },

    onInit: function(options) {
      var chartData1 = {
        key: "Bar 1",
        values: [
          { "y": 84.79595886446555, "x": 0 },
          { "y": 99.89250052861944, "x": 1 },
          { "y": 67.67801413116035, "x": 2 },
          { "y": -59.576507002439314, "x": 3 },
          { "y": 60.66999800702548, "x": 4 },
          { "y": -64.60480868845379, "x": 5 },
          { "y": 85.13522246751887, "x": 6 },
          { "y": 4.177539039938973, "x": 7 },
          { "y": -25.21098265197675, "x": 8 },
          { "y": 24.248918016216727, "x": 9 },
          { "y": -39.260537836083444, "x": 10 }
        ]
      };

      var chartData2 = {
        key: "Bar 2",
        values: [
          { "y": -39.30149871817755, "x": 0 },
          { "y": -55.626052631305214, "x": 1 },
          { "y": -85.04127493232377, "x": 2 },
          { "y": 68.46021869320029, "x": 3 },
          { "y": -7.750133578831061, "x": 4 },
          { "y": 58.675268880971686, "x": 5 },
          { "y": -54.072369222222434, "x": 6 },
          { "y": 78.47001111415672, "x": 7 },
          { "y": -8.786771642698461, "x": 8 },
          { "y": 36.405463334422066, "x": 9 },
          { "y": -49.53377640284663, "x": 10 }
        ]
      };

      var chartData3 =  {
        key: "Bar 3",
        values: [
          { "y": -11.614114374242877, "x": 0 },
          { "y": 41.132804448784725, "x": 1 },
          { "y": 109.22227255418088, "x": 2 },
          { "y": 10.166011977740222, "x": 3 },
          { "y": 107.99532691520172, "x": 4 },
          { "y": 90.34600000639192, "x": 5 },
          { "y": -22.726314674314324, "x": 6 },
          { "y": -10.471630702237743, "x": 7 },
          { "y": 107.75082443654685, "x": 8 },
          { "y": 92.1205237913869, "x": 9 },
          { "y": 73.27666868248478, "x": 10 }
        ]
      };

      this.addChartData(chartData1);
      this.addChartData(chartData2);
      this.addChartData(chartData3);
    }
  });
  return new UINVBarChartDemo() ;
});
