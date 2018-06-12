package uk.co.aquaq.kdb.converter;

import kx.c;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class FlipConverterTest {


    @Test
    public void shouldReturnObjectArraysWhenConvertToObjectArraysAndFlipNotNull() {
        c.Flip testDataFlip = FlipTestUtil.createTestFlip();

        List<Object[]> returnedDataObjectArrays = FlipConverter.convertToDataArrays(testDataFlip);

        Object[] expectedObjectArrays = new Object[]{
                FlipTestUtil.expectedObjectArrayRow1(),
                FlipTestUtil.expectedObjectArrayRow2()
        };

        Object[] actualObjectArrays = returnedDataObjectArrays.toArray(new Object[0][]);

        assertArrayEquals(expectedObjectArrays, actualObjectArrays);
    }

    @Test
    public void shouldReturnEmptyListWhenConvertToObjectArraysAndFlipIsNull() {
        List<Object[]> returnedObjectArrays = FlipConverter.convertToDataArrays(null);

        assertThat(returnedObjectArrays.size(), equalTo(0));
    }

}