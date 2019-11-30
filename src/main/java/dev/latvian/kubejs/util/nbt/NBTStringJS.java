package dev.latvian.kubejs.util.nbt;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class NBTStringJS extends NBTBaseJS
{
	public static final NBTStringJS EMPTY_STRING = new NBTStringJS("");

	private final String string;

	public NBTStringJS(String s)
	{
		string = s;
	}

	public String getString()
	{
		return string;
	}

	public boolean equals(Object o)
	{
		return o == this || o instanceof NBTStringJS && string.equals(((NBTStringJS) o).string);
	}

	public String toString()
	{
		return string;
	}

	public int hashCode()
	{
		return string.hashCode();
	}

	@Nullable
	@Override
	public INBT createNBT()
	{
		return new StringNBT(string);
	}
}