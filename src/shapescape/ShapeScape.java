package shapescape;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import shapescape.command.CommandQueue;
import shapescape.command.CreateVertexCommand;
import shapescape.command.DragCommand;
import shapescape.listener.KeyboardListener;

public class ShapeScape extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener
{
	private static final long serialVersionUID = 8252030148986275166L;
	
	private AffineTransform worldSpace;
	private AffineTransform viewSpace;
	
	private File saveFile;

	private Model model;
	
	private Rectangle selectionArea;
	
	private int gridSize = 30;
	
	private Point cursor;
	
	private Point dragAnchor;
	
	private float zoomFactor = 10f;
	
	private boolean viewDragging = false;
	
	private boolean vertexDragging = false;
	
	private boolean selecting = false;
	
	private Color selectionColor = new Color(20,200,100,128);
	
	private Color cursorColor = Color.ORANGE;
	
	private Color edgeColor = Color.WHITE;
	private Color selectedEdgeColor = Color.ORANGE;
	
	private Color vertexColor = Color.RED;
	private Color selectedVertexColor = Color.BLUE;
	
	private Stroke normalStroke;
	private Stroke edgeStroke;
	
	private CommandQueue commandQueue;
	
	private DragCommand dragCmd;
	
	public static void main(String[] args)
	{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		JFrame frame = new JFrame("Vector Modeler");
		
		ShapeScape modeler = new ShapeScape();
		ModelerToolBar toolbar = new ModelerToolBar(modeler);
		
		frame.setLayout(new BorderLayout());
		
		frame.add(toolbar, BorderLayout.NORTH);
		frame.add(modeler, BorderLayout.CENTER);
		
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public ShapeScape()
	{
		this.viewSpace = new AffineTransform();
		
		this.model = new Model();
		
		this.selectionArea = new Rectangle();
		
		this.edgeStroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		
		this.cursor = new Point();
		
		this.dragAnchor = new Point();
		
		this.commandQueue = new CommandQueue();
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
		
		this.setFocusable(true);
		this.addKeyListener(new KeyboardListener(this));
		
		this.setPreferredSize(new Dimension(500,500));
	}
	
	public void paint(Graphics g)
	{	
		super.paint(g);
		
		Graphics2D g2d = (Graphics2D)g;
		
		normalStroke = g2d.getStroke();
		
		g2d.setPaint(Color.BLACK);
		
		g2d.fillRect(0, 0, getWidth(), getHeight());
		
		g2d.setPaint(Color.GRAY);
		
		int xCopyNum = 100/gridSize;
		int yCopyNum = 100/gridSize;
		
		int gridWindowX = xCopyNum*gridSize;
		int gridWindowY = yCopyNum*gridSize;
		
		/*
		 Create a small subsection of grid, then copy it to the rest of the panel.
		 */
		for(int gridX = 0; gridX < gridWindowX; gridX++)
		{
			for(int gridY = 0; gridY < gridWindowY; gridY++)
			{
				if((gridX % gridSize == 0) || (gridY % gridSize == 0))
				{
					g2d.drawLine(gridX, gridY, gridX, gridY);
				}
			}
		}

		for(int copyX = 0; copyX < getWidth(); copyX += gridWindowX)
		{
			g2d.copyArea(0, 0, gridWindowX, gridWindowY, copyX, 0);
		}
		
		for(int copyY = 0; copyY < getHeight(); copyY += gridWindowY)
		{
			g2d.copyArea(0, 0, getWidth(), gridWindowY, 0, copyY);
		}
		
		/*Draw the model relative to the user's current view*/
		worldSpace = g2d.getTransform();
		g2d.setTransform(viewSpace);
		
		if(model.getVertices().size() > 0)
		{
			Vertex lastV = model.getVertices().get(0);

			g2d.setStroke(edgeStroke);
			
			for(Vertex v : model.getVertices())
			{
				if(!lastV.isSelected() && v.isSelected())
					g2d.setPaint(new GradientPaint(lastV, edgeColor, v, selectedEdgeColor));
				else if(lastV.isSelected() && !v.isSelected())
					g2d.setPaint(new GradientPaint(lastV, selectedEdgeColor, v, edgeColor));
				else if(v.isSelected())
					g2d.setPaint(selectedEdgeColor);
				else
					g2d.setPaint(edgeColor);
				
				g2d.drawLine((int)lastV.getX(), (int)lastV.getY(), (int)v.getX(), (int)v.getY());
			
				lastV = v;
			}
			
			g2d.setStroke(normalStroke);
			
			for(Vertex v : model.getVertices())
			{
				if(v.isSelected())
					g2d.setPaint(selectedVertexColor);
				else
					g2d.setPaint(vertexColor);
				
				g2d.fill(v.getGrabBox());
			}
		}
		
		/*Revert to world space for overlays.*/
		g2d.setTransform(worldSpace);
		
		if(selecting)
		{
			g2d.setPaint(selectionColor);
			g2d.fill(selectionArea);
			
			g2d.setPaint(selectionColor.brighter());
			g2d.draw(selectionArea);
		}
		
		g2d.setPaint(cursorColor);
		g2d.fillOval(cursor.x-2, cursor.y-2, 4, 4);
		
		Point2D worldCursor = viewToWorld(cursor);
		
		g2d.setPaint(Color.WHITE);
		g2d.drawString(String.format("%s, %s" , worldCursor.getX(), worldCursor.getY()), 3, getHeight()-3);
	}
	
	public void undoLastCommand()
	{
		commandQueue.undoLastCommand();
	}
	
	public void createVertexAt(Point2D point2d)
	{
		createVertexAt((int)point2d.getX(), (int)point2d.getY());
	}
	
	public void createVertexAt(int x, int y)
	{
		CreateVertexCommand createVertex = new CreateVertexCommand(this, new Point(x,y));
		
		commandQueue.addCommand(createVertex);
		commandQueue.executeNextCommand();
	}

	public List<Vertex> getSelectedVertices()
	{
		List<Vertex> selected = new ArrayList<Vertex>();
		
		for(Vertex v : model.getVertices())
		{
			if(v.isSelected())
				selected.add(v);
		}
		
		return selected;
	}
	
	public void clearSelectedVertices()
	{
		for(Vertex v : model.getVertices())
			v.setSelected(false);
	}
	
	public boolean selectionEmpty()
	{	
		for(Vertex v : model.getVertices())
		{
			if(v.isSelected())
			{
				return false;
			}
		}
		
		return true;
	}
	
	public Vertex getVertexAt(Point point)
	{
		Point2D[] localPoints = new Point2D[model.getVertices().size()];
		Point2D[] worldPoints = new Point2D[model.getVertices().size()];
		
		model.getVertices().toArray(localPoints);
		
		this.toWorldSpace(localPoints, worldPoints, model.getTransform());
		
		for(int i = 0; i < worldPoints.length; i++)
		{
			if(point.distanceSq(worldPoints[i]) < 5)
				return model.getVertices().get(i);
		}
		
		return null;
	}
	
	public List<Vertex> getVerticesIn(Rectangle bounds)
	{
		List<Vertex> result = new ArrayList<Vertex>();
		
		Point2D localPoints[] = new Point2D[model.getVertices().size()];
		Point2D worldPoints[] = new Point2D[model.getVertices().size()];
		
		model.getVertices().toArray(localPoints);
		
		this.toWorldSpace(localPoints, worldPoints, model.getTransform());
		
		for(int i = 0; i < worldPoints.length; i++)
		{
			if(bounds.contains(worldPoints[i]))
			{
				result.add(model.getVertices().get(i));
			}
		}
		
		return result;
	}
	
	public List<Vertex> getVertices()
	{
		return model.getVertices();
	}
	
	public Model getModel()
	{
		return model;
	}
	
	public int getGridSize()
	{
		return gridSize;
	}
	
	public File getSaveFile()
	{
		return saveFile;
	}
	
	public void setSaveFile(File saveFile)
	{
		this.saveFile = saveFile;
	}
	
	public void updateCursor(MouseEvent e)
	{
		cursor = e.getPoint();
		
		if(e.isControlDown())
		{
			cursor.x = (cursor.x/gridSize)*gridSize;
			cursor.y = (cursor.y/gridSize)*gridSize;
			
			dragAnchor.x = (dragAnchor.x/gridSize)*gridSize;
			dragAnchor.y = (dragAnchor.y/gridSize)*gridSize;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		updateCursor(e);
		
		if(e.getButton() == MouseEvent.BUTTON3)
		{
			Vertex vert = getVertexAt(cursor);
			
			if(vert == null)
			{
				clearSelectedVertices();

				createVertexAt(viewToWorld(cursor));
				
				repaint();
			}
			else
			{
				vert.setSelected(true);
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e)
	{
		dragAnchor = cursor;
		updateCursor(e);
		
		if(e.getButton() == MouseEvent.BUTTON1)
		{
			clearSelectedVertices();
			
			selectionArea.setLocation(cursor);
			
			selecting = true;
		}
		else if(e.getButton() == MouseEvent.BUTTON2)
		{
			viewDragging = true;
		}
		else if(e.getButton() == MouseEvent.BUTTON3)
		{
			int selectCount = getSelectedVertices().size();
			
			if(selectCount > 1 || (selectCount == 1 && getVertexAt(cursor) == null))
			{
				//dragAnchor = cursor;
				dragCmd = new DragCommand(this, getSelectedVertices(), dragAnchor, cursor);
				vertexDragging = true;
			}
			else
			{
				clearSelectedVertices();
				
				for(Vertex v : model.getVertices())
				{
					if(v.getGrabBox().contains(cursor))
					{
						v.setSelected(true);
					}
				}
				
				if(!selectionEmpty())
				{
					//dragAnchor = cursor;
					dragCmd = new DragCommand(this, getSelectedVertices(), dragAnchor, cursor);
					vertexDragging = true;
				}
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		updateCursor(e);
		
		if(e.getButton() == MouseEvent.BUTTON1)
		{
			selectionArea.width = (int)(cursor.getX()-selectionArea.x);
			selectionArea.height = (int)(cursor.getY()-selectionArea.y);
			
			repaint();
			
			for(Vertex v : this.getVerticesIn(selectionArea))
			{
				v.setSelected(true);
			}
			
			selecting = false;
		}
		else if(e.getButton() == MouseEvent.BUTTON2)
		{
			viewDragging = false;
		}
		else if(e.getButton() == MouseEvent.BUTTON3)
		{
			if(vertexDragging)
			{
				dragCmd.setStartPoint(dragCmd.getEndPoint());
				commandQueue.addCommand(dragCmd);
				commandQueue.executeNextCommand();
				
				vertexDragging = false;
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		updateCursor(e);
		
		if(selecting)
		{
			selectionArea.width = (int)(cursor.getX()-selectionArea.x);
			selectionArea.height = (int)(cursor.getY()-selectionArea.y);
			
			repaint();
		}
		else if(vertexDragging)
		{
			Point drag = new Point();
			
			drag.x = (int)(cursor.getX() - dragAnchor.getX());
			drag.y = (int)(cursor.getY() - dragAnchor.getY());
			
			dragCmd.setStartPoint(dragCmd.getEndPoint());
			dragCmd.setEndPoint(cursor);
			
			dragCmd.execute();
		}
		else if(viewDragging)
		{
			viewSpace.translate(cursor.x-dragAnchor.x, cursor.y-dragAnchor.y);
			dragAnchor = e.getPoint();
			
			repaint();
			return;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
		updateCursor(e);
		
		repaint();
	}
	
	public void toWorldSpace(Point2D[] srcPoints, Point2D[] dstPoints, AffineTransform transform)
	{
		AffineTransform pointSpace = new AffineTransform(worldSpace);
		
		pointSpace.concatenate(viewSpace);
		pointSpace.concatenate(transform);
		
		pointSpace.transform(srcPoints, 0, dstPoints, 0, dstPoints.length);
	}
	
	public Point2D toWorldSpace(Point2D point, AffineTransform transform)
	{
		Point2D worldPoint = new Point2D.Double();
		
		AffineTransform pointSpace = new AffineTransform(worldSpace);
		
		pointSpace.concatenate(viewSpace);
		pointSpace.concatenate(transform);
		
		pointSpace.transform(point, worldPoint);
		
		return worldPoint;
	}
	
	public Point2D viewToWorld(Point2D point)
	{
		Point2D worldPoint = new Point2D.Double();
		
		AffineTransform pointSpace = new AffineTransform(worldSpace);
		
		pointSpace.concatenate(viewSpace);
		
		try {
			pointSpace.inverseTransform(point, worldPoint);
		} catch (NoninvertibleTransformException e)
		{
			e.printStackTrace();
		}
		
		return worldPoint;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		double zoom = (10-e.getWheelRotation())/10f;
		
		viewSpace.scale(zoom,zoom);
		
		repaint();
	}
}
