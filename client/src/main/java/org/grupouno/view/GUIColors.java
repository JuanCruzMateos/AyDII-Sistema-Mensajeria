package org.grupouno.view;

import java.awt.*;

public class GUIColors {
    private static final Color LIGHTbackgroundColor = new Color(183, 228, 240);
    public static final SwitchableColor backgroundColor = new SwitchableColor(LIGHTbackgroundColor);
    private static final Color LIGHTbuttonColor = new Color(153, 217, 234);
    public static final SwitchableColor buttonColor = new SwitchableColor(LIGHTbuttonColor);
    private static final Color LIGHTtextFieldColor = new Color(213, 240, 246);
    public static final SwitchableColor textFieldColor = new SwitchableColor(LIGHTtextFieldColor);
    private static final Color LIGHTtextFontColor = new Color(0, 0, 0);
    public static final SwitchableColor textFontColor = new SwitchableColor(LIGHTtextFontColor);

    //Darker... No, darker... No no, DARKER... DARKER!!!
    private static final Color DARKbackgroundColor = LIGHTbackgroundColor.darker().darker().darker().darker();
    private static final Color DARKbuttonColor = LIGHTbuttonColor.darker().darker().darker().darker();
    private static final Color DARKtextFieldColor = LIGHTtextFieldColor.darker().darker().darker().darker();
    private static final Color DARKtextFontColor = LIGHTbackgroundColor;
    private static int theme = 0;

    public static void switchTheme() {
        if (theme == 1) {
            backgroundColor.setColor(LIGHTbackgroundColor);
            buttonColor.setColor(LIGHTbuttonColor);
            textFieldColor.setColor(LIGHTtextFieldColor);
            textFontColor.setColor(LIGHTtextFontColor);
        } else {
            backgroundColor.setColor(DARKbackgroundColor);
            buttonColor.setColor(DARKbuttonColor);
            textFieldColor.setColor(DARKtextFieldColor);
            textFontColor.setColor(DARKtextFontColor);
        }
        theme ^= 1; //theme XOR 1 =>  0<->1
    }

}
