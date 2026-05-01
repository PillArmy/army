/*
 * Copyright 2023-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.army.criteria.impl;


import io.army.criteria.Expression;
import io.army.criteria.SimpleExpression;
import io.army.criteria.SimplePredicate;
import io.army.criteria.TypeInfer;
import io.army.mapping.BooleanType;
import io.army.mapping.DoubleType;
import io.army.mapping.IntegerType;
import io.army.mapping.MappingType;
import io.army.mapping.postgre.spatial.postgre.*;

import java.util.function.BiFunction;

/// 
/// Package class,This class hold postgre geometric function methods.
/// * @since 0.6.0
abstract class PostgreGeometricFunctions extends PostgreDateTimeFunctions {

    /// package constructor
    PostgreGeometricFunctions() {
    }


    /*-------------------below Geometric Functions and Operators -------------------*/

/// 
/// The {@link MappingType} of function return type: {@link  DoubleType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-FUNC-TABLE">area ( geometric_type ) → double precision
/// Computes area. Available for box, path, circle. A path input must be closed, else NULL is returned. Also, if the path is self-intersecting, the result may be meaningless.
/// area(box '(2,2),(0,0)') → 4
/// </a>
    public static SimpleExpression area(Expression geometricType) {
        return LiteralFunctions.oneArgFunc("AREA", geometricType);
    }

/// 
/// The {@link MappingType} of function return type: {@link  PostgrePointType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-FUNC-TABLE">center ( geometric_type ) → point
/// Computes center point. Available for box, circle.
/// center(box '(1,2),(0,0)') → (0.5,1)
/// </a>
    public static SimpleExpression center(Expression geometricType) {
        return LiteralFunctions.oneArgFunc("CENTER", geometricType);
    }

/// 
/// The {@link MappingType} of function return type: {@link  PostgreLsegType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-FUNC-TABLE">diagonal ( box ) → lseg
/// Extracts box's diagonal as a line segment (same as lseg(box)).
/// diagonal(box '(1,2),(0,0)') → [(1,2),(0,0)]
/// </a>
    public static SimpleExpression diagonal(Expression box) {
        return LiteralFunctions.oneArgFunc("DIAGONAL", box);
    }

/// The {@link MappingType} of function return type: {@link  DoubleType}
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-FUNC-TABLE">diameter ( circle ) → double precision
/// Computes diameter of circle.
/// diameter(circle '<(0,0),2>') → 4
/// </a>
    public static SimpleExpression diameter(Expression circle) {
        return LiteralFunctions.oneArgFunc("DIAMETER", circle);
    }

/// 
/// The {@link MappingType} of function return type: {@link  DoubleType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-FUNC-TABLE">height ( box ) → double precision
/// Computes vertical size of box.
/// height(box '(1,2),(0,0)') → 2
/// </a>
    public static SimpleExpression height(Expression box) {
        return LiteralFunctions.oneArgFunc("HEIGHT", box);
    }

/// 
/// The {@link MappingType} of function return type: 
/// - If geometricType is {@link MappingType.SqlGeometry}  or {@link PostgreGeometricType},then {@link DoubleType}
/// - Else {@link IntegerType}
/// 
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-FUNC-TABLE">length ( geometric_type ) → double precision
/// Computes the total length. Available for lseg, path.
/// length(path '((-1,0),(1,0))') → 4
/// </a>
/// @see <a href="https://www.postgresql.org/docs/current/functions-textsearch.html#TEXTSEARCH-FUNCTIONS-TABLE">length ( tsvector ) → integer
/// Returns the number of lexemes in the tsvector.
/// </a>
    public static SimpleExpression length(Expression geometricType) {
        return LiteralFunctions.oneArgFunc("LENGTH", geometricType);
    }

/// 
/// The {@link MappingType} of function return type: {@link  IntegerType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-FUNC-TABLE">npoints ( geometric_type ) → integer
/// Returns the number of points. Available for path, polygon.
/// npoints(path '[(0,0),(1,1),(2,0)]') → 3
/// </a>
    public static SimpleExpression npoints(Expression geometricType) {
        return LiteralFunctions.oneArgFunc("NPOINTS", geometricType);
    }

