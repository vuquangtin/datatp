define([
  'jquery', 
  'underscore', 
  'backbone',
  'ui/nvd3'
], function($, _, Backbone, nvd3) {
  var UINVMultiChartDemo = nvd3.UIMultiChart.extend({
    label: 'NV Multi Chart Demo',

    onInit: function(options) {
      var data =  [
        {
          key: "Stream0", type : "area", yAxis:1,
          values: [
            { "x": 0, "y": -0.2552175299820729 }, { "x": 1, "y": -0.14831527753785057 }, { "x": 2, "y": -0.11107730625292427 }, { "x": 3, "y": -0.152787059235186 },
            { "x": 4, "y": -0.1841135395653229 }, { "x": 5, "y": -0.11623199643751131 }, { "x": 6, "y": -2.522253916059705 }, { "x": 7, "y": -0.16440865450056943 },
            { "x": 8, "y": -0.16771974255081176 }, { "x": 9, "y": -0.1839687581417797 }, { "x": 10, "y": -0.13964989052703936 }, { "x": 11, "y": -0.1514021363077739 },
            { "x": 12, "y": -0.10290638860685997 }, { "x": 13, "y": -0.173686272478341 }, { "x": 14, "y": -0.11078005579366498 }, { "x": 15, "y": -0.17883503333027223 },
            { "x": 16, "y": -0.18099753203866367 }, { "x": 17, "y": -0.19734592222958924 }, { "x": 18, "y": -0.1622774994972066 }, { "x": 19, "y": -0.18274294189989693 },
            { "x": 20, "y": -0.13029012682057425 }, { "x": 21, "y": -0.15651128377209753 }, { "x": 22, "y": -0.12170137729243669 }, { "x": 23, "y": -0.11778421220897783 },
            { "x": 24, "y": -0.14025849541461183 }, { "x": 25, "y": -0.204511604129531 }
          ]
        },
        {
          key: "Stream1", type:  "area", yAxis: 1,
          "values": [
            { "x": 0, "y": -1.4517416303020318 }, { "x": 1, "y": -1.537365663555801 }, { "x": 2, "y": -0.9926944979254532 }, { "x": 3, "y": -0.5547744199485212 },
            { "x": 4, "y": -0.24221502520933136 }, { "x": 5, "y": -0.15156850917658768 }, { "x": 6, "y": -0.26045492787917174 }, { "x": 7, "y": -0.5462364641494453 },
            { "x": 8, "y": -1.158038284456379 }, { "x": 9, "y": -0.9505202202895984 }, { "x": 10, "y": -0.3956306474043157 }, { "x": 11, "y": -0.13072527472201617 },
            { "x": 12, "y": -0.1748291912571741 }, { "x": 13, "y": -1.5406759661962925 }, { "x": 14, "y": -0.16014130454303163 }, { "x": 15, "y": -0.18393537084736003 },
            { "x": 16, "y": -0.21035978561116697 }, { "x": 17, "y": -0.3133739108881646 }, { "x": 18, "y": -0.5563134209321146 }, { "x": 19, "y": -0.9523216738039058 },
            { "x": 20, "y": -1.403614131703907 }, { "x": 21, "y": -1.4117157079928633 }, { "x": 22, "y": -1.0435208775499896 }, { "x": 23, "y": -0.67872109049474 },
            { "x": 24, "y": -0.6387476283392897 }, { "x": 25, "y": -0.8701421009603098 } 
          ] 
        }, 
        { 
          key: "Stream2", type: "line", yAxis: 1,
          values: [ 
            { "x": 0, "y": 2.6712286606266935 }, 
            { "x": 1, "y": 0.8202437987818962 }, { "x": 2, "y": 0.46434052678104004 }, { "x": 3, "y": 1.0238567713352522 }, { "x": 4, "y": 1.9831001149928136 },
            { "x": 5, "y": 2.944174885065787 }, { "x": 6, "y": 3.4622836908782357 }, { "x": 7, "y": 3.126998110292953 }, { "x": 8, "y": 2.1280412059531426 },
            { "x": 9, "y": 1.1543989106664596 }, { "x": 10, "y": 0.5564240115639776 }, { "x": 11, "y": 0.2836694162859948 }, { "x": 12, "y": 0.19461489849770716 },
            { "x": 13, "y": 0.18341184303282262 }, { "x": 14, "y": 0.19223925766496103 }, { "x": 15, "y": 0.1929240917530198 }, { "x": 16, "y": 0.10742338033822829 },
            { "x": 17, "y": 0.11539105438613007 }, { "x": 18, "y": 0.16419926107458088 }, { "x": 19, "y": 0.2856635738280019 }, { "x": 20, "y": 0.549588320574557 },
            { "x": 21, "y": 1.205701923430928 }, { "x": 22, "y": 3.584259641634015 }, { "x": 23, "y": 8.885008798173804 }, { "x": 24, "y": 9.409007016070044 },
            { "x": 25, "y": 3.704885964681752 } 
          ] 
        }, 
        { 
          key: "Stream3", type: "line", yAxis: 2,
          values: [ 
            { "x": 0, "y": 0.19986827150536807 }, { "x": 1, "y": 0.1869006846324092 },
            { "x": 2, "y": 0.16224989996726535 }, { "x": 3, "y": 0.1789950137113639 }, { "x": 4, "y": 0.1652614908747182 }, { "x": 5, "y": 0.30123356821572317 },
            { "x": 6, "y": 0.4692058136389596 }, { "x": 7, "y": 0.716302699566641 }, { "x": 8, "y": 1.1834109263557138 }, { "x": 9, "y": 1.4069522142705935 },
            { "x": 10, "y": 1.261787299022379 }, { "x": 11, "y": 0.9316876183517339 }, { "x": 12, "y": 0.5351263698776165 }, { "x": 13, "y": 0.3445676372081502 },
            { "x": 14, "y": 0.22642697924092955 }, { "x": 15, "y": 0.31642832251479863 }, { "x": 16, "y": 1.3699914601230154 }, { "x": 17, "y": 1.9126501137814524 },
            { "x": 18, "y": 0.7494676685512943 }, { "x": 19, "y": 0.1554205253996673 }, { "x": 20, "y": 0.11599253671102448 }, { "x": 21, "y": 0.10251623157244608 },
            { "x": 22, "y": 0.17245020426156182 }, { "x": 23, "y": 0.11098734195650468 }, { "x": 24, "y": 0.129116802428688 }, { "x": 25, "y": 0.13908302256974 }
          ]
        },
        {
           key: "Stream4", type: "scatter", yAxis: 1,
           values: [
             { "x": 0, "y": 0.20871066990355205 }, { "x": 1, "y": 0.14313601631633696 }, { "x": 2, "y": 0.2026545602794 }, { "x": 3, "y": 0.5489947055110441 },
             { "x": 4, "y": 1.3492531961275522 }, { "x": 5, "y": 2.795601934324318 }, { "x": 6, "y": 4.0219641478057575 }, { "x": 7, "y": 4.005852509249234 },
             { "x": 8, "y": 2.775207569226878 }, { "x": 9, "y": 1.4443912613224268 }, { "x": 10, "y": 0.5455867534257184 }, { "x": 11, "y": 0.20969052216128717 },
             { "x": 12, "y": 1.8147531705917277 }, { "x": 13, "y": 0.16982539008773118 }, { "x": 14, "y": 0.10039027120405423 }, { "x": 15, "y": 0.1154908808595744 },
             { "x": 16, "y": 0.1953381094611475 }, { "x": 17, "y": 0.1252168292499672 }, { "x": 18, "y": 0.16800177375156392 }, { "x": 19, "y": 0.1109869570030353 },
             { "x": 20, "y": 0.16694468115109223 }, { "x": 21, "y": 0.13402430823397649 }, { "x": 22, "y": 0.1572798083148395 }, { "x": 23, "y": 0.12824669255225402 },
             { "x": 24, "y": 0.1654483767784577 }, { "x": 25, "y": 0.18116795235780758 } 
           ] 
        }, 
        { 
          key: "Stream5", type: "scatter", yAxis: 2,
          values: [
            { "x": 0, "y": 2.393897072693466 }, { "x": 1, "y": 2.2207566009016433 }, { "x": 2, "y": 1.5804144574124095 }, { "x": 3, "y": 0.9049592500389505 },
            { "x": 4, "y": 0.5073859353432458 }, { "x": 5, "y": 0.267572922044241 }, { "x": 6, "y": 0.12495287878219188 }, { "x": 7, "y": 0.15437389315658723 },
            { "x": 8, "y": 0.19951339398343837 }, { "x": 9, "y": 0.1304850869453753 }, { "x": 10, "y": 0.1782532580031363 }, { "x": 11, "y": 0.14828566599743526 },
            { "x": 12, "y": 0.18775952954009623 }, { "x": 13, "y": 0.5565077390344324 }, { "x": 14, "y": 1.1773174779552948 }, { "x": 15, "y": 1.3656459897863247 },
            { "x": 16, "y": 0.7372113685665665 }, { "x": 17, "y": 0.26067365743922905 }, { "x": 18, "y": 0.1370282246599544 }, { "x": 19, "y": 0.15262575619856783 },
            { "x": 20, "y": 0.13115449725350053 }, { "x": 21, "y": 0.11498931086893924 }, { "x": 22, "y": 0.15418053751160402 }, { "x": 23, "y": 0.2333562327789065 },
            { "x": 24, "y": 0.5568032760484739 }, { "x": 25, "y": 1.1315623425114167 }
          ] 
        },
        {
          key: "Stream6", type: "bar", yAxis: 2, nonStackable: true,
          values: [
            { "x": 0, "y": 5.34350331913906 }, { "x": 1, "y": 5.659966363807834 }, { "x": 2, "y": 3.941673197552651 }, { "x": 3, "y": 1.8082369168731498 },
            { "x": 4, "y": 0.5950639940061521 }, { "x": 5, "y": 0.21214728083999446 }, { "x": 6, "y": 2.4155677964230287 }, { "x": 7, "y": 0.3613123463482225 },
            { "x": 8, "y": 0.1285935182395106 }, { "x": 9, "y": 0.19630071792514192 }, { "x": 10, "y": 0.11755448100842748 }, { "x": 11, "y": 0.17831825515514466 },
            { "x": 12, "y": 0.11220814725509042 }, { "x": 13, "y": 0.16942101868397277 }, { "x": 14, "y": 0.1946056012245591 }, { "x": 15, "y": 0.1260257556474486 },
            { "x": 16, "y": 0.18864557556562003 }, { "x": 17, "y": 0.11015648940940474 }, { "x": 18, "y": 0.15433638708329805 }, { "x": 19, "y": 0.22351933492629528 },
            { "x": 20, "y": 0.48967360881543487 }, { "x": 21, "y": 0.9925636293047728 }, { "x": 22, "y": 1.821204395207284 }, { "x": 23, "y": 2.430477589408717 },
            { "x": 24, "y": 2.0676921867168625 }, { "x": 25, "y": 1.2878652060348745 }
          ]
        },
        {
          key: "Stream7", type: "bar", yAxis: 2,
          values: [
            { "x": 0, "y": 0.1273016926834947 }, { "x": 1, "y": 0.169327999811195 }, { "x": 2, "y": 0.10894536114683154 }, { "x": 3, "y": 0.12679170571233944 },
            { "x": 4, "y": 0.1458615655360053 }, { "x": 5, "y": 0.11164451006477973 }, { "x": 6, "y": 1.0414749817644267 }, { "x": 7, "y": 0.15596080783931526 },
            { "x": 8, "y": 0.19520958944253214 }, { "x": 9, "y": 0.11621664309966306 }, { "x": 10, "y": 0.14628081332659973 }, { "x": 11, "y": 0.16854976815220946 },
            { "x": 12, "y": 0.1228355908658841 }, { "x": 13, "y": 0.18584696575349835 }, { "x": 14, "y": 0.1568718084228311 }, { "x": 15, "y": 0.1398974021983392 },
            { "x": 16, "y": 0.1907660686276839 }, { "x": 17, "y": 0.16638924494036175 }, { "x": 18, "y": 0.23331473123623997 }, { "x": 19, "y": 1.1232630106725736 },
            { "x": 20, "y": 0.5681124059958769 }, { "x": 21, "y": 0.11906583846470346 }, { "x": 22, "y": 0.173033403421341 }, { "x": 23, "y": 0.17962439192750534 },
            { "x": 24, "y": 0.23870465066217 }, { "x": 25, "y": 0.6066247091652359 }
          ]
        },
        {
          key: "Stream8", type: "bar", yAxis: 2,
          values: [
            { "x": 0, "y": 1.5263192773683434 }, { "x": 1, "y": 1.0183171609223518 }, { "x": 2, "y": 0.5936263661005408 }, { "x": 3, "y": 0.2814801659399139 },
            { "x": 4, "y": 0.20445884603727507 }, { "x": 5, "y": 0.14529290536063216 }, { "x": 6, "y": 0.1246926709385252 }, { "x": 7, "y": 0.19959294999532717 },
            { "x": 8, "y": 0.1701339252832025 }, { "x": 9, "y": 0.17437578303950796 }, { "x": 10, "y": 0.21153968692947037 }, { "x": 11, "y": 1.469611320757532 },
            { "x": 12, "y": 3.279939773872921 }, { "x": 13, "y": 1.013300032913402 }, { "x": 14, "y": 0.13548026684367126 }, { "x": 15, "y": 0.13960361452074896 },
            { "x": 16, "y": 0.18503779617133376 }, { "x": 17, "y": 0.12934548493980944 }, { "x": 18, "y": 0.15778709936009228 }, { "x": 19, "y": 0.16972690751622313 },
            { "x": 20, "y": 0.36828184830975563 }, { "x": 21, "y": 0.24525082960583414 }, { "x": 22, "y": 0.11247233431133288 }, { "x": 23, "y": 0.16779146724308602 },
            { "x": 24, "y": 0.12498780438502084 }, { "x": 25, "y": 0.11436926473842779 }
          ]
        }
      ];

      this.setData(data);
    }
  });
  return new UINVMultiChartDemo() ;
});
