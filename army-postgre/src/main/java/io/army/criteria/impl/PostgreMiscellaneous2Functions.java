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

import io.army.criteria.*;
import io.army.lang.Nullable;
import io.army.mapping.*;
import io.army.mapping.array.IntegerArrayType;
import io.army.mapping.array.ShortArrayType;
import io.army.mapping.array.TextArrayType;
import io.army.mapping.postgre.PgAclItemType;
import io.army.mapping.postgre.PgInetType;
import io.army.mapping.postgre.PgRangeType;
import io.army.mapping.postgre.PgVectorType;
import io.army.mapping.postgre.array.PostgreAclItemArrayType;
import io.army.util._Collections;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@SuppressWarnings("unused")
abstract class PostgreMiscellaneous2Functions extends PostgreMiscellaneousFunctions {

    /// package constructor
    PostgreMiscellaneous2Functions() {
    }


    /// 
    /// The {@link MappingType} of function return type: {@link  StringType}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-textsearch.html#TEXTSEARCH-FUNCTIONS-TABLE">get_current_ts_config ( ) → regconfig</a>
    public static SimpleExpression getCurrentTsConfig() {
        return FunctionUtils.zeroArgFunc("GET_CURRENT_TS_CONFIG");
    }

    /// 
    /// The {@link MappingType} of function return type: {@link  IntegerType}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-textsearch.html#TEXTSEARCH-FUNCTIONS-TABLE">numnode ( tsquery ) → integer</a>
    public static SimpleExpression numNode(Expression exp) {
        return FunctionUtils.oneArgFunc("NUMNODE", exp);
    }


    /// 
    /// The {@link MappingType} of function returned fields type:
    /// - alias {@link TextType}
    /// - description {@link TextType}
    /// - token {@link TextType}
    /// - dictionaries {@link TextArrayType#LINEAR}
    /// - dictionary {@link TextType}
    /// - lexemes {@link TextArrayType#LINEAR}
    /// - ordinality (this is optional) {@link LongType},see {@link _WithOrdinalityClause#withOrdinality()}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-textsearch.html#TEXTSEARCH-FUNCTIONS-DEBUG-TABLE">ts_debug ( [ config regconfig, ] document text ) → setof record ( alias text, description text, token text, dictionaries regdictionary[], dictionary regdictionary, lexemes text[] )
    /// Extracts and normalizes tokens from the document according to the specified or default text search configuration, and returns information about how each token was processed.
    /// ts_debug('english', 'The Brightest supernovaes') → (asciiword,"Word, all ASCII",The,{english_stem},english_stem,{})
    /// </a>
    public static _TabularWithOrdinalityFunction tsDebug(Expression document) {
        return _tsDebug(null, document);
    }

    /// 
    /// The {@link MappingType} of function returned fields type:
    /// - alias {@link TextType}
    /// - description {@link TextType}
    /// - token {@link TextType}
    /// - dictionaries {@link TextArrayType#LINEAR}
    /// - dictionary {@link TextType}
    /// - lexemes {@link TextArrayType#LINEAR}
    /// - ordinality (this is optional) {@link LongType},see {@link _WithOrdinalityClause#withOrdinality()}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-textsearch.html#TEXTSEARCH-FUNCTIONS-DEBUG-TABLE">ts_debug ( [ config regconfig, ] document text ) → setof record ( alias text, description text, token text, dictionaries regdictionary[], dictionary regdictionary, lexemes text[] )
    /// Extracts and normalizes tokens from the document according to the specified or default text search configuration, and returns information about how each token was processed.
    /// ts_debug('english', 'The Brightest supernovaes') → (asciiword,"Word, all ASCII",The,{english_stem},english_stem,{})
    /// </a>
    public static _TabularWithOrdinalityFunction tsDebug(Expression config, Expression document) {
        return _tsDebug(config, document);
    }

    /// 
    /// The {@link MappingType} of function return type: {@link  TextArrayType#LINEAR}.
    /// @see <a href="https://www.postgresql.org/docs/current/functions-textsearch.html#TEXTSEARCH-FUNCTIONS-TABLE">ts_lexize ( dict regdictionary, token text ) → text[]</a>
    public static SimpleExpression tsLexize(Expression dict, Expression token) {
        return FunctionUtils.twoArgFunc("TS_LEXIZE", dict, token);
    }

    /// 
    /// The {@link MappingType} of function returned fields type:
    /// - tokid {@link IntegerType}
    /// - token {@link TextType}
    /// - ordinality (this is optional) {@link LongType},see {@link _WithOrdinalityClause#withOrdinality()}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-textsearch.html#TEXTSEARCH-FUNCTIONS-DEBUG-TABLE">ts_parse ( parser_name text, document text ) → setof record ( tokid integer, token text )
    /// Extracts tokens from the document using the named parser.
    /// ts_parse('default', 'foo - bar') → (1,foo)
    /// ts_parse ( parser_oid oid, document text ) → setof record ( tokid integer, token text )
    /// Extracts tokens from the document using a parser specified by OID.
    /// ts_parse(3722, 'foo - bar') → (1,foo)
    /// </a>
    public static _TabularWithOrdinalityFunction tsParse(Expression parserName, Expression document) {
        final List<Selection> fieldList = _Collections.arrayList(2);

        fieldList.add(ArmySelections.forName("tokid"));
        fieldList.add(ArmySelections.forName("token"));

        return DialectFunctionUtils.twoArgTabularFunc("TS_PARSE", parserName, document, fieldList);
    }

    /// 
    /// The {@link MappingType} of function returned fields type:
    /// - tokid {@link IntegerType}
    /// - alias {@link TextType}
    /// - description {@link TextType}
    /// - ordinality (this is optional) {@link LongType},see {@link _WithOrdinalityClause#withOrdinality()}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-textsearch.html#TEXTSEARCH-FUNCTIONS-DEBUG-TABLE">ts_token_type ( parser_name text ) → setof record ( tokid integer, alias text, description text )
    /// Returns a table that describes each type of token the named parser can recognize.
    /// ts_token_type('default') → (1,asciiword,"Word, all ASCII")
    /// ts_token_type ( parser_oid oid ) → setof record ( tokid integer, alias text, description text )
    /// Returns a table that describes each type of token a parser specified by OID can recognize.
    /// ts_token_type(3722) → (1,asciiword,"Word, all ASCII")
    /// </a>
    public static _TabularWithOrdinalityFunction tsTokenType(Expression exp) {
        final List<Selection> fieldList = _Collections.arrayList(3);

        fieldList.add(ArmySelections.forName("tokid"));
        fieldList.add(ArmySelections.forName("alias"));
        fieldList.add(ArmySelections.forName("description"));

        return DialectFunctionUtils.oneArgTabularFunc("TS_TOKEN_TYPE", exp, fieldList);
    }


    /// 
    /// The {@link MappingType} of function returned fields type:
    /// - word {@link TextType}
    /// - ndoc {@link IntegerType}
    /// - nentry {@link IntegerType}
    /// - ordinality (this is optional) {@link LongType},see {@link _WithOrdinalityClause#withOrdinality()}
    /// 
    /// @see #tsStat(Expression, Expression)
    /// @see <a href="https://www.postgresql.org/docs/current/functions-textsearch.html#TEXTSEARCH-FUNCTIONS-DEBUG-TABLE">ts_stat ( sqlquery text [, weights text ] ) → setof record ( word text, ndoc integer, nentry integer )
    /// Executes the sqlquery, which must return a single tsvector column, and returns statistics about each distinct lexeme contained in the data.
    /// ts_stat('SELECT vector FROM apod') → (foo,10,15)
    /// </a>
    public static _TabularWithOrdinalityFunction tsStat(Expression sqlQuery) {
        return _tsStat(sqlQuery, null);
    }

    /// 
    /// The {@link MappingType} of function returned fields type:
    /// - word {@link TextType}
    /// - ndoc {@link IntegerType}
    /// - nentry {@link IntegerType}
    /// - ordinality (this is optional) {@link LongType},see {@link _WithOrdinalityClause#withOrdinality()}
    /// 
    /// @see #tsStat(Expression)
    /// @see <a href="https://www.postgresql.org/docs/current/functions-textsearch.html#TEXTSEARCH-FUNCTIONS-DEBUG-TABLE">ts_stat ( sqlquery text [, weights text ] ) → setof record ( word text, ndoc integer, nentry integer )
    /// Executes the sqlquery, which must return a single tsvector column, and returns statistics about each distinct lexeme contained in the data.
    /// ts_stat('SELECT vector FROM apod') → (foo,10,15)
    /// </a>
    public static _TabularWithOrdinalityFunction tsStat(Expression sqlQuery, Expression weights) {
        return _tsStat(sqlQuery, weights);
    }

    /// 
    /// The {@link MappingType} of function return type: {@link  UUIDType}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-uuid.html">UUID Functions</a>
    public static SimpleExpression genRandomUuid() {
        return FunctionUtils.zeroArgFunc("GEN_RANDOM_UUID");
    }

    /*-------------------below Sequence Manipulation Functions-------------------*/

    /// 
    /// The {@link MappingType} of function return type: {@link  LongType}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-sequence.html">nextval ( regclass ) → bigint</a>
    public static SimpleExpression nextVal(Expression exp) {
        return FunctionUtils.oneArgFunc("NEXTVAL", exp);
    }

    /// 
    /// The {@link MappingType} of function return type: {@link  LongType}
    /// @see #setVal(Expression, Expression, Expression)
    /// @see <a href="https://www.postgresql.org/docs/current/functions-sequence.html">setval ( regclass, bigint [, boolean ] ) → bigint</a>
    public static SimpleExpression setVal(Expression regClass, Expression value) {
        return FunctionUtils.twoArgFunc("SETVAL", regClass, value);
    }