/// 
/// The {@link MappingType} of function return type: {@link  PostgrePathType}
/// *
/// @param funcRef the reference of method,Note: it's the reference of method,not lambda. Valid method:
/// 
/// - {@link SQLs#param(TypeInfer, Object)}
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#namedParam(TypeInfer, String)} ,used only in INSERT( or batch update/delete ) syntax
/// - {@link SQLs#namedLiteral(TypeInfer, String)} ,used only in INSERT( or batch update/delete in multi-statement) syntax
/// - developer custom method
/// .
/// The first argument of funcRef always is {@link PostgrePathType#INSTANCE}.
/// @param path    non-null and non-empty,it will be passed to funcRef as the second argument of funcRef
/// @see #pclose(Expression)
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-FUNC-TABLE">pclose ( path ) → path
/// Converts path to closed form.
/// pclose(path '[(0,0),(1,1),(2,0)]') → ((0,0),(1,1),(2,0))
/// </a>
    public static SimpleExpression pclose(BiFunction<MappingType, String, Expression> funcRef, String path) {
        return pclose(funcRef.apply(PostgrePathType.INSTANCE, path));
    }

/// 
/// The {@link MappingType} of function return type: {@link  PostgrePathType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-FUNC-TABLE">pclose ( path ) → path
/// Converts path to closed form.
/// pclose(path '[(0,0),(1,1),(2,0)]') → ((0,0),(1,1),(2,0))
/// </a>
    public static SimpleExpression pclose(Expression path) {
        return LiteralFunctions.oneArgFunc("PCLOSE", path);
    }

/// 
/// The {@link MappingType} of function return type: {@link  PostgrePathType}
/// *
/// @param funcRef the reference of method,Note: it's the reference of method,not lambda. Valid method:
/// 
/// - {@link SQLs#param(TypeInfer, Object)}
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#namedParam(TypeInfer, String)} ,used only in INSERT( or batch update/delete ) syntax
/// - {@link SQLs#namedLiteral(TypeInfer, String)} ,used only in INSERT( or batch update/delete in multi-statement) syntax
/// - developer custom method
/// .
/// The first argument of funcRef always is {@link PostgrePathType#INSTANCE}.
/// @param path    non-null and non-empty,it will be passed to funcRef as the second argument of funcRef
/// @see #popen(Expression)
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-FUNC-TABLE">popen ( path ) → path
/// Converts path to open form.
/// popen(path '((0,0),(1,1),(2,0))') → [(0,0),(1,1),(2,0)]
/// </a>
    public static SimpleExpression popen(BiFunction<MappingType, String, Expression> funcRef, String path) {
        return popen(funcRef.apply(PostgrePathType.INSTANCE, path));
    }

/// 
/// The {@link MappingType} of function return type: {@link  PostgrePathType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-FUNC-TABLE">popen ( path ) → path
/// Converts path to open form.
/// popen(path '((0,0),(1,1),(2,0))') → [(0,0),(1,1),(2,0)]
/// </a>
    public static SimpleExpression popen(Expression path) {
        return LiteralFunctions.oneArgFunc("POPEN", path);
    }

/// 
/// The {@link MappingType} of function return type: {@link  DoubleType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-FUNC-TABLE">radius ( circle ) → double precision
/// Computes radius of circle.
/// radius(circle '<(0,0),2>') → 2
/// </a>
    public static SimpleExpression radius(Expression circle) {
        return LiteralFunctions.oneArgFunc("RADIUS", circle);
    }

/// 
/// The {@link MappingType} of function return type: {@link  DoubleType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-FUNC-TABLE">slope ( point, point ) → double precision
/// Computes slope of a line drawn through the two points.
/// slope(point '(0,0)', point '(2,1)') → 0.5
/// </a>
    public static SimpleExpression slope(Expression point1, Expression point2) {
        return LiteralFunctions.twoArgFunc("SLOPE", point1, point2);
    }


