package org.vaadin.teemu.switchui.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchCancelHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchEvent;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchMoveHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasAnimation;
import com.google.gwt.user.client.ui.HasValue;
import com.vaadin.client.ui.FontIcon;
import com.vaadin.client.ui.Icon;

/**
 * SwitchWidget is the client-side implementation of the Switch component.
 *
 * @author Teemu Pöntelin | Vaadin Ltd. | http://vaadin.com/teemu
 */
public class SwitchWidget extends FocusWidget implements HasValue<Boolean>,
        KeyUpHandler, MouseDownHandler, MouseUpHandler, MouseMoveHandler,
        FocusHandler, BlurHandler, HasAnimation, TouchStartHandler,
        TouchEndHandler, TouchMoveHandler, TouchCancelHandler {

    /** Set the CSS class name to allow styling. */
    public static final String CLASSNAME = "v-switch";
    private final int DRAG_THRESHOLD_PIXELS = 10;
    private final int ANIMATION_DURATION_MS = 300;

    private Element slider;

    private boolean value = true;
    protected com.google.gwt.user.client.Element errorIndicatorElement;
    protected Icon icon = null;

    private FaFontIcon faIconOff = null;
    private FaFontIcon faIconOn = null;

    protected boolean immediate;

    private boolean mouseDown;
    private int unvisiblePartWidth = -1;
    private final DragInformation dragInfo = new DragInformation();

    private boolean initialValueSet;
    private boolean animated;
    private boolean faIconStyle;
    private int tabIndex;
    private List<HandlerRegistration> handlers;

    public SwitchWidget() {
        setElement(Document.get().createDivElement());
        setStyleName(CLASSNAME);

        // Build the DOM.
        slider = Document.get().createDivElement();
        slider.setClassName(CLASSNAME + "-" + "slider");
        getElement().appendChild(slider);

        addHandlers();
        updateStyleName();
    }

    private int getUnvisiblePartWidth() {
        if (unvisiblePartWidth < 0) {
            int width = this.getElement().getClientWidth();
            int sliderWidth = this.slider.getClientWidth();
            if (sliderWidth - width > 0) {
                unvisiblePartWidth = sliderWidth - width;
            }
        }
        return unvisiblePartWidth;
    }

    private void addHandlers() {
        handlers = new ArrayList<HandlerRegistration>();
        handlers.add(addKeyUpHandler(this));
        handlers.add(addMouseMoveHandler(this));
        handlers.add(addMouseDownHandler(this));
        handlers.add(addMouseUpHandler(this));
        handlers.add(addFocusHandler(this));
        handlers.add(addBlurHandler(this));

        if (TouchEvent.isSupported()) {
            handlers.add(addTouchStartHandler(this));
            handlers.add(addTouchEndHandler(this));
            handlers.add(addTouchCancelHandler(this));
            handlers.add(addTouchMoveHandler(this));
        }
    }

    private void removeHandlers() {
        if (handlers != null) {
            for (HandlerRegistration handler : handlers) {
                handler.removeHandler();
            }
            handlers = null;
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (!enabled) {
            super.setTabIndex(-1);
            removeHandlers();
        } else {
            super.setTabIndex(tabIndex);
            if (handlers == null) {
                addHandlers();
            }
        }
    }

    @Override
    public void setTabIndex(int index) {
        super.setTabIndex(index);
        tabIndex = index;
    }

    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<Boolean> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public void setValue(Boolean value) {
        setValue(value, false);
    }

    @Override
    public void setValue(Boolean value, boolean fireEvents) {
        if (value == null) {
            value = Boolean.FALSE;
        }

        if (this.value != value) {
            this.value = value;

            // Update the UI.
            updateVisibleState();

            if (fireEvents) {
                ValueChangeEvent.fire(this, value);
            }
        } else {
            initialValueSet = true;
        }
    }

    private void updateVisibleState() {
        ScheduledCommand command = new ScheduledCommand() {

            @Override
            public void execute() {
                final int targetLeft = (value ? 0 : -getUnvisiblePartWidth());

                if (!isAnimationEnabled() || !initialValueSet) {
                    if(isIconStyleEnabled()){
                        if(faIconOff == null){
                            // initialize the icon style mode
                            faIconOff = new FaFontIcon(0XF204);
                            faIconOn = new FaFontIcon(0XF205);
                            slider.appendChild(faIconOff.getElement());
                            slider.appendChild(faIconOn.getElement());

                        }
                        faIconOff.getElement().getStyle().setProperty("display", value ? "none" : "inline-block");
                        faIconOn.getElement().getStyle().setProperty("display", value ? "inline-block" : "none" );

                    } else {
                        slider.getStyle().setProperty("left", targetLeft + "px");
                        updateStyleName();
                    }
                } else {
                    Animation a = new Animation() {

                        @Override
                        protected void onUpdate(double progress) {
                            int currentLeft = slider.getOffsetLeft();
                            int newLeft = (int) (currentLeft + (progress * (targetLeft - currentLeft)));
                            slider.getStyle().setProperty("left",
                                    newLeft + "px");
                        }

                        @Override
                        protected void onComplete() {
                            super.onComplete();
                            updateStyleName();
                        }
                    };
                    a.run(ANIMATION_DURATION_MS);
                }
                initialValueSet = true;
            }
        };

        if (getUnvisiblePartWidth() < 0) {
            Scheduler.get().scheduleDeferred(command);
        } else {
            command.execute();
        }
    }

    private void updateStyleName() {
        if (value) {
            addStyleName("on");
            removeStyleName("off");
        } else {
            addStyleName("off");
            removeStyleName("on");
        }
    }

    @Override
    public void onKeyUp(KeyUpEvent event) {
        if (event.getNativeKeyCode() == 32) {
            // 32 = space bar
            setValue(!value, true);
        }
    }

    @Override
    public void onMouseDown(MouseDownEvent event) {
        handleMouseDown(event.getScreenX());
        event.preventDefault();
    }

    private void handleMouseDown(int clientX) {
        mouseDown = true;
        dragInfo.setDragStartX(clientX);
        dragInfo.setDragStartOffsetLeft(slider.getOffsetLeft());
    }

    @Override
    public void onMouseUp(MouseUpEvent event) {
        handleMouseUp(event.getNativeEvent());
    }

    private void handleMouseUp(NativeEvent event) {
        if (!dragInfo.isDragging()) {
            setValue(!value, true);
        } else {
            if (slider.getOffsetLeft() < (-getUnvisiblePartWidth() / 2)) {
                setValue(false, true);
            } else {
                setValue(true, true);
            }
            updateVisibleState();
            DOM.releaseCapture(getElement());
        }

        mouseDown = false;
        dragInfo.setDragging(false); // not dragging anymore
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {
        handleMouseMove(event.getScreenX());
    }

    private void handleMouseMove(int clientX) {
        if (mouseDown) {
            if (Math.abs(dragInfo.getDragDistanceX(clientX)) > DRAG_THRESHOLD_PIXELS) {
                dragInfo.setDragging(true);
                // Use capture to catch mouse events even if user
                // drags the mouse cursor out of the widget area.
                DOM.setCapture(getElement());
            }

            if (dragInfo.isDragging()) {
                int dragDistance = dragInfo.getDragDistanceX(clientX);

                if(isIconStyleEnabled()){

                    faIconOff.getElement().getStyle().setProperty("display", "inline-block");
                    faIconOn.getElement().getStyle().setProperty("display", "inline-block");

                    int width = this.getElement().getClientWidth();
                    double dragArea = width * 2;
                    double dragval =  Math.abs(dragDistance);
                    if(dragval > dragArea){
                        dragval = dragArea;
                    }
                    double opacity = dragval / dragArea;
                    boolean onDirection = dragval > 0;
                    double opacityOn = onDirection ? opacity : 1 - opacity;
                    double opacityOff = !onDirection ? opacity : 1 - opacity;
                    faIconOff.getElement().getStyle().setProperty("opacity", Double.toString(opacityOn));
                    faIconOn.getElement().getStyle().setProperty("opacity", Double.toString(opacityOff));

                } else {
                    // calculate new left position and
                    // check for boundaries
                    int left = dragInfo.getDragStartOffsetLeft() + dragDistance;
                    if (left < -getUnvisiblePartWidth()) {
                        left = -getUnvisiblePartWidth();
                    } else if (left > 0) {
                        left = 0;
                    }
                    // set the CSS left
                    slider.getStyle().setProperty("left", left + "px");
                }
            }
        }
    }

    @Override
    public void onFocus(FocusEvent event) {
        addStyleDependentName("focus");
    }

    @Override
    public void onBlur(BlurEvent event) {
        removeStyleDependentName("focus");
    }

    @Override
    public boolean isAnimationEnabled() {
        return animated;
    }

    @Override
    public void setAnimationEnabled(boolean enable) {
        animated = enable;
    }

    @Override
    public void onTouchCancel(TouchCancelEvent event) {
        handleMouseUp(event.getNativeEvent());
    }

    @Override
    public void onTouchMove(TouchMoveEvent event) {
        Touch touch = event.getTouches().get(0).cast();
        handleMouseMove(touch.getPageX());
        event.preventDefault();
    }

    @Override
    public void onTouchEnd(TouchEndEvent event) {
        handleMouseUp(event.getNativeEvent());
    }

    @Override
    public void onTouchStart(TouchStartEvent event) {
        Touch touch = event.getTouches().get(0).cast();
        handleMouseDown(touch.getPageX());
        event.preventDefault();
    }

    public void setIconStyleEnabled(boolean faIconStyle) {
        boolean refreshUI = this.faIconStyle != faIconStyle;
        this.faIconStyle = faIconStyle;
        if(refreshUI){
            updateVisibleState();
        }

    }

    public boolean isIconStyleEnabled(){
        return faIconStyle;
    }

    class FaFontIcon extends FontIcon {

        public FaFontIcon(int codepoint) {
            super();
            setCodepoint(codepoint);
            setFontFamily("FontAwesome");

        }

    }

}