    /// 
    /// The {@link MappingType} of function return type: {@link  LongType}
    /// @param isCalled in most case {@link SQLs#TRUE} or {@link SQLs#FALSE}
    /// @see #setVal(Expression, Expression)
    /// @see <a href="https://www.postgresql.org/docs/current/functions-sequence.html">setval ( regclass, bigint [, boolean ] ) → bigint</a>
    public static SimpleExpression setVal(Expression regClass, Expression value, Expression isCalled) {
        return FunctionUtils.threeArgFunc("SETVAL", regClass, value, isCalled);
    }


    /// 
    /// The {@link MappingType} of function return type: {@link  LongType}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-sequence.html">currval ( regclass ) → bigint</a>
    public static SimpleExpression currVal(Expression exp) {
        return FunctionUtils.oneArgFunc("CURRVAL", exp);
    }

    /// 
    /// The {@link MappingType} of function return type: {@link  LongType}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-sequence.html">lastval ( regclass ) → bigint</a>
    public static SimpleExpression lastVal(Expression exp) {
        return FunctionUtils.oneArgFunc("LASTVAL", exp);
    }

    /*-------------------below Conditional Expressions-------------------*/


    /// 
    /// The {@link MappingType} of function return type: the {@link MappingType} of firstValue
    /// @throws CriteriaException throw when
    /// - firstValue isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// - firstValue is multi value {@link Expression},eg: {@link SQLs#rowLiteral(TypeInfer, Collection)}
    /// - the element of rest isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-conditional.html#FUNCTIONS-COALESCE-NVL-IFNULL">COALESCE(value [, ...])</a>
    public static SimpleExpression coalesce(Expression firstValue, Expression... rest) {
        return FunctionUtils.oneAndRestFunc("COALESCE",
                firstValue, rest
        );
    }

    /// 
    /// The {@link MappingType} of function return type: the {@link MappingType} of fist argument
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-conditional.html#FUNCTIONS-COALESCE-NVL-IFNULL">COALESCE(value [, ...])</a>
    public static SimpleExpression coalesce(Consumer<Consumer<Expression>> consumer) {
        return FunctionUtils.consumerAndFirstTypeFunc("COALESCE", consumer);
    }

    /// 
    /// The {@link MappingType} of function return type: the {@link MappingType} of firstValue
    /// @throws CriteriaException throw when
    /// - firstValue isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// - firstValue is multi value {@link Expression},eg: {@link SQLs#rowLiteral(TypeInfer, Collection)}
    /// - the element of rest isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-conditional.html#FUNCTIONS-GREATEST-LEAST">GREATEST(value [, ...])</a>
    public static SimpleExpression greatest(Expression firstValue, Expression... rest) {
        return FunctionUtils.oneAndRestFunc("GREATEST",
                firstValue, rest
        );
    }

    /// 
    /// The {@link MappingType} of function return type: the {@link MappingType} of fist argument
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-conditional.html#FUNCTIONS-GREATEST-LEAST">GREATEST(value [, ...])</a>
    public static SimpleExpression greatest(Consumer<Consumer<Expression>> consumer) {
        return FunctionUtils.consumerAndFirstTypeFunc("GREATEST", consumer);
    }

    /// 
    /// The {@link MappingType} of function return type: the {@link MappingType} of firstValue
    /// @throws CriteriaException throw when
    /// - firstValue isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// - firstValue is multi value {@link Expression},eg: {@link SQLs#rowLiteral(TypeInfer, Collection)}
    /// - the element of rest isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-conditional.html#FUNCTIONS-GREATEST-LEAST">LEAST(value [, ...])</a>
    public static SimpleExpression least(Expression firstValue, Expression... rest) {
        return FunctionUtils.oneAndRestFunc("LEAST",
                firstValue, rest
        );
    }

    /// 
    /// The {@link MappingType} of function return type: the {@link MappingType} of fist argument
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-conditional.html#FUNCTIONS-GREATEST-LEAST">LEAST(value [, ...])</a>
    public static SimpleExpression least(Consumer<Consumer<Expression>> consumer) {
        return FunctionUtils.consumerAndFirstTypeFunc("LEAST", consumer);
    }

    /*-------------------below  Array Functions-------------------*/

    /// The {@link MappingType} of function return type: the {@link MappingType} of fist anyCompatibleArray
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">array_append ( anycompatiblearray, anycompatible ) → anycompatiblearray</a>
    public static SimpleExpression arrayAppend(Expression anyCompatibleArray, Expression anyCompatible) {
        return FunctionUtils.twoArgFunc("ARRAY_APPEND", anyCompatibleArray, anyCompatible);
    }

    /// The {@link MappingType} of function return type: the {@link MappingType} of fist anyCompatibleArray1
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">array_cat ( anycompatiblearray, anycompatiblearray ) → anycompatiblearray</a>
    public static SimpleExpression arrayCat(Expression anyCompatibleArray1, Expression anyCompatibleArray2) {
        return FunctionUtils.twoArgFunc("ARRAY_CAT", anyCompatibleArray1, anyCompatibleArray2);
    }

    /// The {@link MappingType} of function return type: {@link TextType#INSTANCE}
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">array_dims ( anyarray ) → text</a>
    public static SimpleExpression arrayDims(Expression anyArray) {
        return FunctionUtils.oneArgFunc("ARRAY_DIMS", anyArray);
    }

    /// The {@link MappingType} of function return type: the array type of {@link MappingType} of anyElement.
    /// @param funcRefForDimension the reference of method,Note: it's the reference of method,not lambda. Valid method:
    /// 
    /// - {@link SQLs#param(TypeInfer, Object)}
    /// - {@link SQLs#literal(TypeInfer, Object)}
    /// - {@link SQLs#namedParam(TypeInfer, String)} ,used only in INSERT( or batch update/delete ) syntax
    /// - {@link SQLs#namedLiteral(TypeInfer, String)} ,used only in INSERT( or batch update/delete in multi-statement) syntax
    /// - developer custom method
    /// .
    /// The first argument of funcRefForDimension always is {@link IntegerArrayType#LINEAR}.
    /// @param dimensions          non-null,it will be passed to funcRefForDimension as the second argument of funcRefForDimension
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">array_fill ( anyelement, integer[] [, integer[] ] ) → anyarray</a>
    public static <T> Expression arrayFill(Expression anyElement, BiFunction<MappingType, T, Expression> funcRefForDimension,
                                           T dimensions) {
        return _arrayFill(anyElement, funcRefForDimension.apply(IntegerArrayType.LINEAR, dimensions), null);
    }

    /// The {@link MappingType} of function return type: the array type of {@link MappingType} of anyElement.
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">array_fill ( anyelement, integer[] [, integer[] ] ) → anyarray</a>
    public static SimpleExpression arrayFill(Expression anyElement, Expression dimensions) {
        return _arrayFill(anyElement, dimensions, null);
    }

    /// 
    /// The {@link MappingType} of function return type: the array type of {@link MappingType} of anyElement.
    /// @param funcRefForDimension the reference of method,Note: it's the reference of method,not lambda. Valid method:
    /// 
    /// - {@link SQLs#param(TypeInfer, Object)}
    /// - {@link SQLs#literal(TypeInfer, Object)}
    /// - {@link SQLs#namedParam(TypeInfer, String)} ,used only in INSERT( or batch update/delete ) syntax
    /// - {@link SQLs#namedLiteral(TypeInfer, String)} ,used only in INSERT( or batch update/delete in multi-statement) syntax
    /// - developer custom method
    /// .
    /// The first argument of funcRefForDimension always is {@link IntegerArrayType#LINEAR}.
    /// @param dimensions          non-null,it will be passed to funcRefForDimension as the second argument of funcRefForDimension
    /// @param funcRefForBound     the reference of method,Note: it's the reference of method,not lambda. Valid method:
    /// 
    /// - {@link SQLs#param(TypeInfer, Object)}
    /// - {@link SQLs#literal(TypeInfer, Object)}
    /// - {@link SQLs#namedParam(TypeInfer, String)} ,used only in INSERT( or batch update/delete ) syntax
    /// - {@link SQLs#namedLiteral(TypeInfer, String)} ,used only in INSERT( or batch update/delete in multi-statement) syntax
    /// - developer custom method
    /// .
    /// The first argument of funcRefForBound always is {@link IntegerArrayType#LINEAR}.
    /// @param bounds              non-null,it will be passed to funcRefForBound as the second argument of funcRefForBound
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">array_fill ( anyelement, integer[] [, integer[] ] ) → anyarray</a>
    public static <T, U> Expression arrayFill(Expression anyElement, BiFunction<MappingType, T, Expression> funcRefForDimension,
                                              T dimensions, BiFunction<MappingType, U, Expression> funcRefForBound, U bounds) {
        return _arrayFill(anyElement, funcRefForDimension.apply(IntegerArrayType.LINEAR, dimensions),
                funcRefForBound.apply(IntegerArrayType.LINEAR, bounds)
        );
    }

    /// The {@link MappingType} of function return type: the array type of {@link MappingType} of anyElement.
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">array_fill ( anyelement, integer[] [, integer[] ] ) → anyarray</a>
    public static SimpleExpression arrayFill(Expression anyElement, Expression dimensions, Expression bounds) {
        ContextStack.assertNonNull(bounds);
        return _arrayFill(anyElement, dimensions, bounds);
    }

