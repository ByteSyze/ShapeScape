package shapescape.listener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import shapescape.ShapeScape;

public class KeyboardListener implements KeyListener
{
	private ShapeScape modeler;
	
	public KeyboardListener(ShapeScape modeler)
	{
		this.modeler = modeler;
	}

	@Override
	public void keyPressed(KeyEvent e) 
	{
		
		if(e.getExtendedKeyCode() == KeyEvent.VK_Z)
		{
			if(e.isControlDown())
				modeler.undoLastCommand();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) 
	{
		
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		
	}

}
