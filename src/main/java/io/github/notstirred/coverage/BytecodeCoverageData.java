package io.github.notstirred.coverage;

import java.lang.reflect.Field;

public class BytecodeCoverageData {
    public static final byte[] DATA = new byte[1 << 23];

    /** Used from reflection */
    @SuppressWarnings("unused")
    public static volatile boolean written = false;

    private static final Field dataField;
    private static final Field writtenField;

    static {
        try {
            Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass("io.github.notstirred.coverage.BytecodeCoverageData");
            dataField = clazz.getField("DATA");
            writtenField = clazz.getField("written");
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException("Failed initialize bytecode coverage.", e);
        }

        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                // Copy this classloader data to shared data.
                System.arraycopy(DATA, 0, getSharedData(), 0, DATA.length);
                setSharedWritten();
            }));
        } catch (IllegalStateException e) {
            // already shutting down, ignored
        }
    }

    /**
     * This is necessary because the javaagent and coverage data are on different classloaders, this is the only reasonable way to communicate between them.
     *
     * @return {@link #DATA} from the shared {@link ClassLoader#getSystemClassLoader()}
     */
    public static byte[] getSharedData() {
        try {
            return (byte[]) dataField.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to get shared coverage data.", e);
        }
    }

    /**
     * This is necessary because the javaagent and coverage data are on different classloaders, this is the only reasonable way to communicate between them.
     * <p>
     * Tells the javaagent that coverage data has been fully written, and it can begin its output.
     */
    private static void setSharedWritten() {
        try {
            writtenField.setBoolean(null, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set shared written flag.", e);
        }
    }

    /**
     * This is necessary because the javaagent and coverage data are on different classloaders, this is the only reasonable way to communicate between them.
     */
    public static boolean getSharedWritten() {
        try {
            return writtenField.getBoolean(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to get shared written flag.", e);
        }
    }
}
