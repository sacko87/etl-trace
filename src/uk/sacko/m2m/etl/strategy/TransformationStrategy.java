package uk.sacko.m2m.etl.strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingDeque;

import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
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
					} else {
						// ... if it has then we're recalling it
						RecalledTransformation recalled = new RecalledTransformation();
						recalled.setRecalled(nestedTransformation);
						topNestedTransformation.getDependencies().add(recalled);
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
				} else {
				}
			} finally {
				// we have executed a transformation
				this.executionStack.pop();
			}
		}
	}
	
	@Override
	public Collection<?> getEquivalents(Object source, IEolContext context_, List<String> rules) throws EolRuntimeException{
		IEtlContext context = (IEtlContext) context_;
		
		if (pendingTransformations.containsKey(source)) {
			TransformationList transformations = pendingTransformations.remove(source);
			executeTransformations(transformations, context);
		} else {
			// handle equivalents calls, these are be recalls
			Transformation transformation = context.getTransformationTrace().getTransformations(source).get(0);
			// get the top of the execution stack
			Transformation top = this.executionStack.getFirst();
			// are both the correct type?
			if (NestedTransformation.class.isInstance(top) &&
					NestedTransformation.class.isInstance(transformation)) {
				// cast them accordingly
				NestedTransformation topNestedTransformation = NestedTransformation.class.cast(top);
				NestedTransformation nestedTransformation = NestedTransformation.class.cast(transformation);
				
				// if the transformation isn't pending then we are recalling it
				RecalledTransformation recalled = new RecalledTransformation();
				recalled.setRecalled(nestedTransformation);
				topNestedTransformation.getDependencies().add(recalled);
			}
			
		}
		
		if (rules == null || rules.isEmpty()) {
			return flatTrace.get(source);
		}
		
		Collection<Object> equivalents = new ArrayList<Object>();
		for (Transformation transformation : context.getTransformationTrace().getTransformations(source)) {
			if (rules.contains(transformation.getRule().getName())) {
				equivalents.addAll(transformation.getTargets());
			}
		}
		return equivalents;
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
