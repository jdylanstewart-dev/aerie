@Adaptation(module = BanananationResources.class)

@WithMappers(PrimitiveValueMappers.class)
@WithMappers(PrimitiveArrayValueMappers.class)
@WithMappers(BasicValueMappers.class)

@WithActivityType(BiteBananaActivity.class)
@WithActivityType(PeelBananaActivity.class)
@WithActivityType(ParameterTestActivity.class)

package gov.nasa.jpl.ammos.mpsa.aerie.banananation;

import gov.nasa.jpl.ammos.mpsa.aerie.banananation.activities.BiteBananaActivity;
import gov.nasa.jpl.ammos.mpsa.aerie.banananation.activities.ParameterTestActivity;
import gov.nasa.jpl.ammos.mpsa.aerie.banananation.activities.PeelBananaActivity;
import gov.nasa.jpl.ammos.mpsa.aerie.merlin.framework.BasicValueMappers;
import gov.nasa.jpl.ammos.mpsa.aerie.merlin.framework.PrimitiveArrayValueMappers;
import gov.nasa.jpl.ammos.mpsa.aerie.merlin.framework.PrimitiveValueMappers;
import gov.nasa.jpl.ammos.mpsa.aerie.merlin.framework.annotations.Adaptation;
import gov.nasa.jpl.ammos.mpsa.aerie.merlin.framework.annotations.Adaptation.WithActivityType;
import gov.nasa.jpl.ammos.mpsa.aerie.merlin.framework.annotations.Adaptation.WithMappers;