/// 
/// The {@link MappingType} of function return type: {@link  DoubleType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-FUNC-TABLE">width ( box ) → double precision
/// Computes horizontal size of box.
/// width(box '(1,2),(0,0)') → 1
/// </a>
    public static SimpleExpression width(Expression box) {
        return LiteralFunctions.oneArgFunc("WIDTH", box);
    }


/// 
/// The {@link MappingType} of function return type: {@link PostgreBoxType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-CONV-TABLE">box ( circle ) → box
/// Computes box inscribed within the circle.
/// box(circle '<(0,0),2>') → (1.414213562373095,1.414213562373095),(-1.414213562373095,-1.414213562373095)
/// box ( point ) → box
/// Converts point to empty box.
/// box(point '(1,0)') → (1,0),(1,0)
/// Converts any two corner points to box.
/// box ( polygon ) → box
/// Computes bounding box of polygon.
/// box(polygon '((0,0),(1,1),(2,0))') → (2,1),(0,0)
/// </a>
    public static SimpleExpression box(Expression exp) {
        return LiteralFunctions.oneArgFunc("BOX", exp);
    }

/// 
/// The {@link MappingType} of function return type: {@link PostgreBoxType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-CONV-TABLE">box ( point, point ) → box
/// Converts any two corner points to box.
/// box(point '(0,1)', point '(1,0)') → (1,1),(0,0)
/// </a>
    public static SimpleExpression box(Expression exp1, Expression exp2) {
        return LiteralFunctions.twoArgFunc("BOX", exp1, exp2);
    }

/// 
/// The {@link MappingType} of function return type: {@link PostgreBoxType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-CONV-TABLE">bound_box ( box, box ) → box
/// Computes bounding box of two boxes.
/// bound_box(box '(1,1),(0,0)', box '(4,4),(3,3)') → (4,4),(0,0)
/// </a>
    public static SimpleExpression boundBox(Expression exp1, Expression exp2) {
        return LiteralFunctions.twoArgFunc("BOUND_BOX", exp1, exp2);
    }

/// 
/// The {@link MappingType} of function return type: {@link PostgreCircleType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-CONV-TABLE">circle ( box ) → circle
/// Computes smallest circle enclosing box.
/// circle(box '(1,1),(0,0)') → <(0.5,0.5),0.7071067811865476> 
/// circle ( polygon ) → circle
/// Converts polygon to circle. The circle's center is the mean of the positions of the polygon's points, and the radius is the average distance of the polygon's points from that center.
/// circle(polygon '((0,0),(1,3),(2,0))') → <(1,1),1.6094757082487299>
/// </a>
    public static SimpleExpression circle(Expression exp) {
        return LiteralFunctions.oneArgFunc("CIRCLE", exp);
    }

/// 
/// The {@link MappingType} of function return type: {@link PostgreCircleType}
/// *
/// @param funcRefForPoint  the reference of method,Note: it's the reference of method,not lambda. Valid method:
/// 
/// - {@link SQLs#param(TypeInfer, Object)}
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#namedParam(TypeInfer, String)} ,used only in INSERT( or batch update/delete ) syntax
/// - {@link SQLs#namedLiteral(TypeInfer, String)} ,used only in INSERT( or batch update/delete in multi-statement) syntax
/// - developer custom method
/// .
/// The first argument of funcRefForPoint always is {@link PostgrePointType#INSTANCE}.
/// @param point            it will be passed to funcRefForPoint as the second argument of funcRefForPoint
/// @param funcRefForRadius the reference of method,Note: it's the reference of method,not lambda. Valid method:
/// 
/// - {@link SQLs#param(TypeInfer, Object)}
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#namedParam(TypeInfer, String)} ,used only in INSERT( or batch update/delete ) syntax
/// - {@link SQLs#namedLiteral(TypeInfer, String)} ,used only in INSERT( or batch update/delete in multi-statement) syntax
/// - developer custom method
/// .
/// The first argument of funcRefForRadius always is {@link DoubleType#INSTANCE}.
/// @param radius           it will be passed to funcRefForRadius as the second argument of funcRefForRadius
/// @see #circle(Expression, Expression)
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-CONV-TABLE">circle ( point, double precision ) → circle
/// Constructs circle from center and radius.
/// circle(point '(0,0)', 2.0) → <(0,0),2>
/// </a>
    public static <T> Expression circle(BiFunction<MappingType, String, Expression> funcRefForPoint, String point,
                                              BiFunction<MappingType, T, Expression> funcRefForRadius, T radius) {
        return circle(funcRefForPoint.apply(PostgrePointType.INSTANCE, point),
                funcRefForRadius.apply(DoubleType.INSTANCE, radius)
        );
    }

