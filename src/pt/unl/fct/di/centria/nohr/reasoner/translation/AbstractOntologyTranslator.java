package pt.unl.fct.di.centria.nohr.reasoner.translation;

import java.io.IOException;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.profiles.OWL2ELProfile;
import org.semanticweb.owlapi.profiles.OWL2QLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import other.Config;
import pt.unl.fct.di.centria.nohr.reasoner.UnsupportedOWLProfile;
import pt.unl.fct.di.centria.nohr.reasoner.translation.ontology.OntologyLabeler;
import pt.unl.fct.di.centria.nohr.reasoner.translation.ontology.OntologyTranslator;
import pt.unl.fct.di.centria.nohr.reasoner.translation.ontology.TranslationAlgorithm;
import pt.unl.fct.di.centria.nohr.reasoner.translation.ontology.el.ELOntologyTranslator;
import pt.unl.fct.di.centria.nohr.reasoner.translation.ontology.el.UnsupportedAxiomTypeException;
import pt.unl.fct.di.centria.nohr.reasoner.translation.ontology.ql.QLOntologyTranslator;
import utils.Tracer;

public abstract class AbstractOntologyTranslator implements OntologyTranslator {

    public static OntologyTranslator createOntologyTranslator(
	    OWLOntologyManager ontologyManager, OWLOntology ontology)
		    throws OWLOntologyCreationException, OWLOntologyStorageException,
		    UnsupportedOWLProfile, IOException, CloneNotSupportedException,
	    UnsupportedAxiomTypeException {
	final OWLAnnotationProperty labelAnnotation = ontologyManager
		.getOWLDataFactory().getOWLAnnotationProperty(
			OWLRDFVocabulary.RDFS_LABEL.getIRI());
	final OntologyLabeler ontologyLabel = new OntologyLabeler(ontology,
		labelAnnotation);
	switch (getTranslationAlgorithm(ontology)) {
	case DL_LITE_R:
	    return new QLOntologyTranslator(ontology,
		    ontologyManager.getOWLDataFactory(), ontologyManager,
		    ontologyLabel);
	case EL:
	    return new ELOntologyTranslator(ontologyManager, ontology,
		    ontologyLabel);
	default:
	    return null;
	}
    }

    private static TranslationAlgorithm getTranslationAlgorithm(
	    OWLOntology ontology) throws UnsupportedOWLProfile {
	if (Config.translationAlgorithm != null)
	    return Config.translationAlgorithm;
	final OWL2ELProfile elProfile = new OWL2ELProfile();
	final OWL2QLProfile qlProfile = new OWL2QLProfile();
	final OWLProfileReport elReport = elProfile.checkOntology(ontology);
	final OWLProfileReport qlRerport = qlProfile.checkOntology(ontology);
	final boolean isEL = elReport.isInProfile();
	final boolean isQL = qlRerport.isInProfile();
	if (!isEL && !isQL)
	    throw new UnsupportedOWLProfile(qlRerport.getViolations());
	Tracer.logBool("OWL EL", isEL);
	Tracer.logBool("OWL QL", isQL);
	if (Config.translationAlgorithm != null)
	    return Config.translationAlgorithm;
	if (isQL)
	    return TranslationAlgorithm.DL_LITE_R;
	else if (isEL)
	    return TranslationAlgorithm.EL;
	return null;
    }

    protected boolean hasDisjunctions;

    protected OWLOntology ontology;

    protected final OWLOntologyManager ontologyManager;

    public AbstractOntologyTranslator(OWLOntology ontology)
	    throws OWLOntologyCreationException, OWLOntologyStorageException,
	    UnsupportedOWLProfile, IOException, CloneNotSupportedException {
	this(OWLManager.createOWLOntologyManager(), ontology);
    }

    public AbstractOntologyTranslator(OWLOntologyManager ontologyManager,
	    OWLOntology ontology) throws IOException,
	    OWLOntologyCreationException, OWLOntologyStorageException,
	    CloneNotSupportedException, UnsupportedOWLProfile {
	this.ontologyManager = ontologyManager;
	this.ontology = ontology;
    }

    @Override
    public boolean hasDisjunctions() {
	return hasDisjunctions;
    }

}