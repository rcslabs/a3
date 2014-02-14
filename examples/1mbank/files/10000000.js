$(function(){
  
  // Слайдер
  $('.carousel').carousel({
    interval: 10000,
    pause: 'hover'
  });
  
  // Переключение между блоками курсов
  $('#currencies-rates-toggle').click(function(e){
    e.preventDefault();
    $('#currencies-rates').slideUp('slow');
    $('#currencies-calculator').slideDown('slow');
  });
  $('#currencies-calculator-toggle').click(function(e){
    e.preventDefault();
    $('#currencies-calculator').slideUp('slow');
    $('#currencies-rates').slideDown('slow');
  });
  
  // Сумма Из
  
  function getFromAmount() {
    return floatValue($('#currencies-calculator-from-amount').val());
  }
  
  function setFromAmount(value) {
    $('#currencies-calculator-from-amount').val(floatFormat(value));
  }
  
  // Валюта Из
  
  function getFromCurrency() {
    return $('#currencies-calculator-from-currency').text();
  }
  
  function setFromCurrency(value) {
    $('#currencies-calculator-from-currency').text(value);
  }
  
  // Сумма В
  
  function getToAmount() {
    return floatValue($('#currencies-calculator-to-amount').val());
  }
  
  function setToAmount(value) {
    $('#currencies-calculator-to-amount').val(floatFormat(value));
  }
  
  // Валюта В
  
  function getToCurrency() {
    return $('#currencies-calculator-to-currency').text();
  }
  
  function setToCurrency(value) {
    $('#currencies-calculator-to-currency').text(value);
  }
  
  /**
   * Действия пользователя
   */
  
  $('#currencies-calculator-from-amount').change(function(){
    setFromAmount($(this).val());
    setToAmount(
      calculate(
        getFromAmount(),
        getFromCurrency(),
        getToCurrency()
      )
    );
  });
  
  $('#currencies-calculator-to-amount').change(function(){
    setToAmount($(this).val());
    setFromAmount(
      calculate(
        getToAmount(),
        getToCurrency(),
        getFromCurrency()
      )
    );
  });
  
  $('#currencies-calculator-from-currencies a').click(function(e){
    e.preventDefault();
    setFromCurrency($(this).attr('currency'));
    setToAmount(
      calculate(
        getFromAmount(),
        getFromCurrency(),
        getToCurrency()
      )
    );
  });
  
  $('#currencies-calculator-to-currencies a').click(function(e){
    e.preventDefault();
    setToCurrency($(this).attr('currency'));
    setToAmount(
      calculate(
        getFromAmount(),
        getFromCurrency(),
        getToCurrency()
      )
    );
  });
  
  /**
   * Расчет
   */
  function calculate(amount, from, to) {
    var rateFrom = calculatorRates[from];
    var rateTo   = calculatorRates[to];
    if ( ! amount) {
      amount = 0;
    }
    return amount * rateFrom / rateTo;
  }
  
  /**
  * Вспомогательные функции
  */
  
  // Разделение разрядов числа
  function floatFormat(Val) {
    Val = floatValue(Val);
    return Val.toFixed(2);
  }
  
  // Получить числовое значение
  function floatValue(Val) {
    return parseFloat(Val);
  }
  
});