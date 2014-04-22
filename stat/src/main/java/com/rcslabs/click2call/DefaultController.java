package com.rcslabs.click2call;

import org.apache.commons.httpclient.contrib.ssl.EasyX509TrustManager;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@Controller()
@RequestMapping("/")
public class DefaultController {

    private static final String HOSTNAME = "webrtc.v2chat.com";

    private static final String CALLBACK_FORM_ID = "id";
    private static final String CALLBACK_FORM_LANG = "lang";

    private static final String CALLBACK_FORM_NAME = "name";
    private static final String CALLBACK_FORM_PHONE = "phone";
    private static final String CALLBACK_FORM_DATE = "date";
    private static final String CALLBACK_FORM_SUBJECT = "subject";
    private static final String CALLBACK_FORM_MESSAGE = "message";

    private static final String CALLBACK_FORM_LABEL_FOR_NAME = "label4name";
    private static final String CALLBACK_FORM_LABEL_FOR_PHONE = "label4phone";
    private static final String CALLBACK_FORM_LABEL_FOR_DATE = "label4date";
    private static final String CALLBACK_FORM_LABEL_FOR_SUBJECT = "label4subject";
    private static final String CALLBACK_FORM_LABEL_FOR_MESSAGE = "label4message";

    private static final String SMTP_HOST = "mail.luxms.com";
    private static final int    SMTP_PORT = 465;
    private static final String SMTP_ACCOUNT = "click2call@v2chat.com";
    private static final String SMTP_PASSWORD = "pereklikali!";
    private static final String EMAIL_BCC = "allfeedbacks@v2chat.com";
    private static final String EMAIL_SUBJECT = "click2call callback form";

    private static final String HTTP_GET_ACCOUNT_NAME = "accountname";

    private SSLContext ssl;

    @Autowired
    private ButtonService buttonService;

    public DefaultController(){
        try{
            ssl = SSLContext.getInstance("TLS");
            ssl.init(
                    null,
                    new TrustManager[] {(TrustManager)new EasyX509TrustManager(null)},
                    new SecureRandom() );
            ssl.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
            SSLContext.setDefault(ssl);
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            System.out.println(e);
        }
    }

    @RequestMapping(value="/click2call", method=RequestMethod.GET)
    public String handleWebimRequest(HttpServletRequest request, ModelMap model)
    {
        String accountName = HtmlUtils.htmlEscape(request.getParameter(HTTP_GET_ACCOUNT_NAME));
        if(null == accountName) throw new NullPointerException("Parameter accountname is a must.");

        ButtonEntry button = buttonService.getButtonByTitle(accountName);
        if(null == button) throw new NullPointerException("No any button for requested accountname.");

        model.addAttribute("buttonId", button.getButtonId());
        model.addAttribute("hostname", HOSTNAME);

        return "click2call";
    }

    @RequestMapping(value="/service/callback", method=RequestMethod.POST)
    public ResponseEntity<String> handleCallbackForm(HttpServletRequest request){
        try {
            String id   = HtmlUtils.htmlEscape(request.getParameter(CALLBACK_FORM_ID));
            if(null == id){
                throw new Exception("Parameter id is a must.");
            }

            String emailTo = buttonService.getEmailByButtonId(id);
            if(null == emailTo){
                throw new Exception("Email for id="+id+" not found.");
            }

            String lang = HtmlUtils.htmlEscape(request.getParameter(CALLBACK_FORM_LANG));

            // hashmap for sanitized parameters as label : value
            Map<String, String> params = new HashMap<String, String>();
            params.put(HtmlUtils.htmlEscape(request.getParameter(CALLBACK_FORM_LABEL_FOR_NAME)),
                    HtmlUtils.htmlEscape(request.getParameter(CALLBACK_FORM_NAME)));
            params.put(HtmlUtils.htmlEscape(request.getParameter(CALLBACK_FORM_LABEL_FOR_PHONE)),
                    HtmlUtils.htmlEscape(request.getParameter(CALLBACK_FORM_PHONE)));
            params.put(HtmlUtils.htmlEscape(request.getParameter(CALLBACK_FORM_LABEL_FOR_DATE)),
                    HtmlUtils.htmlEscape(request.getParameter(CALLBACK_FORM_DATE)));
            params.put(HtmlUtils.htmlEscape(request.getParameter(CALLBACK_FORM_LABEL_FOR_SUBJECT)),
                    HtmlUtils.htmlEscape(request.getParameter(CALLBACK_FORM_SUBJECT)));
            params.put(HtmlUtils.htmlEscape(request.getParameter(CALLBACK_FORM_LABEL_FOR_MESSAGE)),
                    HtmlUtils.htmlEscape(request.getParameter(CALLBACK_FORM_MESSAGE)));

            StringBuffer msg = new StringBuffer();
            msg.append("<html lang=\"").append(lang).append("\"><head>")
               .append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">")
               .append("</head>");

            // body
            msg.append("<body");
            if("he".equals(lang)){ msg.append(" style=\"direction:rtl;\""); }
            msg.append(">");

            // table
            msg.append("<table width=\"600\"");
            if("he".equals(lang)){ msg.append(" style=\"direction:rtl;\""); }
            msg.append(">");

            for(String label : params.keySet()){
                msg.append("<tr>")
                   .append("<td valign=\"top\" style=\"border-bottom:1px dotted black;\">").append(label).append("</td>")
                   .append("<td style=\"border-bottom:1px dotted black;\">").append(params.get(label)).append("</td>")
                   .append("</tr>");
            }

            msg.append("</table>")
               .append("</body></html>");

            HtmlEmail em = new HtmlEmail();
            em.setHostName(SMTP_HOST);
            em.setSmtpPort(SMTP_PORT);
            em.setAuthentication(SMTP_ACCOUNT, SMTP_PASSWORD);
            em.setSSLCheckServerIdentity(false);
            em.setSSLOnConnect(true);

            em.setFrom(SMTP_ACCOUNT);
            em.addTo(emailTo);
            em.addBcc(EMAIL_BCC);

            em.setSubject(EMAIL_SUBJECT);
            em.setHtmlMsg(msg.toString());

            em.send();

            return new ResponseEntity<String>("OK", HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @ExceptionHandler(Exception.class)
    public ModelAndView handleError(HttpServletRequest req, HttpServletResponse res, Exception exception) {
        //logger.error("Request: " + req.getRequestURL() + " raised " + exception);
        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", exception);
        mav.addObject("url", req.getRequestURL());
        mav.setViewName("error");
        res.setStatus(500);
        return mav;
    }


    private static class DefaultTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] arg0, String arg1)
                throws java.security.cert.CertificateException {
            // TODO Auto-generated method stub

        }

        @Override
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] arg0, String arg1)
                throws java.security.cert.CertificateException {
            // TODO Auto-generated method stub

        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