/// 
/// The {@link MappingType} of function return type: {@link PostgreCircleType}
/// *
/// @param funcRef the reference of method,Note: it's the reference of method,not lambda. Valid method:
/// 
/// - {@link SQLs#param(TypeInfer, Object)}
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#namedParam(TypeInfer, String)} ,used only in INSERT( or batch update/delete ) syntax
/// - {@link SQLs#namedLiteral(TypeInfer, String)} ,used only in INSERT( or batch update/delete in multi-statement) syntax
/// - developer custom method
/// .
/// The first argument of funcRef always is {@link DoubleType#INSTANCE}.
/// @param radius  it will be passed to funcRef as the second argument of funcRef
/// @see #circle(Expression, Expression)
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-CONV-TABLE">circle ( point, double precision ) → circle
/// Constructs circle from center and radius.
/// circle(point '(0,0)', 2.0) → <(0,0),2>
/// </a>
    public static <T> Expression circle(Expression point, BiFunction<MappingType, T, Expression> funcRef,
                                              T radius) {
        return circle(point, funcRef.apply(DoubleType.INSTANCE, radius));
    }

/// 
/// The {@link MappingType} of function return type: {@link PostgreCircleType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-CONV-TABLE">circle ( point, double precision ) → circle
/// Constructs circle from center and radius.
/// circle(point '(0,0)', 2.0) → <(0,0),2>
/// </a>
    public static SimpleExpression circle(Expression point, Expression radius) {
        return LiteralFunctions.twoArgFunc("CIRCLE", point, radius);
    }

/// 
/// The {@link MappingType} of function return type: {@link PostgreLineType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-CONV-TABLE">line ( point, point ) → line
/// Converts two points to the line through them.
/// line(point '(-1,0)', point '(1,0)') → {0,-1,0}
/// </a>
    public static SimpleExpression line(Expression point1, Expression point2) {
        return LiteralFunctions.twoArgFunc("LINE", point1, point2);
    }

/// 
/// The {@link MappingType} of function return type: {@link PostgreLsegType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-CONV-TABLE">lseg ( box ) → lseg
/// Extracts box's diagonal as a line segment.
/// lseg(box '(1,0),(-1,0)') → [(1,0),(-1,0)]
/// </a>
    public static SimpleExpression lseg(Expression exp) {
        return LiteralFunctions.oneArgFunc("LSEG", exp);
    }

/// 
/// The {@link MappingType} of function return type: {@link PostgreLsegType}
/// *
/// @param funcRef the reference of method,Note: it's the reference of method,not lambda. Valid method:
/// 
/// - {@link SQLs#param(TypeInfer, Object)}
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#namedParam(TypeInfer, String)} ,used only in INSERT( or batch update/delete ) syntax
/// - {@link SQLs#namedLiteral(TypeInfer, String)} ,used only in INSERT( or batch update/delete in multi-statement) syntax
/// - developer custom method
/// .
/// The first argument of funcRef always is {@link PostgrePointType#INSTANCE}.
/// @param point1  it will be passed to funcRef as the second argument of funcRef
/// @param point2  it will be passed to funcRef as the second argument of funcRef
/// @see #lseg(Expression, Expression)
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-CONV-TABLE">lseg ( point, point ) → lseg
/// Constructs line segment from two endpoints.
/// lseg(point '(-1,0)', point '(1,0)') → [(-1,0),(1,0)]
/// </a>
    public static SimpleExpression lseg(BiFunction<MappingType, String, Expression> funcRef, String point1,
                                        String point2) {
        return lseg(funcRef.apply(PostgrePointType.INSTANCE, point1),
                funcRef.apply(PostgrePointType.INSTANCE, point2)
        );
    }

