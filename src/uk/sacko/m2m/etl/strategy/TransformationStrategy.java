package uk.sacko.m2m.etl.strategy;

import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingDeque;

import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.etl.dom.TransformationRule;
import org.eclipse.epsilon.etl.execute.context.IEtlContext;
import org.eclipse.epsilon.etl.strategy.FastTransformationStrategy;
import org.eclipse.epsilon.etl.trace.Transformation;
import org.eclipse.epsilon.etl.trace.TransformationList;

import uk.sacko.m2m.etl.trace.NestedTransformation;
import uk.sacko.m2m.etl.trace.RecalledTransformation;

/**
 * A transformation strategy that tracks the execution and wires the graph
 * like structures of a transformation. 
 * 
 * @author John T. Saxon
 * @version 1.0
 */
public class TransformationStrategy extends FastTransformationStrategy {
	protected final Deque<Transformation> executionStack = new LinkedBlockingDeque<Transformation>();

	@Override
	protected void executeTransformations(TransformationList transformations, IEtlContext context)
			throws EolRuntimeException {
		// get the entire transformation list (from the initialisation phase)
		TransformationList instantiationList = context.getTransformationTrace().getTransformations();
		
		// go through each transformation that we've been given
		for (Transformation transformation : transformations) {
			TransformationRule rule = transformation.getRule();

			try {
				// get the top of the execution stack
				Transformation top = this.executionStack.getFirst();
				// are both the correct type?
				if (NestedTransformation.class.isInstance(top) &&
						NestedTransformation.class.isInstance(transformation)) {
					// cast them accordingly
					NestedTransformation topNestedTransformation = NestedTransformation.class.cast(top);
					NestedTransformation nestedTransformation = NestedTransformation.class.cast(transformation);
					
					// if it hasn't been transformed...
					if (!rule.hasTransformed(transformation.getSource())) {
						// ... it is a nested dependency
						topNestedTransformation.getDependencies().add(transformation);
					}

					// if the top most is earlier than the current then we have recalled...
					if(instantiationList.indexOf(topNestedTransformation) < instantiationList.indexOf(nestedTransformation)) {
						RecalledTransformation recalled = new RecalledTransformation();
						recalled.setRecalled(topNestedTransformation);
						nestedTransformation.getDependencies().add(recalled);
					}
				}
			} catch (NoSuchElementException e) { 
				// we don't care if we are root transformation, there
				// are no dependencies here.
			}

			// we are about to execute a transformation
			this.executionStack.push(transformation);
			try {
				// if it hasn't already been done ...
				if (!rule.hasTransformed(transformation.getSource())) {
					// ... transform the source
					rule.transform(transformation.getSource(), transformation.getTargets(), context);
				}
			} finally {
				// we have executed a transformation
				this.executionStack.pop();
			}
		}
	}

	/**
	 * Get the stack trace of the transformation. 
	 * 
	 * @return The execution stack of the transformation.
	 */
	public Deque<Transformation> getExecutionStack() {
		return executionStack;
	}
}
