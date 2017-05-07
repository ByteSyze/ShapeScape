package shapescape;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ModelerToolBar extends JMenuBar implements ActionListener
{
	private static final long serialVersionUID = 2599112429170731548L;
	
	private ShapeScape modeler;
	
	private JFileChooser browser;
	
	// File
	private JMenu fileMenu;
	
	private JMenuItem newItem;
	
	private JMenuItem saveItem;
	private JMenuItem saveAsItem;
	private JMenuItem openItem;
	
	// Edit
	private JMenu editMenu;
	
	private JMenuItem scaleItem;
	private JMenuItem undoItem;
	
	private JMenuItem setGridItem;
	
	// View
	private JMenu viewMenu;
	
	private JToggleButton showGridItem;
	private JToggleButton showBoundsItem;

	public ModelerToolBar(ShapeScape modeler)
	{
		this.modeler = modeler;
		
		browser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Custom Vector Graphics", "cvg");
		browser.setFileFilter(filter);
		
		fileMenu = new JMenu("File");
		
		newItem = new JMenuItem("New");
		saveItem = new JMenuItem("Save");
		saveAsItem = new JMenuItem("Save As...");
		openItem = new JMenuItem("Open");
		
		newItem.addActionListener(this);
		saveItem.addActionListener(this);
		saveAsItem.addActionListener(this);
		openItem.addActionListener(this);
		
		fileMenu.add(newItem);
		fileMenu.addSeparator();
		fileMenu.add(saveItem);
		fileMenu.add(saveAsItem);
		fileMenu.add(openItem);
		
		editMenu = new JMenu("Edit");
		
		scaleItem = new JMenuItem("Scale");
		undoItem = new JMenuItem("Undo");
		
		scaleItem.addActionListener(this);
		undoItem.addActionListener(this);
		
		editMenu.add(scaleItem);
		editMenu.addSeparator();
		editMenu.add(undoItem);
		
		viewMenu = new JMenu("View");
		
		showGridItem = new JCheckBox("Show Grid");
		showBoundsItem = new JCheckBox("Show Bounding Box");
		
		showGridItem.addActionListener(this);
		showBoundsItem.addActionListener(this);
		
		showGridItem.setSelected(true);
		
		viewMenu.add(showGridItem);
		viewMenu.add(showBoundsItem);
		
		this.add(fileMenu);
		this.add(editMenu);
		this.add(viewMenu);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == showGridItem)
		{
			modeler.setShowGrid(showGridItem.isSelected());
			modeler.repaint();
		}
		else if(e.getSource() == showBoundsItem)
		{
			modeler.setShowBounds(showBoundsItem.isSelected());
			modeler.repaint();
		}
		else if(e.getSource() == scaleItem)
		{
			String result = JOptionPane.showInputDialog(modeler, "Select the bounds to scale to", "Object Scaling", JOptionPane.PLAIN_MESSAGE);
			
			if(result == null)
				return;
			
			String[] dimensions = result.split(" ");
			
			Rectangle newBounds = new Rectangle(0,0,Integer.parseInt(dimensions[0]), Integer.parseInt(dimensions[1]));
			Rectangle bounds = modeler.getModel().getBounds();
			
			double xScale = newBounds.getWidth()/bounds.getWidth();
			double yScale = newBounds.getHeight()/bounds.getHeight();
			
			modeler.getModel().applyDirectScaling(xScale, yScale);
		}
		else if(e.getSource() == undoItem)
		{
			modeler.undoLastCommand();
		}
		else if(e.getSource() == newItem)
		{
			modeler.getVertices().clear();
			modeler.resetViewspace();
		}
		else if(e.getSource() == saveItem || e.getSource() == saveAsItem)
		{
			File saveFile = modeler.getSaveFile();
			
			if(saveFile == null || e.getSource() == saveAsItem)
			{
				int value = browser.showSaveDialog(modeler);
				
				if(value != JFileChooser.APPROVE_OPTION)
					return;
				
				saveFile = browser.getSelectedFile();
				
				modeler.setSaveFile(saveFile);
			}
			
			try 
			{
				BufferedWriter out = new BufferedWriter(new FileWriter(saveFile));
				
				Rectangle bounds = modeler.getModel().getBounds();
				
				for(Vertex v : modeler.getVertices())
				{
					out.write((v.getX()-bounds.getX()) + "," + (v.getY()-bounds.getY()));
					out.newLine();
				}
				
				out.close();
			} catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
		else if(e.getSource() == openItem)
		{
			int value = browser.showOpenDialog(modeler);
			
			if(value != JFileChooser.APPROVE_OPTION)
				return;
			
			File saveFile = browser.getSelectedFile();
			
			modeler.resetScene();
			
			try
			{
				BufferedReader br = new BufferedReader(new FileReader(saveFile));
				
				String line;
				
				int offset = modeler.getGridSize();
				
				while((line = br.readLine()) != null)
				{
					String[] coords = line.split(",");
					
					double x = new Double(coords[0]) + offset;
					double y = new Double(coords[1]) + offset;
					
					modeler.createVertexAt(x, y);
				}
				
				br.close();
			} catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
	}
}
