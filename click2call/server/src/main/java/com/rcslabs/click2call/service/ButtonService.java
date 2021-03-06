package com.rcslabs.click2call.service;

import com.rcslabs.click2call.entity.ButtonEntry;

import java.util.List;
import java.util.Map;

/**
 * Created by sx on 21.04.14.
 */
public interface ButtonService {
    String getEmailByButtonId(String id);
    List<ButtonEntry> getButtons();
    Map<String, String> getButtonsTitle();
    ButtonEntry getButtonByTitle(String value);
}
