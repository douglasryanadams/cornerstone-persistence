package com.github.cornerstonews.persistence.jpa.controller;

import com.github.cornerstonews.persistence.jpa.entity.Example;
import com.github.cornerstonews.persistence.jpa.entity.Example_;

import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class ExampleJpaController extends AbstractJpaController<Example> {

    private static final Map<String, SingularAttribute<Example, ?>> ORDERBY_MAP = Map.of(
            "id", Example_.id,
            "someString", Example_.someString,
            "someNumber", Example_.someNumber
    );
    private static final String SUPPORTED_BY = String.join(",", ORDERBY_MAP.keySet());

    public ExampleJpaController(final EntityManagerFactory emf) {
        super(emf, Example.class);
    }

    @Override
    protected SingularAttribute<Example, ?> getValidOrDefaultOrderBy(final String orderBy) {
        if (orderBy == null) return Example_.id;

        if (ORDERBY_MAP.containsKey(orderBy)) {
            return ORDERBY_MAP.get(orderBy);
        }

        throw new RuntimeException("Invalid orderBy: '" + orderBy + "', supported values: " + SUPPORTED_BY);
    }

    @Override
    protected List<Predicate> getSearchPredicates(final Example entity, final CriteriaBuilder cb, final Root<Example> root) {
        final List<Predicate> predicates = new ArrayList<>();
        if (entity.getId() != null) predicates.add(cb.equal(root.get(Example_.id), entity.getId()));
        if (entity.getSomeString() != null) predicates.add(cb.equal(root.get(Example_.someString), entity.getSomeString()));
        if (entity.getSomeNumber() != null) predicates.add(cb.equal(root.get(Example_.someNumber), entity.getSomeNumber()));
        return predicates;
    }

    @Override
    public Object getPrimaryKey(final Example entity) {
        return entity.getId();
    }

    @Override
    public Object convertToPrimaryKeyType(final Object id) {
        return Integer.parseInt(String.valueOf(id));
    }
}