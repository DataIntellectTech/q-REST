package uk.co.aquaq.kdb.converter;


import com.kx.c;

public final class FlipTestUtil {

    private static final String COLUMN_NAME_1 = "column1";
    private static final String COLUMN_NAME_2 = "column2";

    private static final String ROW_1_COLUMN_1 = "dataEntry1";
    private static final int ROW_1_COLUMN_2 = 1;

    private static final String ROW_2_COLUMN_1 = "dataEntry2";
    private static final int ROW_2_COLUMN_2 = 2;

    private FlipTestUtil() { }

    static c.Flip createTestFlip() {
        String[] columnNames = new String[]{COLUMN_NAME_1, COLUMN_NAME_2};
        Object[] dataValues = new Object[]{
                new String[]{ROW_1_COLUMN_1, ROW_2_COLUMN_1},
                new int[]{ROW_1_COLUMN_2, ROW_2_COLUMN_2}
        };
        return new c.Flip(new c.Dict(columnNames, dataValues));
    }

    static Object[] expectedObjectArrayRow1() {
        return new Object[]{ROW_1_COLUMN_1, ROW_1_COLUMN_2};
    }
    static Object[] expectedObjectArrayRow2() {
        return new Object[]{ROW_2_COLUMN_1, ROW_2_COLUMN_2};
    }
}
