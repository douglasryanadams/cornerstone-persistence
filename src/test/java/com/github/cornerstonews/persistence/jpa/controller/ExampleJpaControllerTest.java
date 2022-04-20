package com.github.cornerstonews.persistence.jpa.controller;

import com.github.cornerstonews.persistence.jpa.entity.Example;
import com.github.cornerstonews.persistence.jpa.entity.Example_;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExampleJpaControllerTest {
    private static int EXAMPLES_COUNT = 100;

    private EntityManagerFactory emf;
    private ExampleJpaController controller;
    private List<Example> examples;
    private Example example;

    private List<Example> createExampleEntities() {
        // Creating these elements w/o using the JpaController to keep test setup
        // separate from test execution.

        final var em = emf.createEntityManager();
        final var transaction = em.getTransaction();

        final var examples = new ArrayList<Example>();
        transaction.begin();
        try {
            for (var i = 0; i < EXAMPLES_COUNT; i++) {
                final var someString = (i % 2 == 0) ? "SomeString-" + i : "someString-" + i;
                final var someNumber = (i % 2 == 0) ? 555 : 777;
                final var thisExample = new Example(someString, someNumber);
                em.persist(thisExample);
                examples.add(thisExample);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
        } finally {
            em.close();
        }
        return examples;
    }

    private void deleteExampleEntities() {
        final var em = emf.createEntityManager();
        final var cb = em.getCriteriaBuilder();
        final var deleteQuery = cb.createCriteriaDelete(Example.class);
        final var transaction = em.getTransaction();

        transaction.begin();
        try {
            em.createQuery(deleteQuery).executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
        } finally {
            em.close();
        }
    }

    @BeforeEach
    void beforeEach() {
        emf = Persistence.createEntityManagerFactory("test-persistence-unit");
        controller = new ExampleJpaController(emf);

        examples = createExampleEntities();
        example = examples.get(0);
    }

    @AfterEach
    void afterEach() {
        deleteExampleEntities();
        emf.close();
    }


    @Test
    void testCreateUpdateDelete() {
        final var initialList = controller.getAll();
        assertEquals(EXAMPLES_COUNT, initialList.size());

        // Create
        final var newExample = new Example("a new example", 100);
        final var created = controller.create(newExample);
        final var found = controller.getReference(created.getId());
        assertEquals(created, found);

        // Update
        created.setSomeString("updated string");
        final var updated = controller.update(created);
        final var afterUpdateFound = controller.getReference(created.getId());
        assertEquals(created, afterUpdateFound);
        assertEquals(updated, afterUpdateFound);

        // Delete
        controller.delete(created);
        final var finalList = controller.getAll();
        assertEquals(EXAMPLES_COUNT, finalList.size());
    }

    @Test
    void testTransactionFailure() {
        assertThrows(RollbackException.class, () ->
                controller.create(example)
        );
    }

    @Test
    void testTransactionFailureRollback() {
        assertThrows(RollbackException.class, () -> controller.performTransaction(example,
                        (EntityManager em, Example example) -> {
                            em.persist(new Example("NewExample1", 1000));
                            em.persist(new Example("NewExample1", 1000));
                            em.persist(new Example("NewExample1", 1000));
                            em.persist(new Example("NewExample1", 1000));
                            em.persist(example); // Causes exception
                        }
                )
        );

        // Validate that none of the other entities created in the rolled back transaction are created
        assertEquals(examples, controller.getAll());
    }


    @Test
    void testGetAll() {
        final List<Example> actual = controller.getAll();
        assertEquals(examples, actual);
    }

    private static Stream<Arguments> getCalls() {
        return Stream.of(
                Arguments.of(0, 10, null, false, false),
                Arguments.of(10, 10, null, false, false),
                Arguments.of(0, 20, null, false, false),
                Arguments.of(0, 10, "someString", false, false),
                Arguments.of(0, 10, "someString", true, false),
                Arguments.of(0, 10, "someString", false, true),
                Arguments.of(0, 10, "someString", true, true)
        );
    }


    @ParameterizedTest(name = "startPosition={0} | maxResults={1} | orderBy={2} | orderByIgnoreCase={3} | desc={4}")
    @MethodSource("getCalls")
    void testGet(int startPosition, int maxResults, String orderBy, boolean orderByIgnoreCase, boolean desc) {
        final List<Example> actual = controller.get(startPosition, maxResults, orderBy, orderByIgnoreCase, desc);
        if (orderBy != null) {
            if (orderByIgnoreCase) examples.sort(Comparator.comparing(Example::getSomeString, CASE_INSENSITIVE_ORDER));
            else examples.sort(Comparator.comparing(Example::getSomeString));

        }
        if (desc) Collections.reverse(examples);
        assertEquals(examples.subList(startPosition, startPosition + maxResults), actual);
    }


    @Test
    void testGetCount() {
        assertEquals(100, controller.getCount());
    }


    @Test
    void testGetReference() {
        assertEquals(example, controller.getReference(example.getId()));
    }


    @Test
    void testFind() {
        assertEquals(example, controller.find(example));
    }


    @Test
    void testFindByPrimaryKey() {
        assertEquals(example, controller.findByPrimaryKey(example.getId().toString()));
    }

    private static Stream<Arguments> findByAllCalls() {
        return Stream.of(
                Arguments.of(null, false, false),
                Arguments.of("someString", false, false),
                Arguments.of(null, true, false),
                Arguments.of(null, false, true),
                Arguments.of("someString", true, true)
        );
    }


    @ParameterizedTest(name = "orderBy={2} | orderByIgnoreCase={3} | desc={4}")
    @MethodSource("findByAllCalls")
    void testFindByFieldAll(String orderBy, boolean orderByIgnoreCase, boolean desc) {
        final List<Example> actual = controller.findBy(Example_.someNumber, 555, orderBy, orderByIgnoreCase, desc);

        final List<Example> expected = examples.stream().filter(e -> e.getSomeNumber() == 555).collect(Collectors.toList());
        if (orderBy != null) {
            if (orderByIgnoreCase) expected.sort(Comparator.comparing(Example::getSomeString, CASE_INSENSITIVE_ORDER));
            else expected.sort(Comparator.comparing(Example::getSomeString));
        }
        if (desc) Collections.reverse(expected);
        assertEquals(expected, actual);
    }


    @ParameterizedTest(name = "orderBy={1} | orderByIgnoreCase={2} | desc={3}")
    @MethodSource("findByAllCalls")
    void testFindByEntityAll(String orderBy, boolean orderByIgnoreCase, boolean desc) {
        final var searchEntity = new Example(null, 555);
        final List<Example> actual = controller.findBy(searchEntity, orderBy, orderByIgnoreCase, desc);

        final List<Example> expected = examples.stream().filter(e -> e.getSomeNumber() == 555).collect(Collectors.toList());
        if (orderBy != null) {
            if (orderByIgnoreCase) expected.sort(Comparator.comparing(Example::getSomeString, CASE_INSENSITIVE_ORDER));
            else expected.sort(Comparator.comparing(Example::getSomeString));
        }
        if (desc) Collections.reverse(expected);
        assertEquals(expected, actual);
    }

    private static Stream<Arguments> findByRangeCalls() {
        return Stream.of(
                Arguments.of(0, 10),
                Arguments.of(10, 20)
        );
    }


    @ParameterizedTest(name = "firstResult={1} | maxResults={2}")
    @MethodSource("findByRangeCalls")
    void testFindByFieldRange(int firstResult, int maxResults) {
        final List<Example> actual = controller.findBy(Example_.someNumber, 555, firstResult, maxResults);
        final List<Example> expected = examples.stream().filter(e -> e.getSomeNumber() == 555).collect(Collectors.toList());
        assertEquals(expected.subList(firstResult, firstResult + maxResults), actual);
    }


    @ParameterizedTest(name = "firstResult={1} | maxResults={2}")
    @MethodSource("findByRangeCalls")
    void testFindByEntityRange(int firstResult, int maxResults) {
        final var searchEntity = new Example(null, 555);
        final List<Example> actual = controller.findBy(searchEntity, firstResult, maxResults);
        final List<Example> expected = examples.stream().filter(e -> e.getSomeNumber() == 555).collect(Collectors.toList());
        assertEquals(expected.subList(firstResult, firstResult + maxResults), actual);
    }


    @Test
    void testFindByCountField() {
        assertEquals(50, controller.findByCount(Example_.someNumber, 555));
    }


    @Test
    void testFindByCountEntity() {
        assertEquals(50, controller.findByCount(new Example(null, 555)));
    }


    @Test
    void testGetAllDefaults() {
        assertEquals(examples, controller.getAll());
    }


    @Test
    void testGetPaginatedDefaults() {
        assertEquals(examples.subList(10, 20), controller.get(10, 10));
    }

    @Test
    void testfindByFieldDefaults() {
        final List<Example> expected = examples.stream().filter(e -> e.getSomeNumber() == 555).collect(Collectors.toList());
        assertEquals(expected, controller.findBy(Example_.someNumber, 555));
    }

    @Test
    void testfindByEntityDefaults() {
        final List<Example> expected = examples.stream().filter(e -> e.getSomeNumber() == 555).collect(Collectors.toList());
        assertEquals(expected, controller.findBy(new Example(null, 555)));
    }

    @Test
    void testFindByFieldIgnoreCase() {
        assertEquals(List.of(), controller.findBy(Example_.someString, "somestring-0"));
        assertEquals(List.of(example), controller.findBy(Example_.someString, "somestring-0", true));
    }

    @Test
    void testFindByFieldIgnoreCasePaginated() {
        final List<Example> expected = examples.stream().filter(e -> e.getSomeNumber() == 555).collect(Collectors.toList());
        assertEquals(expected.subList(10, 20), controller.findBy(Example_.someNumber, 555, false, 10, 10));
    }

    @Test
    void testGetSLikeString() {
        assertEquals("%SEARCHSTRING%", controller.getLikeString("searchstring"));
    }

}