/// 
/// The {@link MappingType} of function return type: {@link PostgreLsegType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-CONV-TABLE">lseg ( point, point ) → lseg
/// Constructs line segment from two endpoints.
/// lseg(point '(-1,0)', point '(1,0)') → [(-1,0),(1,0)]
/// </a>
    public static SimpleExpression lseg(Expression point1, Expression point2) {
        return LiteralFunctions.twoArgFunc("LSEG", point1, point2);
    }

/// 
/// The {@link MappingType} of function return type: {@link PostgrePathType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-CONV-TABLE">path ( polygon ) → path
/// Converts polygon to a closed path with the same list of points.
/// path(polygon '((0,0),(1,1),(2,0))') → ((0,0),(1,1),(2,0))
/// </a>
    public static SimpleExpression path(Expression exp) {
        return LiteralFunctions.oneArgFunc("PATH", exp);
    }


/// 
/// The {@link MappingType} of function return type: {@link PostgrePointType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-CONV-TABLE">point ( box ) → point
/// Computes center of box.
/// point(box '(1,0),(-1,0)') → (0,0)
/// point ( circle ) → point
/// Computes center of circle.
/// point(circle '<(0,0),2>') → (0,0)
/// point ( lseg ) → point
/// Computes center of line segment. 
/// point(lseg '[(-1,0),(1,0)]') → (0,0) 
/// point ( polygon ) → point 
/// Computes center of polygon (the mean of the positions of the polygon's points). 
/// point(polygon '((0,0),(1,1),(2,0))') → (1,0.3333333333333333)
/// </a>
    public static SimpleExpression point(Expression exp) {
        return LiteralFunctions.oneArgFunc("POINT", exp);
    }

/// 
/// The {@link MappingType} of function return type: {@link PostgrePointType}
/// *
/// @param funcRef the reference of method,Note: it's the reference of method,not lambda. Valid method:
/// 
/// - {@link SQLs#param(TypeInfer, Object)}
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#namedParam(TypeInfer, String)} ,used only in INSERT( or batch update/delete ) syntax
/// - {@link SQLs#namedLiteral(TypeInfer, String)} ,used only in INSERT( or batch update/delete in multi-statement) syntax
/// - developer custom method
/// .
/// The first argument of funcRef always is {@link DoubleType#INSTANCE}.
/// @param x       it will be passed to funcRef as the second argument of funcRef
/// @param y       it will be passed to funcRef as the second argument of funcRef
/// @see #point(Expression, Expression)
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-CONV-TABLE">point ( double precision, double precision ) → point
/// Constructs point from its coordinates.
/// point(23.4, -44.5) → (23.4,-44.5)
/// </a>
    public static <T> Expression point(BiFunction<MappingType, T, Expression> funcRef, T x, T y) {
        return point(funcRef.apply(DoubleType.INSTANCE, x),
                funcRef.apply(DoubleType.INSTANCE, y)
        );
    }

/// 
/// The {@link MappingType} of function return type: {@link PostgrePointType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-CONV-TABLE">point ( double precision, double precision ) → point
/// Constructs point from its coordinates.
/// point(23.4, -44.5) → (23.4,-44.5)
/// </a>
    public static SimpleExpression point(Expression x, Expression y) {
        return LiteralFunctions.twoArgFunc("POINT", x, y);
    }

/// 
/// The {@link MappingType} of function return type: {@link PostgrePolygonType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-CONV-TABLE">polygon ( box ) → polygon
/// Converts box to a 4-point polygon.
/// polygon(box '(1,1),(0,0)') → ((0,0),(0,1),(1,1),(1,0))
/// polygon ( circle ) → polygon
/// Converts circle to a 12-point polygon.
/// polygon(circle '<(0,0),2>') → ((-2,0),(-1.7320508075688774,0.9999999999999999),(-1.0000000000000002,1.7320508075688772),
/// (-1.2246063538223773e-16,2),(0.9999999999999996,1.7320508075688774),(1.732050807568877,1.0000000000000007), 
/// (2,2.4492127076447545e-16),(1.7320508075688776,-0.9999999999999994),(1.0000000000000009,-1.7320508075688767),
/// (3.673819061467132e-16,-2),(-0.9999999999999987,-1.732050807568878),(-1.7320508075688767,-1.0000000000000009))
/// polygon ( path ) → polygon
/// Converts closed path to a polygon with the same list of points.
/// polygon(path '((0,0),(1,1),(2,0))') → ((0,0),(1,1),(2,0))
/// </a>
    public static SimpleExpression polygon(Expression exp) {
        return LiteralFunctions.oneArgFunc("POLYGON", exp);
    }

