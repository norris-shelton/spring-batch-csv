package com.javaninja.batch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author norris.shelton
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, StepScopeTestExecutionListener.class})
@ContextConfiguration
public class TestReaderAndWriter {

    @Autowired
    private FlatFileItemReader<Car> itemReader;

    @Autowired
    private FlatFileItemWriter<Car> itemWriter;

    public StepExecution getStepExecution() {
        return MetaDataInstanceFactory.createStepExecution();
    }

    @Test
    public void testReader() {
        int count = 0;
        try {
            count = StepScopeTestUtils.doInStepScope(getStepExecution(), () -> {
                int numCars = 0;
                itemReader.open(getStepExecution().getExecutionContext());
                Car car;
                try {
                    while ((car = itemReader.read()) != null) {
                        assertNotNull(car);
                        assertNotNull(car.getMake());
                        assertNotNull(car.getModel());
                        assertNotNull(car.getColor());
                        assertTrue(car.getDoors() > 0);
                        numCars++;
                    }
                } finally {
                    try { itemReader.close(); } catch (ItemStreamException e) { fail(e.toString()); }
                }
                return numCars;
            });
        } catch (Exception e) {
            fail(e.toString());
        }
        assertEquals(100000, count);
    }

    @Test
    public void testWriter() {
        List<Car> cars = new LinkedList<>();
        Car       car;
        for (int i = 1; i < 10001; i++) {
            car = new Car();
            car.setMake("make" + i);
            car.setModel("model" + i);
            car.setColor("color" + i);
            car.setDoors(i);
            cars.add(car);
        }

        try {
            StepScopeTestUtils.doInStepScope(getStepExecution(), () -> {
                try {
                    itemWriter.open(getStepExecution().getExecutionContext());
                    itemWriter.write(cars);
                } finally {
                    itemWriter.close();
                }
                return null;
            });

        } catch (Exception e) {
            fail(e.toString());
        }
    }
}
