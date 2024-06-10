
package info.gridworld.grid;

import java.util.ArrayList;


public interface Grid<E>
{

    int getNumRows();


    int getNumCols();


    boolean isValid(Location loc);


    E put(Location loc, E obj);


    E remove(Location loc);


    E get(Location loc);


    ArrayList<Location> getOccupiedLocations();


    ArrayList<Location> getValidAdjacentLocations(Location loc);


    ArrayList<Location> getEmptyAdjacentLocations(Location loc);


    ArrayList<Location> getOccupiedAdjacentLocations(Location loc);


    ArrayList<E> getNeighbors(Location loc);
}
