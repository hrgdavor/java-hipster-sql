package hr.hrg.hipster.visitor;

@FunctionalInterface
public interface Lambda4<T1,T2,T3,T4> {
	abstract void run(T1 p1, T2 p2, T3 p3, T4 p4);
}
