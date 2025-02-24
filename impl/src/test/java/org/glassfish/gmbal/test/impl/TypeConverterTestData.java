/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.gmbal.test.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedData;
import org.glassfish.gmbal.impl.ManagedObjectManagerInternal;
import org.glassfish.gmbal.impl.TypeConverter;
import org.glassfish.gmbal.typelib.EvaluatedType;
import org.glassfish.gmbal.typelib.TypeEvaluator;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.glassfish.gmbal.test.OpenMBeanTools.array;
import static org.glassfish.gmbal.test.OpenMBeanTools.comp;
import static org.glassfish.gmbal.test.OpenMBeanTools.compV;
import static org.glassfish.gmbal.test.OpenMBeanTools.equalTypes;
import static org.glassfish.gmbal.test.OpenMBeanTools.equalValues;
import static org.glassfish.gmbal.test.OpenMBeanTools.item;
import static org.glassfish.gmbal.test.OpenMBeanTools.list;
import static org.glassfish.gmbal.test.OpenMBeanTools.listO;
import static org.glassfish.gmbal.test.OpenMBeanTools.mkmap;

/**
 *
 * @author ken
 */
public class TypeConverterTestData {
    private TypeConverterTestData() {}

    /* For each test:
     *      Java type
     *      Expected OpenType
     *      Java value
     *      Expected OpenType value
     *      value of isIdentity
     Needed test cases:
    Parameterized type
        SortedSet (MXBean semantics)
        Collection
        Iterable
        Iterator
        Enumeration
        Map
        SortedMap (MXBean semantics)
        Dictionary
        Other
    Array Type
    Default case (currently handleAsString: move to handle as in MXBean)
    @ManagedObject
    enum
    MXBean behavior
        OpenType(J) -> Java class J
            CompositeData -> Java:
                1. J has method public static J from( CompositeData cd )
                2. public @ConstructorProperties constructor
                3. J is a class with public no-arg constructor, and every getter
                   has a setter
                4. J is an interface with only getters

     */

// Data used for any TypeConverter test

    public static class TestData {
        private Object data ;           // The test data
        private OpenType otype ;        // The expected open type
        private Object ovalue ;         // The expected result of toManagedData
        private boolean isIdentity ;    // The expected value of tc.isIdentity()

        public TestData( final Object data, final OpenType otype,
            final Object ovalue, final boolean isIdentity ) {
            this.data = data ;
            this.otype = otype ;
            this.ovalue = ovalue ;
            this.isIdentity = isIdentity ;
        }

        public TestData( final Object data, final OpenType otype,
            final Object ovalue ) {
            this( data, otype, ovalue, false ) ;
        }

        Object data() { return data ; }
        OpenType otype() { return otype ; }
        Object ovalue() { return ovalue ; }
        boolean isIdentity() { return isIdentity ; }

        void test( ManagedObjectManagerInternal mom ) {
            EvaluatedType et = TypeEvaluator.getEvaluatedType(data.getClass()) ;
            TypeConverter tc = mom.getTypeConverter(et) ;

            assertTrue( equalTypes( otype, tc.getManagedType() ) ) ;

            assertEquals( et, tc.getDataType() ) ;

            assertEquals( isIdentity, tc.isIdentity()) ;

            Object mobj = tc.toManagedEntity(data) ;
            assertTrue( equalValues( ovalue, mobj ) );

            try {
                Object res = tc.fromManagedEntity(mobj) ;
                assertEquals( data, res ) ;
            } catch (UnsupportedOperationException exc) {
                System.out.println( "Conversion to Java type not currently supported for "
                    + tc.getManagedType() ) ;
            }
        }
    }

// DATA1: Composite data test

    private static final String LIST_DESC = "Description of the list attribute" ;

    public interface TestBase<T> {
        @ManagedAttribute
        @Description( LIST_DESC )
        List<T> getList() ;
    }

    private static final String VALUE_DESC = "Description of the value attribute" ;
    private static final String DATA1_NAME = "DATA1" ;
    private static final String DATA1_DESC = "Description of Data1 type" ;

    @ManagedData( name=DATA1_NAME )
    @Description( DATA1_DESC )
    public static class Data1 implements TestBase<String> {
        private final int value ;
        private final List<String> list ;

        @ManagedAttribute
        @Description( VALUE_DESC )
        public int value() { return value ; }

        @Override
        @ManagedAttribute
        @Description( LIST_DESC )
        public List<String> getList() { return list ; }

        public Data1( int value, String... args ) {
            this.value = value ;
            this.list = Arrays.asList( args ) ;
        }
    }

    private static final CompositeType DATA1_OTYPE =
        comp( DATA1_NAME, DATA1_DESC,
            item( "list", LIST_DESC, array( 1, SimpleType.STRING )),
            item( "value", VALUE_DESC, SimpleType.INTEGER )
        ) ;

    private static String[] data1List = { "One", "Two", "Three" } ;

    private static final Data1 data1 = new Data1( 21, data1List ) ;

    private static final CompositeData data1Open =
        compV( DATA1_OTYPE,
            mkmap(
                list( "list", "value" ),
                listO( data1List, 21 ) ) ) ;

    public static final TestData Data1TestData = new TestData( data1,
        DATA1_OTYPE, data1Open ) ;

// DATA2: enum test

    enum Color { RED, GREEN, BLUE }

    public static final TestData Data2TestData = new TestData( Color.RED,
        SimpleType.STRING, "RED" ) ;

// DATA3:

    private static final String DOUBLE_INDEX_NAME = "DoubleIndex" ;
    private static final String DOUBLE_INDEX_DESC = "DoubleIndex data test" ;
    private static final String DOUBLE_INDEX_ATTR_DESC_1 = "Attribute 1" ;
    private static final String DOUBLE_INDEX_ATTR_DESC_2 = "Attribute 2" ;

    @ManagedData( name=DOUBLE_INDEX_NAME )
    @Description( DOUBLE_INDEX_DESC )
    public static class DoubleIndexData {
        private static final String[][] data = {
            { "R", "G", "B" },
            { "1", "2", "3", "4", "5" }
        } ;

        @ManagedAttribute
        @Description( DOUBLE_INDEX_ATTR_DESC_1 )
        List<List<String>> get1() {
            List<List<String>> result = new ArrayList<List<String>>() ;
            for (String[] sa : data) {
                result.add( Arrays.asList(sa) ) ;
            }
            return result ;
        }

        @ManagedAttribute
        @Description( DOUBLE_INDEX_ATTR_DESC_2 )
        String[][] get2() { return data ; }
    }

    private static CompositeType doubleIndexOpenType =
        comp( DOUBLE_INDEX_NAME, DOUBLE_INDEX_DESC,
            item( "1", DOUBLE_INDEX_ATTR_DESC_1, array( 2, SimpleType.STRING )),
            item( "2", DOUBLE_INDEX_ATTR_DESC_2, array( 2, SimpleType.STRING ))
        ) ;

    private static CompositeData doubleIndexOpenData = compV( doubleIndexOpenType,
        mkmap( list( "1", "2" ),
               list( (Object)DoubleIndexData.data,
                   (Object)DoubleIndexData.data )));

    public static final TestData Data3TestData = new TestData(
        new DoubleIndexData(),
            doubleIndexOpenType, doubleIndexOpenData ) ;

}
