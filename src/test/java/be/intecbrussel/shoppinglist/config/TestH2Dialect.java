// 1. Hibernate generates a discriminator CHECK constraint that H2 can't evaluate properly.
// When @Inheritance(strategy = InheritanceType.JOINED) is used, Hibernate's DDL generator emits something like:
// sqlCONSTRAINT_2: CHECK (dtype IN ('FoodOriginal', ...))
// H2 evaluates this IN set using a session-bound TreeSet comparator.
// The "database has been closed" error cascades from that comparator trying to reach a
// closed H2 session during constraint validation.
//
// 2. @DataJpaTest may be ignoring your datasource URL.
// @DataJpaTest auto-replaces the datasource with its own embedded H2 unless you opt out, so DB_CLOSE_DELAY=-1 in
// your application-test.properties URL may not even be applied.
//
// Steps to solve:
// src/test/java/be/intecbrussel/shoppinglist/config/TestH2Dialect.java
// Step 1 — Disable the discriminator check constraint: this file.
// Step 2 — Point to it in application-test.properties: see there:
//      spring.jpa.properties.hibernate.dialect=be.intecbrussel.shoppinglist.config.TestH2Dialect
// Step 3 — Opt out of datasource replacement in your test: see there:
//      @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // use YOUR datasource config.

package be.intecbrussel.shoppinglist.config;

import org.hibernate.dialect.H2Dialect;

public class TestH2Dialect extends H2Dialect {
    @Override
    public boolean supportsColumnCheck() {
        return false; // prevents Hibernate from emitting the problematic CHECK (dtype IN (...))
    }
}