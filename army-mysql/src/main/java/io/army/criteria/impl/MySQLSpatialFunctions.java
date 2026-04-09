/*
 * Copyright 2023-2043 the original author or authors.
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


import io.army.criteria.*;
import io.army.mapping.*;

import java.util.List;

/**
 * <p>
 * Package class,this class provider MySQL spatial function.
 *
 * @since 0.6.0
 */
@SuppressWarnings("unused")
abstract class MySQLSpatialFunctions extends MySQLWindowFunctions {

    MySQLSpatialFunctions() {
    }



    /*-------------------below MySQL-Specific Functions-------------------*/


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     * @param geometryList non-null,empty list or list
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-mysql-specific-functions.html#function_geometrycollection">GeometryCollection(g [, g] ...)</a>
     */
    public static SimpleExpression geometryCollection(final List<Expression> geometryList) {
        return LiteralFunctions.multiArgFunc("GeometryCollection", geometryList);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param first non-null
     * @param rest  non-null,empty or non-empty
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-mysql-specific-functions.html#function_geometrycollection">GeometryCollection(g [, g] ...)</a>
     */
    public static SimpleExpression geometryCollection(final Expression first, Expression... rest) {
        return FunctionUtils.oneAndRestFunc("GeometryCollection", first, rest);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param ptList non-null,empty list or list
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-mysql-specific-functions.html#function_linestring">LineString(pt [, pt] ...)</a>
     */
    public static SimpleExpression lineString(final List<Expression> ptList) {
        return LiteralFunctions.multiArgFunc("LineString", ptList);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param first non-null
     * @param rest  non-null,empty or non-empty
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-mysql-specific-functions.html#function_linestring">LineString(pt [, pt] ...)</a>
     */
    public static SimpleExpression lineString(final Expression first, Expression... rest) {
        return FunctionUtils.oneAndRestFunc("LineString", first, rest);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param ptList non-null,empty list or list
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-mysql-specific-functions.html#function_multilinestring">MultiLineString(ls [, ls] ...)</a>
     */
    public static SimpleExpression multiLineString(final List<Expression> ptList) {
        return LiteralFunctions.multiArgFunc("MultiLineString", ptList);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param first non-null
     * @param rest  non-null,empty or non-empty
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-mysql-specific-functions.html#function_multilinestring">MultiLineString(ls [, ls] ...)</a>
     */
    public static SimpleExpression multiLineString(final Expression first, Expression... rest) {
        return FunctionUtils.oneAndRestFunc("MultiLineString", first, rest);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param ptList non-null,empty list or list
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-mysql-specific-functions.html#function_multipoint">MultiPoint(pt [, pt2] ...)</a>
     */
    public static SimpleExpression multiPoint(final List<Expression> ptList) {
        return LiteralFunctions.multiArgFunc("MultiPoint", ptList);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param first non-null
     * @param rest  non-null,empty or non-empty
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-mysql-specific-functions.html#function_multipoint">MultiPoint(pt [, pt2] ...)</a>
     */
    public static SimpleExpression multiPoint(final Expression first, Expression... rest) {
        return FunctionUtils.oneAndRestFunc("MultiPoint", first, rest);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param ptList non-null,empty list or list
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-mysql-specific-functions.html#function_multipolygon">MultiPolygon(poly [, poly] ...)</a>
     */
    public static SimpleExpression multiPolygon(final List<Expression> ptList) {
        return LiteralFunctions.multiArgFunc("MultiPolygon", ptList);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param first non-null
     * @param rest  non-null,empty or non-empty
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-mysql-specific-functions.html#function_multipolygon">MultiPolygon(poly [, poly] ...)</a>
     */
    public static SimpleExpression multiPolygon(final Expression first, Expression... rest) {
        return FunctionUtils.oneAndRestFunc("MultiPolygon", first, rest);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param lsList non-null,empty list or list
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-mysql-specific-functions.html#function_polygon">Polygon(ls [, ls] ...)</a>
     */
    public static SimpleExpression polygon(final List<Expression> lsList) {
        return LiteralFunctions.multiArgFunc("Polygon", lsList);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param first non-null
     * @param rest  non-null,empty or non-empty
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-mysql-specific-functions.html#function_polygon">Polygon(ls [, ls] ...)</a>
     */
    public static SimpleExpression polygon(final Expression first, Expression... rest) {
        return FunctionUtils.oneAndRestFunc("Polygon", first, rest);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param x non-null
     * @param y non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-mysql-specific-functions.html#function_point">Point(x, y)</a>
     */
    public static SimpleExpression point(final Expression x, final Expression y) {
        return LiteralFunctions.twoArgFunc("Point", x, y);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link BooleanType}
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-mbr.html#function_mbrcontains">MBRContains(g1, g2)</a>
     */
    public static SimplePredicate mbrContains(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgPredicate("MBRContains", g1, g2);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link BooleanType}
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-mbr.html#function_mbrcoveredby">MBRCoveredBy(g1, g2)</a>
     */
    public static SimplePredicate mbrCoveredBy(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgPredicate("MBRCoveredBy", g1, g2);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link BooleanType}
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-mbr.html#function_mbrcovers">MBRCovers(g1, g2)</a>
     */
    public static SimplePredicate mbrCovers(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgPredicate("MBRCovers", g1, g2);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link BooleanType}
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-mbr.html#function_mbrdisjoint">MBRDisjoint(g1, g2)</a>
     */
    public static SimplePredicate mbrDisjoint(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgPredicate("MBRDisjoint", g1, g2);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link BooleanType}
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-mbr.html#function_mbrequals">MBREquals(g1, g2)</a>
     */
    public static SimplePredicate mbrEquals(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgPredicate("MBREquals", g1, g2);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link BooleanType}
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-mbr.html#function_mbrintersects">MBRIntersects(g1, g2)</a>
     */
    public static SimplePredicate mbrIntersects(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgPredicate("MBRIntersects", g1, g2);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link BooleanType}
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-mbr.html#function_mbroverlaps">MBROverlaps(g1, g2)</a>
     */
    public static SimplePredicate mbrOverlaps(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgPredicate("MBROverlaps", g1, g2);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link BooleanType}
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-mbr.html#function_mbrtouches">MBRTouches(g1, g2)</a>
     */
    public static SimplePredicate mbrTouches(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgPredicate("MBRTouches", g1, g2);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link BooleanType}
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-mbr.html#function_mbrwithin">MBRWithin(g1, g2)</a>
     */
    public static SimplePredicate mbrWithin(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgPredicate("MBRWithin", g1, g2);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link DoubleType}
     *
     *
     * @param polyOrmpoly non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-polygon-property-functions.html#function_st-area">ST_Area({poly|mpoly})</a>
     */
    public static SimpleExpression stArea(final Expression polyOrmpoly) {
        return LiteralFunctions.oneArgFunc("ST_Area", polyOrmpoly);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param polyOrmpoly non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-polygon-property-functions.html#function_st-centroid">ST_Centroid({poly|mpoly})</a>
     */
    public static SimpleExpression stCentroid(final Expression polyOrmpoly) {
        return LiteralFunctions.oneArgFunc("ST_Centroid", polyOrmpoly);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param poly non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-polygon-property-functions.html#function_st-exteriorring">ST_ExteriorRing(poly)</a>
     */
    public static SimpleExpression stExteriorRing(final Expression poly) {
        return LiteralFunctions.oneArgFunc("ST_ExteriorRing", poly);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param poly non-null
     * @param n    non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-polygon-property-functions.html#function_st-interiorringn">ST_InteriorRingN(poly, N)</a>
     */
    public static SimpleExpression stInteriorRingN(final Expression poly, final Expression n) {
        return LiteralFunctions.twoArgFunc("ST_InteriorRingN", poly, n);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link IntegerType}
     *
     *
     * @param poly non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-polygon-property-functions.html#function_st-numinteriorrings">ST_NumInteriorRing(poly)</a>
     */
    public static SimpleExpression stNumInteriorRing(final Expression poly) {
        return LiteralFunctions.oneArgFunc("ST_NumInteriorRing", poly);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link IntegerType}
     *
     *
     * @param poly non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-polygon-property-functions.html#function_st-numinteriorrings">ST_NumInteriorRings(poly)</a>
     */
    public static SimpleExpression stNumInteriorRings(final Expression poly) {
        return LiteralFunctions.oneArgFunc("ST_NumInteriorRings", poly);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param g non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-format-conversion-functions.html#function_st-asbinary">ST_AsBinary(g [, options])</a>
     */
    public static SimpleExpression stAsBinary(final Expression g) {
        return LiteralFunctions.oneArgFunc("ST_AsBinary", g);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param g       non-null
     * @param options non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-format-conversion-functions.html#function_st-asbinary">ST_AsBinary(g [, options])</a>
     */
    public static SimpleExpression stAsBinary(final Expression g, final Expression options) {
        return LiteralFunctions.twoArgFunc("ST_AsBinary", g, options);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param g non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-format-conversion-functions.html#function_st-asbinary">ST_AsWKB(g [, options])</a>
     */
    public static SimpleExpression stAsWKB(final Expression g) {
        return LiteralFunctions.oneArgFunc("ST_AsWKB", g);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param g       non-null
     * @param options non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-format-conversion-functions.html#function_st-asbinary">ST_AsWKB(g [, options])</a>
     */
    public static SimpleExpression stAsWKB(final Expression g, final Expression options) {
        return LiteralFunctions.twoArgFunc("ST_AsWKB", g, options);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link StringType}
     *
     *
     * @param g non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-format-conversion-functions.html#function_st-astext">ST_AsText(g [, options])</a>
     */
    public static SimpleExpression stAsText(final Expression g) {
        return LiteralFunctions.oneArgFunc("ST_AsText", g);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link StringType}
     *
     *
     * @param g       non-null
     * @param options non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-format-conversion-functions.html#function_st-astext">ST_AsText(g [, options])</a>
     */
    public static SimpleExpression stAsText(final Expression g, final Expression options) {
        return LiteralFunctions.twoArgFunc("ST_AsText", g, options);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link StringType}
     *
     *
     * @param g non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-format-conversion-functions.html#function_st-astext">ST_AsWKT(g [, options])</a>
     */
    public static SimpleExpression stAsWKT(final Expression g) {
        return LiteralFunctions.oneArgFunc("ST_AsWKT", g);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link StringType}
     *
     *
     * @param g       non-null
     * @param options non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-format-conversion-functions.html#function_st-astext">ST_AsWKT(g [, options])</a>
     */
    public static SimpleExpression stAsWKT(final Expression g, final Expression options) {
        return LiteralFunctions.twoArgFunc("ST_AsWKT", g, options);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param g non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-format-conversion-functions.html#function_st-swapxy">ST_SwapXY(g)</a>
     */
    public static SimpleExpression stSwapXY(final Expression g) {
        return LiteralFunctions.oneArgFunc("ST_SwapXY", g);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link StringType}
     *
     *
     * @param g non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-geojson-functions.html#function_st-asgeojson">ST_AsGeoJSON(g [, max_dec_digits [, options]])</a>
     */
    public static SimpleExpression stAsGeoJson(Expression g) {
        return LiteralFunctions.oneArgFunc("ST_AsGeoJSON", g);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link StringType}
     *
     *
     * @param g            non-null
     * @param maxDecDigits non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-geojson-functions.html#function_st-asgeojson">ST_AsGeoJSON(g [, max_dec_digits [, options]])</a>
     */
    public static SimpleExpression stAsGeoJson(Expression g, Expression maxDecDigits) {
        return LiteralFunctions.twoArgFunc("ST_AsGeoJSON", g, maxDecDigits);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link StringType}
     *
     *
     * @param g            non-null
     * @param maxDecDigits non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-geojson-functions.html#function_st-asgeojson">ST_AsGeoJSON(g [, max_dec_digits [, options]])</a>
     */
    public static SimpleExpression stAsGeoJson(Expression g, Expression maxDecDigits, Expression options) {
        return LiteralFunctions.threeArgFunc("ST_AsGeoJSON", g, maxDecDigits, options);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link StringType}
     *
     *
     * @param str non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-geojson-functions.html#function_st-geomfromgeojson">ST_GeomFromGeoJSON(str [, options [, srid]])</a>
     */
    public static SimpleExpression stGeomFromGeoJson(Expression str) {
        return LiteralFunctions.oneArgFunc("ST_GeomFromGeoJSON", str);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link StringType}
     *
     *
     * @param str     non-null
     * @param options non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-geojson-functions.html#function_st-geomfromgeojson">ST_GeomFromGeoJSON(str [, options [, srid]])</a>
     */
    public static SimpleExpression stGeomFromGeoJson(Expression str, Expression options) {
        return LiteralFunctions.twoArgFunc("ST_GeomFromGeoJSON", str, options);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link StringType}
     *
     *
     * @param str     non-null
     * @param options non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-geojson-functions.html#function_st-geomfromgeojson">ST_GeomFromGeoJSON(str [, options [, srid]])</a>
     */
    public static SimpleExpression stGeomFromGeoJson(Expression str, Expression options, Expression srid) {
        return LiteralFunctions.threeArgFunc("ST_GeomFromGeoJSON", str, options, srid);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param g non-null
     * @param d non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-operator-functions.html#function_st-buffer">ST_Buffer(g, d [, strategy1 [, strategy2 [, strategy3]]])</a>
     */
    public static SimpleExpression stBuffer(Expression g, Expression d) {
        return LiteralFunctions.twoArgFunc("ST_Buffer", g, d);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param g non-null
     * @param d non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-operator-functions.html#function_st-buffer">ST_Buffer(g, d [, strategy1 [, strategy2 [, strategy3]]])</a>
     */
    public static SimpleExpression stBuffer(Expression g, Expression d, Expression strategy1) {
        return LiteralFunctions.threeArgFunc("ST_Buffer", g, d, strategy1);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param g non-null
     * @param d non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-operator-functions.html#function_st-buffer">ST_Buffer(g, d [, strategy1 [, strategy2 [, strategy3]]])</a>
     */
    public static SimpleExpression stBuffer(Expression g, Expression d, Expression strategy1, Expression strategy2) {
        return LiteralFunctions.multiArgFunc("ST_Buffer", List.of(g, d, strategy1, strategy2));
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param g non-null
     * @param d non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-operator-functions.html#function_st-buffer">ST_Buffer(g, d [, strategy1 [, strategy2 [, strategy3]]])</a>
     */
    public static SimpleExpression stBuffer(Expression g, Expression d
            , Expression strategy1, Expression strategy2
            , Expression strategy3) {
        return LiteralFunctions.multiArgFunc("ST_Buffer", List.of(g, d, strategy1, strategy2, strategy3));
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType}
     *
     *
     * @param expList non-null ,the list that size in [1,2].
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-operator-functions.html#function_st-buffer-strategy">ST_Buffer_Strategy(strategy [, points_per_circle])</a>
     */
    public static SimpleExpression stBufferStrategy(final List<Expression> expList) {
        final String name = "ST_Buffer_Strategy";
        return switch (expList.size()) {
            case 1 -> LiteralFunctions.oneArgFunc(name, expList.getFirst());
            case 2 -> LiteralFunctions.twoArgFunc(name, expList.getFirst(), expList.get(1));
            default -> throw CriteriaUtils.funcArgError(name, expList);
        };
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType}
     *
     *
     * @param strategy non-null ,the list that size in [1,2].
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-operator-functions.html#function_st-buffer-strategy">ST_Buffer_Strategy(strategy [, points_per_circle])</a>
     */
    public static SimpleExpression stBufferStrategy(Expression strategy) {
        return LiteralFunctions.oneArgFunc("ST_Buffer_Strategy", strategy);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType}
     *
     *
     * @param strategy non-null ,the list that size in [1,2].
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-operator-functions.html#function_st-buffer-strategy">ST_Buffer_Strategy(strategy [, points_per_circle])</a>
     */
    public static SimpleExpression stBufferStrategy(Expression strategy, Expression pointsPerCircle) {
        return LiteralFunctions.twoArgFunc("ST_Buffer_Strategy", strategy, pointsPerCircle);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param g non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-operator-functions.html#function_st-convexhull">ST_ConvexHull(g)</a>
     */
    public static SimpleExpression stConvexHull(final Expression g) {
        return LiteralFunctions.oneArgFunc("ST_ConvexHull", g);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-operator-functions.html#function_st-difference">ST_Difference(g1, g2)</a>
     */
    public static SimpleExpression stDifference(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgFunc("ST_Difference", g1, g2);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-operator-functions.html#function_st-intersection">ST_Intersection(g1, g2)</a>
     */
    public static SimpleExpression stIntersection(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgFunc("ST_Intersection", g1, g2);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param ls                 non-null
     * @param fractionalDistance non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-operator-functions.html#function_st-lineinterpolatepoint">ST_LineInterpolatePoint(ls, fractional_distance)</a>
     */
    public static SimpleExpression stLineInterpolatePoint(final Expression ls, final Expression fractionalDistance) {
        return LiteralFunctions.twoArgFunc("ST_LineInterpolatePoint", ls, fractionalDistance);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param ls                 non-null
     * @param fractionalDistance non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-operator-functions.html#function_st-lineinterpolatepoints">ST_LineInterpolatePoints(ls, fractional_distance)</a>
     */
    public static SimpleExpression stLineInterpolatePoints(final Expression ls, final Expression fractionalDistance) {
        return LiteralFunctions.twoArgFunc("ST_LineInterpolatePoints", ls, fractionalDistance);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param ls       non-null
     * @param distance non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-operator-functions.html#function_st-pointatdistance">ST_PointAtDistance(ls, distance)</a>
     */
    public static SimpleExpression stPointAtDistance(final Expression ls, final Expression distance) {
        return LiteralFunctions.twoArgFunc("ST_PointAtDistance", ls, distance);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-operator-functions.html#function_st-symdifference">ST_SymDifference(g1, g2)</a>
     */
    public static SimpleExpression stSymDifference(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgFunc("ST_SymDifference", g1, g2);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param g          non-null
     * @param targetSrid non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-operator-functions.html#function_st-transform">ST_Transform(g, target_srid)</a>
     */
    public static SimpleExpression stTransform(final Expression g, final Expression targetSrid) {
        return LiteralFunctions.twoArgFunc("ST_Transform", g, targetSrid);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-operator-functions.html#function_st-union">ST_Union(g1, g2)</a>
     */
    public static SimpleExpression stUnion(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgFunc("ST_Union", g1, g2);
    }

    /*-------------------below Spatial Convenience Functions-------------------*/

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link DoubleType}
     *
     *
     * @param expList non-null,size in [2,3].
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-convenience-functions.html#function_st-distance-sphere">ST_Distance_Sphere(g1, g2 [, radius])</a>
     */
    public static SimpleExpression stDistanceSphere(final List<Expression> expList) {
        final String name = "ST_Distance_Sphere";
        return switch (expList.size()) {
            case 2 -> LiteralFunctions.twoArgFunc(name, expList.getFirst(), expList.get(1));
            case 3 -> LiteralFunctions.threeArgFunc(name, expList.getFirst(), expList.get(1), expList.get(2));
            default -> throw CriteriaUtils.funcArgError(name, expList);
        };
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link DoubleType}
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-convenience-functions.html#function_st-distance-sphere">ST_Distance_Sphere(g1, g2 [, radius])</a>
     */
    public static SimpleExpression stDistanceSphere(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgFunc("ST_Distance_Sphere", g1, g2);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link DoubleType}
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-convenience-functions.html#function_st-distance-sphere">ST_Distance_Sphere(g1, g2 [, radius])</a>
     */
    public static SimpleExpression stDistanceSphere(final Expression g1, final Expression g2, Expression radius) {
        return LiteralFunctions.threeArgFunc("ST_Distance_Sphere", g1, g2, radius);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link BooleanType}
     *
     *
     * @param g non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-convenience-functions.html#function_st-isvalid">ST_IsValid(g)</a>
     */
    public static SimplePredicate stIsValid(final Expression g) {
        return LiteralFunctions.oneArgPredicate("ST_IsValid", g);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param pt1 non-null
     * @param pt2 non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-convenience-functions.html#function_st-makeenvelope">ST_MakeEnvelope(pt1, pt2)</a>
     */
    public static SimpleExpression stMakeEnvelope(final Expression pt1, final Expression pt2) {
        return LiteralFunctions.twoArgFunc("ST_MakeEnvelope", pt1, pt2);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param g           non-null
     * @param maxDistance non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-convenience-functions.html#function_st-simplify">ST_Simplify(g, max_distance)</a>
     */
    public static SimpleExpression stSimplify(final Expression g, final Expression maxDistance) {
        return LiteralFunctions.twoArgFunc("ST_Simplify", g, maxDistance);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param g non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-convenience-functions.html#function_st-validate">ST_Validate(g)</a>
     */
    public static SimpleExpression stValidate(final Expression g) {
        return LiteralFunctions.oneArgFunc("ST_Validate", g);
    }


    /*-------------------below LineString and MultiLineString Property Functions-------------------*/

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param ls non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-linestring-property-functions.html#function_st-endpoint">ST_EndPoint(ls)</a>
     */
    public static SimpleExpression stEndPoint(final Expression ls) {
        return LiteralFunctions.oneArgFunc("ST_EndPoint", ls);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link BooleanType}
     *
     *
     * @param ls non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-linestring-property-functions.html#function_st-isclosed">ST_IsClosed(ls)</a>
     */
    public static SimplePredicate stIsClosed(final Expression ls) {
        return LiteralFunctions.oneArgPredicate("ST_IsClosed", ls);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link DoubleType}
     *
     *
     * @param ls non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-linestring-property-functions.html#function_st-length">ST_Length(ls [, unit])</a>
     */
    public static SimpleExpression stLength(Expression ls) {
        return LiteralFunctions.oneArgFunc("ST_Length", ls);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link DoubleType}
     *
     *
     * @param ls   non-null
     * @param unit non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-linestring-property-functions.html#function_st-length">ST_Length(ls [, unit])</a>
     */
    public static SimpleExpression stLength(final Expression ls, Expression unit) {
        return LiteralFunctions.twoArgFunc("ST_Length", ls, unit);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link IntegerType}
     *
     *
     * @param ls non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-linestring-property-functions.html#function_st-numpoints">ST_NumPoints(ls)</a>
     */
    public static SimpleExpression stNumPoints(final Expression ls) {
        return LiteralFunctions.oneArgFunc("ST_NumPoints", ls);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param ls non-null
     * @param n  non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-linestring-property-functions.html#function_st-pointn">ST_PointN(ls, N)</a>
     */
    public static SimpleExpression stPointN(final Expression ls, final Expression n) {
        return LiteralFunctions.twoArgFunc("ST_PointN", ls, n);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param ls non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-linestring-property-functions.html#function_st-startpoint">ST_StartPoint(ls)</a>
     */
    public static SimpleExpression stStartPoint(final Expression ls) {
        return LiteralFunctions.oneArgFunc("ST_StartPoint", ls);
    }


    /*-------------------below Spatial Relation Functions That Use Object Shapes-------------------*/

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link BooleanType}
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-object-shapes.html#function_st-contains">ST_Contains(g1, g2)</a>
     */
    public static SimplePredicate stContains(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgPredicate("ST_Contains", g1, g2);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link BooleanType}
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-object-shapes.html#function_st-crosses">ST_Crosses(g1, g2)</a>
     */
    public static SimplePredicate stCrosses(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgPredicate("ST_Crosses", g1, g2);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link BooleanType}
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-object-shapes.html#function_st-disjoint">ST_Disjoint(g1, g2)</a>
     */
    public static SimplePredicate stDisjoint(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgPredicate("ST_Disjoint", g1, g2);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link DoubleType}
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see #stDistance(Expression, Expression, Expression)
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-object-shapes.html#function_st-distance">ST_Distance(g1, g2 [, unit])</a>
     */
    public static SimpleExpression stDistance(final Expression g1, Expression g2) {
        return LiteralFunctions.twoArgFunc("ST_Distance", g1, g2);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link DoubleType}
     *
     *
     * @param g1   non-null
     * @param g2   non-null
     * @param unit non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see #stDistance(Expression, Expression)
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-object-shapes.html#function_st-distance">ST_Distance(g1, g2 [, unit])</a>
     */
    public static SimpleExpression stDistance(final Expression g1, Expression g2, Expression unit) {
        return LiteralFunctions.threeArgFunc("ST_Distance", g1, g2, unit);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link BooleanType}
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-object-shapes.html#function_st-equals">ST_Equals(g1, g2)</a>
     */
    public static SimplePredicate stEquals(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgPredicate("ST_Equals", g1, g2);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link DoubleType}
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-object-shapes.html#function_st-frechetdistance">ST_FrechetDistance(g1, g2 [, unit])</a>
     */
    public static SimpleExpression stFrechetDistance(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgFunc("ST_FrechetDistance", g1, g2);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link DoubleType}
     *
     *
     * @param g1   non-null
     * @param g2   non-null
     * @param unit non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-object-shapes.html#function_st-frechetdistance">ST_FrechetDistance(g1, g2 [, unit])</a>
     */
    public static SimpleExpression stFrechetDistance(final Expression g1, final Expression g2, final Expression unit) {
        return LiteralFunctions.threeArgFunc("ST_FrechetDistance", g1, g2, unit);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link DoubleType}
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-object-shapes.html#function_st-hausdorffdistance">ST_HausdorffDistance(g1, g2 [, unit])</a>
     */
    public static SimpleExpression stHausdorffDistance(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgFunc("ST_HausdorffDistance", g1, g2);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link DoubleType}
     *
     *
     * @param g1   non-null
     * @param g2   non-null
     * @param unit non-null
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-object-shapes.html#function_st-hausdorffdistance">ST_HausdorffDistance(g1, g2 [, unit])</a>
     */
    public static SimpleExpression stHausdorffDistance(final Expression g1, final Expression g2, final Expression unit) {
        return LiteralFunctions.threeArgFunc("ST_HausdorffDistance", g1, g2, unit);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link BooleanType}
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-object-shapes.html#function_st-intersects">ST_Intersects(g1, g2)</a>
     */
    public static SimplePredicate stIntersects(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgPredicate("ST_Intersects", g1, g2);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link BooleanType}
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when g1 or g2 is multi parameter or literal
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-object-shapes.html#function_st-overlaps">ST_Overlaps(g1, g2)</a>
     */
    public static SimplePredicate stOverlaps(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgPredicate("ST_Overlaps", g1, g2);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link BooleanType}
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when g1 or g2 is multi parameter or literal
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-object-shapes.html#function_st-touches">ST_Touches(g1, g2)</a>
     */
    public static SimplePredicate stTouches(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgPredicate("ST_Touches", g1, g2);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link BooleanType}
     *
     *
     * @param g1 non-null
     * @param g2 non-null
     * @throws CriteriaException throw when g1 or g2 is multi parameter or literal
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-object-shapes.html#function_st-within">ST_Within(g1, g2)</a>
     */
    public static SimplePredicate stWithin(final Expression g1, final Expression g2) {
        return LiteralFunctions.twoArgPredicate("ST_Within", g1, g2);
    }


    /*-------------------below Spatial Geohash Functions-------------------*/

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link StringType}
     *
     *
     * @param point     non-null
     * @param maxLength non-null
     * @throws CriteriaException throw when any argument is multi parameter or literal
     * @see #stGeoHash(Expression, Expression, Expression)
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-geohash-functions.html#function_st-geohash">ST_GeoHash(longitude, latitude, max_length), ST_GeoHash(point, max_length)</a>
     */
    public static SimpleExpression stGeoHash(final Expression point, final Expression maxLength) {
        return LiteralFunctions.twoArgFunc("ST_GeoHash", point, maxLength);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link StringType}
     *
     *
     * @param longitude non-null
     * @param latitude  non-null
     * @param maxLength non-null
     * @throws CriteriaException throw when any argument is multi parameter or literal
     * @see #stGeoHash(Expression, Expression)
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-geohash-functions.html#function_st-geohash">ST_GeoHash(longitude, latitude, max_length), ST_GeoHash(point, max_length)</a>
     */
    public static SimpleExpression stGeoHash(final Expression longitude, final Expression latitude, final Expression maxLength) {
        return LiteralFunctions.threeArgFunc("ST_GeoHash", longitude, latitude, maxLength);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link DoubleType}
     *
     *
     * @param geohashStr non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-geohash-functions.html#function_st-latfromgeohash">ST_LatFromGeoHash(geohash_str)</a>
     */
    public static SimpleExpression stLatFromGeoHash(final Expression geohashStr) {
        return LiteralFunctions.oneArgFunc("ST_LatFromGeoHash", geohashStr);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link DoubleType}
     *
     *
     * @param geohashStr non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-geohash-functions.html#function_st-longfromgeohash">ST_LongFromGeoHash(geohash_str)</a>
     */
    public static SimpleExpression stLongFromGeoHash(final Expression geohashStr) {
        return LiteralFunctions.oneArgFunc("ST_LongFromGeoHash", geohashStr);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType}
     *
     *
     * @param geohashStr non-null
     * @param srid       non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/spatial-geohash-functions.html#function_st-pointfromgeohash">ST_PointFromGeoHash(geohash_str, srid)</a>
     */
    public static SimpleExpression stPointFromGeoHash(final Expression geohashStr, final Expression srid) {
        return LiteralFunctions.twoArgFunc("ST_PointFromGeoHash", geohashStr, srid);
    }

    /*-------------------below Functions That Create Geometry Values from WKT Values-------------------*/


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-geomcollfromtext">ST_GeomCollFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stGeomCollFromText(final Expression wkt) {
        return LiteralFunctions.oneArgFunc("ST_GeomCollFromText", wkt);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-geomcollfromtext">ST_GeomCollFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stGeomCollFromText(final Expression wkt, Expression srid) {
        return LiteralFunctions.twoArgFunc("ST_GeomCollFromText", wkt, srid);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-geomcollfromtext">ST_GeomCollFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stGeomCollFromText(final Expression wkt, Expression srid, Expression options) {
        return LiteralFunctions.threeArgFunc("ST_GeomCollFromText", wkt, srid, options);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-geomfromtext">ST_GeomFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stGeomFromText(final Expression wkt) {
        return LiteralFunctions.oneArgFunc("ST_GeomFromText", wkt);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-geomfromtext">ST_GeomFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stGeomFromText(final Expression wkt, Expression srid) {
        return LiteralFunctions.twoArgFunc("ST_GeomFromText", wkt, srid);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-geomfromtext">ST_GeomFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stGeomFromText(final Expression wkt, Expression srid, Expression options) {
        return LiteralFunctions.threeArgFunc("ST_GeomFromText", wkt, srid, options);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-linefromtext">ST_LineStringFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stLineStringFromText(final Expression wkt) {
        return LiteralFunctions.oneArgFunc("ST_LineStringFromText", wkt);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-linefromtext">ST_LineStringFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stLineStringFromText(final Expression wkt, Expression srid) {
        return LiteralFunctions.twoArgFunc("ST_LineStringFromText", wkt, srid);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-linefromtext">ST_LineStringFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stLineStringFromText(final Expression wkt, Expression srid, Expression options) {
        return LiteralFunctions.threeArgFunc("ST_LineStringFromText", wkt, srid, options);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-mlinefromtext">ST_MultiLineStringFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stMultiLineStringFromText(final Expression wkt) {
        return LiteralFunctions.oneArgFunc("ST_MultiLineStringFromText", wkt);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-mlinefromtext">ST_MultiLineStringFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stMultiLineStringFromText(final Expression wkt, Expression srid) {
        return LiteralFunctions.twoArgFunc("ST_MultiLineStringFromText", wkt, srid);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-mlinefromtext">ST_MultiLineStringFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stMultiLineStringFromText(final Expression wkt, Expression srid, Expression options) {
        return LiteralFunctions.threeArgFunc("ST_MultiLineStringFromText", wkt, srid, options);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-mpointfromtext">ST_MultiPointFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stMultiPointFromText(final Expression wkt) {
        return LiteralFunctions.oneArgFunc("ST_MultiPointFromText", wkt);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-mpointfromtext">ST_MultiPointFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stMultiPointFromText(final Expression wkt, Expression srid) {
        return LiteralFunctions.twoArgFunc("ST_MultiPointFromText", wkt, srid);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-mpointfromtext">ST_MultiPointFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stMultiPointFromText(final Expression wkt, Expression srid, Expression options) {
        return LiteralFunctions.threeArgFunc("ST_MultiPointFromText", wkt, srid, options);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-mpolyfromtext">ST_MultiPolygonFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stMultiPolygonFromText(final Expression wkt) {
        return LiteralFunctions.oneArgFunc("ST_MultiPolygonFromText", wkt);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-mpolyfromtext">ST_MultiPolygonFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stMultiPolygonFromText(final Expression wkt, Expression srid) {
        return LiteralFunctions.twoArgFunc("ST_MultiPolygonFromText", wkt, srid);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-mpolyfromtext">ST_MultiPolygonFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stMultiPolygonFromText(final Expression wkt, Expression srid, Expression options) {
        return LiteralFunctions.threeArgFunc("ST_MultiPolygonFromText", wkt, srid, options);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-pointfromtext">ST_PointFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stPointFromText(final Expression wkt) {
        return LiteralFunctions.oneArgFunc("ST_PointFromText", wkt);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-pointfromtext">ST_PointFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stPointFromText(final Expression wkt, Expression srid) {
        return LiteralFunctions.twoArgFunc("ST_PointFromText", wkt, srid);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-pointfromtext">ST_PointFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stPointFromText(final Expression wkt, Expression srid, Expression options) {
        return LiteralFunctions.threeArgFunc("ST_PointFromText", wkt, srid, options);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-polyfromtext">ST_PolygonFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stPolygonFromText(final Expression wkt) {
        return LiteralFunctions.oneArgFunc("ST_PolygonFromText", wkt);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-polyfromtext">ST_PolygonFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stPolygonFromText(final Expression wkt, Expression srid) {
        return LiteralFunctions.twoArgFunc("ST_PolygonFromText", wkt, srid);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkt-functions.html#function_st-polyfromtext">ST_PolygonFromText(wkt [, srid [, options]])</a>
     */
    public static SimpleExpression stPolygonFromText(final Expression wkt, Expression srid, Expression options) {
        return LiteralFunctions.threeArgFunc("ST_PolygonFromText", wkt, srid, options);
    }

    /*-------------------below Functions That Create Geometry Values from WKB Values-------------------*/


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkb-functions.html#function_st-geomcollfromwkb">ST_GeomCollFromWKB(wkb [, srid [, options]])</a>
     */
    public static SimpleExpression stGeomCollFromWKB(final Expression wkt) {
        return LiteralFunctions.oneArgFunc("ST_GeomCollFromWKB", wkt);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkb-functions.html#function_st-geomcollfromwkb">ST_GeomCollFromWKB(wkb [, srid [, options]])</a>
     */
    public static SimpleExpression stGeomCollFromWKB(final Expression wkt, Expression srid) {
        return LiteralFunctions.twoArgFunc("ST_GeomCollFromWKB", wkt, srid);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkb-functions.html#function_st-geomcollfromwkb">ST_GeomCollFromWKB(wkb [, srid [, options]])</a>
     */
    public static SimpleExpression stGeomCollFromWKB(final Expression wkt, Expression srid, Expression options) {
        return LiteralFunctions.threeArgFunc("ST_GeomCollFromWKB", wkt, srid, options);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkb-functions.html#function_st-geomfromwkb">ST_GeomFromWKB(wkb [, srid [, options]])</a>
     */
    public static SimpleExpression stGeomFromWKB(final Expression wkt) {
        return LiteralFunctions.oneArgFunc("ST_GeomFromWKB", wkt);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkb-functions.html#function_st-geomfromwkb">ST_GeomFromWKB(wkb [, srid [, options]])</a>
     */
    public static SimpleExpression stGeomFromWKB(final Expression wkt, Expression srid) {
        return LiteralFunctions.twoArgFunc("ST_GeomFromWKB", wkt, srid);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkb-functions.html#function_st-geomfromwkb">ST_GeomFromWKB(wkb [, srid [, options]])</a>
     */
    public static SimpleExpression stGeomFromWKB(final Expression wkt, Expression srid, Expression options) {
        return LiteralFunctions.threeArgFunc("ST_GeomFromWKB", wkt, srid, options);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkb-functions.html#function_st-linefromwkb">ST_LineStringFromWKB(wkb [, srid [, options]])</a>
     */
    public static SimpleExpression stLineStringFromWKB(final Expression wkt) {
        return LiteralFunctions.oneArgFunc("ST_LineStringFromWKB", wkt);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkb-functions.html#function_st-linefromwkb">ST_LineStringFromWKB(wkb [, srid [, options]])</a>
     */
    public static SimpleExpression stLineStringFromWKB(final Expression wkt, Expression srid) {
        return LiteralFunctions.twoArgFunc("ST_LineStringFromWKB", wkt, srid);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkb-functions.html#function_st-linefromwkb">ST_LineStringFromWKB(wkb [, srid [, options]])</a>
     */
    public static SimpleExpression stLineStringFromWKB(final Expression wkt, Expression srid, Expression options) {
        return LiteralFunctions.threeArgFunc("ST_LineStringFromWKB", wkt, srid, options);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkb-functions.html#function_st-mlinefromwkb">ST_MultiLineStringFromWKB(wkb [, srid [, options]])</a>
     */
    public static SimpleExpression stMultiLineStringFromWKB(final Expression wkt) {
        return LiteralFunctions.oneArgFunc("ST_MultiLineStringFromWKB", wkt);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkb-functions.html#function_st-mlinefromwkb">ST_MultiLineStringFromWKB(wkb [, srid [, options]])</a>
     */
    public static SimpleExpression stMultiLineStringFromWKB(final Expression wkt, Expression srid) {
        return LiteralFunctions.twoArgFunc("ST_MultiLineStringFromWKB", wkt, srid);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkb-functions.html#function_st-mlinefromwkb">ST_MultiLineStringFromWKB(wkb [, srid [, options]])</a>
     */
    public static SimpleExpression stMultiLineStringFromWKB(final Expression wkt, Expression srid, Expression options) {
        return LiteralFunctions.threeArgFunc("ST_MultiLineStringFromWKB", wkt, srid, options);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkb-functions.html#function_st-mpolyfromwkb">ST_MultiPolygonFromWKB(wkb [, srid [, options]])</a>
     */
    public static SimpleExpression stMultiPolygonFromWKB(final Expression wkt) {
        return LiteralFunctions.oneArgFunc("ST_MultiPolygonFromWKB", wkt);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkb-functions.html#function_st-mpolyfromwkb">ST_MultiPolygonFromWKB(wkb [, srid [, options]])</a>
     */
    public static SimpleExpression stMultiPolygonFromWKB(final Expression wkt, Expression srid) {
        return LiteralFunctions.twoArgFunc("ST_MultiPolygonFromWKB", wkt, srid);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkb-functions.html#function_st-mpolyfromwkb">ST_MultiPolygonFromWKB(wkb [, srid [, options]])</a>
     */
    public static SimpleExpression stMultiPolygonFromWKB(final Expression wkt, Expression srid, Expression options) {
        return LiteralFunctions.threeArgFunc("ST_MultiPolygonFromWKB", wkt, srid, options);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkb-functions.html#function_st-pointfromwkb">ST_PointFromWKB(wkb [, srid [, options]])</a>
     */
    public static SimpleExpression stPointFromWKB(final Expression wkt) {
        return LiteralFunctions.oneArgFunc("ST_PointFromWKB", wkt);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkb-functions.html#function_st-pointfromwkb">ST_PointFromWKB(wkb [, srid [, options]])</a>
     */
    public static SimpleExpression stPointFromWKB(final Expression wkt, Expression srid) {
        return LiteralFunctions.twoArgFunc("ST_PointFromWKB", wkt, srid);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkb-functions.html#function_st-pointfromwkb">ST_PointFromWKB(wkb [, srid [, options]])</a>
     */
    public static SimpleExpression stPointFromWKB(final Expression wkt, Expression srid, Expression options) {
        return LiteralFunctions.threeArgFunc("ST_PointFromWKB", wkt, srid, options);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkb-functions.html#function_st-polyfromwkb">ST_PolygonFromWKB(wkb [, srid [, options]])</a>
     */
    public static SimpleExpression stPolygonFromWKB(final Expression wkt) {
        return LiteralFunctions.oneArgFunc("ST_PolygonFromWKB", wkt);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkb-functions.html#function_st-polyfromwkb">ST_PolygonFromWKB(wkb [, srid [, options]])</a>
     */
    public static SimpleExpression stPolygonFromWKB(final Expression wkt, Expression srid) {
        return LiteralFunctions.twoArgFunc("ST_PolygonFromWKB", wkt, srid);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param wkt non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-wkb-functions.html#function_st-polyfromwkb">ST_PolygonFromWKB(wkb [, srid [, options]])</a>
     */
    public static SimpleExpression stPolygonFromWKB(final Expression wkt, Expression srid, Expression options) {
        return LiteralFunctions.threeArgFunc("ST_PolygonFromWKB", wkt, srid, options);
    }

    /*-------------------below GeometryCollection Property Functions-------------------*/


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param gc non-null
     * @param n  non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-geometrycollection-property-functions.html#function_st-geometryn">ST_GeometryN(gc, N)</a>
     */
    public static SimpleExpression stGeometryN(final Expression gc, final Expression n) {
        return LiteralFunctions.twoArgFunc("ST_GeometryN", gc, n);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link IntegerType}
     *
     *
     * @param gc non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-geometrycollection-property-functions.html#function_st-numgeometries">ST_NumGeometries(gc)</a>
     */
    public static SimpleExpression stNumGeometries(final Expression gc) {
        return LiteralFunctions.oneArgFunc("ST_NumGeometries", gc);
    }

    /*-------------------below General Geometry Property Functions-------------------*/

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link IntegerType}
     *
     *
     * @param g non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-general-property-functions.html#function_st-dimension">ST_Dimension(g)</a>
     */
    public static SimpleExpression stDimension(final Expression g) {
        return LiteralFunctions.oneArgFunc("ST_Dimension", g);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param g non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-general-property-functions.html#function_st-envelope">ST_Envelope(g)</a>
     */
    public static SimpleExpression stEnvelope(final Expression g) {
        return LiteralFunctions.oneArgFunc("ST_Envelope", g);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link StringType}
     *
     *
     * @param g non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-general-property-functions.html#function_st-geometrytype">ST_GeometryType(g)</a>
     */
    public static SimpleExpression stGeometryType(final Expression g) {
        return LiteralFunctions.oneArgFunc("ST_GeometryType", g);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link BooleanType}
     *
     *
     * @param g non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-general-property-functions.html#function_st-isempty">ST_IsEmpty(g)</a>
     */
    public static SimplePredicate stIsEmpty(final Expression g) {
        return LiteralFunctions.oneArgPredicate("ST_IsEmpty", g);
    }


    /**
     * <p>
     * The {@link MappingType} of function return type:{@link BooleanType}
     *
     *
     * @param g non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-general-property-functions.html#function_st-issimple">ST_IsSimple(g)</a>
     */
    public static SimplePredicate stIsSimple(final Expression g) {
        return LiteralFunctions.oneArgPredicate("ST_IsSimple", g);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link IntegerType}
     *
     *
     * @param p non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-general-property-functions.html#function_st-srid">ST_SRID(g [, srid])</a>
     */
    public static SimpleExpression stSRID(final Expression p) {
        return LiteralFunctions.oneArgFunc("ST_SRID", p);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param p non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-general-property-functions.html#function_st-srid">ST_SRID(g [, srid])</a>
     */
    public static SimpleExpression stSRID(final Expression p, final Expression srid) {
        return LiteralFunctions.twoArgFunc("ST_SRID", p, srid);
    }

    /*-------------------below Point Property Functions-------------------*/

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link DoubleType}
     *
     *
     * @param p non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-point-property-functions.html#function_st-latitude">ST_Latitude(p [, new_latitude_val])</a>
     */
    public static SimpleExpression stLatitude(final Expression p) {
        return LiteralFunctions.oneArgFunc("ST_Latitude", p);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param p              non-null
     * @param newLatitudeVal non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-point-property-functions.html#function_st-latitude">ST_Latitude(p [, new_latitude_val])</a>
     */
    public static SimpleExpression stLatitude(final Expression p, final Expression newLatitudeVal) {
        return LiteralFunctions.twoArgFunc("ST_Latitude", p, newLatitudeVal);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link DoubleType}
     *
     *
     * @param p non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-point-property-functions.html#function_st-longitude">ST_Longitude(p [, new_longitude_val])</a>
     */
    public static SimpleExpression stLongitude(final Expression p) {
        return LiteralFunctions.oneArgFunc("ST_Longitude", p);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param p               non-null
     * @param newLongitudeVal non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-point-property-functions.html#function_st-longitude">ST_Longitude(p [, new_longitude_val])</a>
     */
    public static SimpleExpression stLongitude(final Expression p, final Expression newLongitudeVal) {
        return LiteralFunctions.twoArgFunc("ST_Longitude", p, newLongitudeVal);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link DoubleType}
     *
     *
     * @param p non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-point-property-functions.html#function_st-x">ST_X(p [, new_x_val])</a>
     */
    public static SimpleExpression stX(final Expression p) {
        return LiteralFunctions.oneArgFunc("ST_X", p);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param p       non-null
     * @param newXVal non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-point-property-functions.html#function_st-x">ST_X(p [, new_x_val])</a>
     */
    public static SimpleExpression stX(final Expression p, final Expression newXVal) {
        return LiteralFunctions.twoArgFunc("ST_X", p, newXVal);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link DoubleType}
     *
     *
     * @param p non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-point-property-functions.html#function_st-y">ST_Y(p [, new_y_val])</a>
     */
    public static SimpleExpression stY(final Expression p) {
        return LiteralFunctions.oneArgFunc("ST_Y", p);
    }

    /**
     * <p>
     * The {@link MappingType} of function return type:{@link VarBinaryType},Well-Known Binary (WKB) format
     * , not Internal Geometry Storage Format,that is converted by {@link io.army.stmt.Stmt} executor.
     *
     *
     * @param p       non-null
     * @param newYVal non-null
     * @throws CriteriaException throw when invoking this method in non-statement context.
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/gis-point-property-functions.html#function_st-y">ST_Y(p [, new_y_val])</a>
     */
    public static SimpleExpression stY(final Expression p, final Expression newYVal) {
        return LiteralFunctions.twoArgFunc("ST_Y", p, newYVal);
    }


}