    /// The {@link MappingType} of function return type: {@link IntegerType#INSTANCE}
    /// @param funcRef   the reference of method,Note: it's the reference of method,not lambda. Valid method:
    /// 
    /// - {@link SQLs#param(TypeInfer, Object)}
    /// - {@link SQLs#literal(TypeInfer, Object)}
    /// - {@link SQLs#namedParam(TypeInfer, String)} ,used only in INSERT( or batch update/delete ) syntax
    /// - {@link SQLs#namedLiteral(TypeInfer, String)} ,used only in INSERT( or batch update/delete in multi-statement) syntax
    /// - developer custom method
    /// .
    /// The first argument of funcRef always is {@link IntegerType#INSTANCE}.
    /// @param dimension non-null,it will be passed to funcRef as the second argument of funcRef
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see #arrayLength(Expression, Expression)
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">array_length ( anyarray, integer ) → integer</a>
    public static <T> Expression arrayLength(Expression anyArray, BiFunction<MappingType, T, Expression> funcRef,
                                             T dimension) {
        return arrayLength(anyArray, funcRef.apply(IntegerType.INSTANCE, dimension));
    }

    /// 
    /// The {@link MappingType} of function return type: {@link IntegerType#INSTANCE}
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">array_length ( anyarray, integer ) → integer</a>
    public static SimpleExpression arrayLength(Expression anyArray, Expression dimension) {
        return FunctionUtils.twoArgFunc("ARRAY_LENGTH", anyArray, dimension);
    }

    /// 
    /// The {@link MappingType} of function return type: {@link IntegerType#INSTANCE}
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see #arrayUpper(Expression, Expression)
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">array_lower ( anyarray, integer ) → integer</a>
    public static SimpleExpression arrayLower(Expression anyArray, Expression dimension) {
        return FunctionUtils.twoArgFunc("ARRAY_LOWER", anyArray, dimension);
    }

    /// 
    /// The {@link MappingType} of function return type: {@link IntegerType#INSTANCE}
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">array_ndims ( anyarray ) → integer</a>
    public static SimpleExpression arrayNDims(Expression anyArray) {
        return FunctionUtils.oneArgFunc("ARRAY_NDIMS", anyArray);
    }

    /// 
    /// The {@link MappingType} of function return type: {@link IntegerType#INSTANCE}
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see #arrayPosition(Expression, Expression, Expression)
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">array_position ( anycompatiblearray, anycompatible [, integer ] ) → integer</a>
    public static SimpleExpression arrayPosition(Expression anyCompatibleArray, Expression anyCompatible) {
        return FunctionUtils.twoArgFunc("ARRAY_POSITION", anyCompatibleArray, anyCompatible);
    }

    /// 
    /// The {@link MappingType} of function return type: {@link IntegerType#INSTANCE}
    /// @param funcRef   the reference of method,Note: it's the reference of method,not lambda. Valid method:
    /// 
    /// - {@link SQLs#param(TypeInfer, Object)}
    /// - {@link SQLs#literal(TypeInfer, Object)}
    /// - {@link SQLs#namedParam(TypeInfer, String)} ,used only in INSERT( or batch update/delete ) syntax
    /// - {@link SQLs#namedLiteral(TypeInfer, String)} ,used only in INSERT( or batch update/delete in multi-statement) syntax
    /// - {@link SQLs#encodingParam(TypeInfer, Object)},used when only **this** is instance of {@link TableField} and {@link TableField#codec()} is true
    /// - {@link SQLs#encodingLiteral(TypeInfer, Object)},used when only **this** is instance of {@link TableField} and {@link TableField#codec()} is true
    /// - {@link SQLs#encodingNamedParam(TypeInfer, String)} ,used when only **this** is instance of {@link TableField} and {@link TableField#codec()} is true
    /// and in INSERT( or batch update/delete ) syntax
    /// - {@link SQLs#encodingNamedLiteral(TypeInfer, String)} ,used when only **this** is instance of {@link TableField} and {@link TableField#codec()} is true
    /// and in INSERT( or batch update/delete in multi-statement) syntax
    /// - developer custom method
    /// .
    /// The first argument of funcRef always is {@link IntegerType#INSTANCE}.
    /// @param subscript non-null,it will be passed to funcRef as the second argument of funcRef
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see #arrayPosition(Expression, Expression, Expression)
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">array_position ( anycompatiblearray, anycompatible [, integer ] ) → integer</a>
    public static <T> Expression arrayPosition(Expression anyCompatibleArray, Expression anyCompatible,
                                               BiFunction<MappingType, T, Expression> funcRef, T subscript) {
        return arrayPosition(anyCompatibleArray, anyCompatible, funcRef.apply(IntegerType.INSTANCE, subscript));
    }

    /// 
    /// The {@link MappingType} of function return type: {@link IntegerType#INSTANCE}
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">array_position ( anycompatiblearray, anycompatible [, integer ] ) → integer</a>
    public static SimpleExpression arrayPosition(Expression anyCompatibleArray, Expression anyCompatible,
                                                 Expression subscript) {
        return FunctionUtils.threeArgFunc("ARRAY_POSITION", anyCompatibleArray, anyCompatible, subscript);
    }

    /// 
    /// The {@link MappingType} of function return type: {@link IntegerArrayType#LINEAR}
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">array_positions ( anycompatiblearray, anycompatible ) → integer[]</a>
    public static SimpleExpression arrayPositions(Expression anyCompatibleArray, Expression anyCompatible) {
        return FunctionUtils.twoArgFunc("ARRAY_POSITIONS", anyCompatibleArray, anyCompatible);
    }


    /// 
    /// The {@link MappingType} of function return type: the {@link MappingType} of anyCompatibleArray.
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">array_prepend ( anycompatible, anycompatiblearray ) → anycompatiblearray</a>
    public static SimpleExpression arrayPrepend(Expression anyCompatible, Expression anyCompatibleArray) {
        return FunctionUtils.twoArgFunc("ARRAY_PREPEND", anyCompatible, anyCompatibleArray);
    }

    /// 
    /// The {@link MappingType} of function return type: the {@link MappingType} of anyCompatibleArray.
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">array_remove ( anycompatiblearray, anycompatible ) → anycompatiblearray</a>
    public static SimpleExpression arrayRemove(Expression anyCompatibleArray, Expression anyCompatible) {
        return FunctionUtils.twoArgFunc("ARRAY_REMOVE", anyCompatibleArray, anyCompatible);
    }

    /// 
    /// The {@link MappingType} of function return type: the {@link MappingType} of anyCompatibleArray.
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">array_replace ( anycompatiblearray, anycompatible, anycompatible ) → anycompatiblearray</a>
    public static SimpleExpression arrayReplace(Expression anyCompatibleArray, Expression anyCompatible,
                                                Expression replacement) {
        return FunctionUtils.threeArgFunc("ARRAY_REPLACE", anyCompatibleArray, anyCompatible, replacement);
    }

    /// 
    /// The {@link MappingType} of function return type: {@link TextType#INSTANCE}.
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">array_to_string ( array anyarray, delimiter text [, null_string text ] ) → text</a>
    public static SimpleExpression arrayToString(Expression array, Expression delimiter, Expression nullString) {
        return FunctionUtils.threeArgFunc("ARRAY_TO_STRING", array, delimiter, nullString);
    }

    /// 
    /// The {@link MappingType} of function return type: {@link IntegerType#INSTANCE}.
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see #arrayLower(Expression, Expression)
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">array_upper ( anyarray, integer ) → integer</a>
    public static SimpleExpression arrayUpper(Expression anyArray, Expression dimension) {
        return FunctionUtils.twoArgFunc("ARRAY_UPPER", anyArray, dimension);
    }

    /// 
    /// The {@link MappingType} of function return type: {@link IntegerType#INSTANCE}.
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see #arrayLower(Expression, Expression)
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">cardinality ( anyarray ) → integer</a>
    public static SimpleExpression cardinality(Expression anyArray) {
        return FunctionUtils.oneArgFunc("CARDINALITY", anyArray);
    }


    /// 
    /// The {@link MappingType} of function return type: the {@link MappingType} of array.
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see #arrayLower(Expression, Expression)
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">trim_array ( array anyarray, n integer ) → anyarray</a>
    public static SimpleExpression trimArray(Expression array, Expression n) {
        return FunctionUtils.twoArgFunc("TRIM_ARRAY", array, n);
    }

    /// 
    /// The {@link MappingType} of function returned fields type:
    /// - lexeme {@link TextType}
    /// - positions {@link ShortArrayType} with one dimension
    /// - weights {@link TextType}
    /// - ordinality (this is optional) {@link LongType},see {@link _WithOrdinalityClause#withOrdinality()}
    /// 
    /// 
    /// <pre>
    /// select * from unnest('cat:3 fat:2,4 rat:5A'::tsvector) →
    /// lexeme | positions | weights
    /// --------+-----------+---------
    /// cat    | {3}       | {D}
    /// fat    | {2,4}     | {D,D}
    /// rat    | {5}       | {A}
    /// </pre>
    /// 
    /// If exp is array,then the {@link MappingType} of function returned is the {@link MappingType} of the element.
    /// <pre>
    /// unnest ( anyarray ) → setof anyelement
    /// Expands an array into a set of rows. The array's elements are read out in storage order.
    /// unnest(ARRAY[1,2]) →
    /// 1
    /// 2
    /// unnest(ARRAY[['foo','bar'],['baz','quux']]) →
    /// foo
    /// bar
    /// baz
    /// quux
    /// </pre>
    /// @param exp must be {@link TypedExpression}, see {@link TypedField} or  {@link Expression#mapTo(MappingType)} or {@link Expression#castTo(MappingType)}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-textsearch.html#TEXTSEARCH-FUNCTIONS-TABLE">unnest ( tsvector ) → setof record ( lexeme text, positions smallint[], weights text )
    /// Expands a tsvector into a set of rows, one per lexeme
    /// </a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">unnest ( anyarray ) → setof anyelement
    /// Expands an array into a set of rows. The array's elements are read out in storage order.
    /// </a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-range.html#MULTIRANGE-FUNCTIONS-TABLE">unnest ( anymultirange ) → setof anyrange
    /// Expands a multirange into a set of ranges. The ranges are read out in storage order (ascending).
    /// unnest('{[1,2), [3,4)}'::int4multirange) →
    /// [1,2)
    /// [3,4)
    /// </a>
    public static _TabularWithOrdinalityFunction unnest(final TypedExpression exp) {  // here must be TypedExpression or error
        final String name = "unnest";

        final MappingType type;
        type = exp.typeMeta().mappingType();
        final _TabularWithOrdinalityFunction func;
        if (type instanceof MappingType.SqlArray) {
            func = DialectFunctionUtils.oneArgColumnFunction(name, exp, name); // postgre default function name as field name.
        } else if (type instanceof PgVectorType) {
            final List<Selection> fieldList = List.of(
                    ArmySelections.forName("lexeme"),
                    ArmySelections.forName("positions"),
                    ArmySelections.forName("weights")
            );
            func = DialectFunctionUtils.oneArgTabularFunc(name, exp, fieldList);
        } else if (type instanceof PgRangeType.MultiRangeType) {
            func = DialectFunctionUtils.oneArgColumnFunction(name, exp, null);
        } else {
            throw CriteriaUtils.funcArgError(name, exp);
        }
        return func;
    }


