package com.dytech.edge.admin.script.ifmodel;

import java.util.ArrayList;
import java.util.List;

public class Statement
{
	protected List<Block> blocks;

	public Statement()
	{
		blocks = new ArrayList<Block>();
	}

	public void addBlock(Block block)
	{
		blocks.add(block);
		block.setParent(this);
	}

	public boolean isEmpty()
	{
		return blocks.isEmpty();
	}

	public void removeAll()
	{
		blocks.clear();
	}

	public void insertBlock(Block block, int index)
	{
		if( index >= blocks.size() )
		{
			addBlock(block);
		}
		else
		{
			blocks.add(index, block);
			block.setParent(this);
		}
	}

	public void removeBlock(Block block)
	{
		blocks.remove(block);
	}

	public Block getBlock(int index)
	{
		if( index < blocks.size() )
		{
			return blocks.get(index);
		}
		else
		{
			return null;
		}
	}

	public List<Block> getBlocks()
	{
		return blocks;
	}

	public String toScript()
	{
		StringBuilder script = new StringBuilder();
		script.append("var bRet = false; \n");

		boolean first = true;
		for( Block b : blocks )
		{
			script.append(b.toScript(first));
			first = false;
		}

		script.append("return bRet; \n");

		return script.toString();
	}
}
