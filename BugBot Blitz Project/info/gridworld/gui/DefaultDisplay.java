

package info.gridworld.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Component;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.Rectangle;



public class DefaultDisplay implements Display
{
    private static final int MAX_TEXT_LENGTH = 8;


    public void draw(Object obj, Component comp, Graphics2D g2, Rectangle rect)
    {
        Color color = (Color) AbstractDisplay.getProperty(obj, "color");
        if (color == null && obj instanceof Color)
            color = (Color) obj;
        Color textColor = (Color) AbstractDisplay.getProperty(obj, "textColor");
        if (textColor == null) textColor = Color.BLACK;
        if (color != null)
        {
            g2.setPaint(color);
            g2.fill(rect);

            if (color.equals(textColor))
            {
                textColor = new Color(
                        255 - textColor.getRed(),
                        255 - textColor.getGreen(), 
                        255 - textColor.getBlue()); 
            }
        }
        String text = (String) AbstractDisplay.getProperty(obj, "text");
        if (text == null && !(obj instanceof Color))
        {
            text = "" + obj;
        }
        if (text == null) return;
        if (text.length() > MAX_TEXT_LENGTH)
            text = text.substring(0, MAX_TEXT_LENGTH) + "...";
        paintCenteredText(g2, text, rect, 0.8, textColor);
    }


    protected void paintCenteredText(Graphics2D g2, String s, Rectangle rect,
            double fontHeight, Color color)
    {
        g2 = (Graphics2D) g2.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(color);
        Rectangle2D bounds = null;
        LineMetrics lm = null;
        boolean done = false;
        // shrink font in increments of sqrt(2)/2 until string fits
        while (!done)
        {
            g2.setFont(new Font("SansSerif", Font.BOLD,
                    (int) (fontHeight * rect.height)));
            FontRenderContext frc = g2.getFontRenderContext();
            bounds = g2.getFont().getStringBounds(s, frc);
            if (bounds.getWidth() > rect.getWidth())
                fontHeight = fontHeight * Math.sqrt(2) / 2;
            else
            {
                done = true;
                lm = g2.getFont().getLineMetrics(s, frc);
            }
        }
        float centerX = rect.x + rect.width / 2;
        float centerY = rect.y + rect.height / 2;
        float leftX = centerX - (float) bounds.getWidth() / 2;
        float baselineY = centerY - lm.getHeight() / 2 + lm.getAscent();
        g2.drawString(s, leftX, baselineY);
        g2.dispose();
    }
}
