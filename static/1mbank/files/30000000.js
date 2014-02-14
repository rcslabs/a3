$(function() {
  if (window.PIE) {
    $('.pie').each(function() {
      PIE.attach(this);
    });
  }
});
// Iframe buster, @see http://stackoverflow.com/q/958997
// Yandex subdomains are excluded to make Yandex.Metrika work.
if (top != self && top.location.host.search(/.yandex.ru$/) === -1) {
  top.location.replace(self.location.href);
}

$(function(){
  
  /* Окно выбора региона */
  function locationsList(locations) {
    var output = [];
    for (var i=0; i < locations.length; i++) {
      output.push(locations[i]['title']);
    }
    return output;
  }
  $('#locations_location').typeahead({
    'source': locationsList(locationsData),
    'items': 5,
    'minLength': 1
  });
  $('#locations_button').click(function(e){
    e.preventDefault();
    function locationIdByTitle(locations, title) {
      for (var i=0; i < locations.length; i++) {
        if (title == locations[i]['title']) {
          return locations[i]['id'];
        }
      }
      return 0;
    }
    var location_title = $('#locations_location').val();
    var location_id = locationIdByTitle(locationsData, location_title);
    if (location_id) {
      $('#locations').modal('hide');
      window.location.href = '?user_location=' + location_id;
    }
    else {
      $('#locations_location').val('');
    }
  });
  
  /* Вход в интернет-банк */
  $('#internet-bank').popover({
    'html': true,
    'placement': 'bottom',
    'trigger': 'click',
    'delay': 100,
    'title': 'Вход в интернет-банк:',
    'content': '<ul class="unstyled internet-bank-content">'
    + '<li><img src="/file/390_ibank_individual.png" alt="" /> <a href="https://oplata.1mbank.ru/">Физическим лицам</a></li>'
    + '<li><img src="/file/391_ibank_company.png" alt="" /> <a href="https://online.1mbank.ru/">Юридическим лицам</a></li>'
    + '</ul>'
  });
  
  /* Всплывающий бок "Связь с банком" */
  $('#skype_contacts_show').click(function(){
    $('.skype_contact_list_collapsible').html(
      '<ul>'
      +'<li><img alt="[?]" src="http://mystatus.skype.com/smallicon/bank_pervomayski1" /> <a href="skype:bank_pervomayski1?chat">Алла</a></li>'
      +'<li><img alt="[?]" src="http://mystatus.skype.com/smallicon/bank_pervomayski2" /> <a href="skype:bank_pervomayski2?chat">Мария</a></li>'
      +'<li><img alt="[?]" src="http://mystatus.skype.com/smallicon/bank_pervomayski3" /> <a href="skype:bank_pervomayski3?chat">Ольга</a></li>'
      +'<li><img alt="[?]" src="http://mystatus.skype.com/smallicon/bank_pervomayski4" /> <a href="skype:bank_pervomayski4?chat">Елена</a></li>'
      +'<li><img alt="[?]" src="http://mystatus.skype.com/smallicon/bank_pervomayski5" /> <a href="skype:bank_pervomayski5?chat">Нелли</a></li>'
      +'<li><img alt="[?]" src="http://mystatus.skype.com/smallicon/bank_pervomayski6" /> <a href="skype:bank_pervomayski6?chat">Татьяна</a></li>'
      +'<li><img alt="[?]" src="http://mystatus.skype.com/smallicon/bank_pervomayski7" /> <a href="skype:bank_pervomayski7?chat">Елена</a></li>'
      +'<li><img alt="[?]" src="http://mystatus.skype.com/smallicon/bank_pervomayski8" /> <a href="skype:bank_pervomayski8?chat">Яна</a></li>'
      +'<li><img alt="[?]" src="http://mystatus.skype.com/smallicon/bank_pervomayski9" /> <a href="skype:bank_pervomayski9?chat">Юлия</a></li>'
      +'<li><img alt="[?]" src="http://mystatus.skype.com/smallicon/bank_pervomayski10" /> <a href="skype:bank_pervomayski10?chat">Виктория</a></li>'
      +'<li><img alt="[?]" src="http://mystatus.skype.com/smallicon/bank_pervomayski11" /> <a href="skype:bank_pervomayski11?chat">Анна</a></li>'
      +'<li><img alt="[?]" src="http://mystatus.skype.com/smallicon/bank_pervomayski12" /> <a href="skype:bank_pervomayski12?chat">Анна</a></li>'
      +'<li><img alt="[?]" src="http://mystatus.skype.com/smallicon/bank_pervomayski13" /> <a href="skype:bank_pervomayski13?chat">Александра</a></li>'
      +'<li><img alt="[?]" src="http://mystatus.skype.com/smallicon/bank_pervomayski14" /> <a href="skype:bank_pervomayski14?chat">Екатерина</a></li>'
      +'</ul>'
    ).slideToggle();
  });
  
  /* Кнопка Мегафона */
  var megafonIframe = $(
    $(
      $('#megafon-button-wrapper .webcall-button-placeholder')[0]
    ).find('iframe')[0]
  );
  var megafonTriggerAnchor = $('#megafon-button-trigger');
  megafonIframe.load(function () {
    megafonTriggerAnchor.click(function (e) {
      e.preventDefault();
      megafonIframe.trigger('click');
    });
  });
  
  /* Прижимаем футер к низу страницы */
  function footerToBottom(){
    var footer    = $('.footer');
    var container = $('body');
    if ($(window).height() > (container.outerHeight(true) - 3)) {
      footer.css({
        'position': 'absolute',
        'bottom': '0px',
        'width': container.width() + 'px'
      });
      container.css({
        'padding-bottom': footer.outerHeight(true) + 'px'
      });
    }
    else {
      footer.css({
        'position': 'relative',
        'bottom': 'auto',
        'width': 'auto'
      });
      container.css({
        'padding-bottom': '0px'
      });
    }
  };
  footerToBottom();
  $(window)
  .resize(function(){
    footerToBottom();
  }).scroll(function(){
    footerToBottom();
  });
  setInterval(function(){
    footerToBottom();
  }, 500);
});