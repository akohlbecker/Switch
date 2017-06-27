package org.vaadin.teemu.switchui;

import org.vaadin.teemu.switchui.client.SwitchState;

import com.vaadin.data.Property;
import com.vaadin.ui.CheckBox;

/**
 * Switch is basically a decorated CheckBox. Server-side API has all the same
 * functionality and added support for enabling and disabling animation.
 *
 * @see com.vaadin.ui.CheckBox
 * @author Teemu Pöntelin | Vaadin Ltd. | https://vaadin.com/teemu
 */
@SuppressWarnings("serial")
public class Switch extends CheckBox {

    /**
     *
     */
    private static final String FA_ICON_STYLE_NAME = "faIcon";

    public Switch() {
        super();
    }

    public Switch(String caption) {
        super(caption);
    }

    public Switch(String caption, boolean initialState) {
        super(caption, initialState);
    }

    public Switch(String caption, Property<Boolean> dataSource) {
        super(caption, dataSource);
    }

    @Override
    protected SwitchState getState() {
        return (SwitchState) super.getState();
    }

    public boolean isAnimationEnabled() {
        return getState().animated;
    }

    public void setAnimationEnabled(boolean enabled) {
        if (getState().animated != enabled) {
            getState().animated = enabled;
        }
    }

    public void setFaIconStyle(boolean enabled) {
        if (getState().faIconStyle != enabled) {
            getState().faIconStyle = enabled;
        }
        if(getState().faIconStyle && getState().animated){
            getState().animated = false;
        }
        if(enabled){
            setStyleName(FA_ICON_STYLE_NAME);
        }
    }

}
