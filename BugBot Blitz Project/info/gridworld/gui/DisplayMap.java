/* 
 * AP(r) Computer Science GridWorld Case Study:
 * Copyright(c) 2002-2006 College Entrance Examination Board 
 * (http://www.collegeboard.com).
 *
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * @author Alyce Brady
 * @author Jeff Raab, Northeastern University
 * @author Cay Horstmann
 */

package info.gridworld.gui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.HashMap;

import javax.swing.Icon;


public class DisplayMap
{
    private HashMap<Class, Display> map = new HashMap<Class, Display>();
    private Display defaultDisplay = new DefaultDisplay();



    private Display createDisplay(Class cl)
    {
        try
        {
            String className = cl.getName();
            Class dcl = Class.forName(className + "Display");
            if (Display.class.isAssignableFrom(dcl))
            {
                Display display = (Display) dcl.newInstance();
                map.put(cl, display);
                return display;
            }
        }
        catch (Exception e)
        {
            // oh well...
        }

        try
        {
            ImageDisplay display = new ImageDisplay(cl);
            map.put(cl, display);
            return display;
        }
        catch (Exception e)
        {

        }

        return null;
    }


    public Display findDisplayFor(Class cl)
    {


        if (cl == Object.class)
            return defaultDisplay;
        Display display = map.get(cl);
        if (display != null)
            return display;
        display = createDisplay(cl);
        if (display != null)
        {
            map.put(cl, display);
            return display;
        }
        display = findDisplayFor(cl.getSuperclass());
        map.put(cl, display);
        return display;
    }


    public Icon getIcon(Class cl, int w, int h)
    {
        return new DisplayIcon(cl, w, h);
    }

    private class DisplayIcon implements Icon
    {
        private Display displayObj;
        private int width, height;

        public DisplayIcon(Class cl, int w, int h)
        {
            displayObj = findDisplayFor(cl);
            width = w;
            height = h;
        }

        public int getIconWidth()
        {
            return width;
        }

        public int getIconHeight()
        {
            return height;
        }

        public void paintIcon(Component comp, Graphics g, int x, int y)
        {
            Graphics2D g2 = (Graphics2D) g;
            AffineTransform savedTransform = g2.getTransform(); // save current
            displayObj.draw(null, comp, g2, new Rectangle(x, y, getIconWidth(),
                    getIconHeight()));
            g2.setTransform(savedTransform); // restore coordinate system
        }
    }
}
