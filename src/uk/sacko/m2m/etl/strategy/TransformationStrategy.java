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

public class TransformationStrategy extends FastTransformationStrategy {
	protected final Deque<Transformation> executionStack = new LinkedBlockingDeque<Transformation>();

	@Override
	protected void executeTransformations(TransformationList transformations, IEtlContext context)
			throws EolRuntimeException {
		TransformationList instantiationList = context.getTransformationTrace().getTransformations();
		
		for (Transformation transformation : transformations) {
			TransformationRule rule = transformation.getRule();

			try {
				Transformation top = this.executionStack.getFirst();
				if (NestedTransformation.class.isInstance(top) &&
						NestedTransformation.class.isInstance(transformation)) {
					NestedTransformation topNestedTransformation = NestedTransformation.class.cast(top);
					NestedTransformation nestedTransformation = NestedTransformation.class.cast(transformation);
					if (!rule.hasTransformed(transformation.getSource())) {
						topNestedTransformation.getDependencies().add(transformation);
					}

					if(instantiationList.indexOf(topNestedTransformation) < instantiationList.indexOf(nestedTransformation)) {
						RecalledTransformation recalled = new RecalledTransformation();
						recalled.setRecalled(topNestedTransformation);
						nestedTransformation.getDependencies().add(recalled);
					}
				}
			} catch (NoSuchElementException e) { }
			

			this.executionStack.push(transformation);
			try {
				if (!rule.hasTransformed(transformation.getSource())) {
					rule.transform(transformation.getSource(), transformation.getTargets(), context);
				} else {
					
				}
			} finally {
				this.executionStack.pop();
			}
		}
	}

	public Deque<Transformation> getExecutionStack() {
		return executionStack;
	}
}
