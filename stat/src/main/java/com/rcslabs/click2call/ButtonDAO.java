package com.rcslabs.click2call;

import java.util.List;

/**
 * Created by sx on 21.04.14.
 */
public interface ButtonDAO {
    String getEmailByButtonId(String id);
    List<ButtonEntry> getButtonList();
}
