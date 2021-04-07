package hr.hrg.hipster.visitor;

@FunctionalInterface
public interface Lambda2<T1,T2> {
	abstract void run(T1 p1, T2 p2);
}
