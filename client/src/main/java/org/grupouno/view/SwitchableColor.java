package org.grupouno.view;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;

/**
 * Wrapper class para Color, de forma de poder hacerla "Switcheable"
 */
public class SwitchableColor extends Color {

    private Color realColor;

    public SwitchableColor(Color c) {
        super(c.getRed(), c.getGreen(), c.getBlue());
        setColor(c);
    }

    public void setColor(Color c) {
        realColor = c;
    }

    public int getRed() {
        return realColor.getRed();
    }

    public int getGreen() {
        return realColor.getGreen();
    }

    public int getBlue() {
        return realColor.getBlue();
    }

    public int getAlpha() {
        return realColor.getAlpha();
    }

    public int getRGB() {
        return realColor.getRGB();
    }

    public Color brighter() {
        return realColor.brighter();
    }

    public Color darker() {
        return realColor.darker();
    }

    public int hashCode() {
        return realColor.hashCode();
    }

    public boolean equals(Object obj) {
        return realColor.equals(obj);
    }

    public String toString() {
        return realColor.toString();
    }

    public float[] getRGBComponents(float[] compArray) {
        return realColor.getRGBComponents(compArray);
    }

    public float[] getRGBColorComponents(float[] compArray) {
        return realColor.getRGBColorComponents(compArray);
    }

    public float[] getComponents(float[] compArray) {
        return realColor.getComponents(compArray);
    }

    public float[] getColorComponents(float[] compArray) {
        return realColor.getColorComponents(compArray);
    }

    public float[] getComponents(ColorSpace cspace, float[] compArray) {
        return realColor.getComponents(cspace, compArray);
    }

    public float[] getColorComponents(ColorSpace cspace, float[] compArray) {
        return realColor.getColorComponents(cspace, compArray);
    }

    public ColorSpace getColorSpace() {
        return realColor.getColorSpace();
    }

    public synchronized PaintContext createContext(ColorModel cm, Rectangle r,
                                                   Rectangle2D r2d,
                                                   AffineTransform xform,
                                                   RenderingHints hints) {
        return realColor.createContext(cm, r, r2d, xform, hints);
    }

    public int getTransparency() {
        return realColor.getTransparency();
    }

}
