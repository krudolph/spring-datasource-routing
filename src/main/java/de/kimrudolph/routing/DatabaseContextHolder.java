package de.kimrudolph.routing;

/**
 * Thread shared context to point to the datasource which should be used. This
 * enables context switches between different environments.
 */
public class DatabaseContextHolder {

    private static final ThreadLocal<DatabaseEnvironment> CONTEXT =
        new ThreadLocal<>();

    public static void set(DatabaseEnvironment databaseEnvironment) {
        CONTEXT.set(databaseEnvironment);
    }

    public static DatabaseEnvironment getEnvironment() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

}
