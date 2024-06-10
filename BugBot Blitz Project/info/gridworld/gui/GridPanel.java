

package info.gridworld.gui;

import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;


public class GridPanel extends JPanel implements Scrollable,
        PseudoInfiniteViewport.Pannable
{
    private static final int MIN_CELL_SIZE = 12;
    private static final int DEFAULT_CELL_SIZE = 48;
    private static final int DEFAULT_CELL_COUNT = 10;
    private static final int TIP_DELAY = 1000;

    private Grid<?> grid;
    private int numRows, numCols, originRow, originCol;
    private int cellSize; // the size of each cell, EXCLUDING the gridlines
    private boolean toolTipsEnabled;
    private Color backgroundColor = Color.WHITE;
    private ResourceBundle resources;
    private DisplayMap displayMap;
    private Location currentLocation;
    private Timer tipTimer;
    private JToolTip tip;
    private JPanel glassPane;
    

    public GridPanel(DisplayMap map, ResourceBundle res)
    {
        displayMap = map;
        resources = res;
        setToolTipsEnabled(true);
    }


    public void paintComponent(Graphics g)
    {
        Graphics2D g2 = (Graphics2D) g;

        super.paintComponent(g2);
        if (grid == null)
            return;

        Insets insets = getInsets();
        g2.setColor(backgroundColor); 
        g2.fillRect(insets.left, insets.top, numCols * (cellSize + 1) + 1, numRows
                * (cellSize + 1) + 1);

        drawWatermark(g2);
        drawGridlines(g2);
        drawOccupants(g2);
        drawCurrentLocation(g2);
    }


    private void drawOccupant(Graphics2D g2, int xleft, int ytop, Object obj)
    {
        Rectangle cellToDraw = new Rectangle(xleft, ytop, cellSize, cellSize);


        if (cellToDraw.intersects(g2.getClip().getBounds()))
        {
            Graphics2D g2copy = (Graphics2D) g2.create();
            g2copy.clip(cellToDraw);

            Display displayObj = displayMap.findDisplayFor(obj.getClass());
            displayObj.draw(obj, this, g2copy, cellToDraw);
            g2copy.dispose();
        }
    }


    private void drawGridlines(Graphics2D g2)
    {
        Rectangle curClip = g2.getClip().getBounds();
        int top = getInsets().top, left = getInsets().left;

        int miny = Math.max(0, (curClip.y - top) / (cellSize + 1)) * (cellSize + 1) + top;
        int minx = Math.max(0, (curClip.x - left) / (cellSize + 1)) * (cellSize + 1) + left;
        int maxy = Math.min(numRows, 
                (curClip.y + curClip.height - top + cellSize) / (cellSize + 1)) 
                * (cellSize + 1) + top;
        int maxx = Math.min(numCols, 
                (curClip.x + curClip.width - left + cellSize) / (cellSize + 1))
                * (cellSize + 1) + left;

        g2.setColor(Color.GRAY);
        for (int y = miny; y <= maxy; y += cellSize + 1)
            for (int x = minx; x <= maxx; x += cellSize + 1)
            {
                Location loc = locationForPoint(
                        new Point(x + cellSize / 2, y + cellSize / 2));
                if (loc != null && !grid.isValid(loc))
                    g2.fillRect(x + 1, y + 1, cellSize, cellSize);
            }

        g2.setColor(Color.BLACK);
        for (int y = miny; y <= maxy; y += cellSize + 1)

            g2.drawLine(minx, y, maxx, y);

        for (int x = minx; x <= maxx; x += cellSize + 1)

            g2.drawLine(x, miny, x, maxy);
    }


    private void drawOccupants(Graphics2D g2)
    {
        ArrayList<Location> occupantLocs = grid.getOccupiedLocations();
        for (int index = 0; index < occupantLocs.size(); index++)
        {
            Location loc = (Location) occupantLocs.get(index);
            
            int xleft = colToXCoord(loc.getCol());
            int ytop = rowToYCoord(loc.getRow());
            drawOccupant(g2, xleft, ytop, grid.get(loc));
        }
    }

    private void drawCurrentLocation(Graphics2D g2)
    {
        if ("hide".equals(System.getProperty("info.gridworld.gui.selection")))
            return;
        if (currentLocation != null)
        {
            Point p = pointForLocation(currentLocation);
            g2.drawRect(p.x - cellSize / 2 - 2, p.y - cellSize / 2 - 2,
                    cellSize + 3, cellSize + 3);
        }
    }


    private void drawWatermark(Graphics2D g2)
    {
        if ("hide".equals(System.getProperty("info.gridworld.gui.watermark")))
            return;
        g2 = (Graphics2D) g2.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        Rectangle rect = getBounds();
        g2.setPaint(new Color(0xE3, 0xD3, 0xD3));
        final int WATERMARK_FONT_SIZE = 100;
        String s = resources.getString("version.id");
        if ("1.0".compareTo(s) <= 0) return;
        g2.setFont(new Font("SansSerif", Font.BOLD, WATERMARK_FONT_SIZE));
        FontRenderContext frc = g2.getFontRenderContext();
        Rectangle2D bounds = g2.getFont().getStringBounds(s, frc);
        float centerX = rect.x + rect.width / 2;
        float centerY = rect.y + rect.height / 2;
        float leftX = centerX - (float) bounds.getWidth() / 2;
        LineMetrics lm = g2.getFont().getLineMetrics(s, frc);
        float baselineY = centerY - lm.getHeight() / 2 + lm.getAscent();
        g2.drawString(s, leftX, baselineY);
    }

    public void setToolTipsEnabled(boolean flag)
    {
        if ("hide".equals(System.getProperty("info.gridworld.gui.tooltips")))
            flag = false;
        if (flag)
            ToolTipManager.sharedInstance().registerComponent(this);
        else
            ToolTipManager.sharedInstance().unregisterComponent(this);
        toolTipsEnabled = flag;
    }


    public void setGrid(Grid<?> gr)
    {
        currentLocation = new Location(0, 0);
        JViewport vp = getEnclosingViewport(); // before changing, reset
        // scroll/pan position
        if (vp != null)
            vp.setViewPosition(new Point(0, 0));

        grid = gr;
        originRow = originCol = 0;

        if (grid.getNumRows() == -1 && grid.getNumCols() == -1)
        {
            numRows = numCols = 2000; 
            // This determines the "virtual" size of the pan world
        }
        else
        {
            numRows = grid.getNumRows();
            numCols = grid.getNumCols();
        }
        recalculateCellSize(MIN_CELL_SIZE);        
    }

    // private helpers to calculate extra width/height needs for borders/insets.
    private int extraWidth()
    {
        return getInsets().left + getInsets().right;
    }

    private int extraHeight()
    {
        return getInsets().top + getInsets().left;
    }


    public Dimension getPreferredSize()
    {
        return new Dimension(numCols * (cellSize + 1) + 1 + extraWidth(), 
                numRows * (cellSize + 1) + 1 + extraHeight());
    }


    public Dimension getMinimumSize()
    {
        return new Dimension(numCols * (MIN_CELL_SIZE + 1) + 1 + extraWidth(), 
                numRows * (MIN_CELL_SIZE + 1) + 1 + extraHeight());
    }


    public void zoomIn()
    {
        cellSize *= 2;
        revalidate();
    }


    public void zoomOut()
    {
        cellSize = Math.max(cellSize / 2, MIN_CELL_SIZE);
        revalidate();
    }


    public void recenter(Location loc)
    {
        originRow = loc.getRow();
        originCol = loc.getCol();
        repaint();
        JViewport vp = getEnclosingViewport();
        if (vp != null)
        {
            if (!isPannableUnbounded()
                    || !(vp instanceof PseudoInfiniteViewport))
                vp.setViewPosition(pointForLocation(loc));
            else
                showPanTip();
        }
    }


    public Location locationForPoint(Point p)
    {
        return new Location(yCoordToRow(p.y), xCoordToCol(p.x));
    }

    public Point pointForLocation(Location loc)
    {
        return new Point(colToXCoord(loc.getCol()) + cellSize / 2,
                rowToYCoord(loc.getRow()) + cellSize / 2);
    }

    // private helpers to convert between (x,y) and (row,col)
    private int xCoordToCol(int xCoord)
    {
        return (xCoord - 1 - getInsets().left) / (cellSize + 1) + originCol;
    }

    private int yCoordToRow(int yCoord)
    {
        return (yCoord - 1 - getInsets().top) / (cellSize + 1) + originRow;
    }

    private int colToXCoord(int col)
    {
        return (col - originCol) * (cellSize + 1) + 1 + getInsets().left;
    }

    private int rowToYCoord(int row)
    {
        return (row - originRow) * (cellSize + 1) + 1 + getInsets().top;
    }


    public String getToolTipText(MouseEvent evt)
    {
        Location loc = locationForPoint(evt.getPoint());
        return getToolTipText(loc);
    }

    private String getToolTipText(Location loc)
    {
        if (!toolTipsEnabled || loc == null || !grid.isValid(loc))
            return null;
        Object f = grid.get(loc);
        if (f != null)
            return MessageFormat.format(resources
                    .getString("cell.tooltip.nonempty"), new Object[]
                { loc, f });
        else
            return MessageFormat.format(resources
                    .getString("cell.tooltip.empty"), new Object[]
                { loc, f });
    }


    public void setCurrentLocation(Location loc)
    {
        currentLocation = loc;
    }


    public Location getCurrentLocation()
    {
        return currentLocation;
    }


    public void moveLocation(int dr, int dc)
    {
        Location newLocation = new Location(currentLocation.getRow() + dr,
                currentLocation.getCol() + dc);
        if (!grid.isValid(newLocation))
            return;

        currentLocation = newLocation;

        JViewport viewPort = getEnclosingViewport();
        if (isPannableUnbounded())
        {
            if (originRow > currentLocation.getRow())
                originRow = currentLocation.getRow();
            if (originCol > currentLocation.getCol())
                originCol = currentLocation.getCol();
            Dimension dim = viewPort.getSize();
            int rows = dim.height / (cellSize + 1);
            int cols = dim.width / (cellSize + 1);
            if (originRow + rows - 1 < currentLocation.getRow())
                originRow = currentLocation.getRow() - rows + 1;
            if (originCol + rows - 1 < currentLocation.getCol())
                originCol = currentLocation.getCol() - cols + 1;
        }
        else if (viewPort != null)
        {
            int dx = 0;
            int dy = 0;
            Point p = pointForLocation(currentLocation);
            Rectangle locRect = new Rectangle(p.x - cellSize / 2, p.y
                    - cellSize / 2, cellSize + 1, cellSize + 1);

            Rectangle viewRect = viewPort.getViewRect();
            if (!viewRect.contains(locRect))
            {
                while (locRect.x < viewRect.x + dx)
                    dx -= cellSize + 1;
                while (locRect.y < viewRect.y + dy)
                    dy -= cellSize + 1;
                while (locRect.getMaxX() > viewRect.getMaxX() + dx)
                    dx += cellSize + 1;
                while (locRect.getMaxY() > viewRect.getMaxY() + dy)
                    dy += cellSize + 1;

                Point pt = viewPort.getViewPosition();
                pt.x += dx;
                pt.y += dy;
                viewPort.setViewPosition(pt);
            }
        }
        repaint();
        showTip(getToolTipText(currentLocation),
                pointForLocation(currentLocation));
    }


    public void showTip(String tipText, Point pt)
    {
        if (getRootPane() == null)
            return;
        // draw in glass pane to appear on top of other components
        if (glassPane == null)
        {
            getRootPane().setGlassPane(glassPane = new JPanel());
            glassPane.setOpaque(false);
            glassPane.setLayout(null); // will control layout manually
            glassPane.add(tip = new JToolTip());
            tipTimer = new Timer(TIP_DELAY, new ActionListener()
            {
                public void actionPerformed(ActionEvent evt)
                {
                    glassPane.setVisible(false);
                }
            });
            tipTimer.setRepeats(false);
        }
        if (tipText == null)
            return;

        // set tip text to identify current origin of pannable view
        tip.setTipText(tipText);

        // position tip to appear at upper left corner of viewport
        tip.setLocation(SwingUtilities.convertPoint(this, pt, glassPane));
        tip.setSize(tip.getPreferredSize());

        // show glass pane (it contains tip)
        glassPane.setVisible(true);
        glassPane.repaint();

        // this timer will hide the glass pane after a short delay
        tipTimer.restart();
    }


    private void recalculateCellSize(int minSize)
    {
        if (numRows == 0 || numCols == 0)
        {
            cellSize = 0;
        }
        else
        {
            JViewport vp = getEnclosingViewport();
            Dimension viewableSize = (vp != null) ? vp.getSize() : getSize();
            int desiredCellSize = Math.min(
                    (viewableSize.height - extraHeight()) / numRows,
                    (viewableSize.width - extraWidth()) / numCols) - 1;
            // now we want to approximate this with 
            // DEFAULT_CELL_SIZE * Math.pow(2, k)
            cellSize = DEFAULT_CELL_SIZE;
            if (cellSize <= desiredCellSize)                
                while (2 * cellSize <= desiredCellSize)
                    cellSize *= 2;
            else
                while (cellSize / 2 >= Math.max(desiredCellSize, MIN_CELL_SIZE))
                    cellSize /= 2;
        }
        revalidate();
    }

    // helper to get our parent viewport, if we are in one.
    private JViewport getEnclosingViewport()
    {
        Component parent = getParent();
        return (parent instanceof JViewport) ? (JViewport) parent : null;
    }

    // GridPanel implements the Scrollable interface to get nicer behavior in a
    // JScrollPane. The 5 methods below are the methods in that interface

    public int getScrollableUnitIncrement(Rectangle visibleRect,
            int orientation, int direction)
    {
        return cellSize + 1;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,
            int orientation, int direction)
    {
        if (orientation == SwingConstants.VERTICAL)
            return (int) (visibleRect.height * .9);
        else
            return (int) (visibleRect.width * .9);
    }

    public boolean getScrollableTracksViewportWidth()
    {
        return false;
    }

    public boolean getScrollableTracksViewportHeight()
    {
        return false;
    }

    public Dimension getPreferredScrollableViewportSize()
    {
        return new Dimension(DEFAULT_CELL_COUNT * (DEFAULT_CELL_SIZE + 1) + 1 + extraWidth(), 
                DEFAULT_CELL_COUNT * (DEFAULT_CELL_SIZE + 1) + 1 + extraHeight());
    }

    // GridPanel implements the PseudoInfiniteViewport.Pannable interface to
    // play nicely with the pan behavior for unbounded view.
    // The 3 methods below are the methods in that interface.

    public void panBy(int hDelta, int vDelta)
    {
        originCol += hDelta / (cellSize + 1);
        originRow += vDelta / (cellSize + 1);
        repaint();
    }

    public boolean isPannableUnbounded()
    {
        return grid != null && (grid.getNumRows() == -1 || grid.getNumCols() == -1);
    }


    public void showPanTip()
    {
        String tipText = null;
        Point upperLeft = new Point(0, 0);
        JViewport vp = getEnclosingViewport();
        if (!isPannableUnbounded() && vp != null)
            upperLeft = vp.getViewPosition();
        Location loc = locationForPoint(upperLeft);
        if (loc != null)
            tipText = getToolTipText(loc);

        showTip(tipText, getLocation());
    }
}
