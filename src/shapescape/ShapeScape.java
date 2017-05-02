package shapescape;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
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

public class ShapeScape extends JPanel implements MouseListener, MouseMotionListener
{
	private static final long serialVersionUID = 8252030148986275166L;
	
	private AffineTransform worldSpace;
	
	private File saveFile;

	private Model model;
	
	private Rectangle selectionArea;
	
	private int gridSize = 30;
	
	private Point cursor;
	
	private Point dragAnchor;
	
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
		this.worldSpace = new AffineTransform();
		
		this.model = new Model();
		
		this.selectionArea = new Rectangle();
		
		this.edgeStroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		
		this.cursor = new Point();
		
		this.dragAnchor = new Point();
		
		this.commandQueue = new CommandQueue();
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
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
		
		if(selecting)
		{
			g2d.setPaint(selectionColor);
			g2d.fill(selectionArea);
			
			g2d.setPaint(selectionColor.brighter());
			g2d.draw(selectionArea);
		}
		
		g2d.setPaint(cursorColor);
		g2d.fillOval(cursor.x-2, cursor.y-2, 4, 4);
		
		g2d.setPaint(Color.WHITE);
		g2d.drawString(String.format("%d, %d" , cursor.x, cursor.y), 3, getHeight()-3);
	}
	
	public void undoLastCommand()
	{
		commandQueue.undoLastCommand();
	}
	
	public void createVertexAt(Point point)
	{
		createVertexAt(point.x, point.y);
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
		for(Vertex v : model.getVertices())
		{
			if(v.getGrabBox().contains(point))
				return v;
		}
		
		return null;
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

				createVertexAt(cursor);
				
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
			
			for(Vertex v : model.getVertices())
			{
				if(selectionArea.contains(v.getGrabBox()))
				{
					v.setSelected(true);
				}
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
			worldSpace.translate(dragAnchor.x-e.getPoint().x, dragAnchor.y-e.getPoint().y);
			dragAnchor = e.getPoint();
			
			model.transform(worldSpace);
			repaint();
			
			System.out.println(worldSpace.getTranslateX());
			return;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
		updateCursor(e);
		
		repaint();
	}
}
