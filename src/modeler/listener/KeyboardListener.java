package modeler.listener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import modeler.Modeler;

public class KeyboardListener implements KeyListener
{
	private Modeler modeler;
	
	public KeyboardListener(Modeler modeler)
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
