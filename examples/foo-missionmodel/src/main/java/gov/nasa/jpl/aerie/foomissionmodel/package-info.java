@MissionModel(model = Mission.class)

@WithConfiguration(Configuration.class)

@WithMappers(BasicValueMappers.class)
@WithMappers(FooValueMappers.class)

@WithActivityType(BasicActivity.class)
@WithActivityType(FooActivity.class)
@WithActivityType(BarActivity.class)
@WithActivityType(DecompositionTestActivities.ParentActivity.class)
@WithActivityType(DecompositionTestActivities.ChildActivity.class)

package gov.nasa.jpl.aerie.foomissionmodel;

import gov.nasa.jpl.aerie.contrib.serialization.rulesets.BasicValueMappers;
import gov.nasa.jpl.aerie.foomissionmodel.activities.BarActivity;
import gov.nasa.jpl.aerie.foomissionmodel.activities.BasicActivity;
import gov.nasa.jpl.aerie.foomissionmodel.activities.DecompositionTestActivities;
import gov.nasa.jpl.aerie.foomissionmodel.activities.FooActivity;
import gov.nasa.jpl.aerie.foomissionmodel.mappers.FooValueMappers;
import gov.nasa.jpl.aerie.merlin.framework.annotations.MissionModel;
import gov.nasa.jpl.aerie.merlin.framework.annotations.MissionModel.WithActivityType;
import gov.nasa.jpl.aerie.merlin.framework.annotations.MissionModel.WithConfiguration;
import gov.nasa.jpl.aerie.merlin.framework.annotations.MissionModel.WithMappers;
