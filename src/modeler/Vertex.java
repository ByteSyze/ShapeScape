package modeler;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Vertex extends Point2D implements Cloneable
{
	private double x,y;
	
	private Rectangle2D.Double grabBox;
	
	private double grabRadius = 10;
	private double halfRadius = 5;
	
	private boolean selected = false;
	
	public Vertex(Point point)
	{
		grabBox = new Rectangle2D.Double(0,0,grabRadius,grabRadius);
		setLocation(point);
	}
	
	public Vertex(double x, double y)
	{
		grabBox = new Rectangle2D.Double(0,0,grabRadius,grabRadius);
		
		this.x = x;
		this.y = y;
	}
	
	
	public boolean isSelected()
	{
		return selected;
	}
	
	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}

	@Override
	public double getX()
	{
		return x;
	}

	@Override
	public double getY()
	{
		return y;
	}
	
	public Rectangle2D getGrabBox()
	{
		return grabBox;
	}

	@Override
	public void setLocation(double x, double y)
	{
		this.x = x;
		this.y = y;
		
		this.grabBox.x = x-halfRadius;
		this.grabBox.y = y-halfRadius;
	}
	
	public String toString()
	{
		return String.format("(%f, %f)", x, y);
	}
	
	public Vertex clone()
	{
		Vertex clone = new Vertex(x,y);
		clone.setSelected(selected);
		
		return clone;
	}
}
