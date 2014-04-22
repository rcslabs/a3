package com.rcslabs.click2call;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sx on 21.04.14.
 */
@Service
public class ButtonServiceImpl implements ButtonService {

    @Autowired
    private ButtonDAO dao;

    @Override
    public String getEmailByButtonId(String id) {
        return dao.getEmailByButtonId(id);
    }

    @Override
    public List<ButtonEntry> getButtons() {
        return dao.getButtonList();
    }

    @Override
    public Map<String, String> getButtonsTitle() {
        List<ButtonEntry> list = getButtons();
        HashMap<String, String> res = new LinkedHashMap<String, String>();
        for(ButtonEntry be : list){
            res.put(be.getButtonId(), be.getTitle());
        }
        return res;
    }

    @Override
    public ButtonEntry getButtonByTitle(String value) {
        return dao.getButtonByTitle(value);
    }
}
