package uk.sacko.m2m.etl;

import java.io.File;

import org.eclipse.epsilon.emc.emf.EmfModelFactory;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.etl.EtlModule;
import org.eclipse.epsilon.etl.IEtlModule;
import org.eclipse.epsilon.etl.execute.context.EtlContext;
import org.eclipse.epsilon.etl.trace.Transformation;
import org.eclipse.epsilon.etl.trace.TransformationTrace;

import uk.sacko.m2m.etl.execute.listeners.OrphanCaptureListener;
import uk.sacko.m2m.etl.strategy.TransformationStrategy;
import uk.sacko.m2m.etl.trace.NestedTransformation;
import uk.sacko.m2m.etl.trace.NestedTransformationTrace;

public class App {
	public static void main(String[] args) throws Exception {
		// bootstrap application
		new App().run(args);
	}

	public void run(String[] args) throws Exception {
		// configure input model
		IModel model = EmfModelFactory.getInstance().createEmfModel("In", new File("model/Test.model"),
				new File("model/Test.ecore"));
		model.setReadOnLoad(true);
		model.setStoredOnDisposal(false);

		IModel outModel = EmfModelFactory.getInstance().createEmfModel("Out", new File("model/Out.model"),
				new File("model/Test.ecore"));
		outModel.setReadOnLoad(false);
		outModel.setStoredOnDisposal(true);

		IModel traceModel = EmfModelFactory.getInstance().createEmfModel("Trace", new File("model/Trace.model"),
				new File("model/Trace.ecore"));
		traceModel.setReadOnLoad(false);
		traceModel.setStoredOnDisposal(true);

		// create a module element
		IEtlModule module = new EtlModule();
		try {
			// load input model
			model.load();
			outModel.load();
			traceModel.load();

			// parse and execute our ETL upon the model above
			module.setContext(new EtlContext() {
				protected TransformationTrace transformationTrace = new NestedTransformationTrace();

				@Override
				public TransformationTrace getTransformationTrace() {
					return this.transformationTrace;
				}
			});

			module.getContext().setTransformationStrategy(new TransformationStrategy());
			module.parse(new File("model/Test.etl"));
			module.getContext().getModelRepository().addModel(model);
			module.getContext().getModelRepository().addModel(outModel);
			module.getContext().getModelRepository().addModel(traceModel);
			module.getContext().getExecutorFactory().addExecutionListener(new OrphanCaptureListener());
			module.execute();

			for (Transformation transformation : module.getContext().getTransformationTrace().getTransformations()) {
				System.out.println("===");
				System.out.println(transformation);
				if(NestedTransformation.class.isInstance(transformation)) {
					System.out.println(NestedTransformation.class.cast(transformation).getTargets());
				}
				System.out.println("===");
			}
		} catch (EolModelLoadingException e) {
			// in the event the model isn't loaded
			e.printStackTrace();
		} catch (Exception e) {
			// in the case anything else fails
			e.printStackTrace();
		} finally {
			// clean up
			module.getContext().getModelRepository().dispose();
			module.getContext().dispose();
		}
	}
}
