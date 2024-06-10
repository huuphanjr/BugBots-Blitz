/* 
 * AP(r) Computer Science GridWorld Case Study:
 * Copyright(c) 2005-2006 Cay S. Horstmann (http://horstmann.com)
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
 * @author Cay Horstmann
 */

package info.gridworld.actor;

import java.awt.Color;



public class Rock extends Actor
{
    
    
    private static final Color DEFAULT_COLOR = Color.BLACK;


    public Rock()
    {
        setColor(DEFAULT_COLOR);
    }


    public Rock(Color rockColor)
    {
        setColor(rockColor);
    }


    public void act()
    {
    }
}
