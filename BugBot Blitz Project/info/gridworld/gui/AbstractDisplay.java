

package info.gridworld.gui;

import java.awt.Rectangle;
import java.beans.*;
import java.lang.reflect.*;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.BasicStroke;


public abstract class AbstractDisplay implements Display
{

    abstract public void draw(Object obj, Component comp, Graphics2D g2);


    public void draw(Object obj, Component comp, Graphics2D g2, Rectangle rect)
    {
        float scaleFactor = Math.min(rect.width, rect.height);
        g2 = (Graphics2D) g2.create();


        g2.translate(rect.x + rect.width / 2.0, rect.y + rect.height / 2.0);


        if (obj != null)
        {
            Integer direction = (Integer) getProperty(obj, "direction");
            int rotationInDegrees = direction == null ? 0 : direction
                    .intValue();
            g2.rotate(Math.toRadians(rotationInDegrees));
        }

        g2.scale(scaleFactor, scaleFactor);
        g2.setStroke(new BasicStroke(1.0f / scaleFactor));
        draw(obj, comp, g2);
    }

    public static Object getProperty(Object obj, String propertyName)
    {
        if (obj == null)
            return null;
        try
        {
            BeanInfo info = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
            for (int i = 0; i < descriptors.length; i++)
            {
                if (descriptors[i].getName().equals(propertyName))
                {
                    Method getter = descriptors[i].getReadMethod();
                    if (getter == null)
                        return null;
                    try {
                    return getter.invoke(obj);
                    } catch (Exception ex) {
                        System.out.println(descriptors[i].getName());
                        return null;
                    }
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
}
