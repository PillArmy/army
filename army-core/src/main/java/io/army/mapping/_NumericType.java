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

package io.army.mapping;


/// This class is base class class of below:
/// 
/// - {@link BigDecimalType}
/// - {@link BigIntegerType}
/// - {@link ByteType}
/// - {@link DoubleType}
/// - {@link FloatType}
/// - {@link IntegerType}
/// - {@link LongType}
/// - {@link ShortType}
/// - {@link _UnsignedNumericType}
/// 
public abstract class _NumericType extends _ArmyNoInjectionType {



    /// This class is base class class of below:
    /// 
    /// - {@link ByteType}
    /// - {@link ShortType}
    /// - {@link IntegerType}
    /// - {@link LongType}
    /// - {@link BigIntegerType}
    /// 
    public static abstract class _IntegerType extends _NumericType implements SqlInteger {

    }

    /// This class is base class class of below:
    /// 
    /// - {@link FloatType}
    /// - {@link DoubleType}
    /// 
    public static abstract class _FloatNumericType extends _NumericType implements SqlFloat {

    }


    /// This class is base class class of below:
    /// 
    /// - {@link UnsignedBigDecimalType}
    /// - {@link _UnsignedIntegerType}
    /// 
    public static abstract class _UnsignedNumericType extends _NumericType implements SqlUnsignedNumber {

    }

    /// This class is base class class of below:
    /// 
    /// - {@link UnsignedTinyIntType}
    /// - {@link UnsignedSmallIntType}
    /// - {@link UnsignedMediumIntType}
    /// - {@link UnsignedSqlIntType}
    /// - {@link UnsignedBigintType}
    /// - {@link UnsignedBigIntegerType}
    /// 
    public static abstract class _UnsignedIntegerType extends _UnsignedNumericType implements SqlInteger {

    }


}
