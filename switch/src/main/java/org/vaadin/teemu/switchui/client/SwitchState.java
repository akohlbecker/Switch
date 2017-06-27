package org.vaadin.teemu.switchui.client;

import com.vaadin.shared.ui.checkbox.CheckBoxState;

@SuppressWarnings("serial")
public class SwitchState extends CheckBoxState {
    {
        primaryStyleName = "v-switch";
    }

    public boolean animated = true;

    /**
     * true:
     *   the SwitchWidget will be using FontAwesome toggle icons
     *   This implies animated = false
     * false:
     *   theme the widget using the image from resources/org/vaadin/teemu/switchui/public/switch/images/
     */
    public boolean faIconStyle = true;
}
