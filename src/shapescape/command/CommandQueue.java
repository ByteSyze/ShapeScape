package shapescape.command;

import java.util.ArrayList;
import java.util.List;

public class CommandQueue
{
	private List<ReversibleCommand> commands;
	private List<ReversibleCommand> commandHistory;
	
	public CommandQueue()
	{
		commands = new ArrayList<ReversibleCommand>();
		commandHistory = new ArrayList<ReversibleCommand>();
	}
	
	public void addCommand(ReversibleCommand command)
	{
		commands.add(command);
	}
	
	public void executeCommands()
	{
		for(ReversibleCommand command : commands)
		{
			command.execute();
			
			commandHistory.add(command);
			commands.remove(command);
		}
	}
	
	public void executeNextCommand()
	{
		if(!commands.isEmpty())
		{
			ReversibleCommand c = commands.get(0);
			
			c.execute();
			
			commands.remove(0);
			commandHistory.add(c);
		}
	}
	
	public void undoLastCommand()
	{
		if(!commandHistory.isEmpty())
		{
			commandHistory.get(commandHistory.size()-1).undo();
			commandHistory.remove(commandHistory.size()-1);
		}
	}
}
