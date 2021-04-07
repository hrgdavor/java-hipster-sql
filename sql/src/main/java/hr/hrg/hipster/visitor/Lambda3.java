package hr.hrg.hipster.visitor;

@FunctionalInterface
public interface Lambda3<T1,T2,T3> {
	abstract void run(T1 p1, T2 p2, T3 p3);
}
