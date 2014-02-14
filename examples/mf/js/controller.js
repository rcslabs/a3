
var Controller = Backbone.Router.extend({
  
  initialize: function() {
    this.userState = new UserState();
    var mainView = new MainView({model: this.userState, el: document.getElementById('multifon-content')});
    this.phone = null;
    var calls = this.calls = new CallsCollection();

    var callsListView = new CallsListView({
      el: document.getElementById('calls-list'),
      model: this.calls
    });

    try {
      var controller = this;
      var phone = this.phone = new Phone({
        afterTransition: function(fromState, state, event, params) {
          console.log("Transition ", fromState, " -> ", state, " [event=", event, params,"]");
          switch(state) {
            case 'LOGIN':
              $('#login-button').prop('disabled', false);
              if(event === "SESSION_FAILED" && fromState === "LOGINING") {
                $('#multifon-content span.error').show();
              }
              break;
            case 'LOGINING':
              break;
            case 'INITIALIZING_MEDIA':
              if($('#login-remember-me').prop('checked'))
                try {
                  console.log("Saving login-password");
                  window.localStorage.setItem( 'saved-login-phone',    $('#login-phone').val()    );
                  window.localStorage.setItem( 'saved-login-password', $('#login-password').val() );
                }catch(e) {}
              $('#login-password').val('');
              break;
            case 'HARDWARE_SETTINGS':
              break;
            case 'HARDWARE_SETTINGS_OK':
              controller.dialTab();
              break;
          }
        },
        mediaEvent: function(eventName, opt) {
          console.log("MEDIA_EVENT", eventName)
          switch(eventName) {
            case "media:add-remote-stream":
              calls.addRemoteVideoStream(opt);
              break;
            case "media:remove-remote-stream":
              calls.removeRemoteVideoStream(opt);
              break;            
          }
        }
      });

      $('#login-button').click(function() {
        $(this).prop('disabled', true);
        $('#multifon-content span.error').hide();
        phone.event("ui:login", {phone: $('#login-phone').val(),  password: $('#login-password').val()});
      });

      $('#dial-tab .pad button').click(function(){
        var $span = $(this).find('span');
        if($span.length)
          $('#dial-number').val($('#dial-number').val() + $span.text()).focus();
      });
      $('#do-call, #do-video-call').click(function() {
        var isVideo = $(this).attr('id') === 'do-video-call';
        var vv = [true, isVideo];
        var bUri = $('#dial-number').val();
        var callModel = new CallModel({
          bUri    : bUri,
          audio   : true,
          video   : isVideo,
          active  : false,
          hangup: function(){
            phone.hangup(call);
          }
        });
        controller.calls.add(callModel);
        var call = phone.startCall({bUri: bUri, vv: vv}, {
          afterTransition: function(fromState, state, event, params) {
            console.log("Transition ", fromState, " -> ", state, " [event=", event, params,"]");
            switch(state) {
              case "CALL_STARTING":
                controller.contactsTab();
                callModel.set("state", "CALL_STARTING");
                break;
              case "CALL_IN_PROGRESS":
                callModel.set("state", "CALL_IN_PROGRESS");
                break;
              case "CALL_FINISHED":
                callModel.set("state", "CALL_FINISHED");
                window.setTimeout(function() {
                  callModel.set("state", "CALL_REMOVED");
                }, 5000);
                break;
            }
          }
        });
      });

      $('.tab-header-call-log').click(function(event) { controller.callLogTab() ; event.stopPropagation(); event.preventDefault(); return false;});
      $('.tab-header-dial')    .click(function(event) { controller.dialTab()    ; event.stopPropagation(); event.preventDefault(); return false;});
      $('.tab-header-contacts').click(function(event) { controller.contactsTab(); event.stopPropagation(); event.preventDefault(); return false;});


    }catch(ex) {
      console.log("Controller failed ", ex.message, ex.stack);
    }
  },

  // test routes
  routes: {
    ""               :   "login",
    "main/contacts"  :   "contactsTab",
    "main/dial"      :   "dialTab",
    "main/call-log"  :   "callLogTab"
  },

  login: function() {
    this.userState.set("state", "login");
  },

  contactsTab: function() {
    this.userState.set("state", "main");
    this.userState.set("tab", "contacts");
  },
  dialTab: function() {
    this.userState.set("state", "main");
    this.userState.set("tab", "dial")
  },
  callLogTab: function() {
    this.userState.set("state", "main");
    this.userState.set("tab", "call-log");
  }
});




var controller = new Controller();
Backbone.history.start()
