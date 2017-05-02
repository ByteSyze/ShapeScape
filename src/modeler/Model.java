package modeler;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Model
{	
	private AffineTransform transform;
	
	private List<Vertex> vertices;
	
	private Rectangle bounds;
	
	public Model()
	{
		vertices = new ArrayList<Vertex>();
		
		transform = new AffineTransform();
		
		bounds = new Rectangle();
	}
	
	public void transform(AffineTransform transform)
	{
		for(Vertex v : vertices)
		{
			transform.transform(v, v);
		}
	}
	
	public void translate(int x, int y)
	{
		transform.translate(x, y);
	}
	
	public void addVertex(Vertex v)
	{
		vertices.add(v);
		
		if(!bounds.contains(v))
		{
			bounds = generateBounds();
		}
	}
	
	public void removeVertex(Vertex v)
	{
		vertices.remove(v);
		
		bounds = generateBounds();
	}
	
	public List<Vertex> getVertices()
	{
		return vertices;
	}
	
	public List<Vertex> getAbsoluteVertices()
	{
		Vertex[] transformed = new Vertex[vertices.size()];
		
		vertices.toArray(transformed);
		
		for(int i = 0; i < transformed.length; i++)
		{
			transformed[i] = transformed[i].clone();
		}
		
		try {
			transform.createInverse().transform(transformed, 0, transformed, 0, transformed.length);
		} catch (NoninvertibleTransformException e)
		{
			e.printStackTrace();
		}
		
		return (List<Vertex>)Arrays.asList(transformed);
	}
	
	public Rectangle getBounds()
	{
		return bounds;
	}
	
	protected Rectangle generateBounds()
	{
		if(vertices.isEmpty())
			return new Rectangle();
		else
		{
			Vertex left,right,top,bottom;
			
			left = right = top = bottom = vertices.get(0);
			
			for(Vertex v : vertices)
			{
				if(v.getX() < left.getX())
					left = v;
				if(v.getX() > right.getX())
					right = v;
				if(v.getY() < bottom.getY())
					bottom = v;
				if(v.getY() > top.getY())
					top = v;
			}
			
			int l = (int)(left.getX() + transform.getTranslateX());
			int r = (int)(right.getX() - left.getX() + transform.getTranslateX());
			int t = (int)(top.getY() + transform.getTranslateY());
			int b = (int)(bottom.getY() - top.getY() + transform.getTranslateY());
			
			return new Rectangle(l,b,r,t);
		}
	}

}
