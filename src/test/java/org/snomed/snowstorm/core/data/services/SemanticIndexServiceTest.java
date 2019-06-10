package org.snomed.snowstorm.core.data.services;

import com.google.common.collect.Lists;
import io.kaicode.elasticvc.api.BranchService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.snomed.otf.owltoolkit.constants.Concepts;
import org.snomed.snowstorm.AbstractTest;
import org.snomed.snowstorm.TestConfig;
import org.snomed.snowstorm.core.data.domain.Concept;
import org.snomed.snowstorm.core.data.domain.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static io.kaicode.elasticvc.api.VersionControlHelper.LARGE_PAGE;
import static java.lang.Long.parseLong;
import static org.junit.Assert.*;
import static org.snomed.snowstorm.core.data.domain.Concepts.ISA;
import static org.snomed.snowstorm.core.data.domain.Concepts.SNOMEDCT_ROOT;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class SemanticIndexServiceTest extends AbstractTest {

	@Autowired
	private SemanticIndexService semanticIndexService;

	@Autowired
	private BranchService branchService;

	@Autowired
	private ConceptService conceptService;

	public static final String PATH = "MAIN";

	@Before
	public void setup() throws ServiceException {
		branchService.create("MAIN");
		Concept root = new Concept(SNOMEDCT_ROOT);
		Concept allOrPartOf = new Concept(Concepts.ALL_OR_PART_OF).addRelationship(new Relationship(ISA, SNOMEDCT_ROOT)).addFSN("All or part of (attribute)");
		Concept pizza_2 = new Concept("100002").addRelationship(new Relationship(ISA, SNOMEDCT_ROOT)).addFSN("Pizza");
		Concept cheesePizza_3 = new Concept("100005").addRelationship(new Relationship(ISA, pizza_2.getId())).addFSN("Cheese Pizza");
		Concept reallyCheesyPizza_4 = new Concept("100008").addRelationship(new Relationship(ISA, cheesePizza_3.getId())).addFSN("Really Cheesy Pizza");
		Concept reallyCheesyPizza_5 = new Concept("100003")
				.addRelationship(new Relationship(ISA, pizza_2.getId()))
				.addRelationship(new Relationship(Concepts.ALL_OR_PART_OF, cheesePizza_3.getId()))
				.addFSN("So Cheesy Pizza");
		conceptService.batchCreate(Lists.newArrayList(root, allOrPartOf, pizza_2, cheesePizza_3, reallyCheesyPizza_4, reallyCheesyPizza_5), PATH);

	}

	@Test
	public void findConceptReferences() {
		Map<Long, Set<Long>> conceptReferences = semanticIndexService.findConceptReferences(PATH, 100005L, true, LARGE_PAGE).getMap();
		assertEquals(2, conceptReferences.size());
		assertEquals("[100008]", Arrays.toString(conceptReferences.get(parseLong(ISA)).toArray()));
		assertEquals("[100003]", Arrays.toString(conceptReferences.get(parseLong(Concepts.ALL_OR_PART_OF)).toArray()));

		assertEquals(0, semanticIndexService.findConceptReferences(PATH, parseLong(ISA), true, LARGE_PAGE).getTotalElements());
	}
}
