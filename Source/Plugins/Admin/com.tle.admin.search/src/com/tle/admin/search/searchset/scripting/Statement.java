package com.tle.admin.search.searchset.scripting;

public class Statement
{
	protected Block block;

	public Statement()
	{
		// Nothing to do here.
	}

	public void setBlock(Block block)
	{
		this.block = block;
		if( block != null )
		{
			block.setParent(this);
		}
	}

	public boolean isEmpty()
	{
		return block == null;
	}

	public Block getBlock()
	{
		return block;
	}

	public String toScript()
	{
		return block.toScript();
	}
}
