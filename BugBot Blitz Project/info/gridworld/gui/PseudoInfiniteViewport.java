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
 * @author Julie Zelenski
 * @author Cay Horstmann
 */

package info.gridworld.gui;

import java.awt.Point;
import java.awt.Dimension;
import java.awt.Color;
import javax.swing.*;



public class PseudoInfiniteViewport extends JViewport
{

    public interface Pannable
    {
        void panBy(int hDelta, int vDelta);

        boolean isPannableUnbounded();

        void showPanTip();
    }

    private JScrollPane scrollParent;
    private Point panPoint = new Point(0, 0);


    public PseudoInfiniteViewport(JScrollPane parent)
    {
        scrollParent = parent;
        setBackground(Color.lightGray);
    }


    public void setViewPosition(Point pt)
    {
        boolean isAdjusting = scrollParent.getVerticalScrollBar()
                .getValueIsAdjusting()
                || scrollParent.getHorizontalScrollBar().getValueIsAdjusting();
        boolean changed = true;

        if (viewIsUnbounded())
        {
            int hDelta = pt.x - panPoint.x;
            int vDelta = pt.y - panPoint.y;
            if (hDelta != 0 && vDelta == 0)
                getPannableView().panBy(hDelta, vDelta);
            else if (vDelta != 0 && hDelta == 0)
                getPannableView().panBy(hDelta, vDelta);
            else
                changed = false; // no pan action was taken
            panPoint = pt;
            if (!panPoint.equals(getPanCenterPoint()) && !isAdjusting)
            { // needs recentering
                panPoint = getPanCenterPoint();
                fireStateChanged(); // update scrollbars to match
            }
        }
        else
        // ordinary scroll behavior
        {
            changed = !getViewPosition().equals(pt);
            super.setViewPosition(pt);
        }
        if (changed || isAdjusting)
            getPannableView().showPanTip(); // briefly show tip
    }


    public Point getViewPosition()
    {
        return (viewIsUnbounded() ? getPanCenterPoint() : super
                .getViewPosition());
    }


    public Dimension getViewSize()
    {
        return (viewIsUnbounded() ? getView().getPreferredSize() : super
                .getViewSize());
    }

    // some simple private helpers

    private Pannable getPannableView()
    {
        return (Pannable) getView();
    }

    private boolean viewIsUnbounded()
    {
        Pannable p = getPannableView();
        return (p != null && p.isPannableUnbounded());
    }

    private Point getPanCenterPoint()
    {
        Dimension size = getViewSize();
        return new Point(size.width / 2, size.height / 2);
    }
}
