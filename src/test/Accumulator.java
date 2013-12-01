package test;
import java.util.ArrayList;



public class Accumulator extends ArrayList<Integer> {
	private Integer sum;
	private Integer limit;
	private Integer limitIterator;
	public Accumulator() {
		super();
		sum=0;
		limit=0;
	}
	
	public Accumulator(int limit) {
		super();
		sum=0;
		this.limit=limit;
		limitIterator=0;
	}
	
	@Override
	public boolean add(Integer e) {
		if(size()<limit || limit==0) {
			sum+=e;
			return super.add(e);
		}
		else {
			this.set(limitIterator,e);
			limitIterator=(limitIterator+1)%limit;
			return true;
		}
	}
	@Override
	public Integer remove(int index) {
		sum-=get(index);
		return super.remove(index);
	}
	@Override
	public Integer set(int index, Integer element) {
		Integer tmp = super.set(index, element);
		sum-=tmp;
		sum+=element;
		return tmp;
	}
	@Override
	public void clear() {
		sum=0;
		super.clear();
	}
	public int getSum() {
		return sum;
	}
	public int getLimitIterator() {
		return limitIterator;
	}
	public float mean() {
		return (float)sum/(float)size();
	}
	public boolean accumulated() {
		return size()==limit;
	}
}
