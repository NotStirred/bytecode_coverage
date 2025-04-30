package io.github.notstirred.coverage;

import java.lang.reflect.Field;

public class BytecodeCoverageData {
    public static final byte[] DATA = new byte[1 << 23];

    /** Used from reflection */
    @SuppressWarnings("unused")
    public static volatile boolean written = false;

    static {
        try {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass("io.github.notstirred.coverage.BytecodeCoverageData");
                Field dataField = clazz.getField("DATA");
                byte[] data = (byte[]) dataField.get(null);

                System.arraycopy(DATA, 0, data, 0, DATA.length);

                Field writtenField = clazz.getField("written");
                writtenField.setBoolean(null, true);
            } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to send bytecode coverage data.", e);
            }
        }));
        } catch (IllegalStateException e) {
            // already shutting down, ignored
        }
    }
}
