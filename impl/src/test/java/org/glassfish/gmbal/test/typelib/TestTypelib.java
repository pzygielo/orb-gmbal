/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

/*
 * @test
 * @summary Tests com.sun.beans.TypeResolver
 * @author Eamonn McManus
 * @author Ken Cavanaugh
 */

package org.glassfish.gmbal.test.typelib ;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.glassfish.gmbal.typelib.DeclarationFactory;
import org.glassfish.gmbal.typelib.EvaluatedClassDeclaration;
import org.glassfish.gmbal.typelib.EvaluatedMethodDeclaration;
import org.glassfish.gmbal.typelib.EvaluatedType;
import org.glassfish.gmbal.typelib.TypeEvaluator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestTypelib extends TestCase {
    private static final boolean VERBOSE = false ;

    private static class TypelibTestCase extends TestCase {
        private Class cls ;

        TypelibTestCase( Class cls ) {
            super( cls.getName() ) ;
            this.cls = cls ;
        }

        @Override
        protected void runTest() throws Throwable {
            if (VERBOSE) {
                System.out.println("Test " + cls);
            }

            try {
                cls.getMethod("getThing");
            } catch (NoSuchMethodException e) {
                fail( "Class " + cls + " does not have a getThing method defined") ;
            }

            EvaluatedClassDeclaration et =
                (EvaluatedClassDeclaration)TypeEvaluator.getEvaluatedType( cls ) ;
            EvaluatedMethodDeclaration em = TestTypelibDecls.getMethod( et,
                "getThing" ) ;
            if (em == null) {
                fail( "Evaluated Class " + et + " does not have a getThing method") ;
            }
            EvaluatedType rtype = em.returnType() ;

            Object expect = null;
            try {
                Field f = cls.getDeclaredField("expect");
                expect = f.get(null);
            } catch (NoSuchFieldException e) {
                Class<?> outer = cls.getDeclaringClass();
                if (outer != null) {
                    try {
                        Field f = outer.getDeclaredField("expect" + cls.getSimpleName());
                        expect = f.get(null);
                    } catch (NoSuchFieldException e1) {
                    }
                }
            }
            if (expect == null) {
                fail( "TEST ERROR: class " + cls.getName()
                    + " has getThing() method " + "but not expect field");
            }

            if (VERBOSE) {
                System.out.print("..." + rtype);
            }

            // check expected value, and incidentally equals method defined
            // by private implementations of the various Type interfaces
            if (expect.equals(rtype) && rtype.equals(expect)) {
                if (VERBOSE) {
                    System.out.println( "result = " + rtype + ", as expected");
                }
            } else {
                fail( "for class " + cls + ", rtype = " + rtype + " BUT SHOULD BE " + expect );
            }
        }
    }

    /* This test needs additional work to port from the original.
     * What I think should happen here is that T should be bound in Outer,
     * so that Inner.getThing should pick up the outer type bound, even though
     * the Outer class is not the one we are processing?
     *
    public static class Outer<T> {
        public class Inner {
            public T getThing() {
                return null;
            }
        }

        static final EvaluatedType expectInner = EvaluatedType.EOBJECT ;
    }
     */

    public static class Super<T> {
        static final EvaluatedType expect = EvaluatedType.EOBJECT ;

        public T getThing() {
            return null;
        }
    }

    public static class Int extends Super<Integer> {
        static final EvaluatedType expect = EvaluatedType.EINTW ;
    }

/*
    public static class IntOverride extends Int {
        static final EvaluatedType expect = EvaluatedType.EINTW ;

        @Override
        public Integer getThing() {
            return null;
        }
    }
*/

    public static class Mid<X> extends Super<X> {
        static final EvaluatedType expect = EvaluatedType.EOBJECT ;
    }

    public static class Str extends Mid<String> {
        static final EvaluatedType expect = EvaluatedType.ESTRING ;
    }

    public static class ListInt extends Super<List<Integer>> {
        static final EvaluatedType expect = TestTypelibDecls.Prototypes.LIST_INTEGER ;
    }

/*
    public static class ListIntSub extends ListInt {
        static final EvaluatedType expect = TestTypelibDecls.Prototypes.LIST_INTEGER ;

        @Override
        public List<Integer> getThing() {
            return null;
        }
    }
*/

    public static class ListU<U> extends Super<List<U>> {
        static final EvaluatedType expect = TestTypelibDecls.Prototypes.LIST_OBJECT ;
    }

    public static class ListUInt extends ListU<Integer> {
        static final EvaluatedType expect = TestTypelibDecls.Prototypes.LIST_INTEGER ;
    }

/*
    public static class ListUSub<V> extends ListU<V> {
        static final EvaluatedType expect = TestTypelibDecls.Prototypes.LIST_OBJECT ;

        public List<V> getThing() {
            return null;
        }
    }

    public static class ListUSubInt extends ListUSub<Integer> {
        static final EvaluatedType expect = TestTypelibDecls.Prototypes.LIST_INTEGER ;
    }
*/

    public static class TwoParams<S, T> extends Super<S> {
        static final EvaluatedType expect = EvaluatedType.EOBJECT ;
    }

    public static class TwoParamsSub<T> extends TwoParams<T, Integer> {
        static final EvaluatedType expect = EvaluatedType.EOBJECT ;
    }

    public static class TwoParamsSubSub extends TwoParamsSub<String> {
        static final EvaluatedType expect = EvaluatedType.ESTRING ;
    }

    public static interface Intf<T> {
        static final EvaluatedType expect = EvaluatedType.EOBJECT ;

        public T getThing();
    }

    public static abstract class Impl implements Intf<String> {
        static final EvaluatedType expect = EvaluatedType.ESTRING ;
    }

    public static class Impl2 extends Super<String> implements Intf<String> {
        static final EvaluatedType expect = EvaluatedType.ESTRING ;
    }

    public static class Bound<T extends Number> extends Super<T> {
        static final EvaluatedType expect = EvaluatedType.ENUMBER ;
    }

    public static class BoundInt extends Bound<Integer> {
        static final EvaluatedType expect = EvaluatedType.EINTW ;
    }

    public static class RawBound extends Bound {
        static final EvaluatedType expect = EvaluatedType.ENUMBER ;
    }

    public static class RawBoundInt extends BoundInt {
        static final EvaluatedType expect = EvaluatedType.EINTW ;
    }

    public static class MethodParam<T> {
        static final EvaluatedType expect = EvaluatedType.EOBJECT ;

        public <T> T getThing() {
            return null;
        }
    }

    public static class Raw extends Super {
        static final EvaluatedType expect = EvaluatedType.EOBJECT ;
    }

    public static class RawSub extends Raw {
        static final EvaluatedType expect = EvaluatedType.EOBJECT ;
    }

    public static class SimpleArray extends Super<String[]> {
        static final EvaluatedType expect = DeclarationFactory.egat( EvaluatedType.ESTRING ) ;
    }

    public static class GenericArray extends Super<List<String>[]> {
        static final EvaluatedType expect = DeclarationFactory.egat(
            TestTypelibDecls.Prototypes.LIST_STRING ) ;
    }

    public static class GenericArrayT<T> extends Super<T[]> {
        static final EvaluatedType expect =
            DeclarationFactory.egat( EvaluatedType.EOBJECT ) ;
    }

    public static class GenericArrayTSub extends GenericArrayT<String[]> {
        static final EvaluatedType expect = DeclarationFactory.egat(
            DeclarationFactory.egat(EvaluatedType.ESTRING )) ;
    }

    public static class Wildcard extends Super<List<?>> {
        static final EvaluatedType expect = TestTypelibDecls.Prototypes.LIST_OBJECT ;
    }

    public static class WildcardT<T> extends Super<List<? extends T>> {
        static final EvaluatedType expect = TestTypelibDecls.Prototypes.LIST_OBJECT ;
    }

    public static class WildcardTSub extends WildcardT<Integer> {
        static final EvaluatedType expect = TestTypelibDecls.Prototypes.LIST_INTEGER ;
    }

    public static class WildcardTSubSub<X> extends WildcardTSub {
        // X is just so we can have a raw subclass
        static final EvaluatedType expect = WildcardTSub.expect;
    }

    public static class RawWildcardTSubSub extends WildcardTSubSub {
        static final EvaluatedType expect = TestTypelibDecls.Prototypes.LIST_INTEGER ;
    }

    public static class WildcardTSuper<T> extends Super<List<? super T>> {
        static final EvaluatedType expect = TestTypelibDecls.Prototypes.LIST_OBJECT ;
    }

    public static class WildcardTSuperSub extends WildcardTSuper<Integer> {
        static final EvaluatedType expect = TestTypelibDecls.Prototypes.LIST_OBJECT ;
    }

    public static class SuperMap<K, V> {
        static final EvaluatedType expect = TestTypelibDecls.Prototypes.MAP_OBJECT_OBJECT ;

        public Map<K, V> getThing() {
            return null;
        }
    }

    public static class SubMap extends SuperMap<String, Integer> {
        static final EvaluatedType expect = TestTypelibDecls.Prototypes.MAP_STRING_INTEGER ;
    }

    public static class ListListT<T> extends Super<List<List<T>>> {
        static final EvaluatedType expect = TestTypelibDecls.Prototypes.LIST_LIST_OBJECT ;
    }

    public static class ListListString extends ListListT<String> {
        static final EvaluatedType expect = TestTypelibDecls.Prototypes.LIST_LIST_STRING ;
    }

    public static class UExtendsT<T, U extends T> extends Super<U> {
        static final EvaluatedType expect = EvaluatedType.EOBJECT ;
    }

    public static class UExtendsTSub extends UExtendsT<Number, Integer> {
        static final EvaluatedType expect = EvaluatedType.EINTW ;
    }


    public static class SelfRef<T extends SelfRef<T>> extends Super<T> {
        static final EvaluatedType expect = TypeEvaluator.getEvaluatedType(
            SelfRef.class);
    }

    /* This case needs a different implementation here: need to explicitly examine
     * the contents of getEvaluatedType( SelfRefSub.class ): really can't
     * construct an exemplar here.

    public static class SelfRefSub extends SelfRef<SelfRefSub> {
        static final EvaluatedType expect = SelfRefSub.class;
    }
     */

    private static class ClassNameComparator implements Comparator<Class<?>> {
        @Override
        public int compare(Class<?> a, Class<?> b) {
            return a.getName().compareTo(b.getName());
        }
    }

    /* Too much output in tests
    private static class DumpTestCase extends TestCase {
        public void runTest() {
            TypeEvaluator.dumpEvalClassMap();
        }
    }
    */

    private static final Comparator<Class<?>> classNameComparator =
            new ClassNameComparator();

    public static Test suite() {
        TestSuite main = new TestSuite() ;
        Class<?>[] nested = TestTypelib.class.getClasses();
        Arrays.sort(nested, classNameComparator);
        for (Class<?> n : nested) {
            main.addTest( new TypelibTestCase( n )) ;
        }
        // main.addTest( new DumpTestCase() ) ;

        return main ;
    }
}
