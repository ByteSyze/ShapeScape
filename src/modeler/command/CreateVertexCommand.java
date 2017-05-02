package modeler.command;

import java.awt.Point;

import modeler.Modeler;
import modeler.Vertex;

public class CreateVertexCommand implements ReversibleCommand
{
	private Modeler modeler;
	
	private Point point;
	
	private Vertex vertex;
	
	public CreateVertexCommand(Modeler modeler, Point point)
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
