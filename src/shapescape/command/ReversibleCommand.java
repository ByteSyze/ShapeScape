package shapescape.command;

public interface ReversibleCommand extends Command
{
	public void undo();
}
