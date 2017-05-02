package shapescape.command;

import java.awt.Point;
import java.util.List;

import shapescape.ShapeScape;
import shapescape.Vertex;

public class DragCommand implements ReversibleCommand
{
	private ShapeScape modeler;
	private List<Vertex> vertices;
	
	private Point startPoint;
	private Point endPoint;
	
	private int totalDragX = 0;
	private int totalDragY = 0;
	
	public DragCommand(ShapeScape modeler, List<Vertex> vertices, Point startPoint, Point endPoint)
	{
		this.modeler = modeler;
		this.vertices = vertices;
		
		this.startPoint = startPoint;
		this.endPoint = endPoint;
	}
	
	public void setStartPoint(Point startPoint)
	{
		this.startPoint = startPoint;
	}
	
	public void setEndPoint(Point endPoint)
	{
		this.endPoint = endPoint;
	}
	
	public Point getStartPoint()
	{
		return startPoint;
	}
	
	public Point getEndPoint()
	{
		return endPoint;
	}

	@Override
	public void execute() 
	{
		int xOffset = (int)(endPoint.getX() - startPoint.getX());
		int yOffset = (int)(endPoint.getY() - startPoint.getY());
		
		totalDragX += xOffset;
		totalDragY += yOffset;
		
		for(Vertex v : vertices)
			v.setLocation(v.getX()+xOffset, v.getY()+yOffset);
		
		modeler.repaint();
	}

	@Override
	public void undo()
	{
		//int xOffset = (int)(startPoint.getX() - endPoint.getX());
		//int yOffset = (int)(startPoint.getY() - endPoint.getY());
		
		for(Vertex v : vertices)
			v.setLocation(v.getX()-totalDragX, v.getY()-totalDragY);
		
		modeler.repaint();
	}

}
