package utils;

public class Recency<E> {
	private int qtd = 1;
	private E value;
	
	public Recency(E value)
	{
		this.value = value;
	}
	
	public int getQtd()
	{
		return this.qtd;
	}
	
	public E getValue()
	{
		return this.value;
	}
	
	public void increment()
	{
		this.qtd++;
	}
}