/// 
/// The {@link MappingType} of function return type: {@link PostgrePolygonType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-CONV-TABLE">polygon ( integer, circle ) → polygon
/// Converts circle to an n-point polygon.
/// polygon(4, circle '<(3,0),1>') → ((2,0),(3,1),(4,1.2246063538223773e-16),(3,-1))
/// </a>
    public static SimpleExpression polygon(Expression exp1, Expression exp2) {
        return LiteralFunctions.twoArgFunc("POLYGON", exp1, exp2);
    }


/// 
/// The {@link MappingType} of function return type: {@link  BooleanType}
/// *
/// @param funcRef the reference of method,Note: it's the reference of method,not lambda. Valid method:
/// 
/// - {@link SQLs#param(TypeInfer, Object)}
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#namedParam(TypeInfer, String)} ,used only in INSERT( or batch update/delete ) syntax
/// - {@link SQLs#namedLiteral(TypeInfer, String)} ,used only in INSERT( or batch update/delete in multi-statement) syntax
/// - developer custom method
/// .
/// The first argument of funcRef always is {@link PostgrePathType#INSTANCE}.
/// @param path    non-null and non-empty,it will be passed to funcRef as the second argument of funcRef
/// @see #isClosed(Expression)
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-FUNC-TABLE">isclosed ( path ) → boolean
/// Is path closed?
/// isclosed(path '((0,0),(1,1),(2,0))') → t
/// </a>
    public static SimplePredicate isClosed(BiFunction<MappingType, String, Expression> funcRef, String path) {
        return isClosed(funcRef.apply(PostgrePathType.INSTANCE, path));
    }

/// 
/// The {@link MappingType} of function return type: {@link  BooleanType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-FUNC-TABLE">isclosed ( path ) → boolean
/// Is path closed?
/// isclosed(path '((0,0),(1,1),(2,0))') → t
/// </a>
    public static SimplePredicate isClosed(Expression path) {
        return FunctionUtils.oneArgPredicateFunc("ISCLOSED", path);
    }

/// 
/// The {@link MappingType} of function return type: {@link  BooleanType}
/// *
/// @param funcRef the reference of method,Note: it's the reference of method,not lambda. Valid method:
/// 
/// - {@link SQLs#param(TypeInfer, Object)}
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#namedParam(TypeInfer, String)} ,used only in INSERT( or batch update/delete ) syntax
/// - {@link SQLs#namedLiteral(TypeInfer, String)} ,used only in INSERT( or batch update/delete in multi-statement) syntax
/// - developer custom method
/// .
/// The first argument of funcRef always is {@link PostgrePathType#INSTANCE}.
/// @param path    non-null and non-empty,it will be passed to funcRef as the second argument of funcRef
/// @see #isOpen(Expression)
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-FUNC-TABLE">isopen ( path ) → boolean
/// Is path open?
/// isopen(path '[(0,0),(1,1),(2,0)]') → t
/// </a>
    public static SimplePredicate isOpen(BiFunction<MappingType, String, Expression> funcRef, String path) {
        return isOpen(funcRef.apply(PostgrePathType.INSTANCE, path));
    }

/// 
/// The {@link MappingType} of function return type: {@link  BooleanType}
/// *
/// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-FUNC-TABLE">isopen ( path ) → boolean
/// Is path open?
/// isopen(path '[(0,0),(1,1),(2,0)]') → t
/// </a>
    public static SimplePredicate isOpen(Expression path) {
        return FunctionUtils.oneArgPredicateFunc("ISOPEN", path);
    }


}
