package modeler;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ModelerToolBar extends JMenuBar implements ActionListener
{
	private static final long serialVersionUID = 2599112429170731548L;
	
	private Modeler modeler;
	
	private JFileChooser browser;
	
	private JMenuItem newItem;
	
	private JMenuItem saveItem;
	private JMenuItem openItem;
	
	private JMenuItem undoItem;

	public ModelerToolBar(Modeler modeler)
	{
		this.modeler = modeler;
		
		browser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Custom Vector Graphics", "cvg");
		browser.setFileFilter(filter);
		
		JMenu fileMenu = new JMenu("File");
		
		newItem = new JMenuItem("New");
		saveItem = new JMenuItem("Save");
		openItem = new JMenuItem("Open");
		
		newItem.addActionListener(this);
		saveItem.addActionListener(this);
		openItem.addActionListener(this);
		
		fileMenu.add(newItem);
		fileMenu.addSeparator();
		fileMenu.add(saveItem);
		fileMenu.add(openItem);
		
		JMenu editMenu = new JMenu("Edit");
		
		undoItem = new JMenuItem("Undo");
		
		undoItem.addActionListener(this);
		
		editMenu.add(undoItem);
		
		this.add(fileMenu);
		this.add(editMenu);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == undoItem)
		{
			modeler.undoLastCommand();
		}
		else if(e.getSource() == newItem)
		{
			modeler.getVertices().clear();
		}
		else if(e.getSource() == saveItem)
		{
			File saveFile = modeler.getSaveFile();
			
			if(saveFile == null)
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
			
			try
			{
				BufferedReader br = new BufferedReader(new FileReader(saveFile));
				
				String line;
				
				int offset = modeler.getGridSize();
				
				while((line = br.readLine()) != null)
				{
					String[] coords = line.split(",");
					
					int x = (new Double(coords[0])).intValue() + offset;
					int y = (new Double(coords[1])).intValue() + offset;
					
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
