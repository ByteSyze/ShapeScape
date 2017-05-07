package shapescape.command;

import java.awt.Point;
import java.awt.geom.Point2D;

import shapescape.ShapeScape;
import shapescape.Vertex;

public class CreateVertexCommand implements ReversibleCommand
{
	private ShapeScape modeler;
	
	private Point2D point;
	
	private Vertex vertex;
	
	public CreateVertexCommand(ShapeScape modeler, Point2D point)
	{
		this.modeler = modeler;
		
		this.point = point;
		
		this.vertex = new Vertex(point);
	}

	@Override
	public void execute()
	{
		if(!modeler.getVertices().contains(vertex))
		{
			modeler.getModel().addVertex(vertex);
			
			modeler.repaint();
		}
	}

	@Override
	public void undo() 
	{
		modeler.getVertices().remove(vertex);
		
		modeler.repaint();
	}

}