    /// 
    /// If exp is array,then the {@link MappingType} of function returned is the {@link MappingType} of the element.
    /// <pre>
    /// select * from unnest(ARRAY[1,2], ARRAY['foo','bar','baz']) as x(a,b) →
    /// a |  b
    /// ---+-----
    /// 1 | foo
    /// 2 | bar
    /// | baz
    /// </pre>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">unnest ( anyarray, anyarray [, ... ] ) → setof anyelement, anyelement [, ... ]
    /// Expands multiple arrays (possibly of different data types) into a set of rows. If the arrays are not all the same length then the shorter ones are padded with NULLs. This form is only allowed in a query's FROM clause;
    /// </a>
    public static _TabularWithOrdinalityFunction unnest(Expression array1, Expression array2) {
        final List<Selection> fieldList = List.of(
                ArmySelections.forAnonymous(),
                ArmySelections.forAnonymous()
        );
        return DialectFunctionUtils.twoArgTabularFunc("unnest", array1, array2, fieldList);
    }

    /// 
    /// If exp is array,then the {@link MappingType} of function returned is the {@link MappingType} of the element.
    /// <pre>
    /// select * from unnest(ARRAY[1,2], ARRAY['foo','bar','baz']) as x(a,b) →
    /// a |  b
    /// ---+-----
    /// 1 | foo
    /// 2 | bar
    /// | baz
    /// </pre>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">unnest ( anyarray, anyarray [, ... ] ) → setof anyelement, anyelement [, ... ]
    /// Expands multiple arrays (possibly of different data types) into a set of rows. If the arrays are not all the same length then the shorter ones are padded with NULLs. This form is only allowed in a query's FROM clause;
    /// </a>
    public static _TabularWithOrdinalityFunction unnest(Expression array1, Expression array2,
                                                        Expression array3, Expression... restArray) {
        final String name = "unnest";

        final List<Selection> fieldList = _Collections.arrayList(3 + restArray.length);
        fieldList.add(ArmySelections.forAnonymous());
        fieldList.add(ArmySelections.forAnonymous());
        fieldList.add(ArmySelections.forAnonymous());


        final _TabularWithOrdinalityFunction func;
        if (restArray.length == 0) {
            func = DialectFunctionUtils.threeArgTabularFunc(name, array1, array2, array3, fieldList);
        } else {
            final List<ArmyExpression> argList = _Collections.arrayList(3 + restArray.length);
            argList.add((ArmyExpression) array1);
            argList.add((ArmyExpression) array2);
            argList.add((ArmyExpression) array3);

            for (Expression array : restArray) {
                argList.add((ArmyExpression) array);
                fieldList.add(ArmySelections.forAnonymous());
            }
            func = DialectFunctionUtils.multiArgTabularFunc(name, argList, fieldList);
        }
        return func;
    }

    /// 
    /// If exp is array,then the {@link MappingType} of function returned is the {@link MappingType} of the element.
    /// <pre>
    /// select * from unnest(ARRAY[1,2], ARRAY['foo','bar','baz']) as x(a,b) →
    /// a |  b
    /// ---+-----
    /// 1 | foo
    /// 2 | bar
    /// | baz
    /// </pre>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html#ARRAY-FUNCTIONS-TABLE">unnest ( anyarray, anyarray [, ... ] ) → setof anyelement, anyelement [, ... ]
    /// Expands multiple arrays (possibly of different data types) into a set of rows. If the arrays are not all the same length then the shorter ones are padded with NULLs. This form is only allowed in a query's FROM clause;
    /// </a>
    public static _TabularWithOrdinalityFunction unnest(final List<Expression> arrayList) {
        if (arrayList.isEmpty()) {
            throw CriteriaUtils.dontAddAnyItem();
        }
        final String name = "unnest";
        final List<Expression> argList = _Collections.copyList(arrayList);
        final List<Selection> fieldList = _Collections.arrayList();
        final int size = argList.size();
        for (int i = 0; i < size; i++) {
            fieldList.add(ArmySelections.forAnonymous());
        }

        return DialectFunctionUtils.multiArgTabularFunc(name, argList, fieldList);
    }

    /*-------------------below Range/Multirange Functions and Operators -------------------*/

    /// 
    /// The {@link MappingType} of function return type: {@link BooleanType#INSTANCE}.
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-range.html#RANGE-FUNCTIONS-TABLE">isempty ( anyrange ) → boolean
    /// Is the range empty?
    /// isempty(numrange(1.1,2.2)) → f
    /// </a>
    public static SimplePredicate isEmpty(Expression exp) {
        return FunctionUtils.oneArgPredicateFunc("ISEMPTY", exp);
    }

    /// 
    /// The {@link MappingType} of function return type: {@link BooleanType#INSTANCE}.
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-range.html#RANGE-FUNCTIONS-TABLE">lower_inc ( anyrange ) → boolean
    /// Is the range's lower bound inclusive?
    /// lower_inc(numrange(1.1,2.2)) → t
    /// </a>
    public static SimplePredicate lowerInc(Expression exp) {
        return FunctionUtils.oneArgPredicateFunc("LOWER_INC", exp);
    }

    /// 
    /// The {@link MappingType} of function return type: {@link BooleanType#INSTANCE}.
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-range.html#RANGE-FUNCTIONS-TABLE">upper_inc ( anyrange ) → boolean
    /// Is the range's upper bound inclusive?
    /// upper_inc(numrange(1.1,2.2)) → t
    /// </a>
    public static SimplePredicate upperInc(Expression exp) {
        return FunctionUtils.oneArgPredicateFunc("UPPER_INC", exp);
    }

    /// 
    /// The {@link MappingType} of function return type: {@link BooleanType#INSTANCE}.
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-range.html#RANGE-FUNCTIONS-TABLE">lower_inf ( anyrange ) → boolean
    /// Is the range's lower bound infinite?
    /// lower_inf(numrange(1.1,2.2)) → t
    /// </a>
    public static SimplePredicate lowerInf(Expression exp) {
        return FunctionUtils.oneArgPredicateFunc("LOWER_INF", exp);
    }

    /// 
    /// The {@link MappingType} of function return type: {@link BooleanType#INSTANCE}.
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-range.html#RANGE-FUNCTIONS-TABLE">upper_inf ( anyrange ) → boolean
    /// Is the range's upper bound infinite?
    /// upper_inf(numrange(1.1,2.2)) → t
    /// </a>
    public static SimplePredicate upperInf(Expression exp) {
        return FunctionUtils.oneArgPredicateFunc("UPPER_INF", exp);
    }

    /// 
    /// The {@link MappingType} of function return type: the {@link MappingType} of range1.
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-range.html#RANGE-FUNCTIONS-TABLE">range_merge ( anyrange, anyrange ) → anyrange
    /// Computes the smallest range that includes both of the given ranges.
    /// range_merge('[1,2)'::int4range, '[3,4)'::int4range) → [1,4)
    /// </a>
    public static SimpleExpression rangeMerge(Expression range1, Expression range2) {
        return FunctionUtils.twoArgFunc("RANGE_MERGE", range1, range2);
    }

    /// 
    /// The {@link MappingType} of function return type:
    /// - If anyRange is {@link PgRangeType.SingleRangeType} ,then the multi range of the {@link MappingType} of anyRange.
    /// - Else {@link TextType#INSTANCE}
    /// 
    /// @throws CriteriaException throw when
    /// - the element of consumer isn't operable {@link Expression},eg:{@link SQLs#DEFAULT}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-range.html#MULTIRANGE-FUNCTIONS-TABLE">multirange ( anyrange ) → anymultirange
    /// Returns a multirange containing just the given range.
    /// multirange('[1,2)'::int4range) → {[1,2)}
    /// </a>
    public static SimpleExpression multiRange(final Expression anyRange) {
        return FunctionUtils.oneArgFunc("multirange", anyRange);
    }

    /*-------------------below Series Generating Functions-------------------*/

    /// The {@link MappingType} of function return type: the {@link MappingType} of start
    /// @param start literal or {@link Expression}
    /// @param stop  literal or {@link Expression}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-srf.html">Set Returning Functions
    /// </a>
    public static _ColumnWithOrdinalityFunction generateSeries(Object start, Object stop) {
        final Expression startExp;
        startExp = SQLs._nonNullLiteral(start);
        return DialectFunctionUtils.twoArgColumnFunction("generate_series", startExp, stop, null);
    }

    /// The {@link MappingType} of function return type: the {@link MappingType} of start
    /// @param start literal or {@link Expression}
    /// @param stop  literal or {@link Expression}
    /// @param step  literal or {@link Expression}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-srf.html">Set Returning Functions
    /// </a>
    public static _ColumnWithOrdinalityFunction generateSeries(Object start, Object stop, Object step) {
        final Expression startExp;
        startExp = SQLs._nonNullLiteral(start);
        return DialectFunctionUtils.threeArgColumnFunction("generate_series", startExp, stop, step, null
        );
    }

    /// The {@link MappingType} of function return type:  {@link IntegerType#INSTANCE} rt
    /// @see <a href="https://www.postgresql.org/docs/current/functions-srf.html#FUNCTIONS-SRF-SUBSCRIPTS">Subscript Generating Functions
    /// </a>
    public static _ColumnWithOrdinalityFunction generateSubscripts(Expression array, Expression dim) {
        return DialectFunctionUtils.twoArgColumnFunction("generate_subscripts", array, dim, null);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link IntegerType#INSTANCE}
    /// @param reverse in mose case {@link SQLs#TRUE} or {@link SQLs#FALSE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-srf.html#FUNCTIONS-SRF-SUBSCRIPTS">Subscript Generating Functions
    /// </a>
    public static _ColumnWithOrdinalityFunction generateSubscripts(Expression array, Expression dim, Expression reverse) {
        return DialectFunctionUtils.threeArgColumnFunction("generate_subscripts", array, dim, reverse, null
        );
    }

    /*-------------------below  System Information Functions-------------------*/

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SESSION-TABLE">current_database () → name
    /// </a>
    public static SimpleExpression currentDatabase() {
        return FunctionUtils.zeroArgFunc("current_database");
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SESSION-TABLE">current_query () → text
    /// </a>
    public static SimpleExpression currentQuery() {
        return FunctionUtils.zeroArgFunc("current_query");
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SESSION-TABLE">currentSchema () → name
    /// </a>
    public static SimpleExpression currentSchema() {
        return FunctionUtils.zeroArgFunc("current_schema");
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link TextArrayType#LINEAR}
    /// @param includeImplicit in mose case {@link SQLs#TRUE} or {@link SQLs#FALSE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SESSION-TABLE">current_schemas ( include_implicit boolean ) → name[]
    /// </a>
    public static SimpleExpression currentSchema(Expression includeImplicit) {
        return FunctionUtils.oneArgFunc("current_schema", includeImplicit);
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link PgInetType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SESSION-TABLE">inet_client_addr () → inet
    /// </a>
    public static SimpleExpression inetClientAddr() {
        return FunctionUtils.zeroArgFunc("inet_client_addr");
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link IntegerType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SESSION-TABLE">inet_client_port () → integer
    /// </a>
    public static SimpleExpression inetClientPort() {
        return FunctionUtils.zeroArgFunc("inet_client_port");
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link PgInetType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SESSION-TABLE">inet_server_addr () → inet
    /// </a>
    public static SimpleExpression inetServerAddr() {
        return FunctionUtils.zeroArgFunc("inet_server_addr");
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link IntegerType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SESSION-TABLE">inet_server_port () → integer
    /// </a>
    public static SimpleExpression inetServerPort() {
        return FunctionUtils.zeroArgFunc("inet_server_port");
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link IntegerType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SESSION-TABLE">pg_backend_pid () → integer
    /// </a>
    public static SimpleExpression pgBackendPid() {
        return FunctionUtils.zeroArgFunc("pg_backend_pid");
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link IntegerArrayType#PRIMITIVE_LINEAR}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SESSION-TABLE">pg_blocking_pids ( integer ) → integer[]
    /// </a>
    public static SimpleExpression pgBlockingPids(Expression exp) {
        return FunctionUtils.oneArgFunc("pg_blocking_pids", exp);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link OffsetDateTimeType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SESSION-TABLE">pg_conf_load_time () → timestamp with time zone
    /// </a>
    public static SimpleExpression pgConfLoadTime() {
        return FunctionUtils.zeroArgFunc("pg_conf_load_time");
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SESSION-TABLE">pg_current_logfile ( [ text ] ) → text
    /// </a>
    public static SimpleExpression pgCurrentLogFile() {
        return FunctionUtils.zeroArgFunc("pg_current_logfile");
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SESSION-TABLE">pg_current_logfile ( [ text ] ) → text
    /// </a>
    public static SimpleExpression pgCurrentLogFile(Expression exp) {
        return FunctionUtils.oneArgFunc("pg_current_logfile", exp);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link LongType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SESSION-TABLE">pg_my_temp_schema () → oid
    /// </a>
    public static SimpleExpression pgMyTempSchema() {
        return FunctionUtils.zeroArgFunc("pg_my_temp_schema");
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SESSION-TABLE">pg_is_other_temp_schema ( oid ) → boolean
    /// </a>
    public static SimplePredicate pgIsOtherTempSchema(Expression exp) {
        return FunctionUtils.oneArgPredicateFunc("pg_is_other_temp_schema", exp);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SESSION-TABLE">pg_jit_available () → boolean
    /// </a>
    public static SimplePredicate pgJitAvailable() {
        return FunctionUtils.zeroArgFuncPredicate("pg_jit_available");
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SESSION-TABLE">pg_listening_channels () → setof text
    /// </a>
    public static _ColumnWithOrdinalityFunction pgListeningChannels() {
        return DialectFunctionUtils.zeroArgColumnFunction("pg_listening_channels", null);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link DoubleType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SESSION-TABLE">pg_notification_queue_usage () → double precision
    /// </a>
    public static SimpleExpression pgNotificationQueueUsage() {
        return FunctionUtils.zeroArgFunc("pg_notification_queue_usage");
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link OffsetDateTimeType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SESSION-TABLE">pg_postmaster_start_time () → timestamp with time zone
    /// </a>
    public static SimpleExpression pgPostMasterStartTime() {
        return FunctionUtils.zeroArgFunc("pg_postmaster_start_time");
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link IntegerArrayType#PRIMITIVE_LINEAR}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SESSION-TABLE">pg_safe_snapshot_blocking_pids ( integer ) → integer[]
    /// </a>
    public static SimpleExpression pgSafeSnapshotBlockingPids(Expression exp) {
        return FunctionUtils.oneArgFunc("pg_safe_snapshot_blocking_pids", exp);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link IntegerType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SESSION-TABLE">pg_trigger_depth () → integer
    /// </a>
    public static SimpleExpression pgTriggerDepth() {
        return FunctionUtils.zeroArgFunc("pg_trigger_depth");
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SESSION-TABLE">version () → text
    /// </a>
    public static SimpleExpression version() {
        return FunctionUtils.zeroArgFunc("version");
    }


    /*-------------------below Access Privilege Inquiry Functions-------------------*/

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_any_column_privilege ( [ user name or oid, ] table text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasAnyColumnPrivilege(Expression table, Expression privilege) {
        return FunctionUtils.twoArgPredicateFunc("has_any_column_privilege", table, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_any_column_privilege ( [ user name or oid, ] table text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasAnyColumnPrivilege(Expression user, Expression table, Expression privilege) {
        return FunctionUtils.threeArgPredicateFunc("has_any_column_privilege", user, table, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_column_privilege ( [ user name or oid, ] table text or oid, column text or smallint, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasColumnPrivilege(Expression table, Expression column, Expression privilege) {
        return FunctionUtils.threeArgPredicateFunc("has_column_privilege", table, column, privilege);
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_column_privilege ( [ user name or oid, ] table text or oid, column text or smallint, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasColumnPrivilege(Expression user, Expression table, Expression column, Expression privilege) {
        return FunctionUtils.fourArgPredicateFunc("has_column_privilege", user, table, column, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_database_privilege ( [ user name or oid, ] database text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasDatabasePrivilege(Expression database, Expression privilege) {
        return FunctionUtils.twoArgPredicateFunc("has_database_privilege", database, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_database_privilege ( [ user name or oid, ] database text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasDatabasePrivilege(Expression user, Expression database, Expression privilege) {
        return FunctionUtils.threeArgPredicateFunc("has_database_privilege", user, database, privilege);
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_foreign_data_wrapper_privilege ( [ user name or oid, ] fdw text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasForeignDataWrapperPrivilege(Expression fdw, Expression privilege) {
        return FunctionUtils.twoArgPredicateFunc("has_foreign_data_wrapper_privilege", fdw, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_foreign_data_wrapper_privilege ( [ user name or oid, ] fdw text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasForeignDataWrapperPrivilege(Expression user, Expression fdw, Expression privilege) {
        return FunctionUtils.threeArgPredicateFunc("has_foreign_data_wrapper_privilege", user, fdw, privilege);
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_function_privilege ( [ user name or oid, ] function text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasFunctionPrivilege(Expression function, Expression privilege) {
        return FunctionUtils.twoArgPredicateFunc("has_function_privilege", function, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_function_privilege ( [ user name or oid, ] function text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasFunctionPrivilege(Expression user, Expression function, Expression privilege) {
        return FunctionUtils.threeArgPredicateFunc("has_function_privilege", user, function, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_language_privilege ( [ user name or oid, ] language text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasLanguagePrivilege(Expression language, Expression privilege) {
        return FunctionUtils.twoArgPredicateFunc("has_language_privilege", language, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_language_privilege ( [ user name or oid, ] language text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasLanguagePrivilege(Expression user, Expression language, Expression privilege) {
        return FunctionUtils.threeArgPredicateFunc("has_language_privilege", user, language, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_parameter_privilege ( [ user name or oid, ] parameter text, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasParameterPrivilege(Expression parameter, Expression privilege) {
        return FunctionUtils.twoArgPredicateFunc("has_parameter_privilege", parameter, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_parameter_privilege ( [ user name or oid, ] parameter text, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasParameterPrivilege(Expression user, Expression parameter, Expression privilege) {
        return FunctionUtils.threeArgPredicateFunc("has_parameter_privilege", user, parameter, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_schema_privilege ( [ user name or oid, ] schema text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasSchemaPrivilege(Expression schema, Expression privilege) {
        return FunctionUtils.twoArgPredicateFunc("has_schema_privilege", schema, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_schema_privilege ( [ user name or oid, ] schema text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasSchemaPrivilege(Expression user, Expression schema, Expression privilege) {
        return FunctionUtils.threeArgPredicateFunc("has_schema_privilege", user, schema, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_sequence_privilege ( [ user name or oid, ] sequence text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasSequencePrivilege(Expression sequence, Expression privilege) {
        return FunctionUtils.twoArgPredicateFunc("has_sequence_privilege", sequence, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_sequence_privilege ( [ user name or oid, ] sequence text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasSequencePrivilege(Expression user, Expression sequence, Expression privilege) {
        return FunctionUtils.threeArgPredicateFunc("has_sequence_privilege", user, sequence, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_server_privilege ( [ user name or oid, ] server text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasServerPrivilege(Expression server, Expression privilege) {
        return FunctionUtils.twoArgPredicateFunc("has_server_privilege", server, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_server_privilege ( [ user name or oid, ] server text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasServerPrivilege(Expression user, Expression server, Expression privilege) {
        return FunctionUtils.threeArgPredicateFunc("has_server_privilege", user, server, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_table_privilege ( [ user name or oid, ] table text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasTablePrivilege(Expression table, Expression privilege) {
        return FunctionUtils.twoArgPredicateFunc("has_table_privilege", table, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_table_privilege ( [ user name or oid, ] table text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasTablePrivilege(Expression user, Expression table, Expression privilege) {
        return FunctionUtils.threeArgPredicateFunc("has_table_privilege", user, table, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_tablespace_privilege ( [ user name or oid, ] tablespace text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasTablespacePrivilege(Expression tablespace, Expression privilege) {
        return FunctionUtils.twoArgPredicateFunc("has_tablespace_privilege", tablespace, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_tablespace_privilege ( [ user name or oid, ] tablespace text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasTablespacePrivilege(Expression user, Expression tablespace, Expression privilege) {
        return FunctionUtils.threeArgPredicateFunc("has_tablespace_privilege", user, tablespace, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_type_privilege ( [ user name or oid, ] type text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasTypePrivilege(Expression type, Expression privilege) {
        return FunctionUtils.twoArgPredicateFunc("has_type_privilege", type, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">has_type_privilege ( [ user name or oid, ] type text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate hasTypePrivilege(Expression user, Expression type, Expression privilege) {
        return FunctionUtils.threeArgPredicateFunc("has_type_privilege", user, type, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">pg_has_role ( [ user name or oid, ] role text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate pgHasRole(Expression role, Expression privilege) {
        return FunctionUtils.twoArgPredicateFunc("pg_has_role", role, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">pg_has_role ( [ user name or oid, ] role text or oid, privilege text ) → boolean
    /// </a>
    public static SimplePredicate pgHasRole(Expression user, Expression role, Expression privilege) {
        return FunctionUtils.threeArgPredicateFunc("pg_has_role", user, role, privilege);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-ACCESS-TABLE">row_security_active ( table text or oid ) → boolean
    /// </a>
    public static SimplePredicate rowSecurityActive(Expression table) {
        return FunctionUtils.oneArgPredicateFunc("row_security_active", table);
    }

    /*-------------------below aclitem Functions-------------------*/

    /// 
    /// The {@link MappingType} of function return type:  {@link PostgreAclItemArrayType#LINEAR}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-ACLITEM-FN-TABLE">acldefault ( type "char", ownerId oid ) → aclitem[]
    /// </a>
    public static SimpleExpression aclDefault(Expression type, Expression ownerId) {
        return FunctionUtils.twoArgFunc("acldefault", type, ownerId);
    }

    /// 
    /// The {@link MappingType} of function return type:
    /// - grantor : {@link LongType#INSTANCE}
    /// - grantee : {@link LongType#INSTANCE}
    /// - privilege_type : {@link TextType#INSTANCE}
    /// - is_grantable : {@link BooleanType#INSTANCE}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-ACLITEM-FN-TABLE">aclexplode ( aclitem[] ) → setof record ( grantor oid, grantee oid, privilege_type text, is_grantable boolean )
    /// </a>
    public static _TabularWithOrdinalityFunction aclExplode(Expression exp) {
        final List<Selection> fieldList = _Collections.arrayList(4);

        fieldList.add(ArmySelections.forName("grantor"));
        fieldList.add(ArmySelections.forName("grantee"));
        fieldList.add(ArmySelections.forName("privilege_type"));
        fieldList.add(ArmySelections.forName("is_grantable"));

        return DialectFunctionUtils.oneArgTabularFunc("aclexplode", exp, fieldList);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link PgAclItemType#TEXT}
    /// @param isGrantable in most case {@link SQLs#TRUE} or {@link SQLs#FALSE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-ACLITEM-FN-TABLE">makeaclitem ( grantee oid, grantor oid, privileges text, is_grantable boolean ) → aclitem
    /// </a>
    public static SimpleExpression makeAclItem(Expression grantee, Expression grantor, Expression privileges, Expression isGrantable) {
        return FunctionUtils.fourArgFunc("makeaclitem", grantee, grantor, privileges, isGrantable);
    }


    /*-------------------below  Schema Visibility Inquiry Functions-------------------*/

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SCHEMA-TABLE">pg_collation_is_visible ( collation oid ) → boolean
    /// </a>
    public static SimplePredicate pgCollationIsVisible(Expression collation) {
        return FunctionUtils.oneArgPredicateFunc("pg_collation_is_visible", collation);
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SCHEMA-TABLE">pg_conversion_is_visible ( conversion oid ) → boolean
    /// </a>
    public static SimplePredicate pgConversionIsVisible(Expression conversion) {
        return FunctionUtils.oneArgPredicateFunc("pg_conversion_is_visible", conversion);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SCHEMA-TABLE">pg_function_is_visible ( function oid ) → boolean
    /// </a>
    public static SimplePredicate pgFunctionIsVisible(Expression function) {
        return FunctionUtils.oneArgPredicateFunc("pg_function_is_visible", function);
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SCHEMA-TABLE">pg_opclass_is_visible ( opclass oid ) → boolean
    /// </a>
    public static SimplePredicate pgOpClassIsVisible(Expression opclass) {
        return FunctionUtils.oneArgPredicateFunc("pg_opclass_is_visible", opclass);
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SCHEMA-TABLE">pg_operator_is_visible ( operator oid ) → boolean
    /// </a>
    public static SimplePredicate pgOperatorIsVisible(Expression operator) {
        return FunctionUtils.oneArgPredicateFunc("pg_operator_is_visible", operator);
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SCHEMA-TABLE">pg_opfamily_is_visible ( opclass oid ) → boolean
    /// </a>
    public static SimplePredicate pgOpFamilyIsVisible(Expression opClass) {
        return FunctionUtils.oneArgPredicateFunc("pg_opfamily_is_visible", opClass);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SCHEMA-TABLE">pg_statistics_obj_is_visible ( stat oid ) → boolean
    /// </a>
    public static SimplePredicate pgStatisticsObjIsVisible(Expression stat) {
        return FunctionUtils.oneArgPredicateFunc("pg_statistics_obj_is_visible", stat);
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SCHEMA-TABLE">pg_table_is_visible ( table oid ) → boolean
    /// </a>
    public static SimplePredicate pgTableIsVisible(Expression table) {
        return FunctionUtils.oneArgPredicateFunc("pg_table_is_visible", table);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SCHEMA-TABLE">pg_ts_config_is_visible ( config oid ) → boolean
    /// </a>
    public static SimplePredicate pgTsConfigIsVisible(Expression config) {
        return FunctionUtils.oneArgPredicateFunc("pg_ts_config_is_visible", config);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SCHEMA-TABLE">pg_ts_dict_is_visible ( dict oid ) → boolean
    /// </a>
    public static SimplePredicate pgTsDictIsVisible(Expression dict) {
        return FunctionUtils.oneArgPredicateFunc("pg_ts_dict_is_visible", dict);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SCHEMA-TABLE">pg_ts_parser_is_visible ( parser oid ) → boolean
    /// </a>
    public static SimplePredicate pgTsParserIsVisible(Expression parser) {
        return FunctionUtils.oneArgPredicateFunc("pg_ts_parser_is_visible", parser);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SCHEMA-TABLE">pg_ts_template_is_visible ( template oid ) → boolean
    /// </a>
    public static SimplePredicate pgTsTemplateIsVisible(Expression template) {
        return FunctionUtils.oneArgPredicateFunc("pg_ts_template_is_visible", template);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-SCHEMA-TABLE">pg_type_is_visible ( type oid ) → boolean
    /// </a>
    public static SimplePredicate pgTypeIsVisible(Expression type) {
        return FunctionUtils.oneArgPredicateFunc("pg_type_is_visible", type);
    }


    /*-------------------below System Catalog Information Functions-------------------*/

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">format_type ( type oid, typemod integer ) → text
    /// </a>
    public static SimpleExpression formatType(Expression type, Expression typeMode) {
        return FunctionUtils.twoArgFunc("format_type", type, typeMode);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link IntegerType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_char_to_encoding ( encoding name ) → integer
    /// </a>
    public static SimpleExpression pgCharToEncoding(Expression encoding) {
        return FunctionUtils.oneArgFunc("pg_char_to_encoding", encoding);
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_encoding_to_char ( encoding integer ) → name
    /// </a>
    public static SimpleExpression pgEncodingToChar(Expression encoding) {
        return FunctionUtils.oneArgFunc("pg_encoding_to_char", encoding);
    }

    /// 
    /// The {@link MappingType} of function return type:
    /// - fktable : {@link TextType#INSTANCE}
    /// - fkcols : {@link TextArrayType#LINEAR}
    /// - pktable : {@link TextType#INSTANCE}
    /// - pkcols : {@link TextArrayType#LINEAR}
    /// - is_array : {@link BooleanType#INSTANCE}
    /// - is_opt : {@link BooleanType#INSTANCE}
    /// - ordinality (optional) : {@link LongType#INSTANCE} ,see {@link io.army.criteria.impl.Functions._WithOrdinalityClause}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_get_catalog_foreign_keys () → setof record ( fktable regclass, fkcols text[], pktable regclass, pkcols text[], is_array boolean, is_opt boolean )
    /// </a>
    public static _TabularWithOrdinalityFunction pgGetCatalogForeignKeys() {
        final List<Selection> fieldList = _Collections.arrayList(6);

        fieldList.add(ArmySelections.forName("fktable"));
        fieldList.add(ArmySelections.forName("fkcols"));
        fieldList.add(ArmySelections.forName("pktable"));
        fieldList.add(ArmySelections.forName("pkcols"));

        fieldList.add(ArmySelections.forName("is_array"));
        fieldList.add(ArmySelections.forName("is_opt"));
        return DialectFunctionUtils.zeroArgTabularFunc("pg_get_catalog_foreign_keys", fieldList);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_get_constraintdef ( constraint oid [, pretty boolean ] ) → text
    /// </a>
    public static SimpleExpression pgGetConstraintDef(Expression constraint) {
        return FunctionUtils.oneArgFunc("pg_get_constraintdef", constraint);
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @param pretty in most case {@link SQLs#TRUE} or {@link SQLs#FALSE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_get_constraintdef ( constraint oid [, pretty boolean ] ) → text
    /// </a>
    public static SimpleExpression pgGetConstraintDef(Expression constraint, Expression pretty) {
        return FunctionUtils.twoArgFunc("pg_get_constraintdef", constraint, pretty);
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @param pretty in most case {@link SQLs#TRUE} or {@link SQLs#FALSE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_get_expr ( expr pg_node_tree, relation oid [, pretty boolean ] ) → text
    /// </a>
    public static SimpleExpression pgGetExpr(Expression expr, Expression relation, Expression pretty) {
        // TODO hwo to pg_node_tree ?
        return FunctionUtils.threeArgFunc("pg_get_expr", expr, relation, pretty);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_get_functiondef ( func oid ) → text
    /// </a>
    public static SimpleExpression pgGetFunctionDef(Expression func) {
        return FunctionUtils.oneArgFunc("pg_get_functiondef", func);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_get_function_arguments ( func oid ) → text
    /// </a>
    public static SimpleExpression pgGetFunctionArguments(Expression func) {
        return FunctionUtils.oneArgFunc("pg_get_function_arguments", func);
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_get_function_identity_arguments ( func oid ) → text
    /// </a>
    public static SimpleExpression pgGetFunctionIdentityArguments(Expression func) {
        return FunctionUtils.oneArgFunc("pg_get_function_identity_arguments", func);
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_get_function_result ( func oid ) → text
    /// </a>
    public static SimpleExpression pgGetFunctionResult(Expression func) {
        return FunctionUtils.oneArgFunc("pg_get_function_result", func);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_get_indexdef ( index oid [, column integer, pretty boolean ] ) → text
    /// </a>
    public static SimpleExpression pgGetIndexDef(Expression func) {
        return FunctionUtils.oneArgFunc("pg_get_indexdef", func);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @param pretty in most case {@link SQLs#TRUE} or {@link SQLs#FALSE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_get_indexdef ( index oid [, column integer, pretty boolean ] ) → text
    /// </a>
    public static SimpleExpression pgGetIndexDef(Expression func, Expression column, Expression pretty) {
        return FunctionUtils.threeArgFunc("pg_get_indexdef", func, column, pretty);
    }

    /// 
    /// The {@link MappingType} of function return type:
    /// - word : {@link TextType#INSTANCE}
    /// - catcode : {@link CharacterType#INSTANCE}
    /// - barelabel : {@link BooleanType#INSTANCE}
    /// - catdesc : {@link TextType#INSTANCE}
    /// - baredesc : {@link TextType#INSTANCE}
    /// - ordinality (optional) : {@link LongType#INSTANCE} ,see {@link io.army.criteria.impl.Functions._WithOrdinalityClause}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_get_keywords () → setof record ( word text, catcode "char", barelabel boolean, catdesc text, baredesc text )
    /// </a>
    public static _TabularWithOrdinalityFunction pgGetKeywords() {
        final List<Selection> fieldList = _Collections.arrayList(5);

        fieldList.add(ArmySelections.forName("word"));
        fieldList.add(ArmySelections.forName("catcode"));
        fieldList.add(ArmySelections.forName("barelabel"));
        fieldList.add(ArmySelections.forName("catdesc"));

        fieldList.add(ArmySelections.forName("baredesc"));
        return DialectFunctionUtils.zeroArgTabularFunc("pg_get_keywords", fieldList);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_get_ruledef ( rule oid [, pretty boolean ] ) → text
    /// </a>
    public static SimpleExpression pgGetRuleDef(Expression rule) {
        return FunctionUtils.oneArgFunc("pg_get_ruledef", rule);
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @param pretty in most case {@link SQLs#TRUE} or {@link SQLs#FALSE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_get_ruledef ( rule oid [, pretty boolean ] ) → text
    /// </a>
    public static SimpleExpression pgGetRuleDef(Expression rule, Expression pretty) {
        return FunctionUtils.twoArgFunc("pg_get_ruledef", rule, pretty);
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_get_serial_sequence ( table text, column text ) → text
    /// </a>
    public static SimpleExpression pgGetSerialSequence(Expression table, Expression column) {
        return FunctionUtils.twoArgFunc("pg_get_serial_sequence", table, column);
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_get_statisticsobjdef ( statobj oid ) → text
    /// </a>
    public static SimpleExpression pgGetStatisticsObjDef(Expression statObj) {
        return FunctionUtils.oneArgFunc("pg_get_statisticsobjdef", statObj);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_get_triggerdef ( trigger oid [, pretty boolean ] ) → text
    /// </a>
    public static SimpleExpression pgGetTriggerDef(Expression trigger) {
        return FunctionUtils.oneArgFunc("pg_get_triggerdef", trigger);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @param pretty in most case {@link SQLs#TRUE} or {@link SQLs#FALSE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_get_triggerdef ( trigger oid [, pretty boolean ] ) → text
    /// </a>
    public static SimpleExpression pgGetTriggerDef(Expression trigger, Expression pretty) {
        return FunctionUtils.twoArgFunc("pg_get_triggerdef", trigger, pretty);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_get_userbyid ( role oid ) → name
    /// </a>
    public static SimpleExpression pgGetUserById(Expression role) {
        return FunctionUtils.oneArgFunc("pg_get_userbyid", role);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_get_viewdef ( view oid [, pretty boolean ] ) → text
    /// pg_get_viewdef ( view oid, wrap_column integer ) → text
    /// pg_get_viewdef ( view text [, pretty boolean ] ) → text
    /// </a>
    public static SimpleExpression pgGetViewDef(Expression view) {
        return FunctionUtils.oneArgFunc("pg_get_viewdef", view);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_get_viewdef ( view oid [, pretty boolean ] ) → text
    /// pg_get_viewdef ( view oid, wrap_column integer ) → text
    /// pg_get_viewdef ( view text [, pretty boolean ] ) → text
    /// </a>
    public static SimpleExpression pgGetViewDef(Expression view, Expression exp) {
        return FunctionUtils.twoArgFunc("pg_get_viewdef", view, exp);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_index_column_has_property ( index regclass, column integer, property text ) → boolean
    /// </a>
    public static SimplePredicate pgIndexColumnHasProperty(Expression index, Expression column, Expression property) {
        return FunctionUtils.threeArgPredicateFunc("pg_index_column_has_property", index, column, property);
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_index_has_property ( index regclass, property text ) → boolean
    /// </a>
    public static SimplePredicate pgIndexHasProperty(Expression index, Expression property) {
        return FunctionUtils.twoArgPredicateFunc("pg_index_has_property", index, property);
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link BooleanType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_indexam_has_property ( am oid, property text ) → boolean
    /// </a>
    public static SimplePredicate pgIndexAmHasProperty(Expression am, Expression property) {
        return FunctionUtils.twoArgPredicateFunc("pg_indexam_has_property", am, property);
    }


    /// 
    /// The {@link MappingType} of function return type:
    /// - option_name : {@link TextType#INSTANCE}
    /// - option_value : {@link TextType#INSTANCE}
    /// - ordinality (optional) : {@link LongType#INSTANCE} ,see {@link io.army.criteria.impl.Functions._WithOrdinalityClause}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_options_to_table ( options_array text[] ) → setof record ( option_name text, option_value text )
    /// </a>
    public static _TabularWithOrdinalityFunction pgOptionsToTable(Expression optionsArray) {
        final List<Selection> fieldList;
        fieldList = List.of(
                ArmySelections.forName("option_name"),
                ArmySelections.forName("option_value")
        );
        return DialectFunctionUtils.oneArgTabularFunc("pg_options_to_table", optionsArray, fieldList);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextArrayType#LINEAR}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_settings_get_flags ( guc text ) → text[]
    /// </a>
    public static SimpleExpression pgSettingsGetFlags(Expression guc) {
        return FunctionUtils.oneArgFunc("pg_settings_get_flags", guc);
    }


    /// 
    /// The {@link MappingType} of function return type:
    /// -  "Anonymous field" ( you must use as clause definite filed name) : {@link LongType#INSTANCE}
    /// - ordinality (optional) : {@link LongType#INSTANCE} ,see {@link io.army.criteria.impl.Functions._WithOrdinalityClause}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_tablespace_databases ( tablespace oid ) → setof oid
    /// </a>
    public static _ColumnWithOrdinalityFunction pgTablespaceDatabases(Expression tablespace) {
        return DialectFunctionUtils.oneArgColumnFunction("pg_tablespace_databases", tablespace, null);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_tablespace_location ( tablespace oid ) → text
    /// </a>
    public static SimpleExpression pgTablespaceLocation(Expression tablespace) {
        return FunctionUtils.oneArgFunc("pg_tablespace_location", tablespace);
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">pg_typeof ( "any" ) → regtype
    /// </a>
    public static SimpleExpression pgTypeOf(Expression any) {
        return FunctionUtils.oneArgFunc("pg_typeof", any);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">COLLATION FOR ( "any" ) → text
    /// </a>
    public static SimpleExpression collationSpaceFor(Expression any) {
        return FunctionUtils.oneArgFunc("COLLATION FOR", any);
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">to_regclass ( text ) → regclass
    /// </a>
    public static SimpleExpression toRegClass(Expression exp) {
        return FunctionUtils.oneArgFunc("to_regclass", exp);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">to_regcollation ( text ) → regcollation
    /// </a>
    public static SimpleExpression toRegCollation(Expression exp) {
        return FunctionUtils.oneArgFunc("to_regcollation", exp);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">to_regnamespace ( text ) → regnamespace
    /// </a>
    public static SimpleExpression toRegNamespace(Expression exp) {
        return FunctionUtils.oneArgFunc("to_regnamespace", exp);
    }


    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">o_regoper ( text ) → regoper
    /// </a>
    public static SimpleExpression toRegOper(Expression exp) {
        return FunctionUtils.oneArgFunc("to_regoper", exp);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">to_regoperator ( text ) → regoperator
    /// </a>
    public static SimpleExpression toRegOperator(Expression exp) {
        return FunctionUtils.oneArgFunc("to_regoperator", exp);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">to_regproc ( text ) → regproc
    /// </a>
    public static SimpleExpression toRegProc(Expression exp) {
        return FunctionUtils.oneArgFunc("to_regproc", exp);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">to_regprocedure ( text ) → regprocedure
    /// </a>
    public static SimpleExpression toRegProcedure(Expression exp) {
        return FunctionUtils.oneArgFunc("to_regprocedure", exp);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">to_regrole ( text ) → regrole
    /// </a>
    public static SimpleExpression toRegRole(Expression exp) {
        return FunctionUtils.oneArgFunc("to_regrole", exp);
    }

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-CATALOG-TABLE">to_regtype ( text ) → regtype
    /// </a>
    public static SimpleExpression toRegType(Expression exp) {
        return FunctionUtils.oneArgFunc("to_regtype", exp);
    }

    /*-------------------below Object Information and Addressing Functions-------------------*/

    /// 
    /// The {@link MappingType} of function return type:  {@link TextType#INSTANCE}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-OBJECT-TABLE">pg_describe_object ( classid oid, objid oid, objsubid integer ) → text
    /// </a>
    public static SimpleExpression pgDescribeObject(Expression classId, Expression objId, Expression objSubId) {
        return FunctionUtils.threeArgFunc("pg_describe_object", classId, objId, objSubId);
    }

    /// 
    /// The {@link MappingType} of function return type:
    /// 
    /// - type : {@link TextType#INSTANCE}
    /// - schema : {@link TextType#INSTANCE}
    /// - name : {@link TextType#INSTANCE}
    /// - identity : {@link TextType#INSTANCE}
    /// - ordinality (optional) : {@link LongType#INSTANCE} ,see {@link io.army.criteria.impl.Functions._WithOrdinalityClause}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-OBJECT-TABLE">pg_identify_object ( classid oid, objid oid, objsubid integer ) → record ( type text, schema text, name text, identity text )
    /// </a>
    public static _TabularWithOrdinalityFunction pgIdentifyObject(Expression classId, Expression objId, Expression objSubId) {
        final List<Selection> fieldList;
        fieldList = List.of(
                ArmySelections.forName("type"),
                ArmySelections.forName("schema"),
                ArmySelections.forName("name"),
                ArmySelections.forName("identity")
        );
        return DialectFunctionUtils.threeArgTabularFunc("pg_identify_object", classId, objId, objSubId, fieldList);
    }

    /// 
    /// The {@link MappingType} of function return type:
    /// 
    /// - type : {@link TextType#INSTANCE}
    /// - object_names : {@link TextArrayType#LINEAR}
    /// - object_args : {@link TextArrayType#LINEAR}
    /// - ordinality (optional) : {@link LongType#INSTANCE} ,see {@link io.army.criteria.impl.Functions._WithOrdinalityClause}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-OBJECT-TABLE">pg_identify_object_as_address ( classid oid, objid oid, objsubid integer ) → record ( type text, object_names text[], object_args text[] )
    /// </a>
    public static _TabularWithOrdinalityFunction pgIdentifyObjectAsAddress(Expression classId, Expression objId, Expression objSubId) {
        final List<Selection> fieldList;
        fieldList = List.of(
                ArmySelections.forName("type"),
                ArmySelections.forName("object_names"),
                ArmySelections.forName("object_args")
        );
        return DialectFunctionUtils.threeArgTabularFunc("pg_identify_object_as_address", classId, objId, objSubId, fieldList);
    }


    /// 
    /// The {@link MappingType} of function return type:
    /// 
    /// - classid : {@link LongType#INSTANCE}
    /// - objid : {@link LongType#INSTANCE}
    /// - objsubid : {@link IntegerType#INSTANCE}
    /// - ordinality (optional) : {@link LongType#INSTANCE} ,see {@link io.army.criteria.impl.Functions._WithOrdinalityClause}
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-info.html#FUNCTIONS-INFO-OBJECT-TABLE">pg_get_object_address ( type text, object_names text[], object_args text[] ) → record ( classid oid, objid oid, objsubid integer )
    /// </a>
    public static _TabularWithOrdinalityFunction pgGetObjectAddress(Expression type, Expression objectNames, Expression objectArgs) {
        final List<Selection> fieldList;
        fieldList = List.of(
                ArmySelections.forName("classid"),
                ArmySelections.forName("objid"),
                ArmySelections.forName("objsubid")
        );
        return DialectFunctionUtils.threeArgTabularFunc("pg_get_object_address", type, objectNames, objectArgs, fieldList);
    }




    /*-------------------below private method -------------------*/

    /// @see #tsDebug(Expression)
    /// @see #tsDebug(Expression, Expression)
    private static _TabularWithOrdinalityFunction _tsDebug(final @Nullable Expression config, final Expression document) {
        final List<Selection> fieldList = _Collections.arrayList(6);


        fieldList.add(ArmySelections.forName("alias"));
        fieldList.add(ArmySelections.forName("description"));
        fieldList.add(ArmySelections.forName("token"));
        fieldList.add(ArmySelections.forName("dictionaries"));

        fieldList.add(ArmySelections.forName("dictionary"));
        fieldList.add(ArmySelections.forName("lexemes"));

        final String name = "ts_debug";
        final _TabularWithOrdinalityFunction func;
        if (config == null) {
            func = DialectFunctionUtils.oneArgTabularFunc(name, document, fieldList);
        } else {
            func = DialectFunctionUtils.twoArgTabularFunc(name, config, document, fieldList);
        }
        return func;
    }

    /// 
    /// The {@link MappingType} of function returned fields type:
    /// - word {@link TextType}
    /// - ndoc {@link IntegerType}
    /// - nentry {@link IntegerType}
    /// - ordinality (this is optional) {@link LongType},see {@link _WithOrdinalityClause#withOrdinality()}
    /// 
    /// @see #tsStat(Expression)
    /// @see #tsStat(Expression, Expression)
    /// @see <a href="https://www.postgresql.org/docs/current/functions-textsearch.html#TEXTSEARCH-FUNCTIONS-DEBUG-TABLE">ts_stat ( sqlquery text [, weights text ] ) → setof record ( word text, ndoc integer, nentry integer )
    /// Executes the sqlquery, which must return a single tsvector column, and returns statistics about each distinct lexeme contained in the data.
    /// ts_stat('SELECT vector FROM apod') → (foo,10,15)
    /// </a>
    private static _TabularWithOrdinalityFunction _tsStat(final Expression sqlQuery, final @Nullable Expression weights) {
        final List<Selection> fieldList = _Collections.arrayList(3);

        fieldList.add(ArmySelections.forName("word"));
        fieldList.add(ArmySelections.forName("ndoc"));
        fieldList.add(ArmySelections.forName("nentry"));

        final String name = "ts_stat";
        _TabularWithOrdinalityFunction func;
        if (weights == null) {
            func = DialectFunctionUtils.oneArgTabularFunc(name, sqlQuery, fieldList);
        } else {
            func = DialectFunctionUtils.twoArgTabularFunc(name, sqlQuery, weights, fieldList);
        }
        return func;
    }


    /// @see #arrayFill(Expression, Expression)
    /// @see #arrayFill(Expression, Expression, Expression)
    private static SimpleExpression _arrayFill(final Expression anyElement, final Expression dimensions,
                                               final @Nullable Expression bounds) {

        final String name = "array_fill";
        final SimpleExpression func;
        if (bounds == null) {
            func = FunctionUtils.twoArgFunc(name, anyElement, dimensions);
        } else {
            func = FunctionUtils.threeArgFunc(name, anyElement, dimensions, bounds);
        }
        return func;
    }


}